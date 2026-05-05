package net.mcreator.sscresearchanddevelopment.block.entity;

import net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactContext;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactRegistries;
import net.mcreator.sscresearchanddevelopment.component.ArtifactProperties;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactReaction;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactTrigger;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.NodeStatus;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentDataComponents;
import net.mcreator.sscresearchanddevelopment.interfaces.ILinkable;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentModBlockEntities;

import javax.annotation.Nullable;

import java.util.*;

public class ArtefactanalyzerBlockEntity extends BlockEntity implements WorldlyContainer, ILinkable {
	// Используем ContainerHelper для удобства, но храним только 1 слот
	private NonNullList<ItemStack> stacks = NonNullList.withSize(1, ItemStack.EMPTY);
	private boolean connected = false;

	public ArtefactanalyzerBlockEntity(BlockPos position, BlockState state) {
		super(SscResearchAndDevelopmentModBlockEntities.ARTEFACTANALYZER.get(), position, state);
	}

	// --- ЛОГИКА ТИКА (Ядро системы) ---
	public void tick() {
		if (level == null || level.isClientSide) return;

		ItemStack stack = stacks.get(0);
		if (stack.isEmpty()) return;

		ArtifactProperties props = stack.get(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get());
		if (props == null) return;

		Object[] args = tickEvents.toArray();
		ArtefactContext context = new ArtefactContext(level, worldPosition, stack, args, null);

		Map<Integer, ArtefactNode> updatedNodes = new HashMap<>(props.nodes());
		boolean dirty = false;

		for (ArtefactNode node : props.nodes().values()) {
			if (node.status() == NodeStatus.ACTIVE) {
				ResourceLocation triggerLoc = ResourceLocation.parse(node.triggerId().contains(":") ? node.triggerId() : "ssc_research_and_development:" + node.triggerId());
				ArtefactTrigger trigger = ArtefactRegistries.TRIGGERS.getRegistry().get().getValue(triggerLoc);

				if (trigger != null && trigger.check(context, node)) {
					updatedNodes.put(node.id(), node.update(NodeStatus.PRIMED, node.maxTimer()));
					dirty = true;
				}
			}

			if (node.status() == NodeStatus.PRIMED) {
				if (node.timer() > 0) {
					updatedNodes.put(node.id(), node.update(NodeStatus.PRIMED, node.timer() - 1));
					dirty = true;
				} else {
					ResourceLocation reactionLoc = ResourceLocation.parse(node.reactionId().contains(":") ? node.reactionId() : "ssc_research_and_development:" + node.reactionId());
					ArtefactReaction reaction = ArtefactRegistries.REACTIONS.getRegistry().get().getValue(reactionLoc);

					if (reaction != null) reaction.execute(context, node);

					int newTotalPoints = props.totalPoints() + node.points();

					updatedNodes.put(node.id(), node.update(NodeStatus.READY_TO_EXTRACT, 0));

					for (int childId : node.childIds()) {
						ArtefactNode child = updatedNodes.get(childId);
						if (child != null && child.status() == NodeStatus.LOCKED) {
							updatedNodes.put(childId, child.update(NodeStatus.ACTIVE, 0));
						}
					}
					props = new ArtifactProperties(props.tier(), newTotalPoints, updatedNodes);
					dirty = true;
				}
			}
		}

		tickEvents.clear(); // Очищаем события в конце каждого тика

		if (dirty) {
			stack.set(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get(),
					new ArtifactProperties(props.tier(), props.totalPoints(), updatedNodes));
			this.setChanged();
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
	}

	// --- СОХРАНЕНИЕ ДАННЫХ ---
	@Override
	protected void saveAdditional(ValueOutput valueOutput) {
		super.saveAdditional(valueOutput);
		valueOutput.putBoolean("is_connected", this.connected);
		ContainerHelper.saveAllItems(valueOutput, this.stacks);
	}

	@Override
	protected void loadAdditional(ValueInput valueInput) {
		super.loadAdditional(valueInput);
		this.connected = valueInput.getBooleanOr("is_connected", false);
		this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(valueInput, this.stacks);
	}

	// --- СЕТЕВАЯ СИНХРОНИЗАЦИЯ ---
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveWithFullMetadata(registries);
	}

	@Override
	public void onDataPacket(Connection net, ValueInput valueInput) {
		this.loadAdditional(valueInput);
	}

	// --- ИНТЕРФЕЙС WorldlyContainer (Для воронок и труб) ---
	@Override
	public int[] getSlotsForFace(Direction side) { return new int[]{0}; }

	@Override
	public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction dir) {
		// Принимаем только если это артефакт (есть компонент)
		return stack.has(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get());
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction dir) { return true; }

	@Override
	public int getContainerSize() { return 1; }

	@Override
	public boolean isEmpty() { return stacks.get(0).isEmpty(); }

	@Override
	public ItemStack getItem(int index) {return stacks.get(0); }

	@Override
	public ItemStack removeItem(int index, int count) {
		ItemStack result = ContainerHelper.removeItem(this.stacks, index, count);
		if (!result.isEmpty()) this.setChanged();
		return result;
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		return ContainerHelper.takeItem(this.stacks, index);
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		this.stacks.set(0, stack);
		if (stack.getCount() > this.getMaxStackSize()) stack.setCount(this.getMaxStackSize());
		this.setChanged();

		if (this.level != null && !this.level.isClientSide) {
			this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	public void clearContent() { this.stacks.clear(); }

	// --- ILinkable (Твоя система связи) ---
	@Override
	public void setLinkedPos(@Nullable BlockPos pos) {
		this.connected = (pos != null);
		this.setChanged();
		if (this.level != null) {
			this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
		}
	}

	@Override
	public Optional<BlockPos> getLinkedPos() { return Optional.empty(); }

	@Override
	public boolean tryLink(Player player, ItemStack tool) {
		if (this.connected) return false;
		this.setLinkedPos(this.worldPosition);
		this.playLinkEffects(true);
		return true;
	}

	@Override
	public boolean tryUnlink(Player player, ItemStack tool) {
		if (!this.connected) return false;
		this.setLinkedPos(null);
		this.playLinkEffects(false);
		return true;
	}

	@Override public boolean canStoreLink() { return false; }

	private void playLinkEffects(boolean linked) {
		if (level instanceof ServerLevel serverLevel) {
			ParticleOptions particle = linked ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.SMOKE;
			serverLevel.sendParticles(particle, worldPosition.getX() + 0.5, worldPosition.getY() + 1.1, worldPosition.getZ() + 0.5, 15, 0.2, 0.2, 0.2, 0.05);
			SoundEvent sound = linked ? SoundEvents.EXPERIENCE_ORB_PICKUP : SoundEvents.LEAF_LITTER_BREAK;
			level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
		}
	}
	private final List<Object> tickEvents = new ArrayList<>();
	public void onTriggerEvent(Object arg) {
		this.tickEvents.add(arg);
	}
}