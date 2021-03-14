package com.fuzs.easymagic.client.element;

import com.fuzs.easymagic.client.gui.screen.EnchantmentInventoryScreen;
import com.fuzs.easymagic.client.renderer.tileentity.EnchantmentInventoryTileEntityRenderer;
import com.fuzs.easymagic.element.EasyEnchantingElement;
import com.fuzs.puzzleslib_em.element.extension.ElementExtension;
import com.fuzs.puzzleslib_em.element.side.IClientElement;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class EasyEnchantingExtension extends ElementExtension<EasyEnchantingElement> implements IClientElement {

    public ItemRenderType renderContentsType;

    public EasyEnchantingExtension(EasyEnchantingElement parent) {

        super(parent);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void initClient() {

        ScreenManager.registerFactory(EasyEnchantingElement.ENCHANTMENT_CONTAINER, EnchantmentInventoryScreen::new);
        ClientRegistry.bindTileEntityRenderer(EasyEnchantingElement.ENCHANTING_TABLE_TILE_ENTITY, EnchantmentInventoryTileEntityRenderer::new);
    }

    @Override
    public void setupClientConfig(ForgeConfigSpec.Builder builder) {

        addToConfig(builder.comment("Render inventory contents of an enchanting table.").defineEnum("Render Enchanting Table Contents", ItemRenderType.FLOATING), v -> this.renderContentsType = v);
    }

    public enum ItemRenderType {

        NONE, FLAT, FLOATING

    }

}
