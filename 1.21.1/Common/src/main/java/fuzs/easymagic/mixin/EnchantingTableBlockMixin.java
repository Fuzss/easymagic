package fuzs.easymagic.mixin;

import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.config.ServerConfig;
import fuzs.easymagic.util.ChiseledBookshelfHelper;
import fuzs.puzzleslib.api.core.v1.CommonAbstractions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.phys.shapes.Shapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantingTableBlock.class)
abstract class EnchantingTableBlockMixin extends BaseEntityBlock {

    protected EnchantingTableBlockMixin(Properties builder) {
        super(builder);
    }

    @Inject(method = "isValidBookShelf", at = @At("HEAD"), cancellable = true)
    private static void isValidBookShelf(Level level, BlockPos pos, BlockPos offset, CallbackInfoReturnable<Boolean> callback) {
        if (CommonAbstractions.INSTANCE.getEnchantPowerBonus(level.getBlockState(pos.offset(offset)), level, pos.offset(offset)) == 0.0F) {
            if (ChiseledBookshelfHelper.findValidBooks(level, pos, offset) == 0) {
                callback.setReturnValue(false);
                return;
            }
        }
        BlockPos inBetweenPos = pos.offset(offset.getX() / 2, offset.getY(), offset.getZ() / 2);
        boolean isEmpty;
        if (EasyMagic.CONFIG.get(ServerConfig.class).lenientBookshelves) {
            isEmpty = level.getBlockState(inBetweenPos).getCollisionShape(level, inBetweenPos) != Shapes.block();
        } else {
            isEmpty = level.isEmptyBlock(inBetweenPos);
        }
        callback.setReturnValue(isEmpty);
    }
}
