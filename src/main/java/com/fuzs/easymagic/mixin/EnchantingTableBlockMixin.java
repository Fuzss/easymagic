package com.fuzs.easymagic.mixin;

import com.fuzs.easymagic.EasyMagic;
import com.fuzs.easymagic.element.EasyEnchantingElement;
import com.fuzs.easymagic.tileentity.EnchantingTableInventoryTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
@Mixin(EnchantingTableBlock.class)
public abstract class EnchantingTableBlockMixin extends ContainerBlock {

    protected EnchantingTableBlockMixin(Properties builder) {

        super(builder);
    }

    @Inject(method = "createNewTileEntity", at = @At("HEAD"), cancellable = true)
    public void createNewTileEntity(IBlockReader worldIn, CallbackInfoReturnable<TileEntity> callbackInfo) {

        EasyEnchantingElement element = (EasyEnchantingElement) EasyMagic.EASY_ENCHANTING;
        if (element.isEnabled() && element.itemsStay) {

            callbackInfo.setReturnValue(new EnchantingTableInventoryTileEntity());
        }
    }

    @Inject(method = "onBlockActivated", at = @At("HEAD"), cancellable = true)
    public void onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit, CallbackInfoReturnable<ActionResultType> callbackInfo) {

        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (!worldIn.isRemote && tileentity instanceof EnchantingTableInventoryTileEntity) {

            EasyEnchantingElement element = (EasyEnchantingElement) EasyMagic.EASY_ENCHANTING;
            if (element.isEnabled() && element.itemsStay) {

                player.openContainer((INamedContainerProvider) tileentity);
            }

            callbackInfo.setReturnValue(ActionResultType.CONSUME);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onReplaced(@Nonnull BlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {

        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof EnchantingTableInventoryTileEntity && !state.isIn(newState.getBlock())) {

            EasyEnchantingElement element = (EasyEnchantingElement) EasyMagic.EASY_ENCHANTING;
            if (element.isEnabled() && element.itemsStay) {

                InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory) tileentity);
            }

            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

}
