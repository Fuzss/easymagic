package com.fuzs.easymagic.client.gui.screen;

import com.fuzs.easymagic.client.gui.widget.TabWidget;
import com.google.common.collect.Maps;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface ITabbedScreen {

    Map<ResourceLocation, TabWidget> GLOBAL_TABS = Maps.newHashMap();

    default void tickTabs() {

        Stream.concat(this.getLeftTabs().stream(), this.getRightTabs().stream())
                .forEach(TabWidget::tick);
    }

    default TabWidget createLeftTab(String tabIdentifier, TabWidget.Builder tabBuilder) {

        TabWidget tabWidget = tabBuilder.build(TabWidget.TabSide.left(this.getScreen(), this.getLeftTabs()));
        this.copyOldData(tabIdentifier, tabWidget);
        return tabWidget;
    }

    default TabWidget createRightTab(String tabIdentifier, TabWidget.Builder tabBuilder) {

        TabWidget tabWidget = tabBuilder.build(TabWidget.TabSide.right(this.getScreen(), this.getRightTabs()));
        this.copyOldData(tabIdentifier, tabWidget);
        return tabWidget;
    }

    default void copyOldData(String tabIdentifier, TabWidget tabWidget) {

        TabWidget oldTabWidget = GLOBAL_TABS.put(this.getUniqueTabIdentifier(tabIdentifier), tabWidget);
        if (oldTabWidget != null) {

            tabWidget.copyData(oldTabWidget);
        }
    }

    default ResourceLocation getUniqueTabIdentifier(String tabIdentifier) {

        return new ModelResourceLocation(this.getScreenIdentifier(), tabIdentifier);
    }

    List<TabWidget> getLeftTabs();

    List<TabWidget> getRightTabs();

    ContainerScreen<?> getScreen();

    ResourceLocation getScreenIdentifier();

}
