package com.xy.easyanvils.client;

import com.xy.easyanvils.config.EasyAnvilsConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Adds a hint line to name tag tooltips explaining the no-anvil rename (sneak + use). Port of the
 * original NameTagTooltipHandler; only shown while {@code editNameTagsNoAnvil} is enabled.
 */
public class NameTagTooltipHandler {

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (!EasyAnvilsConfig.editNameTagsNoAnvil) {
            return;
        }
        if (event.getItemStack().getItem() != Items.NAME_TAG) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        String sneak = mc.gameSettings.keyBindSneak.getDisplayName();
        String use = mc.gameSettings.keyBindUseItem.getDisplayName();
        event.getToolTip().add(TextFormatting.GRAY
                + I18n.format("easyanvils.item.name_tag.description", sneak, use));
    }
}
