
package hi.potion;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;

public class LocationMobEffect extends MobEffect {
	public LocationMobEffect() {
		super(MobEffectCategory.NEUTRAL, -65536);
		//this.addAttributeModifier(ForgeMod.NAMETAG_DISTANCE.get(), "4a351e2e-c6aa-3534-be1d-93955d9c436c", 200, AttributeModifier.Operation.MULTIPLY_BASE);
		//this.addAttributeModifier(ForgeMod.ENTITY_REACH.get(), "36734528-8338-3c22-86a6-4d4e0821db7d", 200, AttributeModifier.Operation.ADDITION);
		this.addAttributeModifier(Attributes.FOLLOW_RANGE, net.minecraft.resources.ResourceLocation.parse("create_the_air_wars:location_range"), 200, AttributeModifier.Operation.ADD_VALUE);
	}

	
}
