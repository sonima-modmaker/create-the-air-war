package hi.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import hi.CreateTheAirWarsMod;
import hi.world.inventory.DfgdfgMenu;

public record DebugRecipeSaveMessage(int gridSize) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DebugRecipeSaveMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "debug_recipe_save"));
    public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, DebugRecipeSaveMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((buf, msg) -> msg.write(buf), DebugRecipeSaveMessage::new);

    public DebugRecipeSaveMessage(FriendlyByteBuf buffer) {
        this(buffer.readInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(this.gridSize);
    }

    public static void handleData(final DebugRecipeSaveMessage message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player entity = context.player();
            if (entity == null)
                return;
            if (!(entity.containerMenu instanceof DfgdfgMenu menu))
                return;
            ItemStack output = menu.getSlot(25).getItem();
            if (output.isEmpty())
                return;
            int size = Math.max(1, Math.min(5, message.gridSize()));
            List<String> inputs = new ArrayList<>();
            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    int slotIndex = row * 5 + col;
                    ItemStack stack = menu.getSlot(slotIndex).getItem();
                    String id = "";
                    if (!stack.isEmpty()) {
                        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
                        if (key != null)
                            id = key.toString();
                    }
                    inputs.add(id);
                }
            }
            ResourceLocation outputKey = BuiltInRegistries.ITEM.getKey(output.getItem());
            if (outputKey == null)
                return;
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("output", outputKey.toString());
            data.put("size", size);
            data.put("inputs", inputs);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String fileSafe = outputKey.toString().replace(':', '_');
            try {
                Path base = entity.getServer().getWorldPath(LevelResource.ROOT).resolve("debug_recipes");
                Files.createDirectories(base);
                Path file = base.resolve(fileSafe + ".json");
                Files.writeString(file, gson.toJson(data), StandardCharsets.UTF_8);
            } catch (Exception e) {
                CreateTheAirWarsMod.LOGGER.error("Failed to save debug recipe", e);
            }
        });
    }
}
