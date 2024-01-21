package fuzs.easymagic.init;

import fuzs.easymagic.world.level.block.entity.ForgeModEnchantmentTableBlockEntity;
import fuzs.easymagic.world.level.block.entity.ModEnchantmentTableBlockEntity;
import fuzs.puzzleslib.api.init.v2.RegistryReference;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

import static fuzs.easymagic.init.ModRegistry.REGISTRY;

public class ForgeModRegistry {
    public static final RegistryReference<BlockEntityType<ModEnchantmentTableBlockEntity>> ENCHANTING_TABLE_BLOCK_ENTITY_TYPE = REGISTRY.registerBlockEntityType("enchanting_table", () -> BlockEntityType.Builder.of(ForgeModEnchantmentTableBlockEntity::new, Blocks.ENCHANTING_TABLE));


    public static void touch() {

    }
}
