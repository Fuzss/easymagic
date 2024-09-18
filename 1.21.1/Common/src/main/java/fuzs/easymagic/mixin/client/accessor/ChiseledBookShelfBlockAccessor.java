package fuzs.easymagic.mixin.client.accessor;

import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.OptionalInt;

@Mixin(ChiseledBookShelfBlock.class)
public interface ChiseledBookShelfBlockAccessor {

    @Invoker("getHitSlot")
    OptionalInt easymagic$callGetHitSlot(BlockHitResult hitResult, BlockState blockState);
}
