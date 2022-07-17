package fuzs.easymagic.init;

import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.world.inventory.ModEnchantmentMenu;
import fuzs.easymagic.world.level.block.entity.ModEnchantmentTableBlockEntity;
import fuzs.puzzleslib.core.CoreServices;
import fuzs.puzzleslib.registry.RegistryManager;
import fuzs.puzzleslib.registry.RegistryReference;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModRegistry {
    private static final RegistryManager REGISTRY = CoreServices.FACTORIES.registration(EasyMagic.MOD_ID);
    public static final RegistryReference<BlockEntityType<ModEnchantmentTableBlockEntity>> ENCHANTING_TABLE_BLOCK_ENTITY_TYPE = RegistryReference.placeholder(Registry.BLOCK_ENTITY_TYPE_REGISTRY, EasyMagic.MOD_ID, "enchanting_table");
    public static final RegistryReference<MenuType<ModEnchantmentMenu>> ENCHANTMENT_MENU_TYPE = REGISTRY.registerMenuTypeSupplier("enchanting", () -> ModEnchantmentMenu::new);

    public static void touch() {

    }
}
