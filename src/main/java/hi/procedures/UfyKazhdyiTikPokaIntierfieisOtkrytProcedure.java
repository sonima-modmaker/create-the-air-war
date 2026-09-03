package hi.procedures;

import net.minecraft.world.level.LevelAccessor;

import java.util.HashMap;


public class UfyKazhdyiTikPokaIntierfieisOtkrytProcedure {
	public static void execute(LevelAccessor world, HashMap guistate) {
		if (guistate == null)
			return;
		Object outputField = guistate.get("text:output");
		if (outputField == null)
			return;
		try {
			String value = ""; // CreateTheAirWarsModVariables.MapVariables.get(world).WT;
			String currentValue = (String) outputField.getClass().getMethod("getValue").invoke(outputField);
			if (!value.equals(currentValue))
				outputField.getClass().getMethod("setValue", String.class).invoke(outputField, value);
		} catch (ReflectiveOperationException ignored) {
		}
	}
}
