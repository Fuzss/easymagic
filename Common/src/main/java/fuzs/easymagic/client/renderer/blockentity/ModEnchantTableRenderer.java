package fuzs.easymagic.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;

import java.util.ArrayList;
import java.util.List;

public class ModEnchantTableRenderer extends EnchantTableRenderer {

    public ModEnchantTableRenderer(BlockEntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public void render(EnchantmentTableBlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        super.render(blockEntity, partialTicks, poseStack, bufferIn, combinedLightIn, combinedOverlayIn);
        // get inventory contents from two slots
        ItemStack itemToEnchant = ((Container) blockEntity).getItem(0);
        ItemStack catalystItem = ((Container) blockEntity).getItem(1);
        int catalystCount = Math.min(catalystItem.getCount(), 3);
        int posData = (int) blockEntity.getBlockPos().asLong();
        switch (EasyMagic.CONFIG.get(ClientConfig.class).renderContentsType) {
            case FLAT -> {
                List<ItemStack> flatItems = this.getInventoryItemList(itemToEnchant, catalystItem, catalystCount);
                this.renderFlatItemList(flatItems, blockEntity.getBlockPos(), poseStack, bufferIn, combinedLightIn, combinedOverlayIn, posData);
            }
            case FLOATING -> {
                List<ItemStack> fancyFloatingItems = this.getInventoryItemList(ItemStack.EMPTY, catalystItem, catalystCount);
                this.renderHoveringItem(blockEntity, itemToEnchant, partialTicks, poseStack, bufferIn, combinedLightIn);
                this.renderHoveringItemList(fancyFloatingItems, blockEntity.time + partialTicks, poseStack, bufferIn, combinedLightIn, combinedOverlayIn, true, posData);
            }
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

    private void renderFlatItemList(List<ItemStack> inventoryItems, BlockPos pos, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, int posData) {
        // randomise item placement depending on position
        int randomDirection = Math.abs(pos.getX() + pos.getZ()) % 4;
        // render everything just like the campfire does
        for (int i = 0; i < inventoryItems.size(); ++i) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.76171875, 0.5);
            Direction direction = Direction.from2DDataValue((i + randomDirection) % 4);
            float horizontalAngle = -direction.toYRot();
            poseStack.mulPose(Vector3f.YP.rotationDegrees(horizontalAngle));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            poseStack.translate(-0.3125, -0.3125, 0.0);
            poseStack.scale(0.375F, 0.375F, 0.375F);
            ItemStack renderStack = inventoryItems.get(i);
            Minecraft.getInstance().getItemRenderer().renderStatic(renderStack, ItemTransforms.TransformType.FIXED, combinedLightIn, combinedOverlayIn, poseStack, bufferIn, posData + i);
            poseStack.popPose();
        }
    }

    private void renderHoveringItem(EnchantmentTableBlockEntity blockEntity, ItemStack itemToEnchant, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn) {
        if (blockEntity.open == 0.0F && blockEntity.oOpen == 0.0F) return;
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.0F, 0.5F);
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(itemToEnchant, blockEntity.getLevel(), null, 0);
        float hoverOffset = Mth.sin((blockEntity.time + partialTicks) / 10.0F) * 0.1F + 0.1F;
        float modelYScale = model.getTransforms().getTransform(ItemTransforms.TransformType.GROUND).scale.y();
        float openness = Mth.lerp(partialTicks, blockEntity.oOpen, blockEntity.open);
        poseStack.translate(0.0, hoverOffset + 0.25F * modelYScale * openness - 0.15F * (1.0F - openness), 0.0);
        final float scale = openness * 0.8F + 0.2F;
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Vector3f.YP.rotation((blockEntity.time + partialTicks) / 20.0F));
        Minecraft.getInstance().getItemRenderer().render(itemToEnchant, ItemTransforms.TransformType.GROUND, false, poseStack, bufferIn, combinedLightIn, OverlayTexture.NO_OVERLAY, model);
        poseStack.popPose();
    }

    private void renderHoveringItemList(List<ItemStack> inventoryItems, float age, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, boolean rotateItems, int posData) {
        // mostly copied from Botania's runic altar rendering code, thanks!
        float itemRenderAngle = 360.0F / inventoryItems.size();
        for (int i = 0; i < inventoryItems.size(); ++i) {
            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5F, 1.0F, 0.5F);
            matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(i * itemRenderAngle + age));
            matrixStackIn.translate(0.75F, 0.0F, 0.25F);
            matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(rotateItems ? age % 360.0F : 90.0F));
            matrixStackIn.translate(0.0, 0.075 * Math.sin((age + i * 10.0) / 5.0), 0.0F);
            Minecraft.getInstance().getItemRenderer().renderStatic(inventoryItems.get(i), ItemTransforms.TransformType.GROUND, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn, posData + i);
            matrixStackIn.popPose();
        }
    }
}
