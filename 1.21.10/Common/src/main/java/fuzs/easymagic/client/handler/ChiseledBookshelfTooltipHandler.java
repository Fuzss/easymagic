package fuzs.easymagic.client.handler;

import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.config.ClientConfig;
import fuzs.puzzleslib.api.client.gui.v2.tooltip.TooltipRenderHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.OptionalInt;

public class ChiseledBookshelfTooltipHandler {

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!canRenderTooltip(minecraft)) return;
        BlockHitResult hitResult = (BlockHitResult) minecraft.hitResult;
        BlockState blockState = minecraft.level.getBlockState(hitResult.getBlockPos());
        if (blockState.getBlock() instanceof ChiseledBookShelfBlock block) {
            OptionalInt hitSlot = block.getHitSlot(hitResult, blockState.getValue(HorizontalDirectionalBlock.FACING));
            if (hitSlot.isPresent()) {
                BooleanProperty property = ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.get(hitSlot.getAsInt());
                if (blockState.getValue(property)) {
                    if (minecraft.level.getBlockEntity(hitResult.getBlockPos()) instanceof ChiseledBookShelfBlockEntity blockEntity) {
                        ItemStack itemStack = blockEntity.getItem(hitSlot.getAsInt());
                        if (!itemStack.isEmpty()) {
                            renderBookTooltip(guiGraphics,
                                    guiGraphics.guiWidth(),
                                    guiGraphics.guiHeight(),
                                    itemStack,
                                    minecraft.font);
                        }
                    }
                }
            }
        }
    }

    private static boolean canRenderTooltip(Minecraft minecraft) {
        if (EasyMagic.CONFIG.get(ClientConfig.class).chiseledBookshelfTooltip
                == ClientConfig.ChiseledBookshelfTooltip.DISABLED) {
            return false;
        }
        if (!minecraft.options.hideGui && minecraft.options.getCameraType().isFirstPerson()) {
            if (minecraft.gameMode != null && minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR
                    && minecraft.getCameraEntity() instanceof Player player) {
                if (minecraft.hitResult != null && minecraft.hitResult.getType() == HitResult.Type.BLOCK) {
                    return player.isShiftKeyDown() || EasyMagic.CONFIG.get(ClientConfig.class).chiseledBookshelfTooltip
                            == ClientConfig.ChiseledBookshelfTooltip.ENABLED;
                }
            }
        }

        return false;
    }

    private static void renderBookTooltip(GuiGraphics guiGraphics, int screenWidth, int screenHeight, ItemStack itemStack, Font font) {
        List<ClientTooltipComponent> components = TooltipRenderHelper.getTooltip(itemStack, TooltipFlag.NORMAL);
        int posX = screenWidth / 2 - 12 + 22 + EasyMagic.CONFIG.get(ClientConfig.class).chiseledBookshelfTooltipOffsetX;
        int posY = screenHeight / 2 + 12 - getFullTooltipHeight(components, font) / 2 + EasyMagic.CONFIG.get(
                ClientConfig.class).chiseledBookshelfTooltipOffsetY;
        TooltipRenderHelper.renderTooltipComponents(guiGraphics, posX, posY, components, null);
    }

    private static int getFullTooltipHeight(List<ClientTooltipComponent> components, Font font) {
        int height = components.size() == 1 ? -2 : 0;
        height += components.stream().mapToInt((ClientTooltipComponent component) -> {
            return component.getHeight(font);
        }).sum();
        return height;
    }
}
