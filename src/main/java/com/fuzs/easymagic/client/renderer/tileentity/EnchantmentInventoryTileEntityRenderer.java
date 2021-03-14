package com.fuzs.easymagic.client.renderer.tileentity;

import com.fuzs.easymagic.EasyMagic;
import com.fuzs.easymagic.element.EasyEnchantingElement;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.EnchantmentTableTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.EnchantingTableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EnchantmentInventoryTileEntityRenderer extends EnchantmentTableTileEntityRenderer {

    public EnchantmentInventoryTileEntityRenderer(TileEntityRendererDispatcher tileEntityRendererDispatcher) {

        super(tileEntityRendererDispatcher);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void render(EnchantingTableTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

        super.render(tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        EasyEnchantingElement element = (EasyEnchantingElement) EasyMagic.EASY_ENCHANTING;
        if (!element.itemsStay) {

            return;
        }

        // get inventory contents from two slots
        ItemStack itemToEnchant = ((IInventory) tileEntityIn).getStackInSlot(0);
        ItemStack catalystItem = ((IInventory) tileEntityIn).getStackInSlot(1);
        int catalystCount = Math.min(catalystItem.getCount(), 3);

        switch (element.extension.renderContentsType) {

            case FLAT:

                List<ItemStack> flatItems = this.getInventoryItemList(itemToEnchant, catalystItem, catalystCount);
                this.renderFlatItemList(flatItems, tileEntityIn.getPos(), matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
                break;
            case FLOATING:

                List<ItemStack> floatingItems = this.getInventoryItemList(itemToEnchant, catalystItem, catalystCount);
                this.renderHoveringItemList(floatingItems, tileEntityIn.ticks + partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, false);
                break;
            case FANCY_FLOATING:

                List<ItemStack> fancyFloatingItems = this.getInventoryItemList(ItemStack.EMPTY, catalystItem, catalystCount);
                this.renderHoveringItem(itemToEnchant, tileEntityIn.getWorld(), tileEntityIn.ticks + partialTicks, matrixStackIn, bufferIn, combinedLightIn);
                this.renderHoveringItemList(fancyFloatingItems, tileEntityIn.ticks + partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, true);
                break;
        }
    }

    private List<ItemStack> getInventoryItemList(ItemStack itemToEnchant, ItemStack catalystItem, int catalystCount) {

        List<ItemStack> inventoryItems = new ArrayList<>(4);
        inventoryItems.add(itemToEnchant);
        for (int i = 0; i < catalystCount; i++) {

            inventoryItems.add(catalystItem);
        }

        inventoryItems.removeIf(ItemStack::isEmpty);
        return inventoryItems;
    }

    private void renderFlatItemList(List<ItemStack> inventoryItems, BlockPos pos, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

        // randomise item placement depending on position
        int randomDirection = Math.abs(pos.getX() + pos.getZ()) % 4;

        // render everything just like the campfire does
        for (int i = 0; i < inventoryItems.size(); ++i) {

            matrixStackIn.push();
            matrixStackIn.translate(0.5, 0.76171875, 0.5);
            Direction direction = Direction.byHorizontalIndex((i + randomDirection) % 4);
            float horizontalAngle = -direction.getHorizontalAngle();
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(horizontalAngle));
            matrixStackIn.rotate(Vector3f.XP.rotationDegrees(90.0F));
            matrixStackIn.translate(-0.3125, -0.3125, 0.0);
            matrixStackIn.scale(0.375F, 0.375F, 0.375F);
            ItemStack renderStack = inventoryItems.get(i);
            Minecraft.getInstance().getItemRenderer().renderItem(renderStack, ItemCameraTransforms.TransformType.FIXED, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn);
            matrixStackIn.pop();
        }
    }

    private void renderHoveringItemList(List<ItemStack> inventoryItems, float age, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn, boolean rotateItems) {

        float itemRenderAngle = 360.0F / inventoryItems.size();
        for (int i = 0; i < inventoryItems.size(); ++i) {

            matrixStackIn.push();
            matrixStackIn.translate(0.5F, 1.0F, 0.5F);
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(i * itemRenderAngle + age));
            matrixStackIn.translate(0.75F, 0.0F, 0.25F);
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(rotateItems ? age % 360.0F : 90.0F));
            matrixStackIn.translate(0.0, 0.075 * Math.sin((age + i * 10.0) / 5.0), 0.0F);
            Minecraft.getInstance().getItemRenderer().renderItem(inventoryItems.get(i), ItemCameraTransforms.TransformType.GROUND, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn);
            matrixStackIn.pop();
        }
    }

    @SuppressWarnings("deprecation")
    private void renderHoveringItem(ItemStack itemToEnchant, @Nullable World worldIn, float age, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn) {

        matrixStackIn.push();
        matrixStackIn.translate(0.5F, 1.0F, 0.5F);
        IBakedModel model = Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides(itemToEnchant, worldIn, null);
        float hoverOffset = MathHelper.sin(age / 10.0F) * 0.1F + 0.1F;
        float modelYScale = model.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.GROUND).scale.getY();
        matrixStackIn.translate(0.0, hoverOffset + 0.25F * modelYScale, 0.0);
        matrixStackIn.rotate(Vector3f.YP.rotation(age / 20.0F));
        Minecraft.getInstance().getItemRenderer().renderItem(itemToEnchant, ItemCameraTransforms.TransformType.GROUND, false, matrixStackIn, bufferIn, combinedLightIn, OverlayTexture.NO_OVERLAY, model);
        matrixStackIn.pop();
    }

}
