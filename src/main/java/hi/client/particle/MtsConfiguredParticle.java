package hi.client.particle;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;

/** Executes the movement, lifetime, scale, alpha, colour and texture timing from MTS JSONParticle. */
public final class MtsConfiguredParticle extends TextureSheetParticle {
    private final JsonObject definition;
    private final TextureAtlasSprite[] textures;
    private final int[] textureDelays;
    private final double initialXd, initialYd, initialZd;
    private final double accelerationX, accelerationY, accelerationZ;
    private final double terminalX, terminalY, terminalZ;
    private final int movementDuration;
    private final float startScale, endScale, startAlpha, endAlpha;
    private final int fadeScaleIn, fadeScaleOut, fadeAlphaIn, fadeAlphaOut;
    private final float daytimeReduction;
    private final float rollVelocity;
    private final boolean bright;
    private final String type;
    private final boolean terrainTexture;
    private int textureIndex;
    private int textureDelayIndex;
    private float nextTextureAge;

    public MtsConfiguredParticle(ClientLevel level, double x, double y, double z,
                                 double vx, double vy, double vz, JsonObject definition,
                                 TextureAtlasSprite[] textures, int initialTexture, boolean terrainTexture) {
        super(level, x, y, z);
        this.definition = definition;
        this.textures = textures;
        this.textureIndex = Math.clamp(initialTexture, 0, Math.max(0, textures.length - 1));
        this.type = string(definition, "type", "generic");
        this.terrainTexture = terrainTexture;
        this.lifetime = Math.max(1, integer(definition, "duration", defaultLifetime(type)));
        int durationRandomness = integer(definition, "durationRandomness", 0);
        if (durationRandomness > 0) this.lifetime += this.random.nextInt(durationRandomness * 2 + 1) - durationRandomness;
        this.lifetime = Math.max(1, this.lifetime);
        this.hasPhysics = !bool(definition, "ignoreCollision", false);
        this.gravity = 0.0F;
        this.friction = 1.0F;
        this.xd = vx; this.yd = vy; this.zd = vz;
        this.initialXd = vx; this.initialYd = vy; this.initialZd = vz;
        JsonArray movement = array(definition, "movementVelocity");
        this.accelerationX = component(movement, 0); this.accelerationY = component(movement, 1); this.accelerationZ = component(movement, 2);
        JsonArray terminal = array(definition, "terminalVelocity");
        this.terminalX = terminal == null ? Double.POSITIVE_INFINITY : Math.abs(component(terminal, 0));
        this.terminalY = terminal == null ? Double.POSITIVE_INFINITY : Math.abs(component(terminal, 1));
        this.terminalZ = terminal == null ? Double.POSITIVE_INFINITY : Math.abs(component(terminal, 2));
        this.movementDuration = integer(definition, "movementDuration", 0);
        this.startScale = number(definition, "scale", 1.0F);
        float configuredEndScale = number(definition, "toScale", 0.0F);
        this.endScale = configuredEndScale != 0.0F ? configuredEndScale : this.startScale;
        float configuredAlpha = number(definition, "transparency", 0.0F);
        this.startAlpha = configuredAlpha != 0.0F ? configuredAlpha : 1.0F;
        float configuredEndAlpha = number(definition, "toTransparency", 0.0F);
        this.endAlpha = configuredEndAlpha != 0.0F ? configuredEndAlpha : this.startAlpha;
        this.fadeScaleIn = integer(definition, "fadeInScaleTime", 0);
        this.fadeScaleOut = integer(definition, "fadeOutScaleTime", integer(definition, "fadeScaleTime", 0));
        this.fadeAlphaIn = integer(definition, "fadeInTransparencyTime", 0);
        this.fadeAlphaOut = integer(definition, "fadeOutTransparencyTime", integer(definition, "fadeTransparencyTime", 0));
        this.daytimeReduction = number(definition, "daytimeReductionFactor", 0.0F);
        JsonArray rotation = array(definition, "rot");
        JsonArray rotationRandomness = array(definition, "rotationRandomness");
        JsonArray rotationVelocity = array(definition, "rotationVelocity");
        this.roll = this.oRoll = (float) Math.toRadians(component(rotation, 2) + signedComponent(rotationRandomness, 2));
        this.rollVelocity = (float) Math.toRadians(component(rotationVelocity, 2));
        this.bright = bool(definition, "isBright", false);
        this.textureDelays = readInts(array(definition, "textureDelays"));
        this.nextTextureAge = this.textureDelays.length > 0 ? this.textureDelays[0] : this.lifetime / 12.0F;
        if (textures.length > 0) setSprite(textures[this.textureIndex]);
        applyVisualState();
    }

    @Override public ParticleRenderType getRenderType() { return terrainTexture ? ParticleRenderType.TERRAIN_SHEET : ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }
    @Override protected int getLightColor(float partialTick) { return bright ? 0xF000F0 : super.getLightColor(partialTick); }

