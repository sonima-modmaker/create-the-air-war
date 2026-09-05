package hi.util;

import hi.CreateTheAirWarsMod;
import hi.init.CreateTheAirWarsModParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraft.tags.BlockTags;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import net.neoforged.neoforge.network.PacketDistributor;
import hi.network.ScreenshakePacket;

public final class ExplosionUtils {
    private static final double SHAKE_RADIUS = 50.0;
    private static final double SHAKE_KNOCKBACK_BASE = 0.7;
    private static final double SHAKE_VERTICAL_BASE = 0.3;
    private static final String ROCKET_EXPLOSION_CLOSE_SOUND = "create_the_air_wars:rocket_explosion_close";
    private static final String ROCKET_EXPLOSION_MEDIUM_SOUND = "create_the_air_wars:rocket_explosion_medium";
    private static final String ROCKET_EXPLOSION_FAR_SOUND = "create_the_air_wars:rocket_explosion_far";
    private static final double ROCKET_EXPLOSION_CLOSE_SWITCH_DISTANCE_SQR = 48.0 * 48.0;
    private static final double ROCKET_EXPLOSION_FAR_SWITCH_DISTANCE_SQR = 180.0 * 180.0;
    private static final double ROCKET_EXPLOSION_PACKET_DISTANCE = 256.0;
    private static final String[] ROCKET_EXPLOSION_CLOSE_VARIANTS = {
        "create_the_air_wars:rocket_explosion_close_01",
        "create_the_air_wars:rocket_explosion_close_02",
        "create_the_air_wars:rocket_explosion_close_03"
    };
    private static final String[] ROCKET_EXPLOSION_MEDIUM_VARIANTS = {
        "create_the_air_wars:rocket_explosion_medium_01",
        "create_the_air_wars:rocket_explosion_medium_02",
        "create_the_air_wars:rocket_explosion_medium_03",
        "create_the_air_wars:rocket_explosion_medium_04",
        "create_the_air_wars:rocket_explosion_medium_05",
        "create_the_air_wars:rocket_explosion_medium_06",
        "create_the_air_wars:rocket_explosion_medium_01_s",
        "create_the_air_wars:rocket_explosion_medium_02_s",
        "create_the_air_wars:rocket_explosion_medium_03_s",
        "create_the_air_wars:rocket_explosion_medium_04_s",
        "create_the_air_wars:rocket_explosion_medium_05_s",
        "create_the_air_wars:rocket_explosion_medium_07_s",
        "create_the_air_wars:rocket_explosion_medium_08_s",
        "create_the_air_wars:rocket_explosion_medium_09_s",
        "create_the_air_wars:rocket_explosion_medium_10_s"
    };
    private static final String[] ROCKET_EXPLOSION_FAR_VARIANTS = {
        "create_the_air_wars:rocket_explosion_far_01",
        "create_the_air_wars:rocket_explosion_far_02",
        "create_the_air_wars:rocket_explosion_far_03",
        "create_the_air_wars:rocket_explosion_far_04",
        "create_the_air_wars:rocket_explosion_far_05",
        "create_the_air_wars:rocket_explosion_far_07",
        "create_the_air_wars:rocket_explosion_far_08",
        "create_the_air_wars:rocket_explosion_far_09",
        "create_the_air_wars:rocket_explosion_far_10"
    };
    private static final Set<String> ROCKET_EXPLOSION_SOUND_ALIASES = Set.of(
        "create_the_air_wars:explotion",
        "create_the_air_wars:fdgdg",
        "create_the_air_wars:vzriv",
        "create_the_air_wars:explosion",
        "create_the_air_wars:shellexp1",
        "create_the_air_wars:shellexp2",
        "create_the_air_wars:shellex3",
        "create_the_air_wars:fire_big_cannon",
        "create_the_air_wars:explgrenade",
        ROCKET_EXPLOSION_CLOSE_SOUND,
        ROCKET_EXPLOSION_MEDIUM_SOUND,
        ROCKET_EXPLOSION_FAR_SOUND
    );

