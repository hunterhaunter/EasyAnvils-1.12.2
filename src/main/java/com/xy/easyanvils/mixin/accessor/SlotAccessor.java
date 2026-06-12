package com.xy.easyanvils.mixin.accessor;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Lets the anvil container repoint its input slots' backing inventory at the anvil tile entity. */
@Mixin(Slot.class)
public interface SlotAccessor {

    @Mutable
    @Accessor("inventory")
    void easyanvils$setInventory(IInventory inventory);
}
