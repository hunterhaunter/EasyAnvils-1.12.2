package com.xy.easyanvils.config;

import com.xy.easyanvils.EasyAnvils;

import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Re-syncs the static config fields whenever the in-game config screen saves changes. */
@Mod.EventBusSubscriber(modid = EasyAnvils.MODID)
public class ConfigEventHandler {

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (EasyAnvils.MODID.equals(event.getModID())) {
            EasyAnvilsConfig.syncConfig();
        }
    }
}
