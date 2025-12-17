package fuzs.easymagic.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.client.renderer.blockentity.state.ModEnchantTableRenderState;
import fuzs.easymagic.config.ClientConfig;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.blockentity.state.CampfireRenderState;
import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ModEnchantTableRenderer extends EnchantTableRenderer {
    private final ItemModelResolver itemModelResolver;

    public ModEnchantTableRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public EnchantTableRenderState createRenderState() {
        return new ModEnchantTableRenderState();
    }

    @Override
    public void extractRenderState(EnchantingTableBlockEntity blockEntity, EnchantTableRenderState renderState, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        ((ModEnchantTableRenderState) renderState).items = new ArrayList<>();
        ItemDisplayContext itemDisplayContext = this.getItemDisplayContext();
        if (itemDisplayContext != null) {
            List<ItemStack> itemList = this.getItemList((Container) blockEntity);
            int position = (int) blockEntity.getBlockPos().asLong();
            for (int i = 0; i < itemList.size(); i++) {
                ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
                this.itemModelResolver.updateForTopItem(itemStackRenderState,
                        itemList.get(i),
                        itemDisplayContext,
                        blockEntity.getLevel(),
                        null,
                        position + i);
                ((ModEnchantTableRenderState) renderState).items.add(itemStackRenderState);
            }
        }
    }

    private @Nullable ItemDisplayContext getItemDisplayContext() {
        return switch (EasyMagic.CONFIG.get(ClientConfig.class).renderEnchantingTableContents) {
            case FLOATING -> ItemDisplayContext.GROUND;
            case FLAT -> ItemDisplayContext.FIXED;
            default -> null;
        };
    }

    private List<ItemStack> getItemList(Container container) {
        List<ItemStack> itemStacks = new ArrayList<>();
        itemStacks.add(container.getItem(0));
        for (int i = 0; i < 3; i++) {
            if (i < container.getItem(1).getCount()) {
                itemStacks.add(container.getItem(1));
            }

            if (i < container.getItem(2).getCount()) {
                itemStacks.add(container.getItem(2));
            }
        }

        return itemStacks.stream().filter(Predicate.not(ItemStack::isEmpty)).map((ItemStack itemStack) -> {
            return itemStack.copyWithCount(1);
        }).toList();
    }

    @Override
    public void submit(EnchantTableRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        super.submit(renderState, poseStack, nodeCollector, cameraRenderState);
        switch (EasyMagic.CONFIG.get(ClientConfig.class).renderEnchantingTableContents) {
            case FLAT -> {
                this.submitFlatItemList((ModEnchantTableRenderState) renderState, poseStack, nodeCollector);
            }
            case FLOATING -> {
                this.submitFloatingItemStack((ModEnchantTableRenderState) renderState, poseStack, nodeCollector);
                this.submitFloatingItemList((ModEnchantTableRenderState) renderState, poseStack, nodeCollector);
            }
        }
    }

    /**
     * @see net.minecraft.client.renderer.blockentity.CampfireRenderer#submit(CampfireRenderState, PoseStack,
     *         SubmitNodeCollector, CameraRenderState)
     */
    private void submitFlatItemList(ModEnchantTableRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector) {
        // randomise item placement depending on position
        int position = Math.abs(renderState.blockPos.getX() + renderState.blockPos.getZ()) % 4;
        for (int i = 0, j = 0; i < renderState.items.size() && j < 4; ++i) {
            ItemStackRenderState itemStackRenderState = renderState.items.get(i);
            if (!itemStackRenderState.isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(0.5, 0.76171875, 0.5);
                Direction direction = Direction.from2DDataValue((j + position) % 4);
                float horizontalAngle = -direction.toYRot();
                poseStack.mulPose(Axis.YP.rotationDegrees(horizontalAngle));
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.translate(-0.3125, -0.3125, 0.0);
                poseStack.scale(0.375F, 0.375F, 0.375F);
                itemStackRenderState.submit(poseStack,
                        nodeCollector,
                        renderState.lightCoords,
                        OverlayTexture.NO_OVERLAY,
                        0);
                poseStack.popPose();
                j++;
            }
        }
    }

    private void submitFloatingItemStack(ModEnchantTableRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector) {
        if (renderState.open > 0.0F && !renderState.items.isEmpty() && !renderState.items.getFirst().isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.0F, 0.5F);
            float hoverOffset = Mth.sin(renderState.time / 10.0F) * 0.1F + 0.1F;
            AABB aABB = renderState.items.getFirst().getModelBoundingBox();
            float modelYScale = -((float) aABB.minY) + 0.0625F;
            float openness = renderState.open;
            poseStack.translate(0.0, hoverOffset + modelYScale * openness - 0.15F * (1.0F - openness), 0.0);
            float scale = openness * 0.8F + 0.2F;
            poseStack.scale(scale, scale, scale);
            poseStack.mulPose(Axis.YP.rotation(renderState.time / 20.0F));
            renderState.items.getFirst()
                    .submit(poseStack, nodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    private void submitFloatingItemList(ModEnchantTableRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector) {
        this.submitFloatingItemList(renderState, poseStack, nodeCollector, true);
    }

    /**
     * Mostly copied from <a
     * href="https://github.com/VazkiiMods/Botania/blob/1.19.x/Xplat/src/main/java/vazkii/botania/client/render/block_entity/RunicAltarBlockEntityRenderer.java">Botania's
     * Runic Altar</a> rendering code, thanks!
     */
    private void submitFloatingItemList(ModEnchantTableRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, boolean rotateItems) {
        if (!renderState.items.isEmpty()) {
            int nonEmptyItems = (int) renderState.items.subList(1, renderState.items.size())
                    .stream()
                    .filter(Predicate.not(ItemStackRenderState::isEmpty))
                    .count();
            float angle = 360.0F / nonEmptyItems;
            for (int i = 1, j = 0; i < renderState.items.size(); ++i) {
                ItemStackRenderState itemStackRenderState = renderState.items.get(i);
                if (!itemStackRenderState.isEmpty()) {
                    poseStack.pushPose();
                    poseStack.translate(0.5F, 1.0F, 0.5F);
                    poseStack.mulPose(Axis.YP.rotationDegrees(j * angle + renderState.time));
                    poseStack.translate(0.75F, 0.0F, 0.25F);
                    poseStack.mulPose(Axis.YP.rotationDegrees(rotateItems ? renderState.time % 360.0F : 90.0F));
                    poseStack.translate(0.0, 0.075 * Math.sin((renderState.time + j * 10.0) / 5.0), 0.0F);
                    itemStackRenderState.submit(poseStack,
                            nodeCollector,
                            renderState.lightCoords,
                            OverlayTexture.NO_OVERLAY,
                            0);
                    poseStack.popPose();
                    j++;
                }
            }
        }
    }
}
