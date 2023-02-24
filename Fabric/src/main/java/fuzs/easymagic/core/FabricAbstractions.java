package fuzs.easymagic.core;

import fuzs.easymagic.world.level.block.entity.ModEnchantmentTableBlockEntity;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FabricAbstractions implements CommonAbstractions {

    @Override
    public ModEnchantmentTableBlockEntity createModEnchantmentTableBlockEntity(BlockPos worldPosition, BlockState state) {
        return new ModEnchantmentTableBlockEntity(worldPosition, state);
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, Level level, BlockPos pos) {
        return state.is(ConventionalBlockTags.BOOKSHELVES) ? 1.0F : 0.0F;
    }
}
