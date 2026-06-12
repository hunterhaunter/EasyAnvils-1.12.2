package com.xy.easyanvils.proxy;

import com.xy.easyanvils.client.AnvilTileEntityRenderer;
import com.xy.easyanvils.client.GuiNameTagEdit;
import com.xy.easyanvils.tileentity.TileEntityAnvil;

import com.xy.easyanvils.client.NameTagTooltipHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAnvil.class, new AnvilTileEntityRenderer());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        MinecraftForge.EVENT_BUS.register(new NameTagTooltipHandler());
    }

    @Override
    public void openNameTagEditor(EnumHand hand, String currentName) {
        Minecraft.getMinecraft().displayGuiScreen(new GuiNameTagEdit(hand, currentName));
    }
}
