package com.fuzs.easymagic.element;

import com.fuzs.easymagic.EasyMagic;
import com.fuzs.easymagic.client.element.EasyEnchantingExtension;
import com.fuzs.easymagic.inventory.container.EnchantmentInventoryContainer;
import com.fuzs.easymagic.network.message.SEnchantingInfoMessage;
import com.fuzs.easymagic.tileentity.EnchantingTableInventoryTileEntity;
import com.fuzs.puzzleslib_em.PuzzlesLib;
import com.fuzs.puzzleslib_em.element.extension.ClientExtensibleElement;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ObjectHolder;

public class EasyEnchantingElement extends ClientExtensibleElement<EasyEnchantingExtension> {

    @ObjectHolder(EasyMagic.MODID + ":" + "enchanting_table")
    public static final TileEntityType<EnchantingTableInventoryTileEntity> ENCHANTING_TABLE_TILE_ENTITY = null;

    @ObjectHolder(EasyMagic.MODID + ":" + "enchanting")
    public static final ContainerType<EnchantmentInventoryContainer> ENCHANTMENT_CONTAINER = null;

    public boolean itemsStay;
    public boolean reRollEnchantments;
    public boolean allEnchantments;
    public int maxPower;
    public boolean lenientBookshelves;

    public EasyEnchantingElement() {

        super(element -> new EasyEnchantingExtension((EasyEnchantingElement) element));
    }

    @Override
    public String getDescription() {

        return "Small improvements to make working an enchantment table more convenient.";
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setupCommon() {

        PuzzlesLib.getRegistryManager().register("enchanting_table", TileEntityType.Builder.create(EnchantingTableInventoryTileEntity::new, Blocks.ENCHANTING_TABLE).build(null));
        PuzzlesLib.getRegistryManager().register("enchanting", new ContainerType<>(EnchantmentInventoryContainer::new));
        this.addListener(this::onContainerOpen);
    }

    @Override
    public void initCommon() {

        PuzzlesLib.getNetworkHandler().registerMessage(SEnchantingInfoMessage::new, LogicalSide.CLIENT);
    }

    @Override
    public void setupCommonConfig(ForgeConfigSpec.Builder builder) {

        addToConfig(builder.comment("Inventory contents stay in their slot after closing the enchanting screen. Also makes hoppers able to input and output items.").define("Inventory Contents Stay", true), v -> this.itemsStay = v);
        addToConfig(builder.comment("Re-roll possible enchantments in an enchanting table every time an item is placed into the enchanting slot.").define("Re-Roll Enchantments", true), v -> this.reRollEnchantments = v);
        addToConfig(builder.comment("When hovering over an enchanting option show the complete outcome on the tooltip instead of only a single enchantment.").define("Show All Enchantments", false), v -> this.allEnchantments = v);
        addToConfig(builder.comment("Amount of bookshelves required to perform enchantments at the highest level.").defineInRange("Enchanting Power", 15, 0, Integer.MAX_VALUE), v -> this.maxPower = v);
        addToConfig(builder.comment("Blocks without a collision shape do not block bookshelves placed behind from counting towards current enchanting power.").define("Lenient Bookshelves", true), v -> this.lenientBookshelves = v);
    }

    private void onContainerOpen(final PlayerContainerEvent.Open evt) {

        if (evt.getContainer() instanceof EnchantmentInventoryContainer) {

            // items might still be in inventory slots, so this needs to update so that enchantment buttons are shown
            ((EnchantmentInventoryContainer) evt.getContainer()).onCraftMatrixChanged();
        }
    }

}
