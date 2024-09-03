package fuzs.easymagic.init;

import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.world.inventory.ModEnchantmentMenu;
import fuzs.easymagic.world.level.block.entity.EnchantmentTableWithInventoryBlockEntity;
import fuzs.puzzleslib.api.core.v1.ModLoader;
import fuzs.puzzleslib.api.init.v3.registry.RegistryManager;
import fuzs.puzzleslib.api.init.v3.tags.BoundTagFactory;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModRegistry {
    static final RegistryManager REGISTRIES = RegistryManager.from(EasyMagic.MOD_ID);
    public static final Holder.Reference<BlockEntityType<EnchantmentTableWithInventoryBlockEntity>> ENCHANTING_TABLE_BLOCK_ENTITY_TYPE = REGISTRIES.whenNotOn(
                    ModLoader.FORGE)
            .registerBlockEntityType("enchanting_table",
                    () -> BlockEntityType.Builder.of(EnchantmentTableWithInventoryBlockEntity::new)
            );
    public static final Holder.Reference<MenuType<ModEnchantmentMenu>> ENCHANTMENT_MENU_TYPE = REGISTRIES.registerMenuType(
            "enchanting", () -> ModEnchantmentMenu::new);

    static final BoundTagFactory TAGS = BoundTagFactory.make(EasyMagic.MOD_ID);
    public static final TagKey<Item> ENCHANTING_CATALYSTS_ITEM_TAG = TAGS.registerItemTag("enchanting_catalysts");
    public static final TagKey<Item> REROLL_CATALYSTS_ITEM_TAG = TAGS.registerItemTag("reroll_catalysts");
    public static final TagKey<Block> UNALTERED_ENCHANTING_TABLES_BLOCK_TAG = TAGS.registerBlockTag(
            "unaltered_enchanting_tables");

    public static void touch() {
        // NO-OP
    }
}
