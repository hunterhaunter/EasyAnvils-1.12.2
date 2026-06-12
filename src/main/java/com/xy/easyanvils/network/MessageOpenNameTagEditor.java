package com.xy.easyanvils.network;

import com.xy.easyanvils.EasyAnvils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageOpenNameTagEditor implements IMessage {

    private EnumHand hand;
    private String currentName;

    public MessageOpenNameTagEditor() {
    }

    public MessageOpenNameTagEditor(EnumHand hand, String currentName) {
        this.hand = hand;
        this.currentName = currentName;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(hand.ordinal());
        ByteBufUtils.writeUTF8String(buf, currentName);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        hand = EnumHand.values()[buf.readInt()];
        currentName = ByteBufUtils.readUTF8String(buf);
    }

    public static class Handler implements IMessageHandler<MessageOpenNameTagEditor, IMessage> {

        @Override
        public IMessage onMessage(MessageOpenNameTagEditor message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() ->
                EasyAnvils.proxy.openNameTagEditor(message.hand, message.currentName));
            return null;
        }
    }
}
