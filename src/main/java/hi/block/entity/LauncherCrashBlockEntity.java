package hi.block.entity;

import hi.init.CreateTheAirWarsModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;

public class LauncherCrashBlockEntity extends BlockEntity {
    private static final int CRASH_CHECK_INTERVAL_TICKS = 4;
    private final Vector3d lastVelocity = new Vector3d();
    private boolean velocityInitialized = false;
    private int crashCheckCooldown;

    public LauncherCrashBlockEntity(BlockPos pos, BlockState state) {
        super(CreateTheAirWarsModBlockEntities.LAUNCHER_CRASH.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LauncherCrashBlockEntity be) {
        if (level == null || level.isClientSide()) return;
        if (be.crashCheckCooldown-- <= 0) {
            be.crashCheckCooldown = CRASH_CHECK_INTERVAL_TICKS;
            try {
                if (hi.util.LauncherCrashDetector.detectCrash(be, level, be.lastVelocity, be.velocityInitialized)) {
                    hi.util.ExplosionUtils.safeExplode(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5f, true);
                    hi.util.ExplosionUtils.applyShake(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2.0);
                    hi.util.ExplosionUtils.playSoundSafe(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        "create_the_air_wars:shellexp2", net.minecraft.sounds.SoundSource.NEUTRAL, 5f, 1f);
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    return;
                }
                be.velocityInitialized = hi.util.LauncherCrashDetector.updateVelocity(be, be.lastVelocity);
            } catch (Throwable ignored) {
            }
        }
    }
}
