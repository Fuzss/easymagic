package fuzs.easymagic.init;

import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.world.level.block.entity.ForgeModEnchantmentTableBlockEntity;
import fuzs.easymagic.world.level.block.entity.ModEnchantmentTableBlockEntity;
import fuzs.puzzleslib.core.CoreServices;
import fuzs.puzzleslib.registry.RegistryManager;
import fuzs.puzzleslib.registry.RegistryReference;
import fuzs.puzzleslib.registry.builder.ModBlockEntityTypeBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ForgeModRegistry {
    private static final RegistryManager REGISTRY = CoreServices.FACTORIES.registration(EasyMagic.MOD_ID);
    public static final RegistryReference<BlockEntityType<ModEnchantmentTableBlockEntity>> ENCHANTING_TABLE_BLOCK_ENTITY_TYPE = REGISTRY.registerBlockEntityTypeBuilder("enchanting_table", () -> ModBlockEntityTypeBuilder.of(ForgeModEnchantmentTableBlockEntity::new, Blocks.ENCHANTING_TABLE));

    public static void touch() {

    }
}
