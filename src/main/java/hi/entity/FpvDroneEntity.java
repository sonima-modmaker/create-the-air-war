package hi.entity;

import hi.init.CreateTheAirWarsModItems;
import hi.item.DroneControllerItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class FpvDroneEntity extends Entity implements ItemSupplier {
    private static final EntityDataAccessor<Float> THROTTLE = SynchedEntityData.defineId(FpvDroneEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ROLL = SynchedEntityData.defineId(FpvDroneEntity.class, EntityDataSerializers.FLOAT);
    private static final double CRASH_SPEED_SQR = 0.22D;
    private UUID ownerId;
    private float yawInput;
    private float pitchInput;
    private float rollTarget;
    private float throttleDelta;

    public FpvDroneEntity(EntityType<? extends FpvDroneEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(THROTTLE, 0.0F);
        builder.define(ROLL, 0.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            tickPhysics();
        }
    }

    private void tickPhysics() {
        float throttle = Mth.clamp(getThrottle() + throttleDelta, 0.0F, 1.0F);
        setThrottle(throttle);
        throttleDelta = 0.0F;
        this.yRotO = getYRot();
        this.xRotO = getXRot();
        setYRot(getYRot() + yawInput);
        setXRot(Mth.clamp(getXRot() + pitchInput, -70.0F, 70.0F));
        setRoll(Mth.lerp(0.26F, getRoll(), rollTarget));
        yawInput *= 0.45F;
        pitchInput *= 0.45F;
        rollTarget *= 0.82F;

        Vec3 thrustDirection = getThrustVector();
        Vec3 velocity = getDeltaMovement()
            .add(thrustDirection.scale(throttle * 0.078D))
            .add(0.0D, -0.026D, 0.0D)
            .scale(0.965D);
        setDeltaMovement(velocity);
        move(MoverType.SELF, velocity);

        if ((horizontalCollision || verticalCollision) && velocity.lengthSqr() > CRASH_SPEED_SQR) {
            explode();
            return;
        }
        if (tickCount % 12 == 0) {
            level().playSound(null, blockPosition(), SoundEvents.BEE_LOOP, SoundSource.NEUTRAL, 0.18F + throttle * 0.25F, 1.8F + throttle);
        }
    }

    public Vec3 getForwardVector() {
        float yaw = (float) Math.toRadians(getYRot());
        float pitch = (float) Math.toRadians(getXRot());
        float cosPitch = Mth.cos(pitch);
        return new Vec3(-Mth.sin(yaw) * cosPitch, -Mth.sin(pitch), Mth.cos(yaw) * cosPitch).normalize();
    }

    public Vec3 getThrustVector() {
        Vec3 forward = getForwardVector();
        Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = worldUp.cross(forward);
        if (right.lengthSqr() < 1.0E-4D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }
        Vec3 bodyUp = forward.cross(right).normalize();
        double roll = Math.toRadians(getRoll());
        double cos = Math.cos(roll);
        double sin = Math.sin(roll);
        Vec3 rolledUp = bodyUp.scale(cos)
            .add(forward.cross(bodyUp).scale(sin))
            .add(forward.scale(forward.dot(bodyUp) * (1.0D - cos)));
        return rolledUp.normalize();
    }

    public void applyControllerInput(float mouseYaw, float mousePitch, float yaw, float pitch, float throttleChange) {
        this.yawInput += Mth.clamp(mouseYaw + yaw, -4.5F, 4.5F);
        this.pitchInput += Mth.clamp(mousePitch + pitch, -3.8F, 3.8F);
        this.rollTarget = Mth.clamp(-yaw * 20.0F, -42.0F, 42.0F);
        this.throttleDelta += Mth.clamp(throttleChange, -0.04F, 0.04F);
    }

    private void explode() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            discard();
            return;
        }
        serverLevel.explode(this, getX(), getY(), getZ(), 3.5F, Level.ExplosionInteraction.TNT);
        discard();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!DroneControllerItem.isController(stack)) {
            return InteractionResult.PASS;
        }
        if (!level().isClientSide) {
            DroneControllerItem.linkDrone(stack, this);
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("item.create_the_air_wars.drone_controller.linked").withStyle(net.minecraft.ChatFormatting.GREEN), true);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(CreateTheAirWarsModItems.FPV_DRONE.get());
    }

    public float getThrottle() {
        return entityData.get(THROTTLE);
    }

    private void setThrottle(float throttle) {
        entityData.set(THROTTLE, throttle);
    }

    public float getRoll() {
        return entityData.get(ROLL);
    }

    private void setRoll(float roll) {
        entityData.set(ROLL, roll);
    }

    public void setOwner(UUID ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            ownerId = tag.getUUID("Owner");
        }
        setThrottle(tag.getFloat("Throttle"));
        setRoll(tag.getFloat("Roll"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) {
            tag.putUUID("Owner", ownerId);
        }
        tag.putFloat("Throttle", getThrottle());
        tag.putFloat("Roll", getRoll());
    }
}
