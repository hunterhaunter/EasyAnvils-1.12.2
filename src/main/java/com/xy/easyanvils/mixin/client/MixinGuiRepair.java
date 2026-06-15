package com.xy.easyanvils.mixin.client;

import com.xy.easyanvils.config.EasyAnvilsConfig;

import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Constant;

/**
 * Two purely additive client tweaks to the anvil screen, matching the original ModAnvilScreen:
 *   1. Allow item names up to 50 characters (vanilla caps at 35).
 *   2. Make the "Too Expensive!" label respect the configurable cost limit instead of the
 *      hardcoded 40 - so an unlimited (-1) or raised limit no longer paints the warning over a
 *      perfectly craftable result.
 * Formatting-code support is handled server-side (see AnvilCostHandler), so the field itself needs
 * no replacement here.
 */
@Mixin(net.minecraft.client.gui.GuiRepair.class)
public abstract class MixinGuiRepair {

    @Shadow private GuiTextField nameField;

    @Inject(method = "initGui", at = @At("TAIL"))
    private void easyanvils$extendNameLength(CallbackInfo ci) {
        if (this.nameField != null) {
            this.nameField.setMaxStringLength(50);
        }
    }

    @ModifyConstant(method = "drawGuiContainerForegroundLayer", constant = @Constant(intValue = 40))
    private int easyanvils$tooExpensiveLimit(int vanilla) {
        return EasyAnvilsConfig.enableTooExpensiveLimit
                ? EasyAnvilsConfig.tooExpensiveLimit
                : Integer.MAX_VALUE;
    }
}
