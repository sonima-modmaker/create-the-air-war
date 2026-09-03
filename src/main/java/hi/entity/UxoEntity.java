package hi.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import hi.init.CreateTheAirWarsModEntities;
import hi.util.ExplosionUtils;

public class UxoEntity extends Entity {
    private String projectileType = "fab_1500";
    private float explosionRadius = 24.0F;

    public UxoEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    public UxoEntity(Level level, double x, double y, double z, float pitch, float yaw, String projectileType, float explosionRadius) {
        this(CreateTheAirWarsModEntities.UXO_ENTITY.get(), level);
        this.setPos(x, y, z);
        this.setXRot(pitch);
        this.setYRot(yaw);
        this.projectileType = projectileType;
        this.explosionRadius = explosionRadius;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && amount > 1.0F) {
            // Gunfire or explosion detonates the UXO
            ExplosionUtils.safeExplode(this.level(), this.getX(), this.getY(), this.getZ(), this.explosionRadius, true);
            this.discard();
            return true;
        }
        return super.hurt(source, amount);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!this.level().isClientSide) {
            // Disarm UXO on right click with shears / empty hand
            this.level().playSound(null, this.blockPosition(), SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.BLOCKS, 1.0F, 1.2F);
            player.displayClientMessage(Component.literal("§a[UXO] Unexploded Ordnance defused successfully!"), true);
            this.discard();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    public String getProjectileType() {
        return this.projectileType;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("ProjectileType")) {
            this.projectileType = tag.getString("ProjectileType");
        }
        if (tag.contains("ExplosionRadius")) {
            this.explosionRadius = tag.getFloat("ExplosionRadius");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("ProjectileType", this.projectileType);
        tag.putFloat("ExplosionRadius", this.explosionRadius);
    }
}
