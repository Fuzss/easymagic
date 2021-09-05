package com.fuzs.easymagic.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class LineTabWidget extends TabWidget {

    private ITextComponent[][] tabContent = new ITextComponent[0][];

    public LineTabWidget(TabSide tabSide, int color, ITextComponent title) {

        super(tabSide, color, title);
    }

    @Override
    protected void renderTabContents(MatrixStack matrixStack, int x, int y, int currentPage) {

        for (int i = 0; i < this.tabContent[currentPage].length; i++, y += this.minecraft.fontRenderer.FONT_HEIGHT + 1) {

            this.minecraft.fontRenderer.func_243248_b(matrixStack, this.tabContent[currentPage][i], x, y, 2039583);
        }
    }

    public void setLineContent(ITextComponent... tabContent) {

        this.setLineContent(new ITextComponent[][]{tabContent});
    }

    public void setLineContent(ITextComponent[][] tabContent) {

        int maxWidth = this.getMaxTextWidth();
        for (int i = 0; i < tabContent.length; i++) {

            for (int j = 0; j < tabContent[i].length; j++) {

                tabContent[i][j] = new StringTextComponent(this.minecraft.fontRenderer.func_238417_a_(tabContent[i][j], maxWidth).getString());
            }
        }

        this.tabContent = tabContent;
        this.notifyContentChanged();
    }

    @Override
    protected int getPageCount() {

        return this.tabContent.length;
    }

    @Override
    protected int getLineCount(int currentPage) {

        return this.tabContent[currentPage].length;
    }

}
