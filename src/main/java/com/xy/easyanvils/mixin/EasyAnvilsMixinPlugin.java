package com.xy.easyanvils.mixin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Compatibility gate for Easy Anvils mixins.
 *
 * The cost / break-chance / repair / name-tag features are implemented purely with
 * Forge events (AnvilUpdateEvent, AnvilRepairEvent, PlayerInteractEvent) and need no
 * mixins, so they always work alongside other anvil mods. Only the two structural
 * mixins required for the "items stay in the anvil" feature touch vanilla classes:
 *
 *   - MixinBlockAnvil       (gives the anvil a TileEntity)
 *   - MixinContainerRepair  (backs the input slots with that TileEntity)
 *
 * Both are gated here: if another mod already replaces or deeply rewrites the anvil
 * container, the persistent-inventory mixins disable themselves so we never fight it.
 * GuiRepair mixin (name field length + formatting) is purely additive and stays on.
 */
public class EasyAnvilsMixinPlugin implements IMixinConfigPlugin {

    private static final Logger LOGGER = LogManager.getLogger("easyanvils-mixin");

    // Known mods that replace/override the vanilla anvil container wholesale.
    // Detected lazily at first target-class load, by which point all jars are present.
    private static Boolean conflictingAnvilMod;

    /** True when a conflicting anvil mod disabled our persistent-inventory + shift-click XP mixins. */
    public static boolean isAnvilModConflict() {
        return isConflictingAnvilModLoaded();
    }

    private static boolean isConflictingAnvilModLoaded() {
        if (conflictingAnvilMod == null) {
            conflictingAnvilMod = isClassPresent("com.lumberjacksparrow.anvilpatchevil.mixin.ContainerRepairMixin");
            if (conflictingAnvilMod) {
                LOGGER.info("Conflicting anvil container mod detected - disabling Easy Anvils "
                        + "persistent-inventory mixins (cost/break/repair features still active via events)");
            }
        }
        return conflictingAnvilMod;
    }

    private static boolean isClassPresent(String className) {
        try {
            Class.forName(className, false, EasyAnvilsMixinPlugin.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith("MixinContainerRepair")
                || mixinClassName.endsWith("MixinBlockAnvil")) {
            return !isConflictingAnvilModLoaded();
        }
        return true;
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
