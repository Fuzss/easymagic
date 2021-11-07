package com.fuzs.easymagic.inventory.container;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.function.BiPredicate;

public enum ContainerTabManager {

    INSTANCE;

    private static final Multimap<Container, ContainerTab> CONTAINER_TAB_INSTANCES = HashMultimap.create();

    private final Map<BiPredicate<Container, PlayerEntity>, IContainerTabProvider> containerToTabProvider = Maps.newHashMap();
    private final Int2ObjectArrayMap<ContainerTab> idToTab = new Int2ObjectArrayMap<>();

    private int lastTabId;

    ContainerTabManager() {

        MinecraftForge.EVENT_BUS.register(this);
    }

    public void register(BiPredicate<Container, PlayerEntity> testContainer, IContainerTabProvider tabProvider) {

        this.containerToTabProvider.put(testContainer, tabProvider);
    }

    @SubscribeEvent
    public void onContainerOpen(final PlayerContainerEvent.Open evt) {

        Container container = evt.getContainer();
        PlayerEntity player = evt.getPlayer();
        for (Map.Entry<BiPredicate<Container, PlayerEntity>, IContainerTabProvider> entry : containerToTabProvider.entrySet()) {

            if (entry.getKey().test(container, player)) {


            }
        }
    }

    @SubscribeEvent
    public void onContainerClose(final PlayerContainerEvent.Close evt) {

        for (ContainerTab containerTab : CONTAINER_TAB_INSTANCES.get(evt.getContainer())) {

            containerTab.onContainerClosed(evt.getPlayer());
            // TODO clear container from instances
        }

        CONTAINER_TAB_INSTANCES.removeAll(evt.getContainer());
    }

    private int getNextTabId() {

        this.lastTabId = this.lastTabId % 100 + 1;
        return this.lastTabId;
    }

    @FunctionalInterface
    public interface IContainerTabProvider {

        Container createMenu(int windowId, PlayerEntity player, Container container);

    }

}
