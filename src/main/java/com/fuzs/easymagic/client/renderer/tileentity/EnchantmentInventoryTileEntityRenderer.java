package com.fuzs.easymagic.client.renderer.tileentity;

import com.fuzs.easymagic.EasyMagic;
import com.fuzs.easymagic.client.element.EasyEnchantingExtension;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class EnchantmentInventoryTileEntityRenderer extends EnchantmentTableTileEntityRenderer {

    public EnchantmentInventoryTileEntityRenderer(TileEntityRendererDispatcher tileEntityRendererDispatcher) {

        super(tileEntityRendererDispatcher);
    }

    @Override
    public void render(@Nonnull EnchantingTableTileEntity tileEntityIn, float partialTicks, @Nonnull MatrixStack matrixStackIn, @Nonnull IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

        super.render(tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        EasyEnchantingElement element = (EasyEnchantingElement) EasyMagic.EASY_ENCHANTING;
        if (!element.itemsStay || EasyEnchantingExtension.ItemRenderType.NONE == element.extension.renderContentsType) {

            return;
        }

        // randomise item placement via position
        BlockPos pos = tileEntityIn.getPos();
        int randomDirection = Math.abs(pos.getX() + pos.getZ()) % 4;

        // get inventory contents from two slots
        ItemStack itemToEnchant = ((IInventory) tileEntityIn).getStackInSlot(0);
        ItemStack catalystItem = ((IInventory) tileEntityIn).getStackInSlot(1);

        // rendering catalyst depends on amount
        List<ItemStack> renderingList = new ArrayList<>(4);
        renderingList.add(itemToEnchant);
        for (int i = 0; i < Math.min(catalystItem.getCount(), 3); i++) {

            renderingList.add(catalystItem);
        }

        renderingList.removeIf(ItemStack::isEmpty);

        float[] floatingItemAngles = new float[renderingList.size()];
        final float anglePerItem = 360.0F / renderingList.size();
        float currentTotalAngle = 0.0F;
        for (int i = 0; i < floatingItemAngles.length; i++) {

            floatingItemAngles[i] = currentTotalAngle += anglePerItem;
        }

        for (int i = 0; i < renderingList.size(); ++i) {

            // render everything just like the campfire does
            matrixStackIn.push();
            matrixStackIn.translate(0.5D, 0.76171875D, 0.5D);
            Direction direction = Direction.byHorizontalIndex((i + randomDirection) % 4);
            float horizontalAngle = -direction.getHorizontalAngle();
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(horizontalAngle));
            matrixStackIn.rotate(Vector3f.XP.rotationDegrees(90.0F));
            matrixStackIn.translate(-0.3125D, -0.3125D, 0.0D);
            matrixStackIn.scale(0.375F, 0.375F, 0.375F);
            ItemStack renderStack = renderingList.get(i);
            Minecraft.getInstance().getItemRenderer().renderItem(renderStack, ItemCameraTransforms.TransformType.FIXED, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn);
            matrixStackIn.pop();

            matrixStackIn.push();
            matrixStackIn.translate(0.5F, 1.25F, 0.5F);
            float time = tileEntityIn.ticks + partialTicks;
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(floatingItemAngles[i] + time));
            matrixStackIn.translate(0.75F, 0.0F, 0.25F);
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(time % 360.0F));
            matrixStackIn.translate(0.0, 0.075 * Math.sin((time + i * 10.0) / 5.0), 0.0F);
            Minecraft.getInstance().getItemRenderer().renderItem(renderStack, ItemCameraTransforms.TransformType.GROUND, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn);
            matrixStackIn.pop();
        }
    }

}
