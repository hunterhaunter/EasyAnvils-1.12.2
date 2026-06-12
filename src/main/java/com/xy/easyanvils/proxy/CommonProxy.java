package com.xy.easyanvils.proxy;

import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
    }

    public void init(FMLInitializationEvent event) {
    }

    /** Opens the no-anvil name tag editor screen. Client-only; no-op on the dedicated server. */
    public void openNameTagEditor(EnumHand hand, String currentName) {
    }
}
