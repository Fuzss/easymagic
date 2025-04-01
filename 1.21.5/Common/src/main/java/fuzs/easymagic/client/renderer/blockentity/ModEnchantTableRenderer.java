package fuzs.easymagic.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.config.ClientConfig;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;

import java.util.ArrayList;
import java.util.List;

public class ModEnchantTableRenderer extends EnchantTableRenderer {
    private final ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
    public final ItemModelResolver itemModelResolver;
    public final ItemRenderer itemRenderer;

    public ModEnchantTableRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public void render(EnchantingTableBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        super.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        Level level = blockEntity.getLevel();
        if (level != null) {
            ItemStack itemStack = ((Container) blockEntity).getItem(0);
            ItemStack enchantingCatalystItemStack = ((Container) blockEntity).getItem(1);
            ItemStack rerollCatalystItemStack = ((Container) blockEntity).getItem(2);
            int posData = (int) blockEntity.getBlockPos().asLong();
            switch (EasyMagic.CONFIG.get(ClientConfig.class).renderEnchantingTableContents) {
                case FLAT -> {
                    List<ItemStack> itemStacks = collectItemStacks(itemStack,
                            enchantingCatalystItemStack,
                            rerollCatalystItemStack);
                    this.renderFlatItemList(itemStacks,
                            blockEntity.getBlockPos(),
                            poseStack,
                            bufferSource,
                            packedLight,
                            packedOverlay,
                            level,
                            posData);
                }
                case FLOATING -> {
                    List<ItemStack> itemStacks = collectItemStacks(ItemStack.EMPTY,
                            enchantingCatalystItemStack,
                            rerollCatalystItemStack);
                    this.renderHoveringItem(blockEntity,
                            itemStack,
                            partialTick,
                            poseStack,
                            bufferSource,
                            packedLight,
                            packedOverlay);
                    this.renderHoveringItemList(itemStacks,
                            blockEntity.time + partialTick,
                            poseStack,
                            bufferSource,
                            packedLight,
                            packedOverlay,
                            true,
                            level,
                            posData);
                }
            }
        }
    }

    private static List<ItemStack> collectItemStacks(ItemStack itemStack, ItemStack enchantingCatalystItemStack, ItemStack rerollCatalystItemStack) {
        List<ItemStack> itemStacks = new ArrayList<>();
        itemStacks.add(itemStack);
        for (int i = 0; i < 3; i++) {
            if (i < enchantingCatalystItemStack.getCount()) {
                itemStacks.add(enchantingCatalystItemStack);
            }
            if (i < rerollCatalystItemStack.getCount()) {
                itemStacks.add(rerollCatalystItemStack);
            }
        }
        itemStacks.removeIf(ItemStack::isEmpty);
        return itemStacks;
    }

    private void renderFlatItemList(List<ItemStack> itemStacks, BlockPos blockPos, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Level level, int posData) {
        // randomise item placement depending on position
        int randomDirection = Math.abs(blockPos.getX() + blockPos.getZ()) % 4;
        // render everything just like the campfire does
        for (int i = 0; i < Math.min(4, itemStacks.size()); ++i) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.76171875, 0.5);
            Direction direction = Direction.from2DDataValue((i + randomDirection) % 4);
            float horizontalAngle = -direction.toYRot();
            poseStack.mulPose(Axis.YP.rotationDegrees(horizontalAngle));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.translate(-0.3125, -0.3125, 0.0);
            poseStack.scale(0.375F, 0.375F, 0.375F);
            ItemStack renderStack = itemStacks.get(i);
            this.itemRenderer.renderStatic(renderStack,
                    ItemDisplayContext.FIXED,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    bufferSource,
                    level,
                    posData + i);
            poseStack.popPose();
        }
    }

    private void renderHoveringItem(EnchantingTableBlockEntity blockEntity, ItemStack itemStack, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (blockEntity.open != 0.0F || blockEntity.oOpen != 0.0F) {
            this.itemModelResolver.updateForTopItem(this.itemStackRenderState,
                    itemStack,
                    ItemDisplayContext.GROUND,
                    false,
                    blockEntity.getLevel(),
                    null,
                    0);
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.0F, 0.5F);
            float hoverOffset = Mth.sin((blockEntity.time + partialTick) / 10.0F) * 0.1F + 0.1F;
            float modelYScale = this.itemStackRenderState.transform().scale.y();
            float openness = Mth.lerp(partialTick, blockEntity.oOpen, blockEntity.open);
            poseStack.translate(0.0, hoverOffset + 0.25F * modelYScale * openness - 0.15F * (1.0F - openness), 0.0);
            float scale = openness * 0.8F + 0.2F;
            poseStack.scale(scale, scale, scale);
            poseStack.mulPose(Axis.YP.rotation((blockEntity.time + partialTick) / 20.0F));
            this.itemStackRenderState.render(poseStack, bufferSource, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }

    private void renderHoveringItemList(List<ItemStack> itemStacks, float age, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, boolean rotateItems, Level level, int posData) {
        // mostly copied from Botania's runic altar rendering code, thanks!
        float itemRenderAngle = 360.0F / itemStacks.size();
        for (int i = 0; i < itemStacks.size(); ++i) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.0F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(i * itemRenderAngle + age));
            poseStack.translate(0.75F, 0.0F, 0.25F);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotateItems ? age % 360.0F : 90.0F));
            poseStack.translate(0.0, 0.075 * Math.sin((age + i * 10.0) / 5.0), 0.0F);
            this.itemRenderer.renderStatic(itemStacks.get(i),
                    ItemDisplayContext.GROUND,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    bufferSource,
                    level,
                    posData + i);
            poseStack.popPose();
        }
    }
}