    private ExplosionUtils() {}

    public record ProjectileExplosionProfile(
        float radius,
        boolean breakBlocks,
        double shakeIntensity,
        String soundId,
        SoundSource soundSource,
        float soundVolume,
        float soundPitch,
        ParticleOptions particleType,
        int particleCount,
        double particleXOffset,
        double particleYOffset,
        double particleZOffset,
        double particleSpeed,
        int scorchRadius,
        boolean heavyParticleBurst
    ) {}

    public static void safeExplode(Level level, double x, double y, double z, float radius, boolean breakBlocks) {
        if (level == null || level.isClientSide()) return;
        try {
            if (breakBlocks && radius >= 3.0F) {
                createCustomSphericalCrater(level, x, y, z, radius);
                applyShockwaveEffects(level, x, y, z, radius);
            }
            Level.ExplosionInteraction mode = breakBlocks
                ? Level.ExplosionInteraction.BLOCK
                : Level.ExplosionInteraction.NONE;
            level.explode(
                null,
                Explosion.getDefaultDamageSource(level, null),
                null,
                x,
                y,
                z,
                radius * 0.4F,
                false,
                mode,
                CreateTheAirWarsModParticleTypes.MTS_EXPLOSION_FLASH.get(),
                CreateTheAirWarsModParticleTypes.MTS_HEAVY_EXPLOSION_FLASH.get(),
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.EMPTY)
            );
        } catch (Throwable t) {
            damageNearbyEntities(level, x, y, z, radius);
        }
    }

    public static void createCustomSphericalCrater(Level level, double x, double y, double z, float radius) {
        if (level == null || level.isClientSide()) return;

        BlockPos center = BlockPos.containing(x, y, z);
        int maxR = Math.round(radius * 1.35F);
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        // Irregular, non-circular organic crater carving using angular noise & direction distortion
        for (int dx = -maxR; dx <= maxR; dx++) {
            for (int dy = -maxR; dy <= maxR; dy++) {
                for (int dz = -maxR; dz <= maxR; dz++) {
                    double dist2D = Math.hypot(dx, dz);
                    if (dist2D < 1.0E-4D && Math.abs(dy) < 1.0E-4D) continue;

                    double angle = Math.atan2(dz, dx);
                    double pitch = Math.atan2(dy, Math.max(1.0E-4D, dist2D));

                    // Multi-frequency organic directional noise (varies radius between 0.65x and 1.35x)
                    double noise = Math.sin(angle * 3.0D + dy * 0.15D) * 0.22D
                                 + Math.cos(angle * 5.0D + pitch * 2.5D) * 0.18D
                                 + (rand.nextDouble() - 0.5D) * 0.16D;

                    double effectiveRadius = radius * (1.0D + noise);
                    double distSqr = dx * dx + (dy * 1.35D) * (dy * 1.35D) + dz * dz;

                    if (distSqr <= effectiveRadius * effectiveRadius) {
                        BlockPos p = center.offset(dx, dy, dz);
                        BlockState state = level.getBlockState(p);
                        if (!state.isAir() && state.getFluidState().isEmpty()) {
                            if (state.is(Blocks.BEDROCK) || state.is(Blocks.BARRIER)) continue;
                            float resistance = state.getDestroySpeed(level, p);
                            if (resistance < 0.0F) continue;
                            if (state.is(BlockTags.NEEDS_DIAMOND_TOOL)) resistance *= 3.0F;
                            double falloff = Mth.clamp(1.0D - distSqr / (effectiveRadius * effectiveRadius), 0.0D, 1.0D);
                            double damage = radius * 16.0D;
                            double force = radius * (0.25D + rand.nextDouble() * 0.15D) * 0.02D * damage * falloff;
                            if (force > resistance) level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }

        // Irregular Rim edging & interior floor detailing
        int rimRadius = maxR + 2;
        int rimRadiusSqr = rimRadius * rimRadius;

        for (int dx = -rimRadius; dx <= rimRadius; dx++) {
            for (int dz = -rimRadius; dz <= rimRadius; dz++) {
                double dist2D = dx * dx + dz * dz;
                if (dist2D <= rimRadiusSqr) {
                    BlockPos topPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, center.offset(dx, 0, dz));
                    BlockState topState = level.getBlockState(topPos);
                    if (!topState.getFluidState().isEmpty()) continue;
                    if (topState.is(Blocks.GRASS_BLOCK) || topState.is(Blocks.DIRT) || topState.is(Blocks.STONE)) {
                        BlockState rimBlock = rand.nextBoolean() ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.PODZOL.defaultBlockState();
                        level.setBlock(topPos, rimBlock, 3);
                    }

                    // Interior floor cobblestone/gravel/basalt scattering
                    BlockPos floorPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, center.offset(dx, 0, dz));
                    BlockState floorState = level.getBlockState(floorPos);
                    if (!floorState.isAir() && floorState.getFluidState().isEmpty() && (floorState.is(Blocks.STONE) || floorState.is(Blocks.DEEPSLATE) || floorState.is(Blocks.DIRT))) {
                        float chance = rand.nextFloat();
                        if (chance < 0.25F) {
                            level.setBlock(floorPos, Blocks.COBBLESTONE.defaultBlockState(), 3);
                        } else if (chance < 0.40F) {
                            level.setBlock(floorPos, Blocks.GRAVEL.defaultBlockState(), 3);
                        } else if (chance < 0.50F) {
                            level.setBlock(floorPos, Blocks.BASALT.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }

    public static void applyShockwaveEffects(Level level, double x, double y, double z, float explosionRadius) {
        if (level == null || level.isClientSide()) return;

        BlockPos center = BlockPos.containing(x, y, z);
        int glassRadius = Math.round(explosionRadius * 1.8F);
        int glassRadiusSqr = glassRadius * glassRadius;
        int shockwaveRadius = Math.round(explosionRadius * 2.5F);
        int shockwaveRadiusSqr = shockwaveRadius * shockwaveRadius;

        for (int dx = -shockwaveRadius; dx <= shockwaveRadius; dx++) {
            for (int dy = -shockwaveRadius / 2; dy <= shockwaveRadius / 2; dy++) {
                for (int dz = -shockwaveRadius; dz <= shockwaveRadius; dz++) {
                    int dSqr = dx * dx + dy * dy + dz * dz;
                    if (dSqr > shockwaveRadiusSqr) continue;

                    BlockPos p = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(p);
                    ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                    if (blockId == null) continue;

                    String name = blockId.getPath();

                    // Glass shattering in inner shockwave zone
                    if (dSqr <= glassRadiusSqr && (name.contains("glass") || name.contains("pane"))) {
                        level.destroyBlock(p, false);
                        continue;
                    }

                    // Redstone lamps and light fixtures flickering & disabling
                    if (state.getBlock() instanceof RedstoneLampBlock) {
                        if (state.getValue(RedstoneLampBlock.LIT)) {
                            level.setBlock(p, state.setValue(RedstoneLampBlock.LIT, false), 3);
                        }
                    }
                }
            }
        }
    }

    public static void applyShake(Level level, double x, double y, double z, double maxIntensity) {
        if (level == null || !(level instanceof ServerLevel serverLevel)) return;
        AABB box = new AABB(x - SHAKE_RADIUS, y - SHAKE_RADIUS, z - SHAKE_RADIUS,
                            x + SHAKE_RADIUS, y + SHAKE_RADIUS, z + SHAKE_RADIUS);
        Vec3 origin = new Vec3(x, y, z);

        double packetRadius = 64.0;
        double time = 5.0 + maxIntensity;
        double amplitude = maxIntensity * 2.5;

        for (Player player : serverLevel.players()) {
            if (player instanceof ServerPlayer sp) {
                double dist = player.position().distanceTo(origin);
                if (dist < packetRadius) {
                    PacketDistributor.sendToPlayer(sp, new ScreenshakePacket(time, packetRadius, amplitude, x, y, z));
                }
            }

            if (!player.getBoundingBox().intersects(box)) continue;
            Vec3 toPlayer = player.position().subtract(origin);
            double distance = toPlayer.length();
            if (distance >= SHAKE_RADIUS) continue;

            double falloff = 1.0 - (distance / SHAKE_RADIUS);
            double intensity = Math.min(maxIntensity, falloff * falloff * maxIntensity);
            if (intensity < 0.02) continue;

            Vec3 dir = distance > 0.001 ? toPlayer.normalize() : new Vec3(0, 1, 0);
            double pushX = dir.x * SHAKE_KNOCKBACK_BASE * intensity;
            double pushY = (dir.y + 0.4) * SHAKE_VERTICAL_BASE * intensity;
            double pushZ = dir.z * SHAKE_KNOCKBACK_BASE * intensity;

            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.add(pushX, pushY, pushZ));
            player.hurtMarked = true;
            if (player instanceof ServerPlayer sp) {
                sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(sp.getId(), player.getDeltaMovement()));
            }
        }
    }

    public static void playSoundSafe(Level level, double x, double y, double z, String soundId, SoundSource source, float volume, float pitch) {
        if (level == null) return;
        try {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel && ROCKET_EXPLOSION_SOUND_ALIASES.contains(soundId)) {
                playDistanceAwareRocketExplosion(serverLevel, x, y, z, source, volume, pitch);
                return;
            }
            ResourceLocation rl = ResourceLocation.tryParse(soundId);
            if (rl == null) return;
            SoundEvent se = BuiltInRegistries.SOUND_EVENT.get(rl);
            if (se == null) return;
            if (level.isClientSide()) {
                level.playLocalSound(x, y, z, se, source, volume, pitch, false);
            } else {
                level.playSound(null, BlockPos.containing(x, y, z), se, source, volume, pitch);
            }
        } catch (Throwable ignored) {
        }
    }

    private static void playDistanceAwareRocketExplosion(ServerLevel level, double x, double y, double z, SoundSource source, float volume, float pitch) {
        for (ServerPlayer player : level.players()) {
            double distanceSqr = player.distanceToSqr(x, y, z);
            String selectedSoundId;
            float selectedVolume;
            if (distanceSqr <= ROCKET_EXPLOSION_CLOSE_SWITCH_DISTANCE_SQR) {
                selectedSoundId = pickVariant(ROCKET_EXPLOSION_CLOSE_VARIANTS);
                selectedVolume = Math.max(volume, 4.0F);
            } else if (distanceSqr <= ROCKET_EXPLOSION_FAR_SWITCH_DISTANCE_SQR) {
                selectedSoundId = pickVariant(ROCKET_EXPLOSION_MEDIUM_VARIANTS);
                selectedVolume = Math.max(volume, 6.0F);
            } else {
                selectedSoundId = pickVariant(ROCKET_EXPLOSION_FAR_VARIANTS);
                selectedVolume = Math.max(volume, 8.0F);
            }
            SoundEvent selectedSound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(selectedSoundId));
            if (selectedSound == null) {
                continue;
            }
            Holder<SoundEvent> selectedHolder = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(selectedSound);
            float selectedPitch = pitch + (level.random.nextFloat() - 0.5F) * 0.08F;
            long seed = ThreadLocalRandom.current().nextLong();
            Vec3 packetOrigin = projectSoundOriginTowardPlayer(player, new Vec3(x, y, z));
            double jitterX = packetOrigin.x + (ThreadLocalRandom.current().nextDouble() - 0.5D) * 0.18D;
            double jitterY = packetOrigin.y + (ThreadLocalRandom.current().nextDouble() - 0.5D) * 0.12D;
            double jitterZ = packetOrigin.z + (ThreadLocalRandom.current().nextDouble() - 0.5D) * 0.18D;
            player.connection.send(new ClientboundSoundPacket(selectedHolder, source, jitterX, jitterY, jitterZ, selectedVolume, selectedPitch, seed));
        }
    }

    private static Vec3 projectSoundOriginTowardPlayer(ServerPlayer player, Vec3 explosionOrigin) {
        Vec3 listener = player.getEyePosition();
        Vec3 toExplosion = explosionOrigin.subtract(listener);
        double distance = toExplosion.length();
        if (distance <= ROCKET_EXPLOSION_PACKET_DISTANCE || distance < 1.0E-4D) {
            return explosionOrigin;
        }
        return listener.add(toExplosion.scale(ROCKET_EXPLOSION_PACKET_DISTANCE / distance));
    }

    private static String pickVariant(String[] variants) {
        if (variants.length == 0) {
            return ROCKET_EXPLOSION_MEDIUM_SOUND;
        }
        return variants[ThreadLocalRandom.current().nextInt(variants.length)];
    }

    public static void explodeProjectileImpact(LevelAccessor world, double x, double y, double z, Entity projectile, ProjectileExplosionProfile fallbackProfile) {
        ProjectileExplosionProfile profile = resolveProfile(projectile, fallbackProfile);
        Vec3 rawImpactOrigin = new Vec3(x, y, z);
        Vec3 localOrigin = resolveLocalImpactOrigin(x, y, z, projectile);
        Vec3 worldOrigin = resolveWorldImpactOrigin(world, localOrigin, projectile);
        Vec3 rawImpactWorldOrigin = resolveWorldImpactOrigin(world, rawImpactOrigin, projectile);
        boolean onSubLevel = isOnSubLevel(projectile);

        double explodeX = onSubLevel ? localOrigin.x : worldOrigin.x;
        double explodeY = onSubLevel ? localOrigin.y : worldOrigin.y;
        double explodeZ = onSubLevel ? localOrigin.z : worldOrigin.z;
        double effectX = worldOrigin.x;
        double effectY = worldOrigin.y;
        double effectZ = worldOrigin.z;

        if (world instanceof Level level && !level.isClientSide()) {
            // 20% Unexploded Ordnance (UXO) Dud Chance for heavy projectiles
            if (projectile != null && level.random.nextFloat() < 0.20F && profile.radius() >= 6.0F) {
                float pitch = projectile.getXRot();
                float yaw = projectile.getYRot();
                String typeName = projectile.getClass().getSimpleName().toLowerCase();
                hi.entity.UxoEntity uxo = new hi.entity.UxoEntity(level, explodeX, explodeY, explodeZ, pitch, yaw, typeName, profile.radius());
                level.addFreshEntity(uxo);
                return;
            }

            if (projectile instanceof hi.entity.Fab3000trueEntity) {
                // FAB-3000 used to pass radius 120 into the custom O(r^3) crater
                // scanner.  That touched millions of blocks and stalled the server.
                // A strength-10 vanilla explosion is still large (TNT is 4), but is
                // bounded and uses Minecraft's optimized explosion implementation.
                level.explode(projectile, explodeX, explodeY, explodeZ, 10.0F, Level.ExplosionInteraction.TNT);
                applyDelayedProjectileShockwave(level, effectX, effectY, effectZ, 10.0F, projectile);
            } else {
                safeExplode(level, explodeX, explodeY, explodeZ, profile.radius(), profile.breakBlocks());
                applyDelayedProjectileShockwave(level, effectX, effectY, effectZ, profile.radius(), projectile);
            }
            applyShake(level, effectX, effectY, effectZ, profile.shakeIntensity());
        }

        if (world instanceof ServerLevel serverLevel) {
            spawnExplosionParticles(serverLevel, effectX, effectY, effectZ, projectile, profile);
            if (onSubLevel && rawImpactWorldOrigin.distanceToSqr(worldOrigin) > 1.0) {
                spawnExplosionParticles(serverLevel, rawImpactWorldOrigin.x, rawImpactWorldOrigin.y, rawImpactWorldOrigin.z, projectile, profile);
            }
        }

        if (!onSubLevel) {
            scorchGround(world, effectX, effectY, effectZ, profile.scorchRadius());
        }
    }

    public static void sendParticlesLongDistance(ServerLevel level, ParticleOptions type, double x, double y, double z, int count, double xOffset, double yOffset, double zOffset, double speed) {
        if (level == null || type == null || count <= 0) {
            return;
        }
        ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(
            type,
            true,
            x,
            y,
            z,
            (float) xOffset,
            (float) yOffset,
            (float) zOffset,
            (float) speed,
            count
        );
        for (ServerPlayer player : level.players()) {
            player.connection.send(packet);
        }
    }

    public static void scorchGround(LevelAccessor world, double x, double y, double z, int radius) {
        if (radius <= 0 || !(world instanceof Level level) || level.isClientSide()) {
            return;
        }
        BlockPos center = BlockPos.containing(x, y, z);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius) {
                    continue;
                }
                BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, center.offset(dx, 0, dz));
                BlockState state = level.getBlockState(top);
                if (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT) || state.is(Blocks.MYCELIUM)) {
                    level.setBlock(top, Blocks.PODZOL.defaultBlockState(), 3);
                }
            }
        }
    }

    private static void damageNearbyEntities(Level level, double x, double y, double z, float radius) {
        AABB box = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
        Vec3 origin = new Vec3(x, y, z);
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, box).forEach(e -> {
            double d = e.position().distanceTo(origin);
            if (d > radius) return;
            float dmg = (float) ((1.0 - d / radius) * radius * 4.0);
            e.hurt(level.damageSources().explosion(null, null), dmg);
        });
    }

    /**
     * Non-linear, cover-aware shockwave adapted from SuperbWarfare's
     * CustomExplosion (GPL-3.0). Damage and knockback arrive according to the
     * physical speed of sound instead of being applied to every target at once.
     */
    private static void applyDelayedProjectileShockwave(Level level, double x, double y, double z,
                                                        float radius, Entity projectile) {
        if (!(level instanceof ServerLevel serverLevel) || radius <= 0.0F) return;
        Vec3 origin = new Vec3(x, y, z);
        double effectRadius = radius * 2.0D;
        double baseDamage = Math.max(12.0D, radius * 16.0D);
        AABB box = new AABB(x - effectRadius, y - effectRadius, z - effectRadius,
            x + effectRadius, y + effectRadius, z + effectRadius);
        Entity attacker = projectile instanceof Projectile p ? p.getOwner() : null;

        for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class, box,
            entity -> entity.isAlive() && entity != projectile)) {
            double distance = entity.getBoundingBox().getCenter().distanceTo(origin);
            double distanceRate = distance / effectRadius;
            if (distanceRate > 1.0D) continue;

            double exposure = Mth.clamp(Explosion.getSeenPercent(origin, entity), 0.01D, 1.0D);
            double damagePercent = (1.0D - distanceRate) * exposure;
            float damage = (float) (((damagePercent * damagePercent + damagePercent) * 0.5D) * baseDamage);
            if (damage <= 0.01F) continue;

            Vec3 direction = entity.getBoundingBox().getCenter().subtract(origin);
            if (direction.lengthSqr() > 1.0E-6D) direction = direction.normalize();
            Vec3 knockback = direction.scale(Math.max(0.0D, damage * 0.015D));
            int delayTicks = Math.min(100, Math.max(0, (int) Math.floor(distance / 340.0D * 20.0D)));
            LivingEntity captured = entity;
            Vec3 capturedKnockback = knockback;
            Runnable apply = () -> {
                if (!captured.isAlive() || captured.isRemoved()) return;
                captured.invulnerableTime = 1;
                captured.hurt(serverLevel.damageSources().explosion(projectile, attacker), damage);
                captured.setDeltaMovement(captured.getDeltaMovement().add(capturedKnockback));
                captured.hurtMarked = true;
            };
            if (delayTicks == 0) apply.run();
            else CreateTheAirWarsMod.queueServerWork(delayTicks, apply);
        }
    }

    private static ProjectileExplosionProfile resolveProfile(Entity projectile, ProjectileExplosionProfile fallbackProfile) {
        if (projectile instanceof hi.entity.RocketExplosionCarrier carrier) {
            return carrier.getExplosionProfile();
        }
        return fallbackProfile;
    }

    private static Vec3 resolveLocalImpactOrigin(double x, double y, double z, Entity projectile) {
        Vec3 reportedImpact = new Vec3(x, y, z);
        if (projectile != null) {
            Vec3 pos = projectile.position();
            if (Double.isFinite(pos.x) && Double.isFinite(pos.y) && Double.isFinite(pos.z)) {
                if (Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z) && pos.distanceToSqr(reportedImpact) <= 36.0) {
                    return reportedImpact;
                }
                return pos;
            }
        }
        return reportedImpact;
    }

    private static Vec3 resolveWorldImpactOrigin(LevelAccessor world, Vec3 localOrigin, Entity projectile) {
        if (projectile != null && world instanceof Level level) {
            try {
                return dev.ryanhcode.sable.Sable.HELPER.projectOutOfSubLevel(level, localOrigin);
            } catch (Throwable ignored) {
            }
        }
        return localOrigin;
    }

    private static boolean isOnSubLevel(Entity projectile) {
        if (projectile == null) {
            return false;
        }
        try {
            return dev.ryanhcode.sable.Sable.HELPER.getContaining(projectile) != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private enum MtsExplosionStyle { ROCKET, BOMB, HEAVY_BOMB, BOMblet }

    private static void spawnExplosionParticles(ServerLevel level, double x, double y, double z, Entity projectile, ProjectileExplosionProfile profile) {
        sendParticlesLongDistance(level, profile.radius() >= 2.0F ? ParticleTypes.EXPLOSION_EMITTER : ParticleTypes.EXPLOSION,
            x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        MtsExplosionStyle style = resolveMtsExplosionStyle(projectile, profile);
        String profileName = switch (style) {
            case BOMB -> "basicbomb";
            case HEAVY_BOMB -> "heavy_bomb";
            case BOMblet -> "bomblet";
            default -> "bulletrocket";
        };
        BlockPos hitPos = BlockPos.containing(x, y, z);
        BlockState hitState = level.getBlockState(hitPos);
        if (hitState.isAir()) hitState = level.getBlockState(hitPos.below());
        boolean blockHit = !hitState.isAir();
        String material = mtsBlockMaterial(hitState);
        float yaw = projectile == null ? 0.0F : projectile.getYRot();
        float pitch = projectile == null ? 0.0F : projectile.getXRot();
        hi.network.MtsExplosionEffectPacket packet = new hi.network.MtsExplosionEffectPacket(
            profileName, x, y, z, yaw, pitch, blockHit, material);
        for (ServerPlayer player : level.players()) PacketDistributor.sendToPlayer(player, packet);
    }

    private static MtsExplosionStyle resolveMtsExplosionStyle(Entity projectile, ProjectileExplosionProfile profile) {
        if (projectile instanceof hi.entity.Fab3000trueEntity || profile.radius() >= 40.0F) return MtsExplosionStyle.HEAVY_BOMB;
        if (projectile instanceof hi.entity.GvrdcrcdEntity || profile.heavyParticleBurst()) return MtsExplosionStyle.BOMB;
        if (projectile instanceof hi.entity.M24bultEntity) return MtsExplosionStyle.BOMblet;
        return MtsExplosionStyle.ROCKET;
    }

    private static String mtsBlockMaterial(BlockState state) {
        if (state == null || state.isAir()) return "air";
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String name = id == null ? "" : id.getPath();
        if (!state.getFluidState().isEmpty()) return state.getFluidState().is(net.minecraft.tags.FluidTags.LAVA) ? "lava" : "water";
        if (name.contains("glass")) return "glass";
        if (name.contains("leaves")) return "leaves";
        if (name.contains("snow")) return "snow";
        if (name.contains("ice")) return "ice";
        if (name.contains("sand")) return "sand";
        if (name.contains("gravel")) return "gravel";
        if (name.contains("grass")) return "grass";
        if (name.contains("dirt") || name.contains("mud") || name.contains("clay")) return "dirt";
        if (name.contains("log") || name.contains("wood") || name.contains("plank")) return "wood";
        return "stone";
    }
}
