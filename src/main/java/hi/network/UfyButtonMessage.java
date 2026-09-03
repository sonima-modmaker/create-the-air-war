
package hi.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;

import java.util.HashMap;

import hi.world.inventory.UfyMenu;
import hi.procedures.ZdProcedure;
import hi.CreateTheAirWarsMod;

public record UfyButtonMessage(int buttonID, int x, int y, int z) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UfyButtonMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "ufy_button"));
    public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, UfyButtonMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((buf, msg) -> msg.write(buf), UfyButtonMessage::new);

    public UfyButtonMessage(FriendlyByteBuf buffer) {
        this(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(this.buttonID);
        buffer.writeInt(this.x);
        buffer.writeInt(this.y);
        buffer.writeInt(this.z);
    }

    public static void handleData(final UfyButtonMessage message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player entity = context.player();
            int buttonID = message.buttonID;
            int x = message.x;
            int y = message.y;
            int z = message.z;
            handleButtonAction(entity, buttonID, x, y, z);
        });
    }

    public static void handleButtonAction(Player entity, int buttonID, int x, int y, int z) {
        Level world = entity.level();
        HashMap guistate = UfyMenu.guistate;
        if (!world.hasChunkAt(new BlockPos(x, y, z)))
            return;
        if (buttonID == 0) {
            ZdProcedure.execute(world, guistate);
        }
    }
}
