package fuzs.easymagic.core;

import fuzs.easymagic.world.level.block.entity.ModEnchantmentTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface CommonAbstractions {

    ModEnchantmentTableBlockEntity createModEnchantmentTableBlockEntity(BlockPos worldPosition, BlockState state);

    float getEnchantPowerBonus(BlockState state, Level level, BlockPos pos);

    boolean isStackEnchantingFuel(ItemStack stack);
}
