package hi.procedures;

import net.minecraft.world.level.LevelAccessor;

import java.util.HashMap;


public class ZdProcedure {
	public static void execute(LevelAccessor world, HashMap guistate) {
		if (guistate == null)
			return;
		// CreateTheAirWarsModVariables.MapVariables.get(world).WT = guistate.containsKey("text:input") ? ((EditBox) guistate.get("text:input")).getValue() : "";
		// CreateTheAirWarsModVariables.MapVariables.get(world).syncData(world);
	}
}
