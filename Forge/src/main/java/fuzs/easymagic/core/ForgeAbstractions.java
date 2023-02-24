package fuzs.easymagic.core;

import fuzs.easymagic.world.level.block.entity.ForgeModEnchantmentTableBlockEntity;
import fuzs.easymagic.world.level.block.entity.ModEnchantmentTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ForgeAbstractions implements CommonAbstractions {

    @Override
    public ModEnchantmentTableBlockEntity createModEnchantmentTableBlockEntity(BlockPos worldPosition, BlockState state) {
        return new ForgeModEnchantmentTableBlockEntity(worldPosition, state);
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, Level level, BlockPos pos) {
        return state.getEnchantPowerBonus(level, pos);
    }
}
