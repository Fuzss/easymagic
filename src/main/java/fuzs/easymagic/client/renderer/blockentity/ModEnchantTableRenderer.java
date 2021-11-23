package fuzs.easymagic.client.renderer.blockentity;

import fuzs.easymagic.EasyMagic;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ModEnchantTableRenderer extends EnchantTableRenderer {
    public ModEnchantTableRenderer(BlockEntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void render(EnchantmentTableBlockEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        super.render(tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        // get inventory contents from two slots
        ItemStack itemToEnchant = ((Container) tileEntityIn).getItem(0);
        ItemStack catalystItem = ((Container) tileEntityIn).getItem(1);
        int catalystCount = Math.min(catalystItem.getCount(), 3);
        int posData = (int) tileEntityIn.getBlockPos().asLong();
        switch (EasyMagic.CONFIG.client().renderContentsType) {
            case FLAT -> {
                List<ItemStack> flatItems = this.getInventoryItemList(itemToEnchant, catalystItem, catalystCount);
                this.renderFlatItemList(flatItems, tileEntityIn.getBlockPos(), matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, posData);
            }
            case FLOATING -> {
                List<ItemStack> floatingItems = this.getInventoryItemList(itemToEnchant, catalystItem, catalystCount);
                this.renderHoveringItemList(floatingItems, tileEntityIn.time + partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, false, posData);
            }
            case FANCY_FLOATING -> {
                List<ItemStack> fancyFloatingItems = this.getInventoryItemList(ItemStack.EMPTY, catalystItem, catalystCount);
                this.renderHoveringItem(itemToEnchant, tileEntityIn.getLevel(), tileEntityIn.time + partialTicks, matrixStackIn, bufferIn, combinedLightIn);
                this.renderHoveringItemList(fancyFloatingItems, tileEntityIn.time + partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, true, posData);
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

    private void renderFlatItemList(List<ItemStack> inventoryItems, BlockPos pos, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, int posData) {
        // randomise item placement depending on position
        int randomDirection = Math.abs(pos.getX() + pos.getZ()) % 4;
        // render everything just like the campfire does
        for (int i = 0; i < inventoryItems.size(); ++i) {
            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5, 0.76171875, 0.5);
            Direction direction = Direction.from2DDataValue((i + randomDirection) % 4);
            float horizontalAngle = -direction.toYRot();
            matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(horizontalAngle));
            matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            matrixStackIn.translate(-0.3125, -0.3125, 0.0);
            matrixStackIn.scale(0.375F, 0.375F, 0.375F);
            ItemStack renderStack = inventoryItems.get(i);
            Minecraft.getInstance().getItemRenderer().renderStatic(renderStack, ItemTransforms.TransformType.FIXED, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn, posData + i);
            matrixStackIn.popPose();
        }
    }

    private void renderHoveringItemList(List<ItemStack> inventoryItems, float age, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, boolean rotateItems, int posData) {
        // mostly copied from botanias runic altar rendering code
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

    private void renderHoveringItem(ItemStack itemToEnchant, @Nullable Level worldIn, float age, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn) {
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5F, 1.0F, 0.5F);
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(itemToEnchant, worldIn, null, 0);
        float hoverOffset = Mth.sin(age / 10.0F) * 0.1F + 0.1F;
        float modelYScale = model.getTransforms().getTransform(ItemTransforms.TransformType.GROUND).scale.y();
        matrixStackIn.translate(0.0, hoverOffset + 0.25F * modelYScale, 0.0);
        matrixStackIn.mulPose(Vector3f.YP.rotation(age / 20.0F));
        Minecraft.getInstance().getItemRenderer().render(itemToEnchant, ItemTransforms.TransformType.GROUND, false, matrixStackIn, bufferIn, combinedLightIn, OverlayTexture.NO_OVERLAY, model);
        matrixStackIn.popPose();
    }
}
