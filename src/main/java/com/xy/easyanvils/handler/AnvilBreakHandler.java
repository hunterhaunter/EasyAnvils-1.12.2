package com.xy.easyanvils.handler;

import com.xy.easyanvils.config.EasyAnvilsConfig;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AnvilBreakHandler {

    @SubscribeEvent
    public void onAnvilRepair(AnvilRepairEvent event) {
        if (!EasyAnvilsConfig.anvilsCanBreak) {
            event.setBreakChance(0.0F);
            return;
        }
        ItemStack right = event.getIngredientInput();
        if (EasyAnvilsConfig.riskFreeAnvilRenaming && right.isEmpty()) {
            event.setBreakChance(0.0F);
        } else {
            event.setBreakChance((float) EasyAnvilsConfig.anvilBreakChance);
        }
    }
}
