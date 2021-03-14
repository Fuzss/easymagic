package com.fuzs.easymagic.client.gui.screen;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.screen.EnchantmentScreen;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EnchantmentInventoryScreen extends EnchantmentScreen {

    private final List<List<ITextComponent>> slotTooltips = IntStream.range(0, 3)
            .mapToObj(i -> Lists.<ITextComponent>newArrayList())
            .collect(Collectors.toList());

    public EnchantmentInventoryScreen(EnchantmentContainer container, PlayerInventory playerInventory, ITextComponent textComponent) {

        super(container, playerInventory, textComponent);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {

        super.render(mouseX, mouseY, partialTicks);

        // rendering of vanilla tooltip is canceled in #isPointInRegion when this is true
        if (this.showAllEnchantments()) {

            int slot = this.getEnchantingSlot(mouseX, mouseY);
            if (slot != -1 && !this.slotTooltips.get(slot).isEmpty()) {

                List<String> tooltip = this.slotTooltips.get(slot).stream()
                        .map(ITextComponent::getFormattedText)
                        .collect(Collectors.toList());

                this.renderTooltip(tooltip, mouseX, mouseY);
            }
        }
    }

    @Override
    protected boolean isPointInRegion(int x, int y, int width, int height, double mouseX, double mouseY) {

        // small hack to prevent EnchantmentScreen::render from rendering vanilla enchanting tooltips
        if (!this.showAllEnchantments() || this.getSelectedSlot(mouseX, mouseY) != null) {

            return super.isPointInRegion(x, y, width, height, mouseX, mouseY);
        }

        return false;
    }

    @Nullable
    private Slot getSelectedSlot(double mouseX, double mouseY) {

        for (int i = 0; i < this.container.inventorySlots.size(); ++i) {

            Slot slot = this.container.inventorySlots.get(i);
            if (this.isSlotSelected(slot, mouseX, mouseY) && slot.isEnabled()) {

                return slot;
            }
        }

        return null;
    }

    private boolean isSlotSelected(Slot slotIn, double mouseX, double mouseY) {

        return super.isPointInRegion(slotIn.xPos, slotIn.yPos, 16, 16, mouseX, mouseY);
    }

    private boolean showAllEnchantments() {

        return this.slotTooltips.stream().mapToInt(List::size).sum() > 0;
    }

    private int getEnchantingSlot(int mouseX, int mouseY) {

        for (int j = 0; j < 3; ++j) {

            int levels = this.container.enchantLevels[j];
            // coordinates are off, but the rest of the tooltip is computed using the same coordinates
            if (super.isPointInRegion(60, 14 + 19 * j, 108, 17, mouseX, mouseY) && levels > 0) {

                return j;
            }
        }

        return -1;
    }

    public void setSlotTooltip(int slot, List<EnchantmentData> slotData) {

        List<ITextComponent> slotTooltip = this.slotTooltips.get(slot);
        slotTooltip.clear();
        this.addSlotEnchantments(slot, slotData, slotTooltip);
    }

    private void addSlotEnchantments(int slot, List<EnchantmentData> slotData, List<ITextComponent> slotTooltip) {

        boolean isEnchantmentPresent = false;
        for (EnchantmentData data : slotData) {

            slotTooltip.add((new TranslationTextComponent("container.enchant.clue", data.enchantment == null ? "" : data.enchantment.getDisplayName(data.enchantmentLevel))).applyTextStyle(TextFormatting.WHITE));
            if (data.enchantment != null) {

                isEnchantmentPresent = true;
            }
        }

        this.addSlotInfo(slot, slotTooltip, isEnchantmentPresent);
    }

    @SuppressWarnings("ConstantConditions")
    private void addSlotInfo(int slot, List<ITextComponent> slotTooltip, boolean isEnchantmentPresent) {

        if (!isEnchantmentPresent) {

            slotTooltip.add(new StringTextComponent(""));
            slotTooltip.add(new TranslationTextComponent("forge.container.enchant.limitedEnchantability").applyTextStyle(TextFormatting.RED));
        } else if (!this.minecraft.player.abilities.isCreativeMode) {

            slotTooltip.add(new StringTextComponent(""));
            int enchantLevels = this.container.enchantLevels[slot];
            if (this.minecraft.player.experienceLevel < enchantLevels) {

                slotTooltip.add((new TranslationTextComponent("container.enchant.level.requirement", enchantLevels)).applyTextStyle(TextFormatting.RED));
            } else {

                slot++;
                this.addLapisComponent(slot, slotTooltip);
                this.addLevelComponent(slot, slotTooltip);
            }
        }
    }

    private void addLapisComponent(int slot, List<ITextComponent> slotTooltip) {

        ITextComponent lapisComponent;
        if (slot == 1) {

            lapisComponent = new TranslationTextComponent("container.enchant.lapis.one");
        } else {

            lapisComponent = new TranslationTextComponent("container.enchant.lapis.many", slot);
        }

        slotTooltip.add(lapisComponent.applyTextStyle(this.container.getLapisAmount() >= slot ? TextFormatting.GRAY : TextFormatting.RED));
    }

    private void addLevelComponent(int slot, List<ITextComponent> slotTooltip) {

        ITextComponent levelComponent;
        if (slot == 1) {

            levelComponent = new TranslationTextComponent("container.enchant.level.one");
        } else {

            levelComponent = new TranslationTextComponent("container.enchant.level.many", slot);
        }

        slotTooltip.add(levelComponent.applyTextStyle(TextFormatting.GRAY));
    }

}
