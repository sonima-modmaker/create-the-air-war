package hi.network;

import hi.CreateTheAirWarsMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RecipeDevOpenScreenPacket() implements CustomPacketPayload {
    public static final Type<RecipeDevOpenScreenPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "recipe_dev_open_screen"));
    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, RecipeDevOpenScreenPacket> STREAM_CODEC =
        StreamCodec.of((buf, msg) -> msg.write(buf), RecipeDevOpenScreenPacket::new);

    public RecipeDevOpenScreenPacket(FriendlyByteBuf buf) {
        this();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buf) {
    }

    public static void handleData(final RecipeDevOpenScreenPacket message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                Class<?> helperClass = Class.forName("hi.client.RecipeDevClientScreenOpener");
                helperClass.getMethod("open").invoke(null);
            } catch (Exception ignored) {
            }
        });
    }
}
