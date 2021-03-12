package com.fuzs.easymagic.element;

import com.fuzs.puzzleslib_em.PuzzlesLib;
import com.fuzs.puzzleslib_em.element.extension.ClientExtensibleElement;
import com.fuzs.easymagic.EasyMagic;
import com.fuzs.easymagic.client.element.EasyEnchantingExtension;
import com.fuzs.easymagic.tileentity.EnchantingTableInventoryTileEntity;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Rarity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.ForgeConfigSpec;
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
    public void setupCommonConfig(ForgeConfigSpec.Builder builder) {

        addToConfig(builder.comment("Inventory contents stay in their slot after closing the enchanting screen. Also makes hoppers able to input and output items.").define("Contents Stay", true), v -> this.itemsStay = v);
        addToConfig(builder.comment("Reroll possible enchantments in an enchanting table every time an item is placed into the enchanting slot.").define("Reroll Enchantments", true), v -> this.rerollEnchantments = v);
    }

    public static Rarity getFutureRarity(ItemStack stack) {

        if (stack.getItem() == Items.BOOK) {

            return Rarity.UNCOMMON;
        } else {

            switch (stack.getRarity()) {

                case COMMON:
                case UNCOMMON:

                    return Rarity.RARE;
                case RARE:

                    return Rarity.EPIC;
                case EPIC:
                default:

                    return stack.getRarity();
            }
        }
    }

}
