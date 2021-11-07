package com.fuzs.easymagic.inventory.container;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ContainerTabType<T extends ContainerTab> extends ForgeRegistryEntry<ContainerTabType<T>> {

    private static final BiMap<ResourceLocation, ContainerTabType<?>> FACTORIES = HashBiMap.create();

    private final ContainerTabType.IContainerTabProvider<T> factory;

    public static <T extends ContainerTab> ContainerTabType<T> registerFactory(ResourceLocation key, ContainerTabType.IContainerTabProvider<T> factory) {

        ContainerTabType<T> newType = new ContainerTabType<>(factory);
        ContainerTabType<?> oldType = FACTORIES.put(key, newType);
        if (oldType != null) {

            throw new IllegalStateException("Duplicate registration for " + key);
        }

        return newType;
    }

    public static ContainerTabType<?> getValue(ResourceLocation key) {

        return FACTORIES.get(key);
    }

    public static ResourceLocation getKey(ContainerTabType<?> type) {

        return FACTORIES.inverse().get(type);
    }

    public ContainerTabType(ContainerTabType.IContainerTabProvider<T> factory) {

        this.factory = factory;
    }

    public T create(int windowId, PlayerEntity player, Container container) {

        return this.factory.create(windowId, player, container);
    }

    @FunctionalInterface
    public interface IContainerTabProvider<T extends ContainerTab> {

        T create(int windowId, PlayerEntity player, Container container);

    }

}
