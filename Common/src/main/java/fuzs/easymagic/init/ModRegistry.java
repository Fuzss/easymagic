package fuzs.easymagic.init;

import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.core.ModCoreServices;
import fuzs.easymagic.world.inventory.ModEnchantmentMenu;
import fuzs.easymagic.world.level.block.entity.ModEnchantmentTableBlockEntity;
import fuzs.puzzleslib.core.CoreServices;
import fuzs.puzzleslib.registry.RegistryManager;
import fuzs.puzzleslib.registry.RegistryReference;
import fuzs.puzzleslib.registry.builder.ModBlockEntityTypeBuilder;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModRegistry {
    private static final RegistryManager REGISTRY = CoreServices.FACTORIES.registration(EasyMagic.MOD_ID);
    public static final RegistryReference<BlockEntityType<ModEnchantmentTableBlockEntity>> ENCHANTING_TABLE_BLOCK_ENTITY_TYPE = REGISTRY.registerBlockEntityTypeBuilder("enchanting_table", () -> ModBlockEntityTypeBuilder.of(ModCoreServices.ABSTRACTIONS::createModEnchantmentTableBlockEntity, Blocks.ENCHANTING_TABLE));
    public static final RegistryReference<MenuType<ModEnchantmentMenu>> ENCHANTMENT_MENU_TYPE = REGISTRY.registerMenuTypeSupplier("enchanting", () -> ModEnchantmentMenu::new);

    public static void touch() {

    }
}