    @Override public void tick() {
        this.xo = this.x; this.yo = this.y; this.zo = this.z;
        if (++this.age >= this.lifetime) { remove(); return; }
        this.oRoll = this.roll;
        this.roll += this.rollVelocity;

        if (!(bool(definition, "stopsOnGround", false) && this.onGround)) {
            if (movementDuration != 0 && age <= movementDuration) {
                double factor = (movementDuration - age) / (double) movementDuration;
                this.xd = clampMagnitude(this.xd, Math.abs(initialXd * factor));
                this.yd = clampMagnitude(this.yd, Math.abs(initialYd * factor));
                this.zd = clampMagnitude(this.zd, Math.abs(initialZd * factor));
            }
            this.xd += accelerationX; this.yd += accelerationY; this.zd += accelerationZ;
            if (accelerationX == 0 && accelerationY == 0 && accelerationZ == 0) {
                if ("smoke".equals(type)) { this.xd *= 0.9; this.yd += 0.004; this.zd *= 0.9; }
                else if ("flame".equals(type)) { this.xd *= 0.96; this.yd *= 0.96; this.zd *= 0.96; }
                else if ("break".equals(type) && !this.onGround) { this.xd *= 0.98; this.yd = this.yd * 0.98 - 0.04; this.zd *= 0.98; }
            }
            this.xd = Mth.clamp(this.xd, -terminalX, terminalX);
            this.yd = Mth.clamp(this.yd, -terminalY, terminalY);
            this.zd = Mth.clamp(this.zd, -terminalZ, terminalZ);
            move(this.xd, this.yd, this.zd);
            if ("break".equals(type) && this.onGround) this.xd = this.yd = this.zd = 0;
        }

        if (textures.length > 1 && age >= nextTextureAge) {
            textureIndex = (textureIndex + 1) % textures.length;
            setSprite(textures[textureIndex]);
            if (textureDelays.length > 0) {
                textureDelayIndex = (textureDelayIndex + 1) % textureDelays.length;
                nextTextureAge += textureDelays[textureDelayIndex];
            } else nextTextureAge += lifetime / 12.0F;
        }
        applyVisualState();
    }

    private void applyVisualState() {
        float progress = Mth.clamp(age / (float) lifetime, 0, 1);
        float scale = Mth.lerp(progress, startScale, endScale);
        float alpha = Mth.lerp(progress, startAlpha, endAlpha);
        if (fadeScaleIn > 0 && age <= fadeScaleIn) scale *= age / (float) fadeScaleIn;
        if (fadeScaleOut > 0 && lifetime - age < fadeScaleOut) scale *= (lifetime - age) / (float) fadeScaleOut;
        if (fadeAlphaIn > 0 && age <= fadeAlphaIn) alpha *= age / (float) fadeAlphaIn;
        if (fadeAlphaOut > 0 && lifetime - age < fadeAlphaOut) alpha *= (lifetime - age) / (float) fadeAlphaOut;
        if (daytimeReduction != 0.0F) {
            float light = this.level.getMaxLocalRawBrightness(net.minecraft.core.BlockPos.containing(this.x, this.y, this.z)) / 15.0F;
            alpha *= 1.0F - daytimeReduction * light;
        }
        this.quadSize = Math.max(0.0001F, scale);
        this.alpha = Mth.clamp(alpha, 0, 1);
    }

    public void applyColor(int rgb) {
        setColor(((rgb >> 16) & 255) / 255F, ((rgb >> 8) & 255) / 255F, (rgb & 255) / 255F);
    }

    private static double clampMagnitude(double value, double max) { return Math.abs(value) > max ? Math.copySign(max, value) : value; }
    private static int defaultLifetime(String type) { return "smoke".equals(type) ? 80 : "flame".equals(type) ? 20 : "break".equals(type) ? 40 : 20; }
    private static JsonArray array(JsonObject o, String key) { return o.has(key) && o.get(key).isJsonArray() ? o.getAsJsonArray(key) : null; }
    private static double component(JsonArray a, int i) { return a != null && a.size() > i ? a.get(i).getAsDouble() : 0; }
    private static double signedComponent(JsonArray a, int i) { double value = component(a, i); return value == 0 ? 0 : value * 2.0 * Math.random() - value; }
    private static int integer(JsonObject o, String k, int d) { return o.has(k) ? o.get(k).getAsInt() : d; }
    private static float number(JsonObject o, String k, float d) { return o.has(k) ? o.get(k).getAsFloat() : d; }
    private static boolean bool(JsonObject o, String k, boolean d) { return o.has(k) ? o.get(k).getAsBoolean() : d; }
    private static String string(JsonObject o, String k, String d) { return o.has(k) ? o.get(k).getAsString() : d; }
    private static int[] readInts(JsonArray a) { if (a == null) return new int[0]; int[] r = new int[a.size()]; for (int i=0;i<r.length;i++) r[i]=a.get(i).getAsInt(); return r; }
}
