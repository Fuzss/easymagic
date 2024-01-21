package fuzs.easymagic.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.config.ClientConfig;
import fuzs.easymagic.config.ServerConfig;
import fuzs.easymagic.util.PlayerExperienceHelper;
import fuzs.easymagic.world.inventory.ModEnchantmentMenu;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ModEnchantmentScreen extends EnchantmentScreen {
    private static final ResourceLocation ENCHANTING_TABLE_LOCATION = new ResourceLocation("textures/gui/container/enchanting_table.png");
    private static final ResourceLocation ENCHANTING_TABLE_REROLL_LOCATION = new ResourceLocation(EasyMagic.MOD_ID, "textures/gui/container/enchanting_table_reroll.png");

    private final List<List<EnchantmentInstance>> slotData = IntStream.range(0, 3).mapToObj(i -> Lists.<EnchantmentInstance>newArrayList()).collect(Collectors.toList());

    public ModEnchantmentScreen(EnchantmentMenu container, Inventory playerInventory, Component textComponent) {
        super(container, playerInventory, textComponent);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float tickDelta, int mouseX, int mouseY) {
        if (!EasyMagic.CONFIG.get(ServerConfig.class).rerollEnchantments || EasyMagic.CONFIG.get(ClientConfig.class).keepEnchantmentScreenBook) {
            super.renderBg(guiGraphics, tickDelta, mouseX, mouseY);
        } else {
            this.renderVanillaBg(guiGraphics, tickDelta, mouseX, mouseY);
            this.renderRerollButton(guiGraphics, tickDelta, mouseX, mouseY);
        }
        if (EasyMagic.CONFIG.get(ServerConfig.class).dedicatedRerollCatalyst) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            guiGraphics.blit(ENCHANTING_TABLE_LOCATION, this.leftPos + 4, this.topPos + 46, 14, 46, 18, 18);
            guiGraphics.blit(ENCHANTING_TABLE_LOCATION, this.leftPos + 22, this.topPos + 46, 34, 46, 18, 18);
            guiGraphics.blit(ENCHANTING_TABLE_REROLL_LOCATION, this.leftPos + 40, this.topPos + 46, 0, 81, 18, 18);
        }
    }

    private void renderVanillaBg(GuiGraphics guiGraphics, float tickDelta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(ENCHANTING_TABLE_LOCATION, k, l, 0, 0, this.imageWidth, this.imageHeight);
        EnchantmentNames.getInstance().initSeed(this.menu.getEnchantmentSeed());
        int q = this.menu.getGoldCount();

        for (int r = 0; r < 3; ++r) {
            int s = k + 60;
            int t = s + 20;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int u = this.menu.costs[r];
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            if (u == 0) {
                guiGraphics.blit(ENCHANTING_TABLE_LOCATION, s, l + 14 + 19 * r, 0, 185, 108, 19);
            } else {
                String string = String.valueOf(u);
                int v = 86 - this.font.width(string);
                FormattedText formattedText = EnchantmentNames.getInstance().getRandomName(this.font, v);
                int w = 6839882;
                if ((q < r + 1 || this.minecraft.player.experienceLevel < u) && !this.minecraft.player.getAbilities().instabuild) {
                    guiGraphics.blit(ENCHANTING_TABLE_LOCATION, s, l + 14 + 19 * r, 0, 185, 108, 19);
                    guiGraphics.blit(ENCHANTING_TABLE_LOCATION, s + 1, l + 15 + 19 * r, 16 * r, 239, 16, 16);
                    guiGraphics.drawWordWrap(this.font, formattedText, t, l + 16 + 19 * r, v, (w & 16711422) >> 1);
                    w = 4226832;
                } else {
                    int x = mouseX - (k + 60);
                    int y = mouseY - (l + 14 + 19 * r);
                    if (x >= 0 && y >= 0 && x < 108 && y < 19) {
                        guiGraphics.blit(ENCHANTING_TABLE_LOCATION, s, l + 14 + 19 * r, 0, 204, 108, 19);
                        w = 16777088;
                    } else {
                        guiGraphics.blit(ENCHANTING_TABLE_LOCATION, s, l + 14 + 19 * r, 0, 166, 108, 19);
                    }

                    guiGraphics.blit(ENCHANTING_TABLE_LOCATION, s + 1, l + 15 + 19 * r, 16 * r, 223, 16, 16);
                    guiGraphics.drawWordWrap(this.font, formattedText, t, l + 16 + 19 * r, v, w);
                    w = 8453920;
                }

                guiGraphics.drawString(this.font, string, t + 86 - this.font.width(string), l + 16 + 19 * r + 7, w);
            }
        }
    }

    private void renderRerollButton(GuiGraphics guiGraphics, float tickDelta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int experienceCost = EasyMagic.CONFIG.get(ServerConfig.class).rerollExperiencePointsCost;
        int lapisCost = EasyMagic.CONFIG.get(ServerConfig.class).rerollCatalystCost;
        boolean invalid = !this.minecraft.player.getAbilities().instabuild && (PlayerExperienceHelper.getTotalExperience(this.minecraft.player) < experienceCost || this.getMenu().getRerollCatalystCount() < lapisCost);
        int buttonX = this.leftPos + (EasyMagic.CONFIG.get(ServerConfig.class).dedicatedRerollCatalyst ? 12 : 14);
        int buttonY = this.topPos + 16;
        boolean hovered = this.isMouseOverReroll(mouseX, mouseY);
        boolean canUseReroll = this.getMenu().canUseReroll();
        guiGraphics.blit(ENCHANTING_TABLE_REROLL_LOCATION, buttonX, buttonY, 0, !canUseReroll || invalid ? 0 : hovered ? 54 : 27, 38, 27);
        // don't render anything but the background just like vanilla for enchanting slots
        if (canUseReroll) {
            if (experienceCost == 0 && lapisCost == 0) {
                // arrow circle
                guiGraphics.blit(ENCHANTING_TABLE_REROLL_LOCATION, buttonX + 12, buttonY + 6, 64, invalid ? 0 : hovered ? 30 : 15, 15, 15);
            } else {
                // arrow circle
                guiGraphics.blit(ENCHANTING_TABLE_REROLL_LOCATION, buttonX + 3, buttonY + 6, 64, invalid ? 0 : hovered ? 30 : 15, 15, 15);
                if (experienceCost > 0 && lapisCost > 0) {
                    // level orb
                    this.renderCostOrb(guiGraphics, buttonX + (experienceCost > 9 ? 17 : 20), buttonY + 13, 38, invalid ? 39 : 0, experienceCost, invalid ? ChatFormatting.RED : ChatFormatting.GREEN);
                    // lapis orb
                    this.renderCostOrb(guiGraphics, buttonX + (lapisCost > 9 ? 17 : 20), buttonY + 1, 51, invalid ? 39 : 0, lapisCost, invalid ? ChatFormatting.RED : ChatFormatting.BLUE);
                } else if (experienceCost > 0) {
                    // level orb
                    this.renderCostOrb(guiGraphics, buttonX + (experienceCost > 9 ? 17 : 20), buttonY + 7, 38, invalid ? 39 : 0, experienceCost, invalid ? ChatFormatting.RED : ChatFormatting.GREEN);
                } else if (lapisCost > 0) {
                    // lapis orb
                    this.renderCostOrb(guiGraphics, buttonX + (lapisCost > 9 ? 17 : 20), buttonY + 7, 51, invalid ? 39 : 0, lapisCost, invalid ? ChatFormatting.RED : ChatFormatting.BLUE);
                }
            }
        }
    }

    private void renderCostOrb(GuiGraphics guiGraphics, int posX, int posY, int textureX, int textureY, int cost, ChatFormatting color) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(ENCHANTING_TABLE_REROLL_LOCATION, posX, posY, textureX, textureY + Math.min(2, cost / 5) * 13, 13, 13);
        this.renderReadableText(guiGraphics, posX + 8, posY + 3, String.valueOf(cost), color.getColor());
    }

    private void renderReadableText(GuiGraphics guiGraphics, int posX, int posY, String text, int color) {
        // render shadow on every side to avoid readability issues with colorful background
        guiGraphics.drawString(this.font, text, posX - 1, posY, 0, false);
        guiGraphics.drawString(this.font, text, posX + 1, posY, 0, false);
        guiGraphics.drawString(this.font, text, posX, posY - 1, 0, false);
        guiGraphics.drawString(this.font, text, posX, posY + 1, 0, false);
        guiGraphics.drawString(this.font, text, posX, posY, color, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (EasyMagic.CONFIG.get(ServerConfig.class).rerollEnchantments) {
            if (this.isMouseOverReroll((int) mouseX, (int) mouseY)) {
                if (this.menu.clickMenuButton(this.minecraft.player, 4)) {
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 4);
                    // only play this locally
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ENCHANTMENT_TABLE_USE, 1.0F));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        // rendering of vanilla tooltip is canceled in #isPointInRegion when this is true
        int slot = this.getEnchantingSlot(mouseX, mouseY);
        if (slot != -1) {
            if (!this.slotData.get(slot).isEmpty()) {
                List<Component> tooltip = Lists.newArrayList();
                boolean hasValidEnchantment = this.gatherSlotEnchantmentsTooltip(this.slotData.get(slot), tooltip);
                this.gatherSlotCostsTooltip(slot, tooltip, hasValidEnchantment);
                guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            }
        } else if (EasyMagic.CONFIG.get(ServerConfig.class).rerollEnchantments) {
            if (this.isMouseOverReroll(mouseX, mouseY) && this.getMenu().canUseReroll()) {
                List<Component> tooltip = Lists.newArrayList();
                this.gatherRerollTooltip(tooltip);
                guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            }
        }
    }

    private boolean isMouseOverReroll(int mouseX, int mouseY) {
        final int startX = this.leftPos + (EasyMagic.CONFIG.get(ServerConfig.class).dedicatedRerollCatalyst ? 12 : 14);
        final int startY = this.topPos + 16;
        return startX < mouseX && mouseX <= startX + 38 && startY < mouseY && mouseY <= startY + 27;
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

    private boolean gatherSlotEnchantmentsTooltip(List<EnchantmentInstance> slotData, List<Component> slotTooltip) {
        // don't add forge text later when tooltip is purposefully empty
        boolean hasValidEnchantment = slotData.isEmpty();
        for (EnchantmentInstance data : slotData) {
            if (data.enchantment != null) {
                if (EasyMagic.CONFIG.get(ServerConfig.class).enchantmentHint == ServerConfig.EnchantmentHint.ALL) {
                    slotTooltip.add(((MutableComponent) data.enchantment.getFullname(data.level)).withStyle(ChatFormatting.GRAY));
                } else {
                    slotTooltip.add((Component.translatable("container.enchant.clue", data.enchantment.getFullname(data.level))).withStyle(ChatFormatting.GRAY));
                }
                hasValidEnchantment = true;
            }
        }
        return hasValidEnchantment;
    }

    private void gatherSlotCostsTooltip(int slot, List<Component> slotTooltip, boolean hasValidEnchantment) {
        List<Component> additionalTooltip = Lists.newArrayList();
        if (ModLoaderEnvironment.INSTANCE.getModLoader().isForge() && !hasValidEnchantment) {
            additionalTooltip.add(Component.translatable("forge.container.enchant.limitedEnchantability").withStyle(ChatFormatting.RED));
        } else if (!this.minecraft.player.getAbilities().instabuild) {
            int enchantLevels = this.menu.costs[slot];
            if (this.minecraft.player.experienceLevel < enchantLevels) {
                additionalTooltip.add((Component.translatable("container.enchant.level.requirement", enchantLevels)).withStyle(ChatFormatting.RED));
            } else {
                getEnchantingComponent(slot + 1, this.menu.getGoldCount(), "container.enchant.lapis.one", "container.enchant.lapis.many").ifPresent(additionalTooltip::add);
                getEnchantingComponent(slot + 1, this.minecraft.player.experienceLevel, "container.enchant.level.one", "container.enchant.level.many").ifPresent(additionalTooltip::add);
            }
        }
        if (!additionalTooltip.isEmpty()) {
            if (!slotTooltip.isEmpty()) {
                slotTooltip.add(Component.empty());
            }
            slotTooltip.addAll(additionalTooltip);
        }
    }

    private void gatherRerollTooltip(List<Component> tooltip) {
        tooltip.add(Component.translatable("container.enchant.reroll").withStyle(ChatFormatting.GRAY));
        List<Component> additionalTooltip = Lists.newArrayList();
        if (!this.minecraft.player.getAbilities().instabuild) {
            int rerollCatalystCost = EasyMagic.CONFIG.get(ServerConfig.class).rerollCatalystCost;
            if (EasyMagic.CONFIG.get(ServerConfig.class).dedicatedRerollCatalyst) {
                MutableComponent component = Component.literal(rerollCatalystCost + " ").append(Items.AMETHYST_SHARD.getDescription());
                getEnchantingComponent(rerollCatalystCost, this.getMenu().getRerollCatalystCount(), component).ifPresent(additionalTooltip::add);
            } else {
                getEnchantingComponent(rerollCatalystCost, this.getMenu().getRerollCatalystCount(), "container.enchant.lapis.one", "container.enchant.lapis.many").ifPresent(additionalTooltip::add);
            }
            getEnchantingComponent(EasyMagic.CONFIG.get(ServerConfig.class).rerollExperiencePointsCost, PlayerExperienceHelper.getTotalExperience(this.minecraft.player), "container.enchant.experience.one", "container.enchant.experience.many").ifPresent(additionalTooltip::add);
        }
        if (!additionalTooltip.isEmpty()) {
            tooltip.add(Component.empty());
            tooltip.addAll(additionalTooltip);
        }
    }

    private static Optional<Component> getEnchantingComponent(int requiredAmount, int currentAmount, String singleKey, String manyKey) {
        if (requiredAmount < 1) return Optional.empty();
        MutableComponent component;
        if (requiredAmount == 1) {
            component = Component.translatable(singleKey);
        } else {
            component = Component.translatable(manyKey, requiredAmount);
        }
        return getEnchantingComponent(requiredAmount, currentAmount, component);
    }

    private static Optional<Component> getEnchantingComponent(int requiredAmount, int currentAmount, MutableComponent component) {
        if (requiredAmount < 1) return Optional.empty();
        return Optional.of(component.withStyle(currentAmount >= requiredAmount ? ChatFormatting.GRAY : ChatFormatting.RED));
    }

    @Override
    public ModEnchantmentMenu getMenu() {
        return (ModEnchantmentMenu) super.getMenu();
    }
}
