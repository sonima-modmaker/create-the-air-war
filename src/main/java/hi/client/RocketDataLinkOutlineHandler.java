package hi.client;

import hi.item.RocketDataLinkItem;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class RocketDataLinkOutlineHandler {
    private static final int UPDATE_INTERVAL_TICKS = 4;
    private static final Set<String> ACTIVE_KEYS = new HashSet<>();
    
    public static void clientTick() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;
        if (player == null || level == null) {
            clearOutlines();
            return;
        }
        if ((level.getGameTime() % UPDATE_INTERVAL_TICKS) != 0) return;
        
        ItemStack mainHand = player.getMainHandItem();
        if (!(mainHand.getItem() instanceof RocketDataLinkItem)) {
            clearOutlines();
            return;
        }
        
        List<BlockPos> selected = RocketDataLinkItem.getSelectedEngines(mainHand);
        if (selected.isEmpty()) {
            clearOutlines();
            return;
        }

        Set<String> nextKeys = new HashSet<>();
        
        for (int i = 0; i < selected.size(); i++) {
            BlockPos pos = selected.get(i);
            BlockState state = level.getBlockState(pos);
            VoxelShape shape = state.getShape(level, pos);
            AABB box = shape.isEmpty() ? new AABB(pos) : shape.bounds().move(pos);
            String key = "rocket_data_link_engine_" + i;
            nextKeys.add(key);
            
            Outliner.getInstance().showAABB(key, box)
                .colored(0xffcb74)
                .lineWidth(1 / 16f)
                .disableCull();
        }

        for (String key : ACTIVE_KEYS) {
            if (!nextKeys.contains(key)) {
                Outliner.getInstance().remove(key);
            }
        }
        ACTIVE_KEYS.clear();
        ACTIVE_KEYS.addAll(nextKeys);
    }

    private static void clearOutlines() {
        if (ACTIVE_KEYS.isEmpty()) {
            return;
        }
        for (String key : ACTIVE_KEYS) {
            Outliner.getInstance().remove(key);
        }
        ACTIVE_KEYS.clear();
    }
}
