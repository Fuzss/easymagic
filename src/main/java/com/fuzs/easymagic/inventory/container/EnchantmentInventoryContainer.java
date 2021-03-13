package com.fuzs.easymagic.inventory.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.util.IWorldPosCallable;

public class EnchantmentInventoryContainer extends EnchantmentContainer {

    public EnchantmentInventoryContainer(int id, PlayerInventory playerInventory) {

        super(id, playerInventory);
    }

    public EnchantmentInventoryContainer(int id, PlayerInventory playerInventory, IWorldPosCallable worldPosCallable) {

        super(id, playerInventory, worldPosCallable);
    }



}
