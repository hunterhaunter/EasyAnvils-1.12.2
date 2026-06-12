package com.xy.easyanvils.mixin;

import com.xy.easyanvils.mixin.accessor.SlotAccessor;
import com.xy.easyanvils.tileentity.TileEntityAnvil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes the anvil's two input slots read/write a {@link TileEntityAnvil} directly, so items "stay
 * in the anvil" exactly like the original (the block entity IS the input inventory - no copy).
 * Because there is a single live inventory, a hopper can keep filling the anvil while the GUI is
 * open and the result refreshes automatically.
 *
 * Only engages for a real anvil with a {@code TileEntityAnvil}; any other opener keeps vanilla
 * behaviour (including dropping its temporary inputs on close). Gated by {@link EasyAnvilsMixinPlugin}.
 * Uses MCP (deobf) names to match this RetroFuturaGradle workspace.
 */
@Mixin(net.minecraft.inventory.ContainerRepair.class)
public abstract class MixinContainerRepair extends Container {

    @Shadow @Final @Mutable private IInventory inputSlots;
    @Shadow @Final private IInventory outputSlot;
    @Shadow @Final private World world;
    @Shadow @Final private BlockPos pos;
    @Shadow private String repairedItemName;
    @Shadow public int maximumCost;
    @Shadow public int materialCost;

    @Shadow public abstract void updateRepairOutput();

    // Two jobs at the top of every recompute:
    //
    // 1) Clear the stale result. Vanilla updateRepairOutput does NOT clear the result slot when an
    //    AnvilUpdateEvent is canceled - it just returns, leaving the previous result in place. Our
    //    "no valid result" path cancels the event, so without this a swap-the-input dupe is possible
    //    (old result stays takeable). Clearing here makes a canceled recompute show no result.
    //
    // 2) Drive single-item renames through AnvilUpdateEvent. In 1.12.2 Forge only fires
    //    onAnvilChange (AnvilUpdateEvent) when the RIGHT slot is non-empty, so a pure rename
    //    (left item + typed name, right empty) never reaches our AnvilCostHandler and vanilla
    //    charges a flat 1 level - ignoring freeRenames. The original ModAnvilMenu.createResult
    //    handled this case natively. We replicate it by firing the event ourselves for the
    //    single-item case and applying the result exactly like ForgeHooks.onAnvilChange, so the
    //    same config-driven cost math (free renames -> cost 0) applies. Cancelling vanilla then
    //    prevents its flat rename charge. Other mods' AnvilUpdateEvent handlers get a say too.
    @Inject(method = "updateRepairOutput", at = @At("HEAD"), cancellable = true)
    private void easyanvils$clearStaleResult(CallbackInfo ci) {
        this.outputSlot.setInventorySlotContents(0, ItemStack.EMPTY);

        ItemStack left = this.inputSlots.getStackInSlot(0);
        ItemStack right = this.inputSlots.getStackInSlot(1);
        // Only the pure single-item case: combine (right present) already fires onAnvilChange,
        // and an empty left is a vanilla no-op.
        if (left.isEmpty() || !right.isEmpty()) {
            // Push the cleared result to the client now. A recompute driven by a tile change (an
            // invalid swap that cancels the event, or a hopper insert) has no trailing
            // detectAndSendChanges like a slot click does, so without this the just-cleared output
            // is never synced and a stale "ghost" result lingers client-side (vanishes on pickup).
            // Vanilla re-sends the real result on its own success path, so this is harmless.
            this.detectAndSendChanges();
            return;
        }

        AnvilUpdateEvent event = new AnvilUpdateEvent(left, right, this.repairedItemName, left.getRepairCost());
        if (MinecraftForge.EVENT_BUS.post(event) || event.getOutput().isEmpty()) {
            // Cancelled, or no handler produced a result -> no usable result. Sync the cleared
            // output so no ghost result is left on the client (see note above).
            this.maximumCost = 0;
            this.detectAndSendChanges();
            ci.cancel();
            return;
        }
        this.outputSlot.setInventorySlotContents(0, event.getOutput());
        this.maximumCost = event.getCost();
        this.materialCost = event.getMaterialCost();
        this.detectAndSendChanges();
        ci.cancel();
    }

    @Unique
    private TileEntityAnvil easyanvils$tile() {
        if (this.world == null || this.pos == null) {
            return null;
        }
        TileEntity tileEntity = this.world.getTileEntity(this.pos);
        return tileEntity instanceof TileEntityAnvil ? (TileEntityAnvil) tileEntity : null;
    }

    // Point the input inventory + the two input slots straight at the tile entity.
    @Inject(method = "<init>(Lnet/minecraft/entity/player/InventoryPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/EntityPlayer;)V", at = @At("TAIL"))
    private void easyanvils$bindTile(CallbackInfo ci) {
        TileEntityAnvil tile = this.easyanvils$tile();
        if (tile == null) {
            return;
        }
        this.inputSlots = tile;
        // inventorySlots 0 and 1 are the two anvil input slots (output is index 2).
        ((SlotAccessor) (Object) this.inventorySlots.get(0)).easyanvils$setInventory(tile);
        ((SlotAccessor) (Object) this.inventorySlots.get(1)).easyanvils$setInventory(tile);
        // Recompute the result on every tile change (player edit, hopper insert while open, swap).
        // Call updateRepairOutput directly so the result + cost never go stale - relying on
        // onCraftMatrixChanged's "inventoryIn == inputSlots" guard was fragile and could leave a
        // stale result/cost (rename appearing to cost xp, and a swap-the-input dupe).
        tile.setChangeListener(this::updateRepairOutput);
        this.updateRepairOutput();
    }

    // On close: items live in the tile entity, so skip vanilla's input-drop entirely. Only the
    // carried (cursor) item is handled, exactly like Container#onContainerClosed.
    @Inject(method = "onContainerClosed", at = @At("HEAD"), cancellable = true)
    private void easyanvils$close(EntityPlayer player, CallbackInfo ci) {
        TileEntityAnvil tile = this.easyanvils$tile();
        if (tile == null) {
            return; // not anvil-backed -> let vanilla drop its temporary inputs
        }
        tile.setChangeListener(null);
        InventoryPlayer inventory = player.inventory;
        if (!inventory.getItemStack().isEmpty()) {
            player.dropItem(inventory.getItemStack(), false);
            inventory.setItemStack(ItemStack.EMPTY);
        }
        ci.cancel();
    }
}
