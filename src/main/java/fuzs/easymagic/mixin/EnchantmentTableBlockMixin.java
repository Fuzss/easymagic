package fuzs.easymagic.mixin;

import fuzs.easymagic.registry.ModRegistry;
import fuzs.easymagic.world.inventory.ModEnchantmentMenu;
import fuzs.easymagic.world.level.block.entity.ModEnchantmentTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// higher mixin priority to resolve conflict with Lavalogged blocks mod (our implementation does what they do plus more)
@SuppressWarnings("deprecation")
@Mixin(value = EnchantmentTableBlock.class, priority = 1001)
public abstract class EnchantmentTableBlockMixin extends BaseEntityBlock {
    protected EnchantmentTableBlockMixin(Properties builder) {
        super(builder);
    }

    @Inject(method = "newBlockEntity", at = @At("HEAD"), cancellable = true)
    public void newBlockEntity(BlockPos pPos, BlockState pState, CallbackInfoReturnable<BlockEntity> callbackInfo) {
        callbackInfo.setReturnValue(new ModEnchantmentTableBlockEntity(pPos, pState));
    }

    @Inject(method = "getTicker", at = @At("HEAD"), cancellable = true)
    public <T extends BlockEntity> void getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType, CallbackInfoReturnable<BlockEntityTicker<T>> callbackInfo) {
        callbackInfo.setReturnValue(pLevel.isClientSide ? createTickerHelper(pBlockEntityType, ModRegistry.ENCHANTING_TABLE_BLOCK_ENTITY_TYPE.get(), EnchantmentTableBlockEntity::bookAnimationTick) : null);
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> callbackInfo) {
        BlockEntity tileentity = worldIn.getBlockEntity(pos);
        if (!worldIn.isClientSide && tileentity instanceof ModEnchantmentTableBlockEntity) {
            player.openMenu((MenuProvider) tileentity);
            if (player.containerMenu instanceof ModEnchantmentMenu) {
                // items might still be in inventory slots, so this needs to update so that enchantment buttons are shown
                player.containerMenu.slotsChanged((Container) tileentity);
            }
            callbackInfo.setReturnValue(InteractionResult.CONSUME);
        }
    }

    @Redirect(method = "isValidBookShelf", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isEmptyBlock(Lnet/minecraft/core/BlockPos;)Z"))
    private static boolean isEmptyBlock(Level level, BlockPos pos) {
        return ModEnchantmentMenu.isBlockEmpty(level, pos);
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof ModEnchantmentTableBlockEntity) {
                Containers.dropContents(worldIn, pos, (Container) tileentity);
            }
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        BlockEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof Container) {
            ItemStack catalystItem = ((Container) tileentity).getItem(1);
            return Math.min(catalystItem.getCount(), 3);
        }
        return 0;
    }
}
