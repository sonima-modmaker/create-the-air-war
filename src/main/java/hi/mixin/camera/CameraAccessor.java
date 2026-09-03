package hi.mixin.camera;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraAccessor {
    @Accessor("entity")
    Entity ctaw$getEntity();

    @Accessor("entity")
    void ctaw$setEntity(Entity entity);

    @Invoker("setPosition")
    void ctaw$invokeSetPosition(Vec3 position);

    @Invoker("setRotation")
    void ctaw$invokeSetRotation(float yaw, float pitch);
}
