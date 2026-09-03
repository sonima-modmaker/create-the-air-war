
package hi.command;

import org.checkerframework.checker.units.qual.s;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.Direction;
import net.minecraft.commands.Commands;

import hi.procedures.DgfgdgdProcedure;
import net.neoforged.neoforge.network.PacketDistributor;

@net.neoforged.fml.common.EventBusSubscriber
public class CtawCommand {
	@SubscribeEvent
	public static void registerCommand(RegisterCommandsEvent event) {
		RaidRocketCommand.register(event.getDispatcher());
		event.getDispatcher().register(Commands.literal("ctawdb").requires(s -> s.hasPermission(4)).executes(arguments -> {
			Level world = arguments.getSource().getLevel();
			double x = arguments.getSource().getPosition().x();
			double y = arguments.getSource().getPosition().y();
			double z = arguments.getSource().getPosition().z();
			Entity entity = arguments.getSource().getEntity();
			if (entity == null && world instanceof ServerLevel _servLevel)
				entity = FakePlayerFactory.getMinecraft(_servLevel);
			Direction direction = Direction.DOWN;
			if (entity != null)
				direction = entity.getDirection();

			DgfgdgdProcedure.execute(world, x, y, z, entity);
			return 0;
		}));
		event.getDispatcher().register(Commands.literal("ctawrecipes").requires(s -> s.hasPermission(4)).executes(arguments -> {
			Entity entity = arguments.getSource().getEntity();
			if (entity instanceof ServerPlayer player) {
				PacketDistributor.sendToPlayer(player, new hi.network.RecipeDevOpenScreenPacket());
				return 1;
			}
			return 0;
		}));
	}
}
