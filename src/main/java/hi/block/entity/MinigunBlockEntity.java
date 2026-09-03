package hi.block.entity;

import hi.block.MinigunBlock;
import hi.entity.MachinegunShellEntity;
import hi.init.CreateTheAirWarsModBlockEntities;
import hi.init.CreateTheAirWarsModEntities;
import hi.init.CreateTheAirWarsModParticleTypes;
import hi.init.CreateTheAirWarsModSounds;
import hi.util.ProjectileLaunchHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MinigunBlockEntity extends BlockEntity {
    private static final float SPIN_UP_PER_TICK = 0.095F;
    private static final float SPIN_DOWN_PER_TICK = 0.055F;
    private static final float FIRE_SPIN_THRESHOLD = 0.72F;
    private static final float MAX_BARREL_DEGREES_PER_TICK = 60.0F;
    private static final float MAX_SHOTS_PER_TICK = 2.15F;
    private static final int CLIENT_SYNC_INTERVAL = 5;
    private static final int CLIENT_SOUND_UPDATE_INTERVAL = 4;

    private double accumulator;
    private boolean powered;
    private float barrelSpeed;
    private float barrelAngle;

    public MinigunBlockEntity(BlockPos pos, BlockState state) {
        super(CreateTheAirWarsModBlockEntities.MINIGUN.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MinigunBlockEntity blockEntity) {
        boolean poweredNow = level.getBestNeighborSignal(pos) > 0;
        boolean changed = poweredNow != blockEntity.powered;
        blockEntity.powered = poweredNow;

        if (poweredNow) {
            blockEntity.barrelSpeed = Mth.clamp(blockEntity.barrelSpeed + SPIN_UP_PER_TICK, 0.0F, 1.0F);
        } else {
            blockEntity.barrelSpeed = Mth.clamp(blockEntity.barrelSpeed - SPIN_DOWN_PER_TICK, 0.0F, 1.0F);
            blockEntity.accumulator = 0.0D;
        }
        blockEntity.barrelAngle = Mth.wrapDegrees(blockEntity.barrelAngle + MAX_BARREL_DEGREES_PER_TICK * blockEntity.barrelSpeed);

        if (level.isClientSide) {
            tickClient(level, pos, blockEntity);
            return;
        }

        if (changed) {
            level.sendBlockUpdated(pos, state, state, 3);
            if (!poweredNow) {
                level.playSound(null, pos, CreateTheAirWarsModSounds.GUN_12_7MM_LASTSHOT.get(), SoundSource.BLOCKS, 0.9F, 0.78F);
            }
        }

        if (poweredNow && blockEntity.barrelSpeed >= FIRE_SPIN_THRESHOLD) {
            double firePower = (blockEntity.barrelSpeed - FIRE_SPIN_THRESHOLD) / (1.0F - FIRE_SPIN_THRESHOLD);
            blockEntity.accumulator += Mth.clamp(firePower, 0.0D, 1.0D) * MAX_SHOTS_PER_TICK;
            while (blockEntity.accumulator >= 1.0D) {
                blockEntity.accumulator -= 1.0D;
                blockEntity.fireOnce(level, pos, state);
            }
        }

        if (changed || (level.getGameTime() + pos.asLong()) % CLIENT_SYNC_INTERVAL == 0L) {
            blockEntity.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    private void fireOnce(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(MinigunBlock.FACING);
        Vec3 dir = Vec3.atLowerCornerOf(facing.getNormal()).normalize();
        double sx = pos.getX() + 0.5D + dir.x * 1.25D;
        double sy = pos.getY() + 0.34D;
        double sz = pos.getZ() + 0.5D + dir.z * 1.25D;
        MachinegunShellEntity shell = new MachinegunShellEntity(CreateTheAirWarsModEntities.MACHINEGUN_SHELL.get(), level);
        double spread = 0.075D;
        double rx = dir.x + (level.getRandom().nextDouble() - 0.5D) * spread;
        double ry = dir.y + (level.getRandom().nextDouble() - 0.5D) * spread;
        double rz = dir.z + (level.getRandom().nextDouble() - 0.5D) * spread;

        if (level instanceof ServerLevel serverLevel) {
            ProjectileLaunchHelper.LaunchTransform launch = ProjectileLaunchHelper.resolveBlockLaunch(
                serverLevel,
                pos,
                sx,
                sy,
                sz,
                rx,
                ry,
                rz
            );
            shell.setPos(launch.position().x, launch.position().y, launch.position().z);
            shell.shoot(launch.direction().x, launch.direction().y, launch.direction().z, 8.8F, 0);
            shell.setLaunchContext(launch.position(), launch.inheritedVelocity());
            ProjectileLaunchHelper.applyInheritedVelocity(shell, launch.inheritedVelocity());
        } else {
            shell.setPos(sx, sy, sz);
            shell.shoot(rx, ry, rz, 8.8F, 0);
        }

        shell.setBaseDamage(4);
        level.addFreshEntity(shell);
        spawnSmoke(level, pos, state);
    }

    private void spawnSmoke(Level level, BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel serverLevel) {
            Direction facing = state.getValue(MinigunBlock.FACING);
            Vec3 dir = Vec3.atLowerCornerOf(facing.getNormal()).normalize();
            double px = pos.getX() + 0.5D + dir.x * 1.2D;
            double py = pos.getY() + 0.48D;
            double pz = pos.getZ() + 0.5D + dir.z * 1.2D;
            if (serverLevel.getRandom().nextFloat() > 0.3F) {
                return;
            }
            serverLevel.sendParticles(CreateTheAirWarsModParticleTypes.MACHINEGUN_SMOKE.get(), px, py, pz, 1, 0.04D, 0.02D, 0.04D, 0.02D);
        }
    }

    private static void tickClient(Level level, BlockPos pos, MinigunBlockEntity blockEntity) {
        if (((level.getGameTime() + pos.asLong()) & (CLIENT_SOUND_UPDATE_INTERVAL - 1)) != 0L) {
            return;
        }
        hi.client.sound.MinigunClientSound.tick(blockEntity);
    }

    public float getBarrelAngle(float partialTick) {
        return Mth.wrapDegrees(this.barrelAngle + MAX_BARREL_DEGREES_PER_TICK * this.barrelSpeed * partialTick);
    }

    public float getBarrelSpeed() {
        return this.barrelSpeed;
    }

    public boolean isPowered() {
        return this.powered;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.accumulator = tag.getDouble("accumulator");
        this.powered = tag.getBoolean("powered");
        this.barrelSpeed = tag.getFloat("barrelSpeed");
        this.barrelAngle = tag.getFloat("barrelAngle");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putDouble("accumulator", this.accumulator);
        tag.putBoolean("powered", this.powered);
        tag.putFloat("barrelSpeed", this.barrelSpeed);
        tag.putFloat("barrelAngle", this.barrelAngle);
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, provider);
        return tag;
    }
}
