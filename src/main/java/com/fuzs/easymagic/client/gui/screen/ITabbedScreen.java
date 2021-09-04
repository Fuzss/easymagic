package com.fuzs.easymagic.client.gui.screen;

import com.fuzs.easymagic.client.gui.widget.TabGroupWidget;

public interface ITabbedScreen {

    TabGroupWidget[] getTabGroups();

    default void tick() {

        for (TabGroupWidget tabGroup : this.getTabGroups()) {

//            tabGroup.tick();
        }
    }

}
