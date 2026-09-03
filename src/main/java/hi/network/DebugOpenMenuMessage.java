package hi.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.world.entity.player.Player;

import hi.CreateTheAirWarsMod;
import hi.procedures.DgfgdgdProcedure;

public record DebugOpenMenuMessage() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DebugOpenMenuMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "debug_open_menu"));
    public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, DebugOpenMenuMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((buf, msg) -> msg.write(buf), DebugOpenMenuMessage::new);

    public DebugOpenMenuMessage(FriendlyByteBuf buffer) {
        this();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buffer) {
    }

    public static void handleData(final DebugOpenMenuMessage message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player entity = context.player();
            if (entity != null) {
                DgfgdgdProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
            }
        });
    }
}
