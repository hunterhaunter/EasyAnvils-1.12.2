package com.xy.easyanvils.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;

/**
 * Relaxes the anvil result slot's pickup gate so a <em>free</em> (0-level) rename can still be
 * taken.
 *
 * <p>Vanilla's output slot ({@code ContainerRepair$2}) requires {@code maximumCost > 0} in
 * {@code canTakeStack}, which means a result that costs 0 levels can never be removed. The
 * original Easy Anvils mod overrides this (its {@code m_6560_} uses {@code cost >= 0}), which is
 * what makes free renames usable. We reproduce that by widening the {@code > 0} comparison to
 * {@code >= 0} via {@link Constant.Condition#GREATER_THAN_ZERO}.
 *
 * <p>Targets the anonymous result slot by its synthetic name; this is vanilla's own class and the
 * index is stable across environments. Uses the MCP method name to match the RFG workspace.
 */
@Mixin(targets = "net.minecraft.inventory.ContainerRepair$2")
public abstract class MixinContainerRepairResultSlot {

    @ModifyConstant(
            method = "canTakeStack",
            constant = @Constant(intValue = 0, expandZeroConditions = Constant.Condition.GREATER_THAN_ZERO))
    private int easyanvils$allowFreePickup(int original) {
        // expandZeroConditions rewrites "maximumCost > 0" into "maximumCost > <return>".
        // Returning -1 makes it "maximumCost > -1", i.e. "maximumCost >= 0", so a 0-cost free
        // rename result becomes takeable (vanilla blocks it; the original mod's m_6560_ allows it).
        return -1;
    }
}
