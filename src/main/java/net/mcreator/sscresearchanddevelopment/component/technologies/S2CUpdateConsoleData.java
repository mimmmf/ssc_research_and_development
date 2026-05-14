package net.mcreator.sscresearchanddevelopment.component.technologies;

import net.mcreator.sscresearchanddevelopment.client.gui.ResearchConsoleScreen;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.UUID;

public record S2CUpdateConsoleData(
        UUID serverId,
        int points,
        List<String> unlockedTechs
) implements CustomPacketPayload {

    public static final Type<S2CUpdateConsoleData> TYPE = new Type<>(ResourceLocation.parse("ssc:update_console_data"));

    public static final StreamCodec<FriendlyByteBuf, S2CUpdateConsoleData> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, S2CUpdateConsoleData::serverId,
            ByteBufCodecs.VAR_INT, S2CUpdateConsoleData::points,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), S2CUpdateConsoleData::unlockedTechs,
            S2CUpdateConsoleData::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(S2CUpdateConsoleData payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (net.minecraft.client.Minecraft.getInstance().screen instanceof ResearchConsoleScreen screen) {
                screen.updateMenuState(0, "connectedServer", payload.serverId());
                screen.updateMenuState(0, "points", payload.points());
                screen.updateMenuState(0, "unlockedTechs", payload.unlockedTechs());
            }
        });
    }
}