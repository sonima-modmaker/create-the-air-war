package hi.item;

import hi.entity.FpvDroneEntity;
import hi.client.renderer.FpvDroneItemRenderer;
import hi.init.CreateTheAirWarsModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.function.Consumer;

public class FpvDroneItem extends Item {
    public FpvDroneItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return FpvDroneItemRenderer.getInstance();
            }
        });
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level) || context.getPlayer() == null) {
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }
        Vec3 spawn = Vec3.atBottomCenterOf(context.getClickedPos().relative(context.getClickedFace())).add(0.0D, 0.08D, 0.0D);
        FpvDroneEntity drone = new FpvDroneEntity(CreateTheAirWarsModEntities.FPV_DRONE.get(), level);
        drone.setOwner(context.getPlayer().getUUID());
        drone.setPos(spawn.x, spawn.y, spawn.z);
        drone.setYRot(context.getPlayer().getYRot());
        level.addFreshEntity(drone);
        if (!context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.create_the_air_wars.fpv_drone.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
