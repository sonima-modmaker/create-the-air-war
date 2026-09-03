package hi.init;

import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import hi.CreateTheAirWarsMod;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CreateTheAirWarsModForceGroups {
    public static final DeferredRegister<ForceGroup> REGISTRY = DeferredRegister.create(ForceGroups.REGISTRY_KEY, CreateTheAirWarsMod.MODID);

    public static final DeferredHolder<ForceGroup, ForceGroup> ROCKET_THRUST = REGISTRY.register("rocket_thrust",
        () -> new ForceGroup(
            Component.translatable("create_the_air_wars.force_group.rocket_thrust"),
            null,
            0xFF8800, // Orange color
            true
        )
    );

    public static final DeferredHolder<ForceGroup, ForceGroup> GYRO_TORQUE = REGISTRY.register("gyro_torque",
        () -> new ForceGroup(
            Component.translatable("create_the_air_wars.force_group.gyro_torque"),
            null,
            0x00FF88, // Mint/Green color
            true
        )
    );
}