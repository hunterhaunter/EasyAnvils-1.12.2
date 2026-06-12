package com.xy.easyanvils.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

/**
 * Persistent 2-slot inventory attached to vanilla anvils so input items "stay" when the
 * GUI is closed. Mirrors the original AnvilBlockEntity (a BaseContainerBlockEntity +
 * WorldlyContainer holding two stacks). Automation can insert from any side but not the
 * bottom, and cannot extract (matches the original WorldlyContainer rules).
 */
public class TileEntityAnvil extends TileEntity implements ISidedInventory {

    private static final int[] SLOTS_TOP_SIDES = new int[]{0, 1};
    private static final int[] SLOTS_BOTTOM = new int[]{};

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);

    /**
     * Set by an open {@code ContainerRepair} that is backing its input slots directly with this tile
     * entity. Lets external changes (e.g. a hopper inserting while the GUI is open) refresh the
     * anvil result. Cleared when the GUI closes. Not persisted.
     */
    private Runnable changeListener;

    public void setChangeListener(Runnable listener) {
        this.changeListener = listener;
    }

    // ---- NBT ----
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.inventory.clear();
        ItemStackHelper.loadAllItems(compound, this.inventory);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        ItemStackHelper.saveAllItems(compound, this.inventory, true);
        return compound;
    }

    // ---- client sync (for the contents renderer) ----
    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 1, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (this.world != null) {
            net.minecraft.block.state.IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
        if (this.changeListener != null) {
            this.changeListener.run();
        }
    }

    // ---- IInventory ----
    @Override
    public int getSizeInventory() {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.inventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return this.inventory.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = ItemStackHelper.getAndSplit(this.inventory, index, count);
        if (!stack.isEmpty()) {
            this.markDirty();
        }
        return stack;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = ItemStackHelper.getAndRemove(this.inventory, index);
        if (!stack.isEmpty()) {
            this.markDirty();
        }
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.inventory.set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }
        this.markDirty();
    }

    /** Sets a slot without firing a block update; the caller batches a single {@link #markDirty()}. */
    public void setStackInSlotQuiet(int index, ItemStack stack) {
        this.inventory.set(index, stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        if (this.world.getTileEntity(this.pos) != this) {
            return false;
        }
        return player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        this.inventory.clear();
    }

    // ---- names (vanilla repair container title) ----
    @Override
    public String getName() {
        return "container.repair";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public net.minecraft.util.text.ITextComponent getDisplayName() {
        return new net.minecraft.util.text.TextComponentTranslation(this.getName());
    }

    // ---- ISidedInventory ----
    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return side == EnumFacing.DOWN ? SLOTS_BOTTOM : SLOTS_TOP_SIDES;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return this.isItemValidForSlot(index, itemStackIn);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return false;
    }
}
