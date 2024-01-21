package fuzs.easymagic.forge.init;

import fuzs.easymagic.forge.world.level.block.entity.ForgeEnchantmentTableWithInventoryBlockEntity;
import fuzs.easymagic.init.ModRegistry;
import fuzs.easymagic.world.level.block.entity.EnchantmentTableWithInventoryBlockEntity;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ForgeModRegistry extends ModRegistry {
    public static final Holder.Reference<BlockEntityType<EnchantmentTableWithInventoryBlockEntity>> ENCHANTING_TABLE_BLOCK_ENTITY_TYPE = REGISTRY.registerBlockEntityType("enchanting_table", () -> BlockEntityType.Builder.of(ForgeEnchantmentTableWithInventoryBlockEntity::new, ENCHANTMENT_TABLE_BLOCK.value()));


    public static void touch() {

    }
}
