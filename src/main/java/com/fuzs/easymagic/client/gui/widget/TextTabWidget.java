package com.fuzs.easymagic.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IBidiRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.util.text.ITextComponent;

import java.util.stream.Stream;

public class TextTabWidget extends TabWidget {

    private IBidiRenderer[] tabContentRenderers = new IBidiRenderer[0];

    public TextTabWidget(ContainerScreen<?> parent, Side side, int color, ITextComponent title) {

        super(parent, side, color, title);
    }

    @Override
    protected void renderTabContents(MatrixStack matrixStack, int x, int y, int currentPage) {

        this.tabContentRenderers[currentPage].func_241866_c(matrixStack, x, y, this.minecraft.fontRenderer.FONT_HEIGHT + 1, 2039583);
    }

    public void setTextContent(ITextComponent... tabContent) {

        boolean isPaged = tabContent.length > 1;
        this.tabContentRenderers = Stream.of(tabContent)
                .map(content -> IBidiRenderer.func_243259_a(this.minecraft.fontRenderer, content, this.getMaxTextWidth(), this.getMaxLineCount(isPaged)))
                .toArray(IBidiRenderer[]::new);
        this.markContentChanged();
    }

    @Override
    protected int getPageCount() {

        return this.tabContentRenderers.length;
    }

    @Override
    protected int getLineCount(int currentPage) {

        return this.tabContentRenderers[currentPage].func_241862_a();
    }

}
