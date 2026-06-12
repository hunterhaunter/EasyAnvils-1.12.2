package com.xy.easyanvils.mixin;

import com.xy.easyanvils.tileentity.TileEntityAnvil;

import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

/**
 * Gives vanilla anvils a {@link TileEntityAnvil} so input items persist when the GUI closes
 * ("items stay in the anvil"). Purely additive: it only adds the Forge Block tile-entity hooks
 * and a comparator output; it does not change any vanilla method body. Gated off by
 * {@link EasyAnvilsMixinPlugin} when another mod replaces the anvil container.
 */
@Mixin(net.minecraft.block.BlockAnvil.class)
public abstract class MixinBlockAnvil extends BlockFalling {

    protected MixinBlockAnvil() {
        super(Material.ANVIL);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityAnvil();
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileEntityAnvil) {
            InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory) tileEntity);
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World worldIn, BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileEntityAnvil) {
            return Container.calcRedstoneFromInventory((IInventory) tileEntity);
        }
        return 0;
    }
}
