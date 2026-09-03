package hi.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import hi.CreateTheAirWarsMod;
import hi.entity.C75RocketEntity;
import hi.entity.Fab3000trueEntity;
import hi.entity.GvrdcrcdEntity;
import hi.entity.TomahawkbultEntity;
import hi.entity.X25mlEntity;
import hi.init.CreateTheAirWarsModEntities;
import hi.util.ProjectileChunkLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

public final class RaidRocketCommand {
    private static final int SHOT_INTERVAL_TICKS = 16;
    private static final int WAVE_INTERVAL_TICKS = 120;

    private RaidRocketCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("raid_rocket")
            .requires(source -> source.hasPermission(2))
            .then(Commands.argument("waves", IntegerArgumentType.integer(1, 50))
                .then(Commands.argument("distance", StringArgumentType.word())
                    .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[] {"low", "mid", "long"}, builder))
                    .then(Commands.argument("type", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                            new String[] {"rocket_pack", "bomb_pack", "kh25", "tomahawk", "c75"}, builder))
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 512))
                            .executes(context -> execute(
                                context.getSource(),
                                IntegerArgumentType.getInteger(context, "waves"),
                                StringArgumentType.getString(context, "distance"),
                                StringArgumentType.getString(context, "type"),
                                6,
                                IntegerArgumentType.getInteger(context, "radius"),
                                false
                            ))
                            .then(Commands.argument("count", IntegerArgumentType.integer(1, 32))
                                .executes(context -> execute(
                                    context.getSource(),
                                    IntegerArgumentType.getInteger(context, "waves"),
                                    StringArgumentType.getString(context, "distance"),
                                    StringArgumentType.getString(context, "type"),
                                    IntegerArgumentType.getInteger(context, "count"),
                                    IntegerArgumentType.getInteger(context, "radius"),
                                    false
                                ))
                                .then(Commands.argument("maneuver", BoolArgumentType.bool())
                                    .executes(context -> execute(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "waves"),
                                        StringArgumentType.getString(context, "distance"),
                                        StringArgumentType.getString(context, "type"),
                                        IntegerArgumentType.getInteger(context, "count"),
                                        IntegerArgumentType.getInteger(context, "radius"),
                                        BoolArgumentType.getBool(context, "maneuver")
                                    )))))))));
    }

    private static int execute(CommandSourceStack source, int waves, String distanceName, String typeName, int count, int radius, boolean maneuver) {
        int distance = switch (distanceName.toLowerCase(Locale.ROOT)) {
            case "low" -> 192;
            case "mid" -> 384;
            case "long" -> 768;
            default -> -1;
        };
        RaidType type = RaidType.parse(typeName);
        if (distance < 0) {
            source.sendFailure(Component.literal("Distance must be low, mid or long"));
            return 0;
        }
        if (type == null) {
            source.sendFailure(Component.literal("Type must be rocket_pack, bomb_pack, kh25, tomahawk, or c75"));
            return 0;
        }

        ServerLevel level = source.getLevel();
        Vec3 center = source.getPosition();
        for (int wave = 0; wave < waves; wave++) {
            int waveNumber = wave + 1;
            CreateTheAirWarsMod.queueServerWork(wave * WAVE_INTERVAL_TICKS + 1,
                () -> spawnWave(level, center, distance, type, count, radius, maneuver, waveNumber));
        }
        source.sendSuccess(() -> Component.literal("Raid scheduled: " + waves + " waves, " + count + "x " + type.id + ", radius " + radius + (maneuver ? " (maneuvering)" : "")), true);
        return waves;
    }

    private static void spawnWave(ServerLevel level, Vec3 center, int distance, RaidType type, int count, int radius, boolean maneuver, int waveNumber) {
        if (level.getServer() == null || !level.getServer().isRunning()) return;
        for (int shot = 0; shot < count; shot++) {
            int shotNumber = shot;
            CreateTheAirWarsMod.queueServerWork(shot * SHOT_INTERVAL_TICKS,
                () -> spawnProjectile(level, center, distance, type, radius, maneuver, waveNumber, shotNumber));
        }
    }

    private static void spawnProjectile(ServerLevel level, Vec3 center, int distance, RaidType type, int radius, boolean maneuver, int waveNumber, int shotNumber) {
        if (level.getServer() == null || !level.getServer().isRunning()) return;
        double scatterAngle = level.random.nextDouble() * Mth.TWO_PI;
        double scatterRadius = Math.sqrt(level.random.nextDouble()) * radius;
        double targetX = center.x + Math.cos(scatterAngle) * scatterRadius;
        double targetZ = center.z + Math.sin(scatterAngle) * scatterRadius;
        double targetY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(targetX), Mth.floor(targetZ)) + 0.5D;

        double startX;
        double startZ;
        double startY;
        if (type == RaidType.BOMB_PACK) {
            double dropHeight = switch (distance) { case 192 -> 90.0D; case 384 -> 130.0D; default -> 175.0D; };
            startX = targetX;
            startZ = targetZ;
            startY = Math.min(level.getMaxBuildHeight() - 8.0D, targetY + dropHeight);
        } else {
            double approachAngle = level.random.nextDouble() * Mth.TWO_PI;
            double approachDistance = distance * (0.9D + level.random.nextDouble() * 0.2D);
            double altitude = switch (distance) { case 192 -> 105.0D; case 384 -> 145.0D; default -> 190.0D; };
            startX = targetX + Math.cos(approachAngle) * approachDistance;
            startZ = targetZ + Math.sin(approachAngle) * approachDistance;
            startY = Math.min(level.getMaxBuildHeight() - 8.0D, targetY + altitude);
        }
        Vec3 start = new Vec3(startX, startY, startZ);
        double cepScatterAngle = level.random.nextDouble() * Mth.TWO_PI;
        double cepScatterDist = 4.0D + level.random.nextDouble() * (radius * 0.4D + 12.0D);
        Vec3 target = new Vec3(
            targetX + Math.cos(cepScatterAngle) * cepScatterDist,
            targetY,
            targetZ + Math.sin(cepScatterAngle) * cepScatterDist
        );
        Vec3 direction = target.subtract(start).normalize();

        AbstractArrow projectile = createProjectile(level, type, shotNumber, start, target, direction, maneuver);
        if (projectile == null) return;
        projectile.setOwner(null);
        if (projectile instanceof X25mlEntity x25) {
            double terrainY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mth.floor(startX), Mth.floor(startZ));
            start = new Vec3(startX, Math.min(level.getMaxBuildHeight() - 8.0D, terrainY + 24.0D), startZ);
            Vec3 towardTarget = target.subtract(start);
            direction = new Vec3(towardTarget.x, 0.0D, towardTarget.z).normalize();
            x25.setInitialDirection(direction);
            x25.setLaunchStartPos(start);
            x25.setRaidTarget(target);
            x25.setManeuvering(maneuver);
            x25.setDeltaMovement(direction.scale(X25mlEntity.INITIAL_FORWARD_SPEED));
        } else if (projectile instanceof TomahawkbultEntity tomahawk) {
            tomahawk.setTarget(target.x, target.y, target.z);
            tomahawk.setManeuvering(maneuver);
        }
        projectile.setPos(start);
        projectile.setYRot((float) Math.toDegrees(Math.atan2(direction.x, direction.z)));
        projectile.setXRot((float) Math.toDegrees(Math.atan2(direction.y, Math.max(1.0E-4D, direction.horizontalDistance()))));

        level.getChunkAt(BlockPos.containing(start));
        ProjectileChunkLoader.prime(projectile);
        if (!level.addFreshEntity(projectile)) ProjectileChunkLoader.release(projectile);
        if (shotNumber == 0) {
            level.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("Raid wave " + waveNumber + " arrived (" + type.id + ")"), false);
        }
    }

    private static AbstractArrow createProjectile(ServerLevel level, RaidType type, int shotNumber, Vec3 start, Vec3 target, Vec3 direction, boolean maneuver) {
        int payload = shotNumber % 2;
        if (type == RaidType.BOMB_PACK) {
            if (payload == 0) {
                Fab3000trueEntity bomb = new Fab3000trueEntity(CreateTheAirWarsModEntities.FAB_3000TRUE.get(), level);
                bomb.setNoGravity(false);
                bomb.setDeltaMovement(0.0D, -0.45D, 0.0D);
                return bomb;
            }
            GvrdcrcdEntity bomb = new GvrdcrcdEntity(CreateTheAirWarsModEntities.GVRDCRCD.get(), level);
            bomb.setNoGravity(false);
            bomb.setDeltaMovement(0.0D, -0.5D, 0.0D);
            return bomb;
        }

        if (type == RaidType.KH25) {
            X25mlEntity missile = new X25mlEntity(CreateTheAirWarsModEntities.X25ML_MISSILE.get(), level);
            missile.setInitialDirection(direction);
            missile.setLaunchStartPos(start);
            missile.setRaidTarget(target);
            missile.setManeuvering(maneuver);
            missile.setNoGravity(true);
            missile.setDeltaMovement(direction.scale(X25mlEntity.INITIAL_FORWARD_SPEED));
            return missile;
        }

        if (type == RaidType.TOMAHAWK) {
            TomahawkbultEntity missile = new TomahawkbultEntity(CreateTheAirWarsModEntities.TOMAHAWKBULT.get(), level);
            missile.setTarget(target.x, target.y, target.z);
            missile.setManeuvering(maneuver);
            missile.setNoGravity(true);
            missile.setDeltaMovement(direction.scale(2.8D));
            return missile;
        }

        if (type == RaidType.C75) {
            C75RocketEntity missile = new C75RocketEntity(CreateTheAirWarsModEntities.C75_ROCKET.get(), level);
            missile.setNoGravity(true);
            missile.setDeltaMovement(direction.scale(5.5D));
            return missile;
        }

        // Default ROCKET_PACK
        return switch (payload) {
            case 0 -> {
                TomahawkbultEntity missile = new TomahawkbultEntity(CreateTheAirWarsModEntities.TOMAHAWKBULT.get(), level);
                missile.setTarget(target.x, target.y, target.z);
                missile.setManeuvering(maneuver);
                missile.setNoGravity(true);
                missile.setDeltaMovement(direction.scale(2.8D));
                yield missile;
            }
            default -> {
                X25mlEntity missile = new X25mlEntity(CreateTheAirWarsModEntities.X25ML_MISSILE.get(), level);
                missile.setInitialDirection(direction);
                missile.setLaunchStartPos(start);
                missile.setRaidTarget(target);
                missile.setManeuvering(maneuver);
                missile.setNoGravity(true);
                missile.setDeltaMovement(direction.scale(X25mlEntity.INITIAL_FORWARD_SPEED));
                yield missile;
            }
        };
    }

    private enum RaidType {
        ROCKET_PACK("rocket_pack"), BOMB_PACK("bomb_pack"), KH25("kh25"), TOMAHAWK("tomahawk"), C75("c75");
        private final String id;
        RaidType(String id) { this.id = id; }

        private static RaidType parse(String value) {
            return switch (value.toLowerCase(Locale.ROOT)) {
                case "rocket_pack", "rockets", "ракеты", "ракетный_пакет" -> ROCKET_PACK;
                case "bomb_pack", "bombs", "бомбы", "бомбовый_пакет" -> BOMB_PACK;
                case "kh25", "kh", "x25", "хашка", "ха25", "х25" -> KH25;
                case "tomahawk", "томагавк" -> TOMAHAWK;
                case "c75", "с75" -> C75;
                default -> null;
            };
        }
    }
}
