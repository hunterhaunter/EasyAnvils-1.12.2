package com.xy.easyanvils.handler;

import com.xy.easyanvils.config.EasyAnvilsConfig;
import com.xy.easyanvils.network.MessageOpenNameTagEditor;
import com.xy.easyanvils.network.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NameTagHandler {

    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!EasyAnvilsConfig.editNameTagsNoAnvil) return;
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = event.getItemStack();
        if (!player.isSneaking() || stack.getItem() != Items.NAME_TAG) return;
        if (!event.getWorld().isRemote && player instanceof EntityPlayerMP) {
            String current = stack.hasDisplayName() ? stack.getDisplayName() : "";
            PacketHandler.INSTANCE.sendTo(
                new MessageOpenNameTagEditor(event.getHand(), current),
                (EntityPlayerMP) player
            );
        }
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
    }
}
