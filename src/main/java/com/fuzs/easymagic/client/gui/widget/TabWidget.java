package com.fuzs.easymagic.client.gui.widget;

import com.fuzs.easymagic.EasyMagic;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;

public abstract class TabWidget extends Widget implements ITickable {

    private static final ResourceLocation INFO_TAB_LOCATION = new ResourceLocation(EasyMagic.MODID, "textures/gui/tab.png");
    private static final int TAB_WIDTH = 22;
    private static final int TAB_HEIGHT = 24;

    protected final Minecraft minecraft = Minecraft.getInstance();
    protected final int textIndent = 3;
    private final int defaultWidth = 112;
    private final TabSide tabSide;
    private final int color;
    private final ImageButton[] pageControls;

    private ItemStack itemIcon = ItemStack.EMPTY;
    private Pair<ResourceLocation, ResourceLocation> atlasIcon;

    private int prevWidth;
    private int prevHeight;
    private int targetWidth;
    private int targetHeight;
    private int currentPage;

    public TabWidget(TabSide tabSide, int color, ITextComponent title) {

        super(tabSide.getXPos(TAB_WIDTH), tabSide.getDefaultYPos(TAB_HEIGHT), TAB_WIDTH, TAB_HEIGHT, title);
        this.tabSide = tabSide;
        this.color = color;
        this.pageControls = this.getPageControls();
        this.collapse(true);
    }

    private ImageButton[] getPageControls() {

        ImageButton[] pageControls = new ImageButton[2];
        pageControls[0] = new ImageButton(0, 0, 11, 11, 44, 0, 11, INFO_TAB_LOCATION, button -> this.adjustCurrentPage(-1));
        pageControls[1] = new ImageButton(0, 0, 11, 11, 55, 0, 11, INFO_TAB_LOCATION, button -> this.adjustCurrentPage(1));

        return pageControls;
    }

    private void adjustCurrentPage(int amount) {

        final int pageCount = this.getPageCount();
        amount %= pageCount;
        this.currentPage = (this.currentPage + amount + pageCount) % pageCount;
        this.expand(true);
    }

