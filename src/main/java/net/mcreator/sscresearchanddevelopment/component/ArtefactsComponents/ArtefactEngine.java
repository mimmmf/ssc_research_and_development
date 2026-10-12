package net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents;

import net.mcreator.sscresearchanddevelopment.component.ArtifactProperties;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.mcreator.sscresearchanddevelopment.enums.NodeStatus;
import net.mcreator.sscresearchanddevelopment.init.ArtefactRegistries;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentDataComponents;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentSoundSets;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ArtefactEngine {
    public static boolean tickArtifact(ItemStack stack, ArtefactContext context) {
        if (stack == null || stack.isEmpty()) return false;

        // Защита курсора инвентаря
        if (context.source() instanceof Player player) {
            ItemStack carried = player.containerMenu.getCarried();
            if (!carried.isEmpty()) {
                ArtifactProperties carriedData = carried.get(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get());
                ArtifactProperties stackData = stack.get(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get());
                if (carriedData != null && Objects.equals(carriedData, stackData)) {
                    return false;
                }
            }
        }

        ArtifactProperties props = stack.get(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get());
        if (props == null) return false;

        Map<Integer, ArtefactNode> updatedNodes = new HashMap<>(props.nodes());
        boolean dirty = false;
        int newTotalPoints = props.totalPoints();

        long currentGameTime = context.level().getGameTime();

        // Проходим по СНИМКУ исходных узлов (props.nodes()), чтобы узлы, разблокированные
        // на этом тике, не срабатывали мгновенно в этом же цикле!
        for (Map.Entry<Integer, ArtefactNode> entry : props.nodes().entrySet()) {
            ArtefactNode originalNode = entry.getValue();
            ArtefactNode currentNode = updatedNodes.get(entry.getKey());
            if (currentNode == null) continue;

            // 1. АКТИВАЦИЯ (только если узел БЫЛ активен до начала этого тика)
            if (originalNode.status() == NodeStatus.ACTIVE) {
                ResourceLocation triggerLoc = parseLocation(currentNode.triggerId());
                ArtefactTrigger trigger = ArtefactRegistries.TRIGGERS.getRegistry().get().getValue(triggerLoc);

                if (trigger != null && trigger.check(context, currentNode)) {
                    long targetTime = currentGameTime + currentNode.maxTimer();
                    updatedNodes.put(currentNode.id(), currentNode.update(NodeStatus.PRIMED, (int)targetTime));
                    dirty = true;
                    playActivationEffects(context);
                }
            }
            // 2. ОЖИДАНИЕ ТАЙМЕРА
            else if (originalNode.status() == NodeStatus.PRIMED) {
                if (currentGameTime >= currentNode.timer()) {
                    ResourceLocation reactionLoc = parseLocation(currentNode.reactionId());
                    ArtefactReaction reaction = ArtefactRegistries.REACTIONS.getRegistry().get().getValue(reactionLoc);

                    if (reaction != null) {
                        reaction.execute(context, currentNode);
                    }

                    newTotalPoints += currentNode.points();
                    updatedNodes.put(currentNode.id(), currentNode.update(NodeStatus.READY_TO_EXTRACT, 0));

                    // Разблокировка дочерних узлов (они перейдут в ACTIVE, но обработаются только на СЛЕДУЮЩЕМ тике)
                    for (int childId : currentNode.childIds()) {
                        ArtefactNode child = updatedNodes.get(childId);
                        if (child != null && child.status() == NodeStatus.LOCKED) {
                            updatedNodes.put(childId, child.update(NodeStatus.ACTIVE, 0));
                        }
                    }
                    dirty = true;
                }
            }
        }

        if (dirty) {
            ArtifactProperties newProps = new ArtifactProperties(props.tier(), newTotalPoints, updatedNodes);
            stack.set(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get(), newProps);

            if (context.source() instanceof ServerPlayer serverPlayer) {
                serverPlayer.containerMenu.broadcastChanges();
            }
        }

        return dirty;
    }

    private static void playActivationEffects(ArtefactContext context) {
        if (context.level() instanceof ServerLevel serverLevel) {
            BlockPos pos = context.pos();
            double x = pos.getX() + 0.5D;
            double y = pos.getY() + 0.5D;
            double z = pos.getZ() + 0.5D;
            SoundEvent randomSound = SscResearchAndDevelopmentSoundSets.ARTEFACT_ACTIVATIONS.get(serverLevel.getRandom().nextInt(SscResearchAndDevelopmentSoundSets.ARTEFACT_ACTIVATIONS.size())).get();
            serverLevel.playSound(null, pos, randomSound, SoundSource.BLOCKS, 1.0F, 1.0F);
            serverLevel.sendParticles(ParticleTypes.ENCHANT, x, y, z, 15, 0.3D, 0.3D, 0.3D, 0.1D);
            serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 5, 0.2D, 0.2D, 0.2D, 0.05D);
        }
    }

    private static ResourceLocation parseLocation(String id) {
        return id.contains(":") ? ResourceLocation.parse(id) : ResourceLocation.fromNamespaceAndPath(ArtefactRegistries.MODID, id);
    }
}