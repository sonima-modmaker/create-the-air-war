package hi.client.particle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hi.mixin.camera.ParticleEngineAccessor;
import hi.network.MtsExplosionEffectPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Loads and executes the unmodified MTSOfficialPack bullet rendering definitions. */
public final class MtsExplosionEffectEngine {
    private static final Map<String, JsonObject> DEFINITIONS = new HashMap<>();
    private static final double SOUND_RANGE_MULTIPLIER = 2.5D;
    private static final double SPEED_OF_SOUND_BLOCKS_PER_SECOND = 343.0D;

    private MtsExplosionEffectEngine() {}

    public static void spawn(MtsExplosionEffectPacket event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.particleEngine == null) return;
        JsonObject root = load(event.profile());
        if (root == null || !root.has("rendering")) return;
        JsonArray particles = root.getAsJsonObject("rendering").getAsJsonArray("particles");
        if (particles == null) return;

        TextureAtlas atlas = ((ParticleEngineAccessor) minecraft.particleEngine).ctaw$getParticleTextureAtlas();
        for (JsonElement element : particles) {
            if (!element.isJsonObject()) continue;
            JsonObject definition = element.getAsJsonObject();
            if (!isActive(definition, event)) continue;
            int quantity = integer(definition, "quantity", 1);
            for (int i = 0; i < quantity; i++) spawnOne(minecraft, level, atlas, definition, event);
        }
        playSounds(minecraft, root.getAsJsonObject("rendering").getAsJsonArray("sounds"), event);
    }

    private static void playSounds(Minecraft minecraft, JsonArray sounds, MtsExplosionEffectPacket event) {
        if (sounds == null || minecraft.player == null) return;
        double distance = minecraft.player.position().distanceTo(new Vec3(event.x(), event.y(), event.z()));
        int propagationDelayTicks = Math.max(0, Mth.floor(distance / SPEED_OF_SOUND_BLOCKS_PER_SECOND * 20.0D));
        for (JsonElement element : sounds) {
            if (!element.isJsonObject()) continue;
            JsonObject sound = element.getAsJsonObject();
            if (!isActive(sound, event)) continue;
            double maxDistance = number(sound, "maxDistance", 0) * SOUND_RANGE_MULTIPLIER;
            if (maxDistance > 0 && distance > maxDistance) continue;
            // Stretch the original MTS distance curve without changing its shape.
            float volume = distanceVolume(sound, distance / SOUND_RANGE_MULTIPLIER);
            if (volume <= 0.001F) continue;
            List<String> choices = new ArrayList<>();
            JsonArray variations = sound.getAsJsonArray("soundVariations");
            if (variations != null) for (JsonElement variation : variations) choices.add(variation.getAsString());
            if (choices.isEmpty() && sound.has("name")) choices.add(sound.get("name").getAsString());
            if (choices.isEmpty()) continue;
            String original = choices.get(minecraft.level.random.nextInt(choices.size()));
            String baseName = original.substring(original.indexOf(':') + 1);
            ResourceLocation eventId = ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "mts_exact_" + baseName);
            SimpleSoundInstance instance = new SimpleSoundInstance(
                eventId, SoundSource.BLOCKS, volume, 1.0F, SoundInstance.createUnseededRandom(),
                false, 0, SoundInstance.Attenuation.NONE, event.x(), event.y(), event.z(), false);
            if (propagationDelayTicks > 0) minecraft.getSoundManager().playDelayed(instance, propagationDelayTicks);
            else minecraft.getSoundManager().play(instance);
        }
    }

    private static float distanceVolume(JsonObject sound, double distance) {
        double minD = number(sound, "minDistance", 0), midD = number(sound, "middleDistance", minD), maxD = number(sound, "maxDistance", midD);
        float minV = (float) number(sound, "minDistanceVolume", 1), midV = (float) number(sound, "middleDistanceVolume", minV), maxV = (float) number(sound, "maxDistanceVolume", 0);
        if (distance <= midD) return Mth.lerp((float) Mth.clamp((distance - minD) / Math.max(1.0, midD - minD), 0, 1), minV, midV);
        return Mth.lerp((float) Mth.clamp((distance - midD) / Math.max(1.0, maxD - midD), 0, 1), midV, maxV);
    }

    private static void spawnOne(Minecraft minecraft, ClientLevel level, TextureAtlas atlas,
                                 JsonObject definition, MtsExplosionEffectPacket event) {
        Vec3 position = vec(definition.getAsJsonArray("pos"));
        Vec3 initial = vec(definition.getAsJsonArray("initialVelocity"));
        JsonArray spreadArray = definition.getAsJsonArray("spreadRandomness");
        if (definition.has("initialVelocity")) {
            if (spreadArray != null) {
                Vec3 spread = vec(spreadArray);
                initial = initial.add(signed(spread.x), signed(spread.y), signed(spread.z));
            } else {
                initial = initial.add(0.2 - Math.random() * 0.4, 0.2 - Math.random() * 0.4, 0.2 - Math.random() * 0.4);
            }
            initial = initial.scale(0.1);
        }

        String orientation = string(definition, "spawningOrientation", "entity");
        if (!"world".equals(orientation)) {
            position = rotate(position, event.yaw(), event.pitch());
            initial = rotate(initial, event.yaw(), event.pitch());
        }
        position = position.add(event.x(), event.y(), event.z());

        List<String> textureNames = textures(definition);
        boolean terrainTexture = textureNames.isEmpty() && "break".equals(string(definition, "type", ""));
        TextureAtlasSprite[] sprites;
        BlockState struckState = struckState(level, event);
        if (terrainTexture) {
            if (struckState.isAir()) return;
            sprites = new TextureAtlasSprite[] { minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(struckState) };
        } else {
            if (textureNames.isEmpty()) return;
            sprites = new TextureAtlasSprite[textureNames.size()];
            for (int i = 0; i < sprites.length; i++) sprites[i] = atlas.getSprite(remapTexture(textureNames.get(i)));
        }
        int textureIndex = bool(definition, "randomTexture", false) ? level.random.nextInt(sprites.length) : 0;

        MtsConfiguredParticle particle = new MtsConfiguredParticle(level, position.x, position.y, position.z,
            initial.x, initial.y, initial.z, definition, sprites, textureIndex, terrainTexture);
        int color = bool(definition, "useBlockColor", false) && !struckState.isAir()
            ? minecraft.getBlockColors().getColor(struckState, level, BlockPos.containing(event.x(), event.y(), event.z()), 0)
            : resolveColor(level, definition);
        particle.applyColor(color == -1 ? 0xFFFFFF : color);
        minecraft.particleEngine.add(particle);
    }

    private static boolean isActive(JsonObject definition, MtsExplosionEffectPacket event) {
        JsonArray animations = definition.getAsJsonArray("activeAnimations");
        if (animations == null) return true;
        for (JsonElement element : animations) {
            if (!element.isJsonObject()) continue;
            JsonObject animation = element.getAsJsonObject();
            if (!"visibility".equals(string(animation, "animationType", ""))) continue;
            String variable = string(animation, "variable", "");
            double value = variableValue(variable, event);
            double min = animation.has("clampMin") ? animation.get("clampMin").getAsDouble() : 1.0;
            double max = animation.has("clampMax") ? animation.get("clampMax").getAsDouble() : Double.POSITIVE_INFINITY;
            if (value < min || value > max) return false;
        }
        return true;
    }

    private static double variableValue(String variable, MtsExplosionEffectPacket event) {
        boolean inverted = variable.startsWith("!");
        if (inverted) variable = variable.substring(1);
        double value;
        if ("bullet_hit".equals(variable)) value = 1;
        else if ("bullet_hit_block".equals(variable)) value = event.blockHit() ? 1 : 0;
        else if (variable.startsWith("blockmaterial_")) value = event.material().equals(variable.substring("blockmaterial_".length())) ? 1 : 0;
        else if ("bullet_burntime".equals(variable)) value = 0;
        else if ("terrain_distance".equals(variable)) value = 0;
        else if (variable.endsWith("_cycle")) value = 0;
        else value = 0;
        return inverted ? (value == 0 ? 1 : 0) : value;
    }

    private static JsonObject load(String profile) {
        String safe = switch (profile) {
            case "basicbomb", "heavy_bomb", "bullet3700he", "bomblet" -> profile;
            default -> "bulletrocket";
        };
        if (DEFINITIONS.containsKey(safe)) return DEFINITIONS.get(safe);
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "mts_explosions/" + safe + ".json");
        try (Reader reader = Minecraft.getInstance().getResourceManager().getResourceOrThrow(id).openAsReader()) {
            JsonObject definition = JsonParser.parseReader(reader).getAsJsonObject();
            DEFINITIONS.put(safe, definition);
            return definition;
        } catch (Exception error) {
            System.err.println("[CTAW-MTS] Could not load exact explosion profile " + id + ": " + error);
            return null;
        }
    }

    private static List<String> textures(JsonObject definition) {
        List<String> result = new ArrayList<>();
        if (definition.has("texture")) result.add(definition.get("texture").getAsString());
        JsonArray list = definition.getAsJsonArray("textureList");
        if (list != null) for (JsonElement element : list) result.add(element.getAsString());
        if (result.isEmpty() && "smoke".equals(string(definition, "type", ""))) {
            for (int i = 0; i <= 11; i++) result.add("mts:textures/particles/big_smoke_" + i + ".png");
        }
        // MTS break particles use the struck block atlas. Use its pack fallback only when one was supplied.
        return result;
    }

    private static ResourceLocation remapTexture(String mtsName) {
        ResourceLocation original = ResourceLocation.parse(mtsName);
        String path = original.getPath();
        if (path.startsWith("textures/")) path = path.substring("textures/".length());
        if (path.endsWith(".png")) path = path.substring(0, path.length() - 4);
        return ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "mts_exact/" + original.getNamespace() + "/" + path);
    }

    private static BlockState struckState(ClientLevel level, MtsExplosionEffectPacket event) {
        BlockPos pos = BlockPos.containing(event.x(), event.y(), event.z());
        BlockState state = level.getBlockState(pos);
        return state.isAir() ? level.getBlockState(pos.below()) : state;
    }

    private static int resolveColor(ClientLevel level, JsonObject definition) {
        if (definition.has("color")) return parseColor(definition.get("color").getAsString());
        JsonArray colors = definition.getAsJsonArray("colorList");
        if (colors != null && !colors.isEmpty()) {
            int index = bool(definition, "randomColor", false) ? level.random.nextInt(colors.size()) : 0;
            return parseColor(colors.get(index).getAsString());
        }
        return 0xFFFFFF;
    }

    private static int parseColor(String text) {
        try { return Integer.parseInt(text.replace("#", ""), 16) & 0xFFFFFF; }
        catch (RuntimeException ignored) { return 0xFFFFFF; }
    }

    private static Vec3 rotate(Vec3 input, float yaw, float pitch) {
        double yawRad = -yaw * Mth.DEG_TO_RAD;
        double pitchRad = pitch * Mth.DEG_TO_RAD;
        double cy = Math.cos(yawRad), sy = Math.sin(yawRad), cp = Math.cos(pitchRad), sp = Math.sin(pitchRad);
        double y1 = input.y * cp - input.z * sp;
        double z1 = input.y * sp + input.z * cp;
        return new Vec3(input.x * cy + z1 * sy, y1, -input.x * sy + z1 * cy);
    }

    private static double signed(double maximum) { return maximum * 2.0 * Math.random() - maximum; }
    private static Vec3 vec(JsonArray a) { return a == null ? Vec3.ZERO : new Vec3(component(a,0), component(a,1), component(a,2)); }
    private static double component(JsonArray a, int i) { return a != null && a.size() > i ? a.get(i).getAsDouble() : 0; }
    private static int integer(JsonObject o, String k, int d) { return o.has(k) ? o.get(k).getAsInt() : d; }
    private static double number(JsonObject o, String k, double d) { return o.has(k) ? o.get(k).getAsDouble() : d; }
    private static boolean bool(JsonObject o, String k, boolean d) { return o.has(k) ? o.get(k).getAsBoolean() : d; }
    private static String string(JsonObject o, String k, String d) { return o.has(k) ? o.get(k).getAsString().toLowerCase() : d; }
}
