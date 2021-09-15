package com.fuzs.easymagic.client.gui.screen;

import com.fuzs.easymagic.EasyMagic;
import com.fuzs.easymagic.client.gui.widget.TabWidget;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public enum TabManager {

    INSTANCE;

    private static final Map<ResourceLocation, TabWidget> ALL_TABS = Maps.newHashMap();

    private final Map<ResourceLocation, TabBuilder<?>> leftTabBuilders = Maps.newHashMap();
    private final Map<ResourceLocation, TabBuilder<?>> rightTabBuilders = Maps.newHashMap();
    private final List<ITickable> tickableTabs = Lists.newArrayList();

    TabManager() {

        MinecraftForge.EVENT_BUS.register(this);
    }

    public void registerLeft(ResourceLocation tabIdentifier, Consumer<Widget> addButton, TabBuilder<?> builder) {

        this.registerTab(tabIdentifier, builder, addButton);
        this.leftTabBuilders.put(tabIdentifier, builder);
    }

    public void registerRight(ResourceLocation tabIdentifier, Consumer<Widget> addButton, TabBuilder<?> builder) {

        this.registerTab(tabIdentifier, builder, addButton);
        this.rightTabBuilders.put(tabIdentifier, builder);
    }

    private void registerTab(ResourceLocation tabIdentifier, TabBuilder<?> builder, Consumer<Widget> addButton) {

        builder.addSetupAction(tab -> this.copyOldData(tabIdentifier, tab));
        builder.addSetupAction(addButton::accept);
        if (this.leftTabBuilders.containsKey(tabIdentifier) || this.rightTabBuilders.containsKey(tabIdentifier)) {

            throw new RuntimeException("duplicate tab identifier " + tabIdentifier);
        }
    }

    private void copyOldData(ResourceLocation tabIdentifier, TabWidget tabWidget) {

        TabWidget oldTabWidget = ALL_TABS.put(tabIdentifier, tabWidget);
        if (oldTabWidget != null) {

            tabWidget.copyData(oldTabWidget);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onInitGuiPost(final GuiScreenEvent.InitGuiEvent.Post evt) {

        if (evt.getGui() instanceof ContainerScreen && evt.getGui() == Minecraft.getInstance().currentScreen) {

            this.tickableTabs.clear();
            this.buildTabs(this.leftTabBuilders.values(), (ContainerScreen<?>) evt.getGui(), false);
            this.buildTabs(this.rightTabBuilders.values(), (ContainerScreen<?>) evt.getGui(), true);
            this.leftTabBuilders.clear();
            this.rightTabBuilders.clear();
        }
    }

    private void buildTabs(Collection<TabBuilder<?>> tabBuilders, ContainerScreen<?> screen, boolean right) {

        List<TabBuilder<?>> sortedTabBuilders = tabBuilders.stream()
                .sorted(Collections.reverseOrder(Comparator.comparingInt(TabBuilder::getPriority)))
                .collect(Collectors.toList());
        TabWidget[] tabSiblings = new TabWidget[sortedTabBuilders.size()];
        for (int i = 0; i < sortedTabBuilders.size(); i++) {

            TabWidget tabWidget = sortedTabBuilders.get(i).build(new TabWidget.TabSide(screen, right, i, tabSiblings));
            tabSiblings[i] = tabWidget;
            this.tickableTabs.add(tabWidget);
        }

        for (int i = 0; i < sortedTabBuilders.size(); i++) {

            sortedTabBuilders.get(i).setup(tabSiblings[i]);
        }
    }

    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent evt) {

        if (evt.phase == TickEvent.Phase.END && Minecraft.getInstance().currentScreen instanceof ContainerScreen) {

            this.tickableTabs.forEach(ITickable::tick);
        }
    }

    public static class TabBuilder<T extends TabWidget> {

        private final ContainerScreen<?> screen;
        private final ITabWidgetFactory<T> factory;
        private final int color;
        private final ITextComponent title;
        private final LinkedList<Consumer<T>> setupQueue = Lists.newLinkedList();

        private int priority = 10;
        private Item itemIcon;
        private Pair<ResourceLocation, ResourceLocation> atlasIcon;

        public TabBuilder(ContainerScreen<?> screen, ITabWidgetFactory<T> factory, int color, ITextComponent title) {

            this.screen = screen;
            this.factory = factory;
            this.color = color;
            this.title = title;
        }

        public TabBuilder<T> setItemIcon(Item itemIcon) {

            if (this.atlasIcon != null) {

                throw new RuntimeException("can't have both item and atlas icon");
            }

            this.itemIcon = itemIcon;
            return this;
        }

        public TabBuilder<T> setAtlasIcon(ResourceLocation atlasLocation, ResourceLocation spriteLocation) {

            if (this.itemIcon != null) {

                throw new RuntimeException("can't have both item and atlas icon");
            }

            this.atlasIcon = Pair.of(atlasLocation, spriteLocation);
            return this;
        }

        public TabBuilder<T> addSetupAction(Consumer<T> action) {

            this.setupQueue.offer(action);
            return this;
        }

        public TabBuilder<T> setPriority(int priority) {

            this.priority = priority;
            return this;
        }

        private int getPriority() {

            return this.priority;
        }

        public T build(TabWidget.TabSide tabSide) {

            if (tabSide.screen != this.screen) {

                EasyMagic.LOGGER.warn("tab screen mismatch: {} != {}", tabSide.screen, this.screen);
            }

            if (this.itemIcon == null && this.atlasIcon == null) {

                throw new RuntimeException("missing tab icon");
            }

            T tabWidget = this.factory.make(tabSide, this.color, this.title);
            if (this.itemIcon != null) {

                tabWidget.setItemIcon(this.itemIcon);
            }

            if (this.atlasIcon != null) {

                tabWidget.setAtlasIcon(this.atlasIcon.getFirst(), this.atlasIcon.getSecond());
            }

            return tabWidget;
        }

        private void setup(TabWidget tabWidget) {

            while (!this.setupQueue.isEmpty()) {

                this.setupQueue.poll().accept((T) tabWidget);
            }
        }

    }

    @FunctionalInterface
    public interface ITabWidgetFactory<T extends TabWidget> {

        T make(TabWidget.TabSide tabSide, int color, ITextComponent title);

    }

}
