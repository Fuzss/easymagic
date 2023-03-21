package fuzs.easymagic.client.handler;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.config.ClientConfig;
import fuzs.easymagic.mixin.client.accessor.ChiseledBookShelfBlockAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ChiseledBookshelfTooltipHandler {
    private static final Screen SCREEN;

    static {
        // a dummy screen instance we need  for access to the tooltip rendering method
        SCREEN = new Screen(Component.empty()) {

        };
        // prevent tooltips from being rendered to the left when they would otherwise reach beyond screen border
        SCREEN.init(Minecraft.getInstance(), Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public static void setupOverlayRenderState() {
        // Forge has a dedicated method for this, so here it gets quite big
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.setShaderTexture(0, Gui.GUI_ICONS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
    }

    public static void tryRenderBookTooltip(Minecraft minecraft, PoseStack poseStack, int screenWidth, int screenHeight) {
        if (!canRenderTooltip(minecraft)) return;
        BlockHitResult hitResult = (BlockHitResult) minecraft.hitResult;
        BlockState state = minecraft.level.getBlockState(hitResult.getBlockPos());
        if (state.is(Blocks.CHISELED_BOOKSHELF)) {
            Optional<Vec2> optional = ChiseledBookShelfBlockAccessor.easymagic$callGetRelativeHitCoordinatesForBlockFace(hitResult, state.getValue(HorizontalDirectionalBlock.FACING));
            if (optional.isPresent()) {
                int hitSlot = ChiseledBookShelfBlockAccessor.easymagic$callGetHitSlot(optional.get());
                if (state.getValue(ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.get(hitSlot))) {
                    if (minecraft.level.getBlockEntity(hitResult.getBlockPos()) instanceof ChiseledBookShelfBlockEntity blockEntity) {
                        ItemStack stack = blockEntity.getItem(hitSlot);
                        if (stack.isEmpty()) return;
                        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                        renderBookTooltip(poseStack, screenWidth, screenHeight, stack);
                    }
                }
            }
        }
    }

    private static boolean canRenderTooltip(Minecraft minecraft) {
        if (EasyMagic.CONFIG.get(ClientConfig.class).chiseledBookshelfTooltip == ClientConfig.ChiseledBookshelfTooltip.DISABLED) {
            return false;
        }
        if (!minecraft.options.hideGui && minecraft.options.getCameraType().isFirstPerson())
            if (minecraft.gameMode != null && minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR && minecraft.cameraEntity instanceof Player player) {
                if (minecraft.hitResult != null && minecraft.hitResult.getType() == HitResult.Type.BLOCK) {
                    return player.isShiftKeyDown() || EasyMagic.CONFIG.get(ClientConfig.class).chiseledBookshelfTooltip == ClientConfig.ChiseledBookshelfTooltip.ENABLED;
                }
            }
        return false;
    }

    private static void renderBookTooltip(PoseStack poseStack, int screenWidth, int screenHeight, ItemStack stack) {
        int posX = screenWidth / 2 - 12 + 22 + EasyMagic.CONFIG.get(ClientConfig.class).offsetX;
        int posY = screenHeight / 2 + 12 - getFullTooltipHeight(stack) / 2 + EasyMagic.CONFIG.get(ClientConfig.class).offsetY;
        SCREEN.renderTooltip(poseStack, SCREEN.getTooltipFromItem(stack), stack.getTooltipImage(), posX, posY);
    }

    private static int getFullTooltipHeight(ItemStack stack) {
        List<ClientTooltipComponent> components = getClientTooltipComponent(stack);
        int height = components.size() == 1 ? -2 : 0;
        height += components.stream().mapToInt(ClientTooltipComponent::getHeight).sum();
        return height;
    }

    private static List<ClientTooltipComponent> getClientTooltipComponent(ItemStack stack) {
        return getClientTooltipComponent(SCREEN.getTooltipFromItem(stack), stack.getTooltipImage());
    }

    private static List<ClientTooltipComponent> getClientTooltipComponent(List<Component> tooltips, Optional<TooltipComponent> visualTooltipComponent) {
        List<ClientTooltipComponent> list = tooltips.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).collect(Collectors.toList());
        visualTooltipComponent.ifPresent((tooltipComponent) -> {
            list.add(1, ClientTooltipComponent.create(tooltipComponent));
        });
        return list;
    }
}
