package com.fuzs.easymagic.client.gui.screen;

import net.minecraft.client.gui.screen.EnchantmentScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.util.text.ITextComponent;

public class EnchantmentInventoryScreen extends EnchantmentScreen {

    public EnchantmentInventoryScreen(EnchantmentContainer container, PlayerInventory playerInventory, ITextComponent textComponent) {

        super(container, playerInventory, textComponent);
    }

}
