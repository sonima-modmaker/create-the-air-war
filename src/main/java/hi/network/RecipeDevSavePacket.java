package hi.network;

import hi.CreateTheAirWarsMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public record RecipeDevSavePacket(String folder, String fileName, String json) implements CustomPacketPayload {
    private static final int MAX_TEXT = 32767;

    public static final Type<RecipeDevSavePacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "recipe_dev_save"));
    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, RecipeDevSavePacket> STREAM_CODEC =
        StreamCodec.of((buf, msg) -> msg.write(buf), RecipeDevSavePacket::new);

    public RecipeDevSavePacket(FriendlyByteBuf buf) {
        this(buf.readUtf(MAX_TEXT), buf.readUtf(MAX_TEXT), buf.readUtf(MAX_TEXT));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(folder, MAX_TEXT);
        buf.writeUtf(fileName, MAX_TEXT);
        buf.writeUtf(json, MAX_TEXT);
    }

    public static void handleData(final RecipeDevSavePacket message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null || player.getServer() == null) {
                return;
            }

            Path base = player.getServer().getWorldPath(LevelResource.ROOT).resolve("debug_recipes").normalize();
            String safeFolder = sanitizePath(message.folder());
            String safeFile = sanitizeFileName(message.fileName());
            if (safeFile.isBlank()) {
                player.displayClientMessage(Component.literal("Recipe dev save failed: empty file name"), false);
                return;
            }

            try {
                Files.createDirectories(base);
                Path folderPath = safeFolder.isBlank() ? base : base.resolve(safeFolder).normalize();
                if (!folderPath.startsWith(base)) {
                    player.displayClientMessage(Component.literal("Recipe dev save failed: invalid folder"), false);
                    return;
                }
                Files.createDirectories(folderPath);
                Path file = folderPath.resolve(safeFile + ".json").normalize();
                if (!file.startsWith(base)) {
                    player.displayClientMessage(Component.literal("Recipe dev save failed: invalid file path"), false);
                    return;
                }

                Files.writeString(file, message.json(), StandardCharsets.UTF_8);
                player.displayClientMessage(Component.literal("Saved recipe: " + base.relativize(file)), false);
            } catch (Exception e) {
                CreateTheAirWarsMod.LOGGER.error("Failed to save recipe dev json", e);
                player.displayClientMessage(Component.literal("Recipe dev save failed: " + e.getClass().getSimpleName()), false);
            }
        });
    }

    private static String sanitizePath(String input) {
        if (input == null) {
            return "";
        }
        String normalized = input.replace('\\', '/').trim();
        normalized = normalized.replaceAll("[^a-zA-Z0-9_./-]", "_");
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        normalized = normalized.replace("..", "_");
        return normalized;
    }

    private static String sanitizeFileName(String input) {
        if (input == null) {
            return "";
        }
        String normalized = input.trim().replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        if (slash >= 0) {
            normalized = normalized.substring(slash + 1);
        }
        normalized = normalized.replaceAll("[^a-zA-Z0-9_.-]", "_");
        normalized = normalized.replaceAll("\\.json$", "");
        return normalized;
    }
}
