package hi.network;

import hi.block.entity.RocketDataLinkBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import hi.CreateTheAirWarsMod;

public record RocketDataLinkPacket(BlockPos pos, Action action, BlockPos targetPos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RocketDataLinkPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "rocket_data_link_packet"));
    public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, RocketDataLinkPacket> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((buf, msg) -> msg.write(buf), RocketDataLinkPacket::new);

    public enum Action {
        START_ALL,
        STOP_ALL,
        REMOVE_ENGINE
    }

    public RocketDataLinkPacket(BlockPos pos, Action action) {
        this(pos, action, null);
    }

    public RocketDataLinkPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readEnum(Action.class), buf.readBoolean() ? buf.readBlockPos() : null);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeEnum(action);
        buf.writeBoolean(targetPos != null);
        if (targetPos != null) {
            buf.writeBlockPos(targetPos);
        }
    }

    public static void handleData(final RocketDataLinkPacket message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null && player.level() != null) {
                BlockEntity be = player.level().getBlockEntity(message.pos);
                if (be instanceof RocketDataLinkBlockEntity dataLink) {
                    switch (message.action) {
                        case START_ALL -> dataLink.startAllEngines();
                        case STOP_ALL -> dataLink.stopAllEngines();
                        case REMOVE_ENGINE -> {
                            if (message.targetPos != null) {
                                dataLink.removeEngine(message.targetPos);
                            }
                        }
                    }
                }
            }
        });
    }
}
