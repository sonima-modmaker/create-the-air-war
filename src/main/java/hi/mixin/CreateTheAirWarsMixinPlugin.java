package hi.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class CreateTheAirWarsMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith("SectionOcclusionGraphMixin")) {
            return false;
        }
        if (mixinClassName.endsWith("SodiumRenderSectionManagerMixin")) {
            return isModLoaded("sodium");
        }
        if (mixinClassName.contains("Sodium")) {
            return isModLoaded("sodium");
        }
        if (mixinClassName.contains("LodRenderer") || mixinClassName.contains("ClientApi")) {
            return isModLoaded("distanthorizons");
        }
        return true;
    }

    private boolean isModLoaded(String modid) {
        try {
            return net.neoforged.fml.loading.LoadingModList.get().getModFileById(modid) != null;
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
