package com.xy.easyanvils;

import com.xy.easyanvils.config.EasyAnvilsConfig;
import com.xy.easyanvils.handler.AnvilBreakHandler;
import com.xy.easyanvils.handler.AnvilCostHandler;
import com.xy.easyanvils.handler.AnvilRepairInteractionHandler;
import com.xy.easyanvils.handler.CompatWarningHandler;
import com.xy.easyanvils.handler.NameTagHandler;
import com.xy.easyanvils.init.ModRegistry;
import com.xy.easyanvils.network.PacketHandler;
import com.xy.easyanvils.proxy.CommonProxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = EasyAnvils.MODID, name = EasyAnvils.NAME, version = EasyAnvils.VERSION,
     guiFactory = "com.xy.easyanvils.client.EasyAnvilsGuiFactory")
public class EasyAnvils {

    public static final String MODID = "easyanvils";
    public static final String NAME = "Easy Anvils";
    public static final String VERSION = "1.1.0";

    @Mod.Instance(MODID)
    public static EasyAnvils instance;

    @SidedProxy(
        clientSide = "com.xy.easyanvils.proxy.ClientProxy",
        serverSide = "com.xy.easyanvils.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        EasyAnvilsConfig.init(event.getSuggestedConfigurationFile());
        PacketHandler.init();
        ModRegistry.registerTileEntities();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // All non-structural features are pure Forge events => maximum mod compatibility.
        MinecraftForge.EVENT_BUS.register(new AnvilCostHandler());
        MinecraftForge.EVENT_BUS.register(new AnvilBreakHandler());
        MinecraftForge.EVENT_BUS.register(new AnvilRepairInteractionHandler());
        MinecraftForge.EVENT_BUS.register(new NameTagHandler());
        MinecraftForge.EVENT_BUS.register(new CompatWarningHandler());
        ModRegistry.registerDispenserBehaviour();
        proxy.init(event);
    }
}
