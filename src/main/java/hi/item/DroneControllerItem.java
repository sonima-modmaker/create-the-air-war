package hi.item;

import hi.entity.FpvDroneEntity;
import hi.client.renderer.DroneControllerItemRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class DroneControllerItem extends Item {
    private static final String TAG_DRONE_ID = "LinkedFpvDrone";
    private static final String TAG_CONTROLLING = "ControllingFpvDrone";

    public DroneControllerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.CROSSBOW;
    }

    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return DroneControllerItemRenderer.getInstance();
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        UUID droneId = getLinkedDroneId(stack);
        if (droneId == null) {
            FpvDroneEntity lookedAtDrone = findLookedAtDrone(level, player);
            if (lookedAtDrone != null) {
                if (!level.isClientSide) {
                    linkDrone(stack, lookedAtDrone);
                    player.displayClientMessage(Component.translatable("item.create_the_air_wars.drone_controller.linked").withStyle(ChatFormatting.GREEN), true);
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("item.create_the_air_wars.drone_controller.no_drone").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        boolean next = !isControlling(stack);
        setControlling(stack, next);
        player.startUsingItem(hand);
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable(next ? "item.create_the_air_wars.drone_controller.control_on" : "item.create_the_air_wars.drone_controller.control_off").withStyle(ChatFormatting.GREEN), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.create_the_air_wars.drone_controller.tooltip").withStyle(ChatFormatting.GRAY));
        UUID linked = getLinkedDroneId(stack);
        if (linked != null) {
            tooltip.add(Component.translatable("item.create_the_air_wars.drone_controller.linked").append(Component.literal(" " + linked.toString().substring(0, 8))).withStyle(ChatFormatting.GREEN));
        }
    }

    public static boolean isController(ItemStack stack) {
        return stack != null && stack.getItem() instanceof DroneControllerItem;
    }

    public static UUID getLinkedDroneId(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return null;
        }
        CompoundTag tag = customData.copyTag();
        return tag.hasUUID(TAG_DRONE_ID) ? tag.getUUID(TAG_DRONE_ID) : null;
    }

    public static void linkDrone(ItemStack stack, FpvDroneEntity drone) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putUUID(TAG_DRONE_ID, drone.getUUID());
            tag.putBoolean(TAG_CONTROLLING, false);
        });
    }

    public static void clearDrone(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.remove(TAG_DRONE_ID);
            tag.remove(TAG_CONTROLLING);
        });
    }

    public static boolean isControlling(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return false;
        }
        return customData.copyTag().getBoolean(TAG_CONTROLLING);
    }

    public static void setControlling(ItemStack stack, boolean controlling) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(TAG_CONTROLLING, controlling));
    }

    private static FpvDroneEntity findLookedAtDrone(Level level, Player player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(8.0D));
        AABB search = player.getBoundingBox().expandTowards(look.scale(8.0D)).inflate(1.0D);
        FpvDroneEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Entity entity : level.getEntities(player, search, entity -> entity instanceof FpvDroneEntity && entity.isAlive())) {
            AABB box = entity.getBoundingBox().inflate(0.45D);
            var hit = box.clip(eye, end);
            if (hit.isEmpty()) {
                continue;
            }
            double distance = eye.distanceToSqr(hit.get());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = (FpvDroneEntity) entity;
            }
        }
        return best;
    }
}
