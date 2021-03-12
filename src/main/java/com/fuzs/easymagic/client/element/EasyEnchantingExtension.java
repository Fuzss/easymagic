package com.fuzs.easymagic.client.element;

import com.fuzs.puzzleslib_em.element.extension.ElementExtension;
import com.fuzs.puzzleslib_em.element.side.IClientElement;
import com.fuzs.easymagic.client.renderer.tileentity.EnchantmentInventoryTileEntityRenderer;
import com.fuzs.easymagic.element.EasyEnchantingElement;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class EasyEnchantingExtension extends ElementExtension<EasyEnchantingElement> implements IClientElement {

    public boolean allEnchantments;
    public boolean displayContents;

    public EasyEnchantingExtension(EasyEnchantingElement parent) {

        super(parent);
    }

    @Override
    public void loadClient() {

        ClientRegistry.bindTileEntityRenderer(EasyEnchantingElement.ENCHANTING_TABLE_TILE_ENTITY, EnchantmentInventoryTileEntityRenderer::new);
    }

    @Override
    public void setupClientConfig(ForgeConfigSpec.Builder builder) {

        addToConfig(builder.comment("When hovering over an enchanting option show the complete outcome on the tooltip.").define("Show All Enchantments", false), v -> this.allEnchantments = v);
        addToConfig(builder.comment("Show contents of an enchantment table lying on top of the block.").define("Display Contents", true), v -> this.displayContents = v);
    }

}
