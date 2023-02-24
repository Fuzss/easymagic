package fuzs.easymagic.init;

import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.core.CommonAbstractions;
import fuzs.easymagic.world.inventory.ModEnchantmentMenu;
import fuzs.easymagic.world.level.block.entity.ModEnchantmentTableBlockEntity;
import fuzs.puzzleslib.core.CommonFactories;
import fuzs.puzzleslib.init.RegistryManager;
import fuzs.puzzleslib.init.RegistryReference;
import fuzs.puzzleslib.init.builder.ModBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModRegistry {
    private static final RegistryManager REGISTRY = CommonFactories.INSTANCE.registration(EasyMagic.MOD_ID);
    public static final RegistryReference<BlockEntityType<ModEnchantmentTableBlockEntity>> ENCHANTING_TABLE_BLOCK_ENTITY_TYPE = REGISTRY.registerBlockEntityTypeBuilder("enchanting_table", () -> ModBlockEntityTypeBuilder.of(CommonAbstractions.INSTANCE::createModEnchantmentTableBlockEntity, Blocks.ENCHANTING_TABLE));
    public static final RegistryReference<MenuType<ModEnchantmentMenu>> ENCHANTMENT_MENU_TYPE = REGISTRY.registerMenuTypeSupplier("enchanting", () -> ModEnchantmentMenu::new);

    public static final TagKey<Item> ENCHANTING_CATALYSTS_ITEM_TAG = TagKey.create(Registry.ITEM_REGISTRY, EasyMagic.id("enchanting_catalysts"));
    public static final TagKey<Item> REROLL_CATALYSTS_ITEM_TAG = TagKey.create(Registry.ITEM_REGISTRY, EasyMagic.id("reroll_catalysts"));

    public static void touch() {

    }
}
