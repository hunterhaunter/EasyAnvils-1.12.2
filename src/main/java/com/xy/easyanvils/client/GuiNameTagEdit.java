package com.xy.easyanvils.client;

import com.xy.easyanvils.config.EasyAnvilsConfig;
import com.xy.easyanvils.network.MessageNameTagUpdate;
import com.xy.easyanvils.network.PacketHandler;
import com.xy.easyanvils.util.FormattingHelper;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

/**
 * No-anvil name tag editor (sneak + right-click a name tag). 1.12.2 GuiScreen port of the
 * original NameTagEditScreen: a 176x48 panel with a name field (max 50 chars, optional
 * formatting codes) and a Done button that sends the new name to the server.
 */
public class GuiNameTagEdit extends GuiScreen {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("easyanvils", "textures/gui/edit_name_tag.png");
    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 48;

    private final EnumHand hand;
    private int leftPos;
    private int topPos;
    private String itemName;
    private GuiTextField nameField;

    public GuiNameTagEdit(EnumHand hand, String currentName) {
        this.hand = hand;
        this.itemName = currentName == null ? "" : currentName;
    }

    @Override
    public void initGui() {
        this.leftPos = (this.width - IMAGE_WIDTH) / 2;
        this.topPos = this.height / 4;

        this.buttonList.clear();
        this.addButton(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120, 200, 20,
                I18n.format("gui.done")));

        this.nameField = new GuiTextField(0, this.fontRenderer, this.leftPos + 62, this.topPos + 26, 103, 12);
        this.nameField.setEnableBackgroundDrawing(false);
        this.nameField.setTextColor(-1);
        this.nameField.setDisabledTextColour(-1);
        this.nameField.setMaxStringLength(50);
        this.nameField.setText(this.itemName);
        this.nameField.setFocused(true);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            sendAndClose();
        }
    }

    private void sendAndClose() {
        String name = this.itemName;
        if (EasyAnvilsConfig.renamingSupportsFormatting) {
            name = FormattingHelper.toFormattingCodes(name);
        }
        PacketHandler.INSTANCE.sendToServer(new MessageNameTagUpdate(this.hand, name));
        this.mc.displayGuiScreen(null);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { // ESC
            this.mc.displayGuiScreen(null);
            return;
        }
        if (keyCode == 28 || keyCode == 156) { // enter
            sendAndClose();
            return;
        }
        if (this.nameField.textboxKeyTyped(typedChar, keyCode)) {
            this.itemName = this.nameField.getText();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        this.nameField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        this.drawTexturedModalRect(this.leftPos, this.topPos, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        String title = I18n.format("easyanvils.name_tag.edit", new ItemStack(Items.NAME_TAG).getDisplayName());
        this.fontRenderer.drawString(title, this.leftPos + 60, this.topPos + 8, 0x404040);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // scaled name tag icon
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.leftPos + 17, this.topPos + 8, 0.0F);
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        RenderHelper.enableGUIStandardItemLighting();
        this.mc.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Items.NAME_TAG), 0, 0);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        this.nameField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
