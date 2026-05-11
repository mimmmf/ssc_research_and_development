package net.mcreator.sscresearchanddevelopment.component.fabricator;

import net.mcreator.sscresearchanddevelopment.block.entity.AutolatheBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SWithdrawMaterial(BlockPos pos, String materialType, int amount) implements CustomPacketPayload {
    public static final Type<C2SWithdrawMaterial> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("ssc", "withdraw_material"));

    public static final StreamCodec<FriendlyByteBuf, C2SWithdrawMaterial> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, C2SWithdrawMaterial::pos,
            ByteBufCodecs.STRING_UTF8, C2SWithdrawMaterial::materialType,
            ByteBufCodecs.VAR_INT, C2SWithdrawMaterial::amount,
            C2SWithdrawMaterial::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handleServer(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player.level().getBlockEntity(pos) instanceof AutolatheBlockEntity be) {
                be.withdrawMaterial(player, materialType, amount);
            }
        });
    }
}