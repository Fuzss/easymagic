package com.fuzs.easymagic.client.gui.widget;

import com.fuzs.easymagic.EasyMagic;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public enum TabManager {

    INSTANCE;

    private static final Map<ContainerType<?>, ScreenManager.IScreenFactory<?, ?>> FACTORIES = Maps.newHashMap();
    private static final Map<ResourceLocation, TabWidget> TAB_INSTANCES = Maps.newHashMap();

    private final Map<ResourceLocation, TabBuilder<?>> leftTabBuilders = Maps.newHashMap();
    private final Map<ResourceLocation, TabBuilder<?>> rightTabBuilders = Maps.newHashMap();
    private final List<ITickable> tickableTabs = Lists.newArrayList();

    TabManager() {

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static <T extends Container> Optional<ScreenManager.IScreenFactory<T, ?>> getScreenFactory(@Nullable ContainerType<T> type) {
        if (type == null) {
            EasyMagic.LOGGER.warn("Trying to open invalid screen");
        } else {
            ScreenManager.IScreenFactory<T, ?> iscreenfactory = getFactory(type);
            if (iscreenfactory == null) {
                EasyMagic.LOGGER.warn("Failed to create screen for menu type: {}", ForgeRegistries.CONTAINERS.getKey(type));
            } else {
                return java.util.Optional.of(iscreenfactory);
            }
        }
        return Optional.empty();
    }

    @Nullable
    private static <T extends Container> ScreenManager.IScreenFactory<T, ?> getFactory(ContainerType<T> type) {
        return (ScreenManager.IScreenFactory<T, ?>)FACTORIES.get(type);
    }

    public static <M extends Container, U extends Screen & IHasContainer<M>> void registerFactory(ContainerType<? extends M> type, ScreenManager.IScreenFactory<M, U> factory) {
        ScreenManager.IScreenFactory<?, ?> iscreenfactory = FACTORIES.put(type, factory);
        if (iscreenfactory != null) {
            throw new IllegalStateException("Duplicate registration for " + ForgeRegistries.CONTAINERS.getKey(type));
        }
    }

    public Optional<TabWidget> getTabInstance(ResourceLocation identifier) {

        return Optional.ofNullable(TAB_INSTANCES.get(identifier));
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
        // use map just for checking duplicate identifiers
        if (this.leftTabBuilders.containsKey(tabIdentifier) || this.rightTabBuilders.containsKey(tabIdentifier)) {

            throw new RuntimeException("duplicate tab identifier " + tabIdentifier);
        }
    }

    private void copyOldData(ResourceLocation tabIdentifier, TabWidget tabWidget) {

        TabWidget oldTabWidget = TAB_INSTANCES.put(tabIdentifier, tabWidget);
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

        for (TabBuilder<?> sortedTabBuilder : sortedTabBuilders) {

            sortedTabBuilder.setup();
        }

        tabBuilders.clear();
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
        @Nullable
        private T builtTab;

        public TabBuilder(ContainerScreen<?> screen, ITabWidgetFactory<T> factory, int color, ITextComponent title) {

            this.screen = screen;
            this.factory = factory;
            this.color = color;
            this.title = title;
        }

        public TabBuilder<T> setItemIcon(Item itemIcon) {

            assert this.atlasIcon == null : "can't have both item and atlas icon";

            this.itemIcon = itemIcon;
            return this;
        }

        public TabBuilder<T> setAtlasIcon(ResourceLocation atlasLocation, ResourceLocation spriteLocation) {

            assert this.itemIcon == null : "can't have both item and atlas icon";

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

            assert this.itemIcon != null || this.atlasIcon != null : "missing tab icon";

            this.builtTab = this.factory.make(tabSide, this.color, this.title);
            if (this.itemIcon != null) {

                this.builtTab.setItemIcon(this.itemIcon);
            }

            if (this.atlasIcon != null) {

                this.builtTab.setAtlasIcon(this.atlasIcon.getFirst(), this.atlasIcon.getSecond());
            }

            return this.builtTab;
        }

        private void setup() {

            assert this.builtTab != null : "trying to setup tab before it was built";

            while (!this.setupQueue.isEmpty()) {

                this.setupQueue.poll().accept(this.builtTab);
            }
        }

    }

    @FunctionalInterface
    public interface ITabWidgetFactory<T extends TabWidget> {

        T make(TabWidget.TabSide tabSide, int color, ITextComponent title);

    }

}
