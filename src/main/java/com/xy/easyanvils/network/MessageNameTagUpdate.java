package com.xy.easyanvils.network;

import com.xy.easyanvils.util.FormattingHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageNameTagUpdate implements IMessage {

    private EnumHand hand;
    private String name;

    public MessageNameTagUpdate() {
    }

    public MessageNameTagUpdate(EnumHand hand, String name) {
        this.hand = hand;
        this.name = name;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(hand.ordinal());
        ByteBufUtils.writeUTF8String(buf, name);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        hand = EnumHand.values()[buf.readInt()];
        name = ByteBufUtils.readUTF8String(buf);
    }

    public static class Handler implements IMessageHandler<MessageNameTagUpdate, IMessage> {

        @Override
        public IMessage onMessage(MessageNameTagUpdate message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                ItemStack stack = player.getHeldItem(message.hand);
                String s = FormattingHelper.filterText(message.name);
                if (stack.getItem() == Items.NAME_TAG && s.length() <= 50) {
                    if (s.isEmpty()) {
                        stack.clearCustomName();
                    } else {
                        stack.setStackDisplayName(s);
                    }
                }
            });
            return null;
        }
    }
}
