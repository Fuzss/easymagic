package com.fuzs.easymagic.client.gui.widget;

import com.fuzs.easymagic.EasyMagic;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.function.Supplier;

public class ExpandableTabWidget extends Widget {

    private static final ResourceLocation INFO_TAB_TEXTURES = new ResourceLocation(EasyMagic.MODID, "textures/gui/tab.png");
    private static final int TAB_WIDTH = 22;
    private static final int TAB_HEIGHT = 24;

    private final Minecraft minecraft = Minecraft.getInstance();
    private final int contentOffset = 3;
    private final ContainerScreen<?> parent;
    private final Side side;
    private final int color;
    private final ResourceLocation icon;
    private final Supplier<List<ITextComponent>> tabContent;
    private final int animationTime;

    private boolean isExpanding;
    private int animationTicks;

    public ExpandableTabWidget(ContainerScreen<?> parent, Side side, int color, ITextComponent title, ResourceLocation icon, Supplier<List<ITextComponent>> tabContent) {

        super(side.getXPos(parent, TAB_WIDTH), side.getYPos(parent, TAB_HEIGHT), TAB_WIDTH, TAB_HEIGHT, title);
        this.parent = parent;
        this.side = side;
        this.color = color;
        this.icon = icon;
        this.tabContent = tabContent;
        this.animationTime = this.getFullWidth(100, 8) / 15;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {

        if (this.isHovered()) {

            this.renderToolTip(matrixStack, mouseX, mouseY);
        }

        List<ITextComponent> tabContent = this.tabContent.get();
        this.updateDimensions(partialTicks, this.getFullWidth(100, 8), this.getFullHeight(tabContent));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        RenderSystem.color4f((this.color >> 16 & 255) / 255.0F, (this.color >> 8 & 255) / 255.0F, (this.color & 255) / 255.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(INFO_TAB_TEXTURES);
        this.buildTexture(matrixStack, this.x, this.y, this.width, this.height, this.side.getTextureX(TAB_WIDTH), this.side.getTextureY(), TAB_WIDTH, TAB_HEIGHT, 4);

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(this.icon);
        blit(matrixStack, this.x + this.side.getIconXOffset(), this.y + this.side.getIconYOffset(), this.getBlitOffset(), 0, 0, 16, 16, 16, 16);

        if (this.isExpanded()) {

            drawString(matrixStack, this.minecraft.fontRenderer, this.getMessage(), this.x + this.side.getIconXOffset() + 16 + this.contentOffset, this.y + this.side.getIconYOffset() + this.contentOffset + 2, -1);
            for (int i = 0, posX = this.x + this.side.getIconXOffset() + this.contentOffset, posY = this.y + this.side.getIconYOffset() + 16 + this.contentOffset; i < tabContent.size(); i++, posY += this.minecraft.fontRenderer.FONT_HEIGHT + 1) {

                drawString(matrixStack, this.minecraft.fontRenderer, tabContent.get(i), posX, posY, -1);
            }
        }
    }

    private void buildTexture(MatrixStack matrixStack, int posX, int posY, int width, int height, int textureX, int textureY, int sourceWidth, int sourceHeight, int borderSize) {

        final int midWidth = sourceWidth - borderSize * 2;
        final int midHeight = sourceHeight - borderSize * 2;
        if (midWidth <= 0 || midHeight <= 0) {

            throw new RuntimeException("texture too small for given border size");
        }

        // top left
        this.blit(matrixStack, posX, posY, textureX, textureY, borderSize, borderSize);
        // top right
        this.blit(matrixStack, posX + width - borderSize, posY, textureX + sourceWidth - borderSize, textureY, borderSize, borderSize);
        // bottom left
        this.blit(matrixStack, posX, posY + height - borderSize, textureX, textureY + sourceHeight - borderSize, borderSize, borderSize);
        // bottom right
        this.blit(matrixStack, posX + width - borderSize, posY + height - borderSize, textureX + sourceWidth - borderSize, textureY + sourceHeight - borderSize, borderSize, borderSize);

        // top bar
        for (int drawingX = borderSize; drawingX < width - borderSize; drawingX += midWidth) {

            this.blit(matrixStack, posX + drawingX, posY, textureX + borderSize, textureY, Math.min(midWidth, width - borderSize - drawingX), borderSize);
        }
        // left bar
        for (int drawingY = borderSize; drawingY < height - borderSize; drawingY += midHeight) {

            this.blit(matrixStack, posX, posY + drawingY, textureX, textureY + borderSize, borderSize, Math.min(midHeight, height - borderSize - drawingY));
        }
        // bottom bar
        for (int drawingX = borderSize; drawingX < width - borderSize; drawingX += midWidth) {

            this.blit(matrixStack, posX + drawingX, posY + height - borderSize, textureX + borderSize, textureY + sourceHeight - borderSize, Math.min(midWidth, width - borderSize - drawingX), borderSize);
        }
        // right bar
        for (int drawingY = borderSize; drawingY < height - borderSize; drawingY += midHeight) {

            this.blit(matrixStack, posX + width - borderSize, posY + drawingY, textureX + sourceWidth - borderSize, textureY + borderSize, borderSize, Math.min(midHeight, height - borderSize - drawingY));
        }

        // center
        for (int drawingX = borderSize; drawingX < width - borderSize; drawingX += midWidth) {

            for (int drawingY = borderSize; drawingY < height - borderSize; drawingY += midHeight) {

                this.blit(matrixStack, posX + drawingX, posY + drawingY, textureX + borderSize, textureY + borderSize, Math.min(midWidth, width - borderSize - drawingX), Math.min(midHeight, height - borderSize - drawingY));
            }
        }
    }

    private int getFullWidth(int defaultWidth, int minBorderDistance) {

        int borderDistance = (this.parent.width - this.parent.getXSize()) / 2 - defaultWidth;
        int borderDiff = borderDistance - minBorderDistance;
        if (borderDiff < 0) {

            defaultWidth += borderDiff;
        }

        return defaultWidth;
    }

    private int getFullHeight(List<ITextComponent> tabContent) {

        int height = this.side.getIconYOffset() + 16 + this.contentOffset;
        if (!tabContent.isEmpty()) {

            height += tabContent.size() * (this.minecraft.fontRenderer.FONT_HEIGHT + 1);
            height += this.contentOffset - 1;
        }

        return height + this.side.getIconYOffset();
    }

    private void updateDimensions(float partialTicks, int fullWidth, int fullHeight) {

        if (this.isExpanding) {

            this.updateDimensions(partialTicks, TAB_WIDTH, TAB_HEIGHT, fullWidth, fullHeight);
        } else {

            this.updateDimensions(partialTicks, fullWidth, fullHeight, TAB_WIDTH, TAB_HEIGHT);
        }
    }

    private void updateDimensions(float partialTicks, int widthStart, int heightStart, int widthEnd, int heightEnd) {

        float animationProgress = this.nextAnimationTicks(partialTicks) / this.animationTime;
        if (this.width != widthEnd || this.height != heightEnd) {

            this.width = TAB_WIDTH + (int) (Math.abs(widthStart - widthEnd) * animationProgress);
            this.height = TAB_HEIGHT + (int) (Math.abs(heightStart - heightEnd) * animationProgress);
            this.x = this.side.getXPos(this.parent, this.width);
            this.y = this.side.getYPos(this.parent, this.height);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {

        this.isExpanding = !this.isExpanding;
    }

    private boolean isCollapsed() {

        return !this.isExpanding && this.animationTicks == 0;
    }

    private boolean isExpanded() {

        return this.isExpanding && this.animationTicks == this.animationTime;
    }

    @Override
    public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {

        if (this.isCollapsed()) {

            this.parent.renderTooltip(matrixStack, this.getMessage(), mouseX, mouseY);
        }
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent evt) {

        if (evt.phase == TickEvent.Phase.END) {

            this.animationTicks = (int) this.nextAnimationTicks(1.0F);
        }
    }

    public void remove() {

        MinecraftForge.EVENT_BUS.unregister(this);
    }

    private float nextAnimationTicks(float updateAmount) {

        return this.isExpanding ? Math.min(this.animationTicks + updateAmount, this.animationTime) : Math.max(this.animationTicks - updateAmount, 0);
    }

    public static class Side {

        private final boolean right;
        private final int sideIndex;

        private Side(boolean right, int sideIndex) {

            this.right = right;
            this.sideIndex = sideIndex;
        }

        public int getXPos(ContainerScreen<?> parent, int tabWidth) {

            if (this.right) {

                return (parent.width + parent.getXSize()) / 2;
            }

            return (parent.width - parent.getXSize()) / 2 - tabWidth;
        }

        public int getYPos(ContainerScreen<?> parent, int tabHeight) {

            return (parent.height - parent.getYSize()) / 2 + 4 + this.sideIndex * (tabHeight + 1);
        }

        public int getTextureX(int tabWidth) {

            return this.right ? tabWidth : 0;
        }

        public int getTextureY() {

            return 0;
        }

        public int getIconXOffset() {

            return this.right ? 2 : 4;
        }

        public int getIconYOffset() {

            return 4;
        }

        public static Side left(int sideIndex) {

            return new Side(false, sideIndex);
        }

        public static Side right(int sideIndex) {

            return new Side(true, sideIndex);
        }

    }

}