    public void copyData(TabWidget other) {

        this.currentPage = Math.max(0, Math.min(other.currentPage, this.getPageCount() - 1));
        if (other.isExpanded()) {

            this.expand(true);
            this.tabSide.collapseOthers(this, true);
        }
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {

        final int visualWidth = Math.round(MathHelper.lerp(partialTicks, this.prevWidth, this.width));
        final int visualHeight = Math.round(MathHelper.lerp(partialTicks, this.prevHeight, this.height));
        final int visualX = this.tabSide.getXPos(visualWidth);
        final int visualY = this.tabSide.getVisualYPos(TAB_HEIGHT, partialTicks);

        if (this.isHovered() && this.isDoneAnimating(visualWidth, visualHeight) && this.isCollapsed()) {

            this.renderToolTip(matrixStack, mouseX, mouseY);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.color4f((this.color >> 16 & 255) / 255.0F, (this.color >> 8 & 255) / 255.0F, (this.color & 255) / 255.0F, 1.0F);

        this.minecraft.getTextureManager().bindTexture(INFO_TAB_LOCATION);
        this.buildTexture(matrixStack, visualX, visualY, visualWidth, visualHeight, this.tabSide.getTextureX(TAB_WIDTH), this.tabSide.getTextureY(), TAB_WIDTH, TAB_HEIGHT, 4);
        this.renderIcon(matrixStack, visualX, visualY);

        if (this.isDoneAnimating(visualWidth, visualHeight) && this.isExpanded()) {

            drawString(matrixStack, this.minecraft.fontRenderer, this.getMessage(), visualX + this.tabSide.getLeftOffset() + 16 + this.textIndent, visualY + this.tabSide.getTopOffset() + this.textIndent + 2, 16777215);
            if (this.getPageCount() > 0) {

                if (this.isPaged()) {

                    this.renderPages(matrixStack, mouseX, mouseY, partialTicks, visualX + visualWidth / 2, visualY + this.tabSide.getTopOffset() + 16 + this.textIndent);
                }

                this.renderTabContents(matrixStack, visualX + this.tabSide.getLeftOffset() + this.textIndent, visualY + this.tabSide.getTopOffset() + 16 + this.textIndent, this.currentPage);
            }
        }
    }

    private int getVisualBottom(float partialTicks) {

        return this.tabSide.getVisualYPos(TAB_HEIGHT, partialTicks) + Math.round(MathHelper.lerp(partialTicks, this.prevHeight, this.height));
    }

    private void renderIcon(MatrixStack matrixStack, int x, int y) {

        x += this.tabSide.getLeftOffset();
        y += this.tabSide.getTopOffset();
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

    private void renderPages(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, int x, int y) {

        int lineCount = this.getCappedLineCount();
        if (lineCount > 0) {

            y += this.textIndent + this.getContentHeight(lineCount);
        }

        this.renderPageControls(matrixStack, mouseX, mouseY, partialTicks, x, y);
        ITextComponent pageNumber = new StringTextComponent(String.format("%s / %s", this.currentPage + 1, this.getPageCount()));
        drawCenteredString(matrixStack, this.minecraft.fontRenderer, pageNumber, x, y + 2, 16777215);
    }

    private void renderPageControls(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, int x, int y) {

        final int buttonOffset = 20;
        this.pageControls[0].setPosition(x - 11 - buttonOffset, y);
        this.pageControls[0].render(matrixStack, mouseX, mouseY, partialTicks);
        this.pageControls[1].setPosition(x - 1 + buttonOffset, y);
        this.pageControls[1].render(matrixStack, mouseX, mouseY, partialTicks);
    }

    protected abstract void renderTabContents(MatrixStack matrixStack, int x, int y, int currentPage);

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

    protected void notifyContentChanged() {

        if (this.isExpanded()) {

            this.expand();
        }
    }

    private int getMinExpandedWidth() {

        return this.tabSide.getLeftOffset() + 16 + this.textIndent + this.minecraft.fontRenderer.getStringPropertyWidth(this.getMessage()) + this.textIndent + this.tabSide.getRightOffset();
    }

    private int getMaxExpandedWidth() {

        final int borderDistance = 16;
        return (this.tabSide.screen.width - this.tabSide.screen.getXSize()) / 2 - borderDistance;
    }

    private int getExpandedWidth(int defaultWidth) {

        return Math.min(Math.max(defaultWidth, this.getMinExpandedWidth()), this.getMaxExpandedWidth());
    }

    protected int getMaxTextWidth() {

        return this.getExpandedWidth(this.defaultWidth) - this.tabSide.getLeftOffset() - this.textIndent * 2 - this.tabSide.getRightOffset();
    }

    private int getMinExpandedHeight(boolean isPaged) {

        int minHeight = this.tabSide.getTopOffset() + 16 + this.tabSide.getBottomOffset();
        if (isPaged) {

            minHeight += this.minecraft.fontRenderer.FONT_HEIGHT + 2 + this.textIndent;
        }

        return minHeight;
    }

    private int getMaxExpandedHeight() {

        return this.tabSide.screen.getYSize() - (this.tabSide.getDefaultYPos(TAB_HEIGHT) - this.tabSide.screen.getGuiTop()) - 4 - (TAB_HEIGHT + 1) * this.tabSide.getTabsBelow();
    }

    private int getExpandedHeight(int lineCount) {

        int minHeight = this.getMinExpandedHeight(this.isPaged());
        if (lineCount > 0) {

            minHeight += this.getContentHeight(lineCount) + 2 * this.textIndent;
        }

        return minHeight;
    }

    private int getContentHeight(int lineCount) {

        return (this.minecraft.fontRenderer.FONT_HEIGHT + 1) * lineCount - 1;
    }

    protected int getMaxLineCount(boolean isPaged) {

        return (this.getMaxExpandedHeight() - this.getMinExpandedHeight(isPaged) - this.textIndent * 2 - 1) / (this.minecraft.fontRenderer.FONT_HEIGHT + 1);
    }

    protected abstract int getLineCount(int currentPage);

    protected int getCappedLineCount() {

        return Math.min(this.getMaxLineCount(this.isPaged()), this.getLineCount(this.currentPage));
    }

    protected abstract int getPageCount();

    private boolean isPaged() {

        return this.getPageCount() > 1;
    }

    private void setRawDimensions(int width, int height) {

        this.width = width;
        this.height = height;
        this.prevWidth = width;
        this.prevHeight = height;
        this.targetWidth = width;
        this.targetHeight = height;
    }

    private void setTargetDimensions(int width, int height) {

        this.targetWidth = width;
        this.targetHeight = height;
    }

    private void collapse(boolean immediate) {

        if (immediate) {

            this.setRawDimensions(TAB_WIDTH, TAB_HEIGHT);
        } else {

            this.setTargetDimensions(TAB_WIDTH, TAB_HEIGHT);
        }
    }

    private void expand(boolean immediate) {

        int width = this.getExpandedWidth(this.defaultWidth);
        int height = this.getExpandedHeight(this.getCappedLineCount());
        if (immediate) {

            this.setRawDimensions(width, height);
        } else {

            this.setTargetDimensions(width, height);
        }
    }

    private void collapse() {

        this.collapse(false);
    }

    private void expand() {

        this.expand(false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        for (Button pageControl : this.pageControls) {

            if (pageControl.mouseClicked(mouseX, mouseY, button)) {

                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {

        if (this.isCollapsed()) {

            this.expand();
            this.tabSide.collapseOthers(this, false);
        } else {

            this.collapse();
        }
    }

    public boolean isCollapsed() {

        return this.targetWidth == TAB_WIDTH && this.targetHeight == TAB_HEIGHT;
    }

    public boolean isExpanded() {

        return !this.isCollapsed();
    }

    private boolean isDoneAnimating(int width, int height) {

        return width == this.targetWidth && height == this.targetHeight;
    }

    @Override
    public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {

        this.tabSide.screen.renderTooltip(matrixStack, this.getMessage(), mouseX, mouseY);
    }

    @Override
    public void tick() {

        this.prevWidth = this.width;
        this.prevHeight = this.height;
        final int updateAmount = 24;
        this.width = Math.round(this.nextAnimationSize(this.width, this.targetWidth, updateAmount));
        this.height = Math.round(this.nextAnimationSize(this.height, this.targetHeight, updateAmount));
        this.x = this.tabSide.getXPos(this.width);
        this.y = this.tabSide.getYPos(TAB_HEIGHT);
        this.tickPageControls();
    }

    private void tickPageControls() {

        for (ImageButton pageControl : this.pageControls) {

            pageControl.visible = this.isPaged() && this.isDoneAnimating(this.width, this.height);
        }
    }

    private float nextAnimationSize(int size, int targetSize, float updateAmount) {

        if (size != targetSize) {

            return MathHelper.clamp(size + Math.signum(targetSize - size) * updateAmount, Math.min(size, targetSize), Math.max(size, targetSize));
        }

        return size;
    }

    public static class TabSide {

        public final ContainerScreen<?> screen;
        private final boolean right;
        private final int ownIndex;
        private final TabWidget[] siblings;

        public TabSide(ContainerScreen<?> screen, boolean right, int ownIndex, TabWidget[] siblings) {

            this.screen = screen;
            this.right = right;
            this.ownIndex = ownIndex;
            this.siblings = siblings;
        }

        public int getXPos(int tabWidth) {

            if (this.right) {

                return (this.screen.width + this.screen.getXSize()) / 2;
            }

            return (this.screen.width - this.screen.getXSize()) / 2 - tabWidth;
        }

        public int getYPos(int defaultTabHeight) {

            if (this.getTabAbove() == null) {

                return this.getDefaultYPos(defaultTabHeight);
            }

            return this.getTabAbove().y + this.getTabAbove().height + 1;
        }

        public int getVisualYPos(int defaultTabHeight, float partialTicks) {

            if (this.getTabAbove() == null) {

                return this.getDefaultYPos(defaultTabHeight);
            }

            return this.getTabAbove().getVisualBottom(partialTicks) + 1;
        }

        public int getDefaultYPos(int defaultTabHeight) {

            return (this.screen.height - this.screen.getYSize()) / 2 + 4 + this.ownIndex * (defaultTabHeight + 1);
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

        public void collapseOthers(TabWidget tab, boolean immediate) {

            for (TabWidget sibling : this.siblings) {

                if (sibling != tab) {

                    sibling.collapse(immediate);
                }
            }
        }

        public int getTabsBelow() {

            return this.siblings.length - 1 - this.ownIndex;
        }

        @Nullable
        private TabWidget getTabAbove() {

            return this.ownIndex == 0 ? null : this.siblings[this.ownIndex - 1];
        }

    }

}
