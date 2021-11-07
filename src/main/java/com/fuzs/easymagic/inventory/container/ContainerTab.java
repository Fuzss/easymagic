package com.fuzs.easymagic.inventory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public abstract class ContainerTab extends Container {

    private final PlayerEntity player;
    private Container container;

    protected ContainerTab(int id, PlayerInventory playerInventory) {

        this(id, playerInventory.player, null);
    }

    protected ContainerTab(int id, PlayerEntity player, @Nullable Container container) {

        super(null, id);
        this.player = player;
        this.container = container;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {

        return this.container.canInteractWith(playerIn);
    }

    public PlayerEntity getPlayer() {

        return this.player;
    }

    public Container getContainer() {

        return this.container;
    }

    @OnlyIn(Dist.CLIENT)
    public void setContainer(Container container) {

        this.container = container;
    }

}
