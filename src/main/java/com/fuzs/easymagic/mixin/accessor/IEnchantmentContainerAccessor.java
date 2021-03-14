package com.fuzs.easymagic.mixin.accessor;

import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Random;

@Mixin(EnchantmentContainer.class)
public interface IEnchantmentContainerAccessor {

    @Accessor
    void setTableInventory(IInventory tableInventory);

    @Accessor
    IInventory getTableInventory();

    @Accessor
    IntReferenceHolder getXpSeed();

    @Accessor
    Random getRand();

    @Accessor(value = "field_217006_g")
    IWorldPosCallable getWorldPosCallable();

    @Invoker
    List<EnchantmentData> callGetEnchantmentList(ItemStack stack, int enchantSlot, int level);

}
