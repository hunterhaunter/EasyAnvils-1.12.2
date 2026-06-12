package com.xy.easyanvils.handler;

import com.xy.easyanvils.config.EasyAnvilsConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AnvilRepairInteractionHandler {

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!EasyAnvilsConfig.anvilRepairing) return;
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = event.getItemStack();
        if (stack.getItem() != Item.getItemFromBlock(Blocks.IRON_BLOCK)) return;
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        IBlockState state = world.getBlockState(pos);
        if (AnvilRepairLogic.getRepairedState(state) == null) return;
        if (AnvilRepairLogic.tryRepairAnvil(world, pos, state)) {
            if (!player.capabilities.isCreativeMode) stack.shrink(1);
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.SUCCESS);
        }
    }
}
