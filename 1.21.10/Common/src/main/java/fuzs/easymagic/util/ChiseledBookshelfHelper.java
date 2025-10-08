package fuzs.easymagic.util;

import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.config.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ChiseledBookshelfHelper {

    public static int findValidBooks(Level level, BlockPos pos, BlockPos offset) {
        BlockState state = level.getBlockState(pos.offset(offset));
        $1:
        if (state.getBlock() instanceof ChiseledBookShelfBlock && state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            if (EasyMagic.CONFIG.get(ServerConfig.class).chiseledBookshelfEnchantingPower == ServerConfig.ChiseledBookshelfPower.NONE) {
                return 0;
            }
            if (level.getBlockEntity(pos.offset(offset)) instanceof ChiseledBookShelfBlockEntity blockEntity) {
                if (EasyMagic.CONFIG.get(ServerConfig.class).chiseledBookshelfEnchantingPower == ServerConfig.ChiseledBookshelfPower.FACING) {
                    Direction direction = state.getValue(HorizontalDirectionalBlock.FACING).getOpposite();
                    int axisOffset = direction.getAxis().choose(offset.getX(), offset.getY(), offset.getZ());
                    if (Math.abs(axisOffset) != 2 || Math.signum(axisOffset) != direction.getAxisDirection().getStep()) {
                        break $1;
                    }
                }
                return blockEntity.count();
            }
        }
        return 0;
    }
}
