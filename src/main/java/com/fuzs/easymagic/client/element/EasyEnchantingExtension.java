package com.fuzs.easymagic.client.element;

import com.fuzs.easymagic.EasyMagic;
import com.fuzs.easymagic.client.gui.screen.EnchantmentInventoryScreen;
import com.fuzs.easymagic.client.renderer.tileentity.EnchantmentInventoryTileEntityRenderer;
import com.fuzs.easymagic.element.EasyEnchantingElement;
import com.fuzs.puzzleslib_em.element.extension.ElementExtension;
import com.fuzs.puzzleslib_em.element.side.IClientElement;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class EasyEnchantingExtension extends ElementExtension<EasyEnchantingElement> implements IClientElement {

    public static final ResourceLocation QUESTION_MARK_LOCATION = new ResourceLocation(EasyMagic.MODID, "item/question_mark");

    public ItemRenderType renderContentsType;

    public EasyEnchantingExtension(EasyEnchantingElement parent) {

        super(parent);
    }

    @Override
    public void setupClient() {

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onTextureStitch);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void initClient() {

        ScreenManager.registerFactory(EasyEnchantingElement.ENCHANTMENT_CONTAINER, EnchantmentInventoryScreen::new);
        ClientRegistry.bindTileEntityRenderer(EasyEnchantingElement.ENCHANTING_TABLE_TILE_ENTITY, EnchantmentInventoryTileEntityRenderer::new);
    }

    @Override
    public void setupClientConfig(ForgeConfigSpec.Builder builder) {

        addToConfig(builder.comment("Render inventory contents of an enchanting table.").defineEnum("Render Enchanting Table Contents", ItemRenderType.FANCY_FLOATING), v -> this.renderContentsType = v);
    }

    private void onTextureStitch(final TextureStitchEvent.Pre evt) {

        if (evt.getMap().getTextureLocation().equals(PlayerContainer.LOCATION_BLOCKS_TEXTURE)) {

            evt.addSprite(QUESTION_MARK_LOCATION);
        }
    }

    @SuppressWarnings("unused")
    public enum ItemRenderType {

        NONE, FLAT, FLOATING, FANCY_FLOATING

    }

}
