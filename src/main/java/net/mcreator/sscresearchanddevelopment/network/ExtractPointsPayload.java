package net.mcreator.sscresearchanddevelopment.network;

import io.netty.buffer.ByteBuf;
import net.mcreator.ssc.init.Ssc14ModItems;
import net.mcreator.sscresearchanddevelopment.block.entity.ArtefactanalyzerBlockEntity;
import net.mcreator.sscresearchanddevelopment.block.entity.ArtefactconsoleBlockEntity;
import net.mcreator.sscresearchanddevelopment.component.ArtifactProperties;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentDataComponents;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentSounds;
import net.mcreator.sscresearchanddevelopment.libs.Ssc14Handle;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod.MODID;

public record ExtractPointsPayload(BlockPos pos, ArtefactNode node) implements CustomPacketPayload {
    public static final Type<ExtractPointsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "extract_points"));

    public static final StreamCodec<ByteBuf, ArtefactNode> ARTEFACT_NODE_STREAM_CODEC =
            ByteBufCodecs.fromCodec(ArtefactNode.CODEC);

    public static final StreamCodec<FriendlyByteBuf, ExtractPointsPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ExtractPointsPayload::pos,
            ARTEFACT_NODE_STREAM_CODEC, ExtractPointsPayload::node,
            ExtractPointsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(ExtractPointsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                Level level = serverPlayer.level();
                BlockPos pos = payload.pos();
                ArtifactProperties.extractNodePoints(serverPlayer, pos, payload.node().id());

                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SscResearchAndDevelopmentSounds.RAD_PULSE_11.get(), SoundSource.BLOCKS, 1, 1);

                if (serverPlayer.level().getBlockEntity(pos) instanceof ArtefactconsoleBlockEntity console) {
                    if (console.getLinkedPos().isEmpty()) return;
                    if (serverPlayer.level().getBlockEntity(console.getLinkedPos().get()) instanceof ArtefactanalyzerBlockEntity analyzer) {
                        ItemStack stack = analyzer.getItem(0);
                        if (stack.isEmpty()) return;

                        ArtifactProperties props = stack.get(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get());
                        if (props == null) return;

                        ArtefactNode node = payload.node();
                        String messageText = "Успешно извлечено " + node.points() + " очков из артефакта";

                        Ssc14Handle.sendRadioMessage(
                                level,
                                "Научный",
                                13466829,
                                Ssc14ModItems.ENCRYPTION_KEY_RND.get(),
                                "Консоль артефактов",
                                messageText
                        );
                    }
                }
            }
        });
    }
}