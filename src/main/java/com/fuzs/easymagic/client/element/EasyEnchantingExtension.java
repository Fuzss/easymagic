package com.fuzs.easymagic.client.element;

import com.fuzs.puzzleslib_em.element.extension.ElementExtension;
import com.fuzs.puzzleslib_em.element.side.IClientElement;
import com.fuzs.easymagic.client.renderer.tileentity.EnchantmentInventoryTileEntityRenderer;
import com.fuzs.easymagic.element.EasyEnchantingElement;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class EasyEnchantingExtension extends ElementExtension<EasyEnchantingElement> implements IClientElement {

    public boolean allEnchantments;
    public boolean renderContents;

    public EasyEnchantingExtension(EasyEnchantingElement parent) {

        super(parent);
    }

    @Override
    public void initClient() {

        ClientRegistry.bindTileEntityRenderer(EasyEnchantingElement.ENCHANTING_TABLE_TILE_ENTITY, EnchantmentInventoryTileEntityRenderer::new);
    }

    @Override
    public void setupClientConfig(ForgeConfigSpec.Builder builder) {

        addToConfig(builder.comment("When hovering over an enchanting option show the complete outcome on the tooltip instead of only a single enchantment.").define("Show All Enchantments", false), v -> this.allEnchantments = v);
        addToConfig(builder.comment("Render item contents of an enchantment table lying on top of the block.").define("Render Table Contents", true), v -> this.renderContents = v);
    }

}
