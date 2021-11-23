package fuzs.easymagic.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ModEnchantmentScreen extends EnchantmentScreen {
    private final List<List<EnchantmentInstance>> slotData = IntStream.range(0, 3).mapToObj(i -> Lists.<EnchantmentInstance>newArrayList()).collect(Collectors.toList());

    public ModEnchantmentScreen(EnchantmentMenu container, Inventory playerInventory, Component textComponent) {
        super(container, playerInventory, textComponent);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        // rendering of vanilla tooltip is canceled in #isPointInRegion when this is true
        int slot = this.getEnchantingSlot(mouseX, mouseY);
        if (slot != -1 && !this.slotData.get(slot).isEmpty()) {
            List<Component> tooltip = Lists.newArrayList();
            this.addSlotEnchantments(slot, this.slotData.get(slot), tooltip);
            this.renderComponentTooltip(matrixStack, tooltip, mouseX, mouseY);
        }
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        // small hack to prevent EnchantmentScreen::render from rendering vanilla enchanting tooltips
        if (this.getSelectedSlot(mouseX, mouseY) != null) {
            return super.isHovering(x, y, width, height, mouseX, mouseY);
        }
        return false;
    }

    @Nullable
    private Slot getSelectedSlot(double mouseX, double mouseY) {
        for (int i = 0; i < this.menu.slots.size(); ++i) {
            Slot slot = this.menu.slots.get(i);
            if (this.isSlotSelected(slot, mouseX, mouseY) && slot.isActive()) {
                return slot;
            }
        }
        return null;
    }

    private boolean isSlotSelected(Slot slotIn, double mouseX, double mouseY) {
        return super.isHovering(slotIn.x, slotIn.y, 16, 16, mouseX, mouseY);
    }

    private int getEnchantingSlot(int mouseX, int mouseY) {
        for (int j = 0; j < 3; ++j) {
            int levels = this.menu.costs[j];
            // coordinates are off, but the rest of the tooltip is computed using the same coordinates
            if (super.isHovering(60, 14 + 19 * j, 108, 17, mouseX, mouseY) && levels > 0) {
                return j;
            }
        }
        return -1;
    }

    public void setSlotData(int slot, List<EnchantmentInstance> data) {
        this.slotData.set(slot, data);
    }

    private void addSlotEnchantments(int slot, List<EnchantmentInstance> slotData, List<Component> slotTooltip) {
        // don't add forge text later when tooltip is purposefully empty
        boolean hasValidEnchantment = slotData.isEmpty();
        for (EnchantmentInstance data : slotData) {
            if (data.enchantment != null) {
                slotTooltip.add((new TranslatableComponent("container.enchant.clue", data.enchantment.getFullname(data.level))).withStyle(ChatFormatting.WHITE));
                hasValidEnchantment = true;
            }
        }
        this.addSlotInfo(slot, slotTooltip, hasValidEnchantment);
    }

    @SuppressWarnings("ConstantConditions")
    private void addSlotInfo(int slot, List<Component> slotTooltip, boolean hasValidEnchantment) {
        List<Component> additionalTooltip = Lists.newArrayList();
        if (!hasValidEnchantment) {
            additionalTooltip.add(new TranslatableComponent("forge.container.enchant.limitedEnchantability").withStyle(ChatFormatting.RED));
        } else if (!this.minecraft.player.getAbilities().instabuild) {
            int enchantLevels = this.menu.costs[slot];
            if (this.minecraft.player.experienceLevel < enchantLevels) {
                additionalTooltip.add((new TranslatableComponent("container.enchant.level.requirement", enchantLevels)).withStyle(ChatFormatting.RED));
            } else {
                slot++;
                this.addLapisComponent(slot, additionalTooltip);
                this.addLevelComponent(slot, additionalTooltip);
            }
        }
        if (!additionalTooltip.isEmpty()) {
            if (!slotTooltip.isEmpty()) {
                slotTooltip.add(TextComponent.EMPTY);
            }
            slotTooltip.addAll(additionalTooltip);
        }
    }

    private void addLapisComponent(int slot, List<Component> slotTooltip) {
        MutableComponent lapisComponent;
        if (slot == 1) {
            lapisComponent = new TranslatableComponent("container.enchant.lapis.one");
        } else {
            lapisComponent = new TranslatableComponent("container.enchant.lapis.many", slot);
        }
        slotTooltip.add(lapisComponent.withStyle(this.menu.getGoldCount() >= slot ? ChatFormatting.GRAY : ChatFormatting.RED));
    }

    private void addLevelComponent(int slot, List<Component> slotTooltip) {
        MutableComponent levelComponent;
        if (slot == 1) {
            levelComponent = new TranslatableComponent("container.enchant.level.one");
        } else {
            levelComponent = new TranslatableComponent("container.enchant.level.many", slot);
        }
        slotTooltip.add(levelComponent.withStyle(ChatFormatting.GRAY));
    }
}
