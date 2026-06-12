package com.xy.easyanvils.network;

import com.xy.easyanvils.EasyAnvils;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {

    public static SimpleNetworkWrapper INSTANCE;

    public static void init() {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(EasyAnvils.MODID);
        INSTANCE.registerMessage(
            MessageNameTagUpdate.Handler.class,
            MessageNameTagUpdate.class,
            0,
            Side.SERVER
        );
        INSTANCE.registerMessage(
            MessageOpenNameTagEditor.Handler.class,
            MessageOpenNameTagEditor.class,
            1,
            Side.CLIENT
        );
    }
}
