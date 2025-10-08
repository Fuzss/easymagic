package fuzs.easymagic.mixin;

import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.config.CommonConfig;
import fuzs.easymagic.handler.BlockConversionHandler;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldGenRegion.class)
abstract class WorldGenRegionMixin {

    @ModifyVariable(method = "setBlock", at = @At(value = "LOAD", ordinal = 0), argsOnly = true)
    public BlockState setBlock(BlockState blockState) {
        if (EasyMagic.CONFIG.get(CommonConfig.class).convertVanillaEnchantingTableDuringWorldGen) {
            return BlockConversionHandler.convertFromVanillaBlock(blockState);
        } else {
            return blockState;
        }
    }
}
