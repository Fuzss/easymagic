package fuzs.easymagic.core;

import fuzs.easymagic.world.level.block.entity.ModEnchantmentTableBlockEntity;
import fuzs.puzzleslib.api.core.v1.ServiceProviderHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface CommonAbstractions {
    CommonAbstractions INSTANCE = ServiceProviderHelper.load(CommonAbstractions.class);

    ModEnchantmentTableBlockEntity createModEnchantmentTableBlockEntity(BlockPos worldPosition, BlockState state);

    float getEnchantPowerBonus(BlockState state, Level level, BlockPos pos);
}
