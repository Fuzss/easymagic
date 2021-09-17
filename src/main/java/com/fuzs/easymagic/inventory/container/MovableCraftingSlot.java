package com.fuzs.easymagic.inventory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MovableCraftingSlot extends CraftingResultSlot implements IMovableSlot {

    private boolean visible = false;

    public MovableCraftingSlot(PlayerEntity player, CraftingInventory craftingInventory, IInventory inventoryIn, int slotIndex) {

        super(player, craftingInventory, inventoryIn, slotIndex, 0, 0);
    }

    @Override
    public Slot getSlot() {

        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public final boolean isEnabled() {

        return this.visible;
    }

    @Override
    public void setVisible(boolean visible) {

        this.visible = visible;
    }

}
