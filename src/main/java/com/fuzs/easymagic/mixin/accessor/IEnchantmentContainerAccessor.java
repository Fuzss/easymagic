package com.fuzs.easymagic.mixin.accessor;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.EnchantmentContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EnchantmentContainer.class)
public interface IEnchantmentContainerAccessor {

    @Accessor
    void setTableInventory(IInventory tableInventory);

}
