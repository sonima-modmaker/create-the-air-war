
package hi.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.resources.ResourceKey;

import hi.CreateTheAirWarsMod;
import hi.init.CreateTheAirWarsModBlocks;
import hi.init.CreateTheAirWarsModItems;
import hi.block.entity.TomahawkBlockEntity;

public record FdgddButtonMessage(int buttonID, int x, int y, int z, int hand, int targetX, int targetY, int targetZ) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FdgddButtonMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "fdgdd_button"));
    public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, FdgddButtonMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((buf, msg) -> msg.write(buf), FdgddButtonMessage::new);

    public FdgddButtonMessage(FriendlyByteBuf buffer) {
        this(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt());
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
        buffer.writeInt(this.hand);
        buffer.writeInt(this.targetX);
        buffer.writeInt(this.targetY);
        buffer.writeInt(this.targetZ);
    }

    public static void handleData(final FdgddButtonMessage message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player entity = context.player();
            handleButtonAction(entity, message.buttonID, message.x, message.y, message.z, message.hand,
                message.targetX, message.targetY, message.targetZ);
        });
    }

    public static void handleButtonAction(Player entity, int buttonID, int x, int y, int z, int hand,
                                          int targetX, int targetY, int targetZ) {
        Level world = entity.level();
        if (!world.hasChunkAt(new BlockPos(x, y, z)))
            return;
        if (buttonID == 0) {
            var stack = hand == 0 ? entity.getMainHandItem() : entity.getOffhandItem();
            if (stack.isEmpty() || stack.getItem() != CreateTheAirWarsModItems.TARGET_GUNNERGADGET.get())
                return;
            net.minecraft.nbt.CompoundTag tag = stack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)
                ? stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag()
                : new net.minecraft.nbt.CompoundTag();
            tag.putInt("targetX", targetX);
            tag.putInt("targetY", targetY);
            tag.putInt("targetZ", targetZ);
            stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
        }
    }
}
