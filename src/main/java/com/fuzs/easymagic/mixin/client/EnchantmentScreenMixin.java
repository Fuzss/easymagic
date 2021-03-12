package com.fuzs.easymagic.mixin.client;

import com.fuzs.easymagic.EasyMagic;
import com.fuzs.easymagic.element.EasyEnchantingElement;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.EnchantmentScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("unused")
@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin extends ContainerScreen<EnchantmentContainer> {

    @Shadow
    @Final
    private Random random;

    @Unique
    private final List<List<EnchantmentData>> slotEnchantments = new ArrayList<>(3);
    @Unique
    private ItemStack lastStack;

    public EnchantmentScreenMixin(EnchantmentContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {

        super(screenContainer, inv, titleIn);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0))
    public boolean add(List<ITextComponent> list, Object component, MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {

        EasyEnchantingElement element = (EasyEnchantingElement) EasyMagic.EASY_ENCHANTING;
        if (!element.isEnabled() || !element.extension.allEnchantments) {

            return list.add((ITextComponent) component);
        }

        this.updateEnchantmentData();
        this.addEnchantingClues(list, mouseX, mouseY);

        return !list.isEmpty();
    }

    @Unique
    private void addEnchantingClues(List<ITextComponent> toAddTo, int mouseX, int mouseY) {

        int slot = this.getEnchantingSlot(mouseX, mouseY);
        boolean isBook = this.lastStack.getItem() == Items.BOOK;
        toAddTo.add(new StringTextComponent("").append(isBook ? new ItemStack(Items.ENCHANTED_BOOK).getDisplayName() : this.lastStack.getDisplayName()).mergeStyle(EasyEnchantingElement.getFutureRarity(this.lastStack).color));
        for (EnchantmentData data : this.slotEnchantments.get(slot)) {

            toAddTo.add((new TranslationTextComponent("container.enchant.clue", data.enchantment == null ? "" : data.enchantment.getDisplayName(data.enchantmentLevel))).mergeStyle(TextFormatting.WHITE));
        }
    }

    @Unique
    private void updateEnchantmentData() {

        Slot slotIn = this.container.inventorySlots.get(0);
        if (this.lastStack == null || !ItemStack.areItemStacksEqual(slotIn.getStack(), this.lastStack)) {

            this.lastStack = slotIn.getStack();
            this.slotEnchantments.clear();
            for (int i = 0; i < 3; i++) {

                int availableLevels = this.container.enchantLevels[i];
                this.slotEnchantments.add(this.getEnchantmentList(slotIn.getStack(), i, availableLevels));
            }
        }
    }

    @Unique
    private List<EnchantmentData> getEnchantmentList(ItemStack stack, int enchantSlot, int level) {

        // getXpSeed
        this.random.setSeed(this.container.func_217005_f() + enchantSlot);
        List<EnchantmentData> list = EnchantmentHelper.buildEnchantmentList(this.random, stack, level, false);
        // normal Minecraft behavior to make enchanted books less op
        if (stack.getItem() == Items.BOOK && list.size() > 1) {

            list.remove(this.random.nextInt(list.size()));
        }

        return list;
    }

    @Unique
    private int getEnchantingSlot(int mouseX, int mouseY) {

        for (int j = 0; j < 3; ++j) {

            int levels = this.container.enchantLevels[j];
            // coordinates are off, but the rest of the tooltip is computed using the same coordinates
            if (this.isPointInRegion(60, 14 + 19 * j, 108, 17, mouseX, mouseY) && levels > 0) {

                return j;
            }
        }

        return -1;
    }

}
