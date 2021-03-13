package com.fuzs.easymagic.element;

import com.fuzs.easymagic.EasyMagic;
import com.fuzs.easymagic.client.element.EasyEnchantingExtension;
import com.fuzs.easymagic.network.message.SEnchantingInfoMessage;
import com.fuzs.easymagic.tileentity.EnchantingTableInventoryTileEntity;
import com.fuzs.puzzleslib_em.PuzzlesLib;
import com.fuzs.puzzleslib_em.element.extension.ClientExtensibleElement;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ObjectHolder;

public class EasyEnchantingElement extends ClientExtensibleElement<EasyEnchantingExtension> {

    @ObjectHolder(EasyMagic.MODID + ":" + "enchanting_table")
    public static final TileEntityType<EnchantingTableInventoryTileEntity> ENCHANTING_TABLE_TILE_ENTITY = null;

    public boolean itemsStay;
    public boolean rerollEnchantments;

    public EasyEnchantingElement() {

        super(element -> new EasyEnchantingExtension((EasyEnchantingElement) element));
    }

    @Override
    public String getDescription() {

        return "Small improvements to make working an enchantment table more fun.";
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setupCommon() {

        PuzzlesLib.getRegistryManager().register("enchanting_table", TileEntityType.Builder.create(EnchantingTableInventoryTileEntity::new, Blocks.ENCHANTING_TABLE).build(null));
    }

    @Override
    public void initCommon() {

        PuzzlesLib.getNetworkHandler().registerMessage(SEnchantingInfoMessage::new, LogicalSide.CLIENT);
    }

    @Override
    public void setupCommonConfig(ForgeConfigSpec.Builder builder) {

        addToConfig(builder.comment("Inventory contents stay in their slot after closing the enchanting screen. Also makes hoppers able to input and output items.").define("Inventory Contents Stay", true), v -> this.itemsStay = v);
        addToConfig(builder.comment("Re-roll possible enchantments in an enchanting table every time an item is placed into the enchanting slot.").define("Re-Roll Enchantments", true), v -> this.rerollEnchantments = v);
    }

}
