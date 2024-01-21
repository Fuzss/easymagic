package fuzs.easymagic.mixin.client.accessor;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.Optional;

@Mixin(ChiseledBookShelfBlock.class)
public interface ChiseledBookShelfBlockAccessor {

    @Invoker("getRelativeHitCoordinatesForBlockFace")
    static Optional<Vec2> easymagic$callGetRelativeHitCoordinatesForBlockFace(BlockHitResult hitResult, Direction face) {
        throw new RuntimeException();
    }

    @Invoker("getHitSlot")
    static int easymagic$callGetHitSlot(Vec2 hitPos) {
        throw new RuntimeException();
    }
}
