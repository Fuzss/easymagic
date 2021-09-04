package com.fuzs.easymagic.client.gui.widget;

import com.fuzs.easymagic.EasyMagic;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IBidiRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ExpandableTabWidget extends Widget {

    private static final ResourceLocation INFO_TAB_LOCATION = new ResourceLocation(EasyMagic.MODID, "textures/gui/tab.png");
    private static final int TAB_WIDTH = 22;
    private static final int TAB_HEIGHT = 24;

    private final Minecraft minecraft = Minecraft.getInstance();
    private final int textIndent = 3;
    private final int defaultWidth = 100;
    private final ContainerScreen<?> parent;
    private final Side side;
    private final int color;

    private ItemStack itemIcon = ItemStack.EMPTY;
    private Pair<ResourceLocation, ResourceLocation> atlasIcon;

    private IBidiRenderer tabContentRenderer = IBidiRenderer.field_243257_a;
    private int prevWidth;
    private int prevHeight;
    private int targetWidth;
    private int targetHeight;

    public ExpandableTabWidget(ContainerScreen<?> parent, Side side, int color, ITextComponent title) {

        super(side.getXPos(parent, TAB_WIDTH), side.getYPos(parent, TAB_HEIGHT), TAB_WIDTH, TAB_HEIGHT, title);
        this.parent = parent;
        this.side = side;
        this.color = color;
        this.setRawDimensions(TAB_WIDTH, TAB_HEIGHT);
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {

        if (this.isHovered() && !this.isExpanded()) {

            this.renderToolTip(matrixStack, mouseX, mouseY);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        RenderSystem.color4f((this.color >> 16 & 255) / 255.0F, (this.color >> 8 & 255) / 255.0F, (this.color & 255) / 255.0F, 1.0F);
        final int width = Math.round(MathHelper.lerp(partialTicks, this.prevWidth, this.width));
        final int height = Math.round(MathHelper.lerp(partialTicks, this.prevHeight, this.height));
        final int x = this.side.getXPos(this.parent, width);
        final int y = this.side.getYPos(this.parent, TAB_HEIGHT);

        this.minecraft.getTextureManager().bindTexture(INFO_TAB_LOCATION);
        this.buildTexture(matrixStack, x, y, width, height, this.side.getTextureX(TAB_WIDTH), this.side.getTextureY(), TAB_WIDTH, TAB_HEIGHT, 4);
        this.renderIcon(matrixStack, x, y);
        if (this.isVisuallyExpanded(width, height)) {

            drawString(matrixStack, this.minecraft.fontRenderer, this.getMessage(), x + this.side.getLeftOffset() + 16 + this.textIndent, y + this.side.getTopOffset() + this.textIndent + 2, 16777215);
            this.renderTabContents(matrixStack, x, y);
        }
    }

    private void renderIcon(MatrixStack matrixStack, int x, int y) {

        x += this.side.getLeftOffset();
        y += this.side.getTopOffset();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (!this.itemIcon.isEmpty()) {

            this.minecraft.getItemRenderer().renderItemAndEffectIntoGuiWithoutEntity(this.itemIcon, x, y);
        } else if (this.atlasIcon != null) {

            TextureAtlasSprite textureatlassprite = this.minecraft.getAtlasSpriteGetter(this.atlasIcon.getFirst()).apply(this.atlasIcon.getSecond());
            this.minecraft.getTextureManager().bindTexture(textureatlassprite.getAtlasTexture().getTextureLocation());
            blit(matrixStack, x, y, this.getBlitOffset(), 16, 16, textureatlassprite);

        } else {

            throw new RuntimeException("missing icon");
        }
    }

    private void renderTabContents(MatrixStack matrixStack, int x, int y) {

        this.tabContentRenderer.func_241866_c(matrixStack, x + this.side.getLeftOffset() + this.textIndent, y + this.side.getTopOffset() + 16 + this.textIndent, this.minecraft.fontRenderer.FONT_HEIGHT + 1, 2039583);
    }

    public void setAtlasIcon(ResourceLocation atlasLocation, ResourceLocation spriteLocation) {

        this.atlasIcon = Pair.of(atlasLocation, spriteLocation);
    }

    public void setItemIcon(Item item) {

        this.itemIcon = new ItemStack(item);
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

    public void setTabContent(String tabContent) {

        this.tabContentRenderer = IBidiRenderer.func_243259_a(this.minecraft.fontRenderer, new StringTextComponent(tabContent), this.getMaxTextWidth(), this.getMaxLineCount());
        if (!this.isCollapsing()) {

            this.updateDimensions(true);
        }
    }

    private int getMinExpandedWidth() {

        return this.side.getLeftOffset() + 16 + this.textIndent + this.minecraft.fontRenderer.getStringPropertyWidth(this.getMessage()) + this.textIndent + this.side.getRightOffset();
    }

    private int getMaxExpandedWidth() {

        final int borderDistance = 20;
        return (this.parent.width - this.parent.getXSize()) / 2 - borderDistance;
    }

    private int getExpandedWidth(int defaultWidth) {

        return Math.min(Math.max(defaultWidth, this.getMinExpandedWidth()), this.getMaxExpandedWidth());
    }

    private int getMaxTextWidth() {

        return this.getExpandedWidth(this.defaultWidth) - this.side.getLeftOffset() - this.textIndent * 2 - this.side.getRightOffset();
    }

    private int getMinExpandedHeight() {

        return this.side.getTopOffset() + 16 + this.side.getBottomOffset();
    }

    private int getMaxExpandedHeight() {

        return this.parent.getYSize() - (this.side.getYPos(this.parent, TAB_HEIGHT) - this.parent.getGuiTop()) - 4;
    }

    private int getExpandedHeight(int contentLines) {

        int minHeight = this.getMinExpandedHeight();
        if (contentLines > 0) {

            minHeight += (this.minecraft.fontRenderer.FONT_HEIGHT + 1) * contentLines;
            minHeight += 2 * this.textIndent - 1;
        }

        return minHeight;
    }

    private int getMaxLineCount() {

        return (this.getMaxExpandedHeight() - this.getMinExpandedHeight() - this.textIndent * 2 - 1) / (this.minecraft.fontRenderer.FONT_HEIGHT + 1);
    }

    private void setTargetDimensions(int targetWidth, int targetHeight) {

        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
    }

    private void setRawDimensions(int width, int height) {

        this.width = width;
        this.height = height;
        this.prevWidth = width;
        this.prevHeight = height;
        this.targetWidth = width;
        this.targetHeight = height;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {

        this.updateDimensions(this.targetWidth == TAB_WIDTH && this.targetHeight == TAB_HEIGHT);
    }

    private void updateDimensions(boolean expanding) {

        if (expanding) {

            int width = this.getExpandedWidth(this.defaultWidth);
            int lineCount = Math.min(this.getMaxLineCount(), this.tabContentRenderer.func_241862_a());
            int height = this.getExpandedHeight(lineCount);
            this.setTargetDimensions(width, height);
        } else {

            this.setTargetDimensions(TAB_WIDTH, TAB_HEIGHT);
        }
    }

    private boolean isCollapsed() {

        return this.isDoneAnimating() && this.isCollapsing();
    }

    private boolean isExpanded() {

        return this.isDoneAnimating() && !this.isCollapsing();
    }

    private boolean isVisuallyExpanded(int width, int height) {

        return this.isDoneAnimating(width, height) && !this.isCollapsing();
    }

    private boolean isCollapsing() {

        return this.targetWidth == TAB_WIDTH && this.targetHeight == TAB_HEIGHT;
    }

    private boolean isDoneAnimating() {

        return this.isDoneAnimating(this.width, this.height);
    }

    private boolean isDoneAnimating(int width, int height) {

        return width == this.targetWidth && height == this.targetHeight;
    }

    @Override
    public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {

        this.parent.renderTooltip(matrixStack, this.getMessage(), mouseX, mouseY);
    }

    public void tick() {

        this.prevWidth = this.width;
        this.prevHeight = this.height;
        final int updateAmount = 24;
        this.width = Math.round(this.nextAnimationSize(this.width, this.targetWidth, updateAmount));
        this.height = Math.round(this.nextAnimationSize(this.height, this.targetHeight, updateAmount));
        this.x = this.side.getXPos(this.parent, this.width);
        this.y = this.side.getYPos(this.parent, TAB_HEIGHT);
    }

    private float nextAnimationSize(int size, int targetSize, float updateAmount) {

        if (size != targetSize) {

            return MathHelper.clamp(size + Math.signum(targetSize - size) * updateAmount, Math.min(size, targetSize), Math.max(size, targetSize));
        }

        return size;
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

        public int getLeftOffset() {

            return this.right ? 2 : 4;
        }

        public int getRightOffset() {

            return this.right ? 4 : 2;
        }

        public int getTopOffset() {

            return 4;
        }

        public int getBottomOffset() {

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
