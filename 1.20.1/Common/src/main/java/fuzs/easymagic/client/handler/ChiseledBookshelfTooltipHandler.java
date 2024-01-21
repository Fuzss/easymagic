package fuzs.easymagic.client.handler;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.config.ClientConfig;
import fuzs.easymagic.mixin.client.accessor.ChiseledBookShelfBlockAccessor;
import fuzs.puzzleslib.api.client.screen.v2.TooltipRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;

import java.util.List;
import java.util.Optional;

public class ChiseledBookshelfTooltipHandler {

    public static void tryRenderBookTooltip(Minecraft minecraft, GuiGraphics guiGraphics, float tickDelta, int screenWidth, int screenHeight) {
        if (!canRenderTooltip(minecraft)) return;
        BlockHitResult hitResult = (BlockHitResult) minecraft.hitResult;
        BlockState state = minecraft.level.getBlockState(hitResult.getBlockPos());
        if (state.getBlock() instanceof ChiseledBookShelfBlock && state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            Optional<Vec2> optional = ChiseledBookShelfBlockAccessor.easymagic$callGetRelativeHitCoordinatesForBlockFace(hitResult, state.getValue(HorizontalDirectionalBlock.FACING));
            if (optional.isPresent()) {
                int hitSlot = ChiseledBookShelfBlockAccessor.easymagic$callGetHitSlot(optional.get());
                BooleanProperty property = ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.get(hitSlot);
                if (state.hasProperty(property) && state.getValue(property)) {
                    if (minecraft.level.getBlockEntity(hitResult.getBlockPos()) instanceof ChiseledBookShelfBlockEntity blockEntity) {
                        ItemStack stack = blockEntity.getItem(hitSlot);
                        if (stack.isEmpty()) return;
                        RenderSystem.enableBlend();
                        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                        RenderSystem.disableDepthTest();
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        renderBookTooltip(guiGraphics, screenWidth, screenHeight, stack);
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

    private static void renderBookTooltip(GuiGraphics guiGraphics, int screenWidth, int screenHeight, ItemStack stack) {
        int posX = screenWidth / 2 - 12 + 22 + EasyMagic.CONFIG.get(ClientConfig.class).offsetX;
        int posY = screenHeight / 2 + 12 - getFullTooltipHeight(stack) / 2 + EasyMagic.CONFIG.get(ClientConfig.class).offsetY;
        TooltipRenderHelper.renderTooltip(guiGraphics, posX, posY, stack);
    }

    private static int getFullTooltipHeight(ItemStack stack) {
        List<ClientTooltipComponent> components = TooltipRenderHelper.getTooltip(stack);
        int height = components.size() == 1 ? -2 : 0;
        height += components.stream().mapToInt(ClientTooltipComponent::getHeight).sum();
        return height;
    }
}
