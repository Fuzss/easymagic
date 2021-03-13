package com.fuzs.easymagic.client.renderer.tileentity;

import com.fuzs.easymagic.EasyMagic;
import com.fuzs.easymagic.element.EasyEnchantingElement;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.EnchantmentTableTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.EnchantingTableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nonnull;

public class EnchantmentInventoryTileEntityRenderer extends EnchantmentTableTileEntityRenderer {

    public EnchantmentInventoryTileEntityRenderer(TileEntityRendererDispatcher tileEntityRendererDispatcher) {

        super(tileEntityRendererDispatcher);
    }

    @Override
    public void render(@Nonnull EnchantingTableTileEntity tileEntityIn, float partialTicks, @Nonnull MatrixStack matrixStackIn, @Nonnull IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

        super.render(tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        EasyEnchantingElement element = (EasyEnchantingElement) EasyMagic.EASY_ENCHANTING;
        if (!element.isEnabled() || !element.extension.renderContents || !(tileEntityIn instanceof IInventory)) {

            return;
        }

        // randomise item placement via position
        BlockPos pos = tileEntityIn.getPos();
        int direction = Math.abs(pos.getX() + pos.getZ()) % 4;

        // get inventory contents from two slots
        ItemStack itemToEnchant = ((IInventory) tileEntityIn).getStackInSlot(0);
        ItemStack catalyst = ((IInventory) tileEntityIn).getStackInSlot(1);
        NonNullList<ItemStack> renderingList = NonNullList.withSize(4, ItemStack.EMPTY);
        renderingList.set(0, itemToEnchant);

        // rendering catalyst depends on amount
        for (int i = 1; i <= Math.min(catalyst.getCount(), 3); i++) {

            renderingList.set(i, catalyst);
        }

        // render everything just like the campfire does
        for (int i = 0; i < renderingList.size(); ++i) {

            ItemStack itemstack = renderingList.get(i);
            if (itemstack != ItemStack.EMPTY) {

                matrixStackIn.push();
                matrixStackIn.translate(0.5D, 0.76171875D, 0.5D);
                Direction direction1 = Direction.byHorizontalIndex((i + direction) % 4);
                float horizontalAngle = -direction1.getHorizontalAngle();
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(horizontalAngle));
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(90.0F));
                matrixStackIn.translate(-0.3125D, -0.3125D, 0.0D);
                matrixStackIn.scale(0.375F, 0.375F, 0.375F);
                Minecraft.getInstance().getItemRenderer().renderItem(itemstack, ItemCameraTransforms.TransformType.FIXED, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn);
                matrixStackIn.pop();
            }
        }
    }

}
