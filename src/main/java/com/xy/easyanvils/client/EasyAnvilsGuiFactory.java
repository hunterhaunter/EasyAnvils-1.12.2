package com.xy.easyanvils.client;

import com.xy.easyanvils.EasyAnvils;
import com.xy.easyanvils.config.EasyAnvilsConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Provides the in-game "Config" button on the Mods list for Easy Anvils, building an editable
 * screen from the mod's config categories.
 */
public class EasyAnvilsGuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft minecraftInstance) {
    }

    @Override
    public boolean hasConfigGui() {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        List<IConfigElement> elements = new ArrayList<>();
        for (String category : EasyAnvilsConfig.getConfig().getCategoryNames()) {
            elements.add(new ConfigElement(EasyAnvilsConfig.getConfig().getCategory(category)));
        }
        return new GuiConfig(parentScreen, elements, EasyAnvils.MODID, false, false, "Easy Anvils");
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }
}
