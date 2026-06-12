package com.xy.easyanvils.init;

import com.xy.easyanvils.EasyAnvils;
import com.xy.easyanvils.config.EasyAnvilsConfig;
import com.xy.easyanvils.handler.AnvilRepairLogic;
import com.xy.easyanvils.tileentity.TileEntityAnvil;

import net.minecraft.block.BlockDispenser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class ModRegistry {

    private ModRegistry() {
    }

    public static void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityAnvil.class, new ResourceLocation(EasyAnvils.MODID, "anvil"));
    }

    /**
     * Iron blocks repair the anvil in front of a dispenser (original "Can be automated using
     * dispensers"). Wraps the previous behaviour so non-anvil dispenses still work.
     */
    public static void registerDispenserBehaviour() {
        final Item ironBlock = Item.getItemFromBlock(Blocks.IRON_BLOCK);
        final IBehaviorDispenseItem fallback = BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(ironBlock);
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(ironBlock, new BehaviorDefaultDispenseItem() {
            @Override
            protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
                if (!EasyAnvilsConfig.anvilRepairing) {
                    return dispenseFallback(source, stack);
                }
                World world = source.getWorld();
                EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);
                BlockPos pos = source.getBlockPos().offset(facing);
                IBlockState state = world.getBlockState(pos);
                if (AnvilRepairLogic.getRepairedState(state) != null) {
                    if (AnvilRepairLogic.tryRepairAnvil(world, pos, state)) {
                        stack.shrink(1);
                    }
                    return stack;
                }
                return dispenseFallback(source, stack);
            }

            private ItemStack dispenseFallback(IBlockSource source, ItemStack stack) {
                if (fallback != null && fallback != this) {
                    return fallback.dispense(source, stack);
                }
                return super.dispenseStack(source, stack);
            }
        });
    }
}
