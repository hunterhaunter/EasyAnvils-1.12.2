package com.xy.easyanvils.handler;

import com.xy.easyanvils.mixin.EasyAnvilsMixinPlugin;

import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

/**
 * Notifies a joining player in chat (like Falling Leaves' login notice) when another anvil mod has
 * disabled Easy Anvils' structural mixins. In that state items no longer "stay in the anvil" and the
 * shift-click XP protection is off, so this surfaces the degraded behaviour instead of failing
 * silently. Only posts when a conflict is actually detected; a clean install stays quiet.
 */
public class CompatWarningHandler {

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!EasyAnvilsMixinPlugin.isAnvilModConflict()) {
            return;
        }
        event.player.sendMessage(new TextComponentString(
                TextFormatting.GOLD + "[Easy Anvils] " + TextFormatting.YELLOW
                        + "Another anvil mod was detected. \"Items stay in the anvil\" and the "
                        + "shift-click XP protection are disabled to avoid conflicts — cost, "
                        + "break, repair and rename features still work."));
    }
}
