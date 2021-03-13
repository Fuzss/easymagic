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

    public EnchantmentInventoryTileEntityRenderer(TileEntityRendererDispatcher p_i226010_1_) {

        super(p_i226010_1_);
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
        int direction = (pos.getX() + pos.getZ()) % 4;

        // get inventory contents from two slots
        ItemStack itemToEnchant = ((IInventory) tileEntityIn).getStackInSlot(0);
        ItemStack enchantingCatalyst = ((IInventory) tileEntityIn).getStackInSlot(1);
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(4, ItemStack.EMPTY);
        nonnulllist.set(0, itemToEnchant);

        // rendering catalyst depends on amount
        for (int i = 1; i <= Math.min(enchantingCatalyst.getCount(), 3); i++) {

            nonnulllist.set(i, enchantingCatalyst);
        }

        // render everything just like the campfire does
        for (int i = 0; i < nonnulllist.size(); ++i) {

            ItemStack itemstack = nonnulllist.get(i);
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
