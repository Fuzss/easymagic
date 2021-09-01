package com.fuzs.easymagic.mixin;

import com.fuzs.easymagic.EasyMagic;
import com.fuzs.easymagic.inventory.container.EnchantmentInventoryContainer;
import com.fuzs.easymagic.tileentity.EnchantingTableInventoryTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// higher mixin priority to resolve conflict with Lavalogged blocks mod (our implementation does what they do plus more)
@SuppressWarnings({"unused", "NullableProblems", "deprecation"})
@Mixin(value = EnchantingTableBlock.class, priority = 1001)
public abstract class EnchantingTableBlockMixin extends ContainerBlock {

    protected EnchantingTableBlockMixin(Properties builder) {

        super(builder);
    }

    @Inject(method = "createNewTileEntity", at = @At("HEAD"), cancellable = true)
    public void createNewTileEntity(IBlockReader worldIn, CallbackInfoReturnable<TileEntity> callbackInfo) {

        if (EasyMagic.EASY_ENCHANTING.isEnabled()) {

            callbackInfo.setReturnValue(new EnchantingTableInventoryTileEntity());
        }
    }

    @Inject(method = "onBlockActivated", at = @At("HEAD"), cancellable = true)
    public void onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit, CallbackInfoReturnable<ActionResultType> callbackInfo) {

        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (!worldIn.isRemote && tileentity instanceof EnchantingTableInventoryTileEntity) {

            player.openContainer((INamedContainerProvider) tileentity);
            if (player.openContainer instanceof EnchantmentInventoryContainer) {

                // items might still be in inventory slots, so this needs to update so that enchantment buttons are shown
                player.openContainer.onCraftMatrixChanged((IInventory) tileentity);
            }

            callbackInfo.setReturnValue(ActionResultType.CONSUME);
        }
    }

    @Redirect(method = "animateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isAirBlock(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean isBlockEmpty(World world, BlockPos pos) {

        return EnchantmentInventoryContainer.isBlockEmpty(world, pos);
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {

        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (!state.isIn(newState.getBlock()) && tileentity instanceof EnchantingTableInventoryTileEntity) {

            InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory) tileentity);
        }

        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState state) {

        return true;
    }

    @Override
    public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {

        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof IInventory) {

            ItemStack catalystItem = ((IInventory) tileentity).getStackInSlot(1);
            return Math.min(catalystItem.getCount(), 3);
        }

        return 0;
    }

}
