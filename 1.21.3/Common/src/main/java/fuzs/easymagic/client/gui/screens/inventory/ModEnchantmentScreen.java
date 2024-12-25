package fuzs.easymagic.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.config.ClientConfig;
import fuzs.easymagic.config.ServerConfig;
import fuzs.easymagic.util.PlayerExperienceHelper;
import fuzs.easymagic.world.inventory.ModEnchantmentMenu;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import fuzs.puzzleslib.api.core.v1.utility.ResourceLocationHelper;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ModEnchantmentScreen extends EnchantmentScreen {
    public static final String KEY_ONE_ENCHANT_CATALYST = "container.enchant.lapis.one";
    public static final String KEY_MANY_ENCHANT_CATALYSTS = "container.enchant.lapis.many";
    public static final String KEY_ONE_REROLL_CATALYST = "container.enchant.reroll.one";
    public static final String KEY_MANY_REROLL_CATALYSTS = "container.enchant.reroll.many";
    public static final String KEY_ONE_ENCHANTMENT_LEVEL = "container.enchant.level.one";
    public static final String KEY_MANY_ENCHANTMENT_LEVELS = "container.enchant.level.many";
    public static final String KEY_ONE_EXPERIENCE_POINT = "container.enchant.experience.one";
    public static final String KEY_MANY_EXPERIENCE_POINTS = "container.enchant.experience.many";
    public static final String KEY_REROLL = "container.enchant.reroll";
    private static final ResourceLocation ENCHANTING_TABLE_LOCATION = ResourceLocationHelper.withDefaultNamespace(
            "textures/gui/container/enchanting_table.png");
    private static final ResourceLocation ENCHANTING_TABLE_REROLL_LOCATION = EasyMagic.id(
            "textures/gui/container/enchanting_table_reroll.png");

    public ModEnchantmentScreen(EnchantmentMenu enchantmentMenu, Inventory inventory, Component title) {
        super(enchantmentMenu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float tickDelta, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, tickDelta, mouseX, mouseY);
        if (this.renderRerollButton()) {
            this.renderRerollButton(guiGraphics, tickDelta, mouseX, mouseY);
        }
        if (EasyMagic.CONFIG.get(ServerConfig.class).dedicatedRerollCatalyst) {
            guiGraphics.blit(RenderType::guiTextured,
                    ENCHANTING_TABLE_LOCATION,
                    this.leftPos + 4,
                    this.topPos + 46,
                    14,
                    46,
                    18,
                    18,
                    256,
                    256);
            guiGraphics.blit(RenderType::guiTextured,
                    ENCHANTING_TABLE_LOCATION,
                    this.leftPos + 22,
                    this.topPos + 46,
                    34,
                    46,
                    18,
                    18,
                    256,
                    256);
            guiGraphics.blit(RenderType::guiTextured,
                    ENCHANTING_TABLE_REROLL_LOCATION,
                    this.leftPos + 40,
                    this.topPos + 46,
                    0,
                    81,
                    18,
                    18,
                    256,
                    256);
        }
    }

    private boolean renderRerollButton() {
        return EasyMagic.CONFIG.get(ServerConfig.class).rerollEnchantments &&
                !EasyMagic.CONFIG.get(ClientConfig.class).keepEnchantmentScreenBook;
    }

    @Override
    protected void renderBook(GuiGraphics guiGraphics, int x, int y, float partialTick) {
        if (!this.renderRerollButton()) {
            super.renderBook(guiGraphics, x, y, partialTick);
        }
    }

    private void renderRerollButton(GuiGraphics guiGraphics, float tickDelta, int mouseX, int mouseY) {
        int experienceCost = EasyMagic.CONFIG.get(ServerConfig.class).rerollExperiencePointsCost;
        int lapisCost = EasyMagic.CONFIG.get(ServerConfig.class).rerollCatalystCost;
        boolean invalid = !this.minecraft.player.getAbilities().instabuild &&
                (PlayerExperienceHelper.getTotalExperience(this.minecraft.player) < experienceCost ||
                        this.getMenu().getRerollCatalystCount() < lapisCost);
        int buttonX = this.leftPos + (EasyMagic.CONFIG.get(ServerConfig.class).dedicatedRerollCatalyst ? 12 : 14);
        int buttonY = this.topPos + 16;
        boolean hovered = this.isMouseOverReroll(mouseX, mouseY);
        boolean canUseReroll = this.getMenu().canUseReroll();
        guiGraphics.blit(RenderType::guiTextured,
                ENCHANTING_TABLE_REROLL_LOCATION,
                buttonX,
                buttonY,
                0,
                !canUseReroll || invalid ? 0 : hovered ? 54 : 27,
                38,
                27,
                256,
                256);
        // don't render anything but the background just like vanilla for enchanting slots
        if (canUseReroll) {
            if (experienceCost == 0 && lapisCost == 0) {
                // arrow circle
                guiGraphics.blit(RenderType::guiTextured,
                        ENCHANTING_TABLE_REROLL_LOCATION,
                        buttonX + 12,
                        buttonY + 6,
                        64,
                        invalid ? 0 : hovered ? 30 : 15,
                        15,
                        15,
                        256,
                        256);
            } else {
                // arrow circle
                guiGraphics.blit(RenderType::guiTextured,
                        ENCHANTING_TABLE_REROLL_LOCATION,
                        buttonX + 3,
                        buttonY + 6,
                        64,
                        invalid ? 0 : hovered ? 30 : 15,
                        15,
                        15,
                        256,
                        256);
                if (experienceCost > 0 && lapisCost > 0) {
                    // level orb
                    this.renderCostOrb(guiGraphics,
                            buttonX + (experienceCost > 9 ? 17 : 20),
                            buttonY + 13,
                            38,
                            invalid ? 39 : 0,
                            experienceCost,
                            invalid ? ChatFormatting.RED : ChatFormatting.GREEN);
                    // lapis orb
                    this.renderCostOrb(guiGraphics,
                            buttonX + (lapisCost > 9 ? 17 : 20),
                            buttonY + 1,
                            51,
                            invalid ? 39 : 0,
                            lapisCost,
                            invalid ? ChatFormatting.RED : ChatFormatting.BLUE);
                } else if (experienceCost > 0) {
                    // level orb
                    this.renderCostOrb(guiGraphics,
                            buttonX + (experienceCost > 9 ? 17 : 20),
                            buttonY + 7,
                            38,
                            invalid ? 39 : 0,
                            experienceCost,
                            invalid ? ChatFormatting.RED : ChatFormatting.GREEN);
                } else if (lapisCost > 0) {
                    // lapis orb
                    this.renderCostOrb(guiGraphics,
                            buttonX + (lapisCost > 9 ? 17 : 20),
                            buttonY + 7,
                            51,
                            invalid ? 39 : 0,
                            lapisCost,
                            invalid ? ChatFormatting.RED : ChatFormatting.BLUE);
                }
            }
        }
    }

    private void renderCostOrb(GuiGraphics guiGraphics, int posX, int posY, int textureX, int textureY, int cost, ChatFormatting color) {
        guiGraphics.blit(RenderType::guiTextured,
                ENCHANTING_TABLE_REROLL_LOCATION,
                posX,
                posY,
                textureX,
                textureY + Math.min(2, cost / 5) * 13,
                13,
                13,
                256,
                256);
        // render shadow on every side to avoid readability issues with colorful background
        FormattedCharSequence formattedCharSequence = Component.literal(String.valueOf(cost)).getVisualOrderText();
        guiGraphics.drawSpecial(bufferSource -> {
            this.font.drawInBatch8xOutline(formattedCharSequence,
                    posX + 8,
                    posY + 3,
                    color.getColor(),
                    0,
                    guiGraphics.pose().last().pose(),
                    bufferSource,
                    0XF000F0);
        });
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (EasyMagic.CONFIG.get(ServerConfig.class).rerollEnchantments) {
            if (this.isMouseOverReroll((int) mouseX, (int) mouseY)) {
                if (this.menu.clickMenuButton(this.minecraft.player, 4)) {
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 4);
                    // only play this locally
                    this.minecraft.getSoundManager()
                            .play(SimpleSoundInstance.forUI(SoundEvents.ENCHANTMENT_TABLE_USE, 1.0F));
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
            List<EnchantmentInstance> slotData = this.getSlotData(slot);
            if (!slotData.isEmpty()) {
                List<Component> tooltip = new ArrayList<>();
                boolean hasValidEnchantment = this.gatherSlotEnchantmentsTooltip(slotData, tooltip::add);
                this.gatherSlotCostsTooltip(slot, tooltip, hasValidEnchantment);
                guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            }
        } else if (EasyMagic.CONFIG.get(ServerConfig.class).rerollEnchantments) {
            if (this.isMouseOverReroll(mouseX, mouseY) && this.getMenu().canUseReroll()) {
                List<Component> tooltip = new ArrayList<>();
                this.gatherRerollTooltip(tooltip);
                guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            }
        }
    }

    private List<EnchantmentInstance> getSlotData(int slot) {
        return this.getMenu().getEnchantingData(this.minecraft.level.registryAccess()).get(slot);
    }

    private boolean isMouseOverReroll(int mouseX, int mouseY) {
        final int startX = this.leftPos + (EasyMagic.CONFIG.get(ServerConfig.class).dedicatedRerollCatalyst ? 12 : 14);
        final int startY = this.topPos + 16;
        return startX < mouseX && mouseX <= startX + 38 && startY < mouseY && mouseY <= startY + 27;
    }

    private int getEnchantingSlot(int mouseX, int mouseY) {
        for (int j = 0; j < 3; ++j) {
            int levels = this.menu.costs[j];
            // coordinates are off, but the rest of the tooltip is computed using the same coordinates
            if (this.isHovering(60, 14 + 19 * j, 108, 17, mouseX, mouseY) && levels > 0) {
                return j;
            }
        }
        return -1;
    }

    private boolean gatherSlotEnchantmentsTooltip(List<EnchantmentInstance> slotData, Consumer<Component> tooltipAdder) {
        Object2IntMap<Holder<Enchantment>> enchantments = slotData.stream()
                .collect(Collectors.toMap((EnchantmentInstance enchantmentInstance) -> {
                    return enchantmentInstance.enchantment;
                }, (EnchantmentInstance enchantmentInstance) -> {
                    return enchantmentInstance.level;
                }, (o1, o2) -> o2, Object2IntLinkedOpenHashMap::new));
        HolderSet<Enchantment> holderSet = getTagOrEmpty(this.minecraft.getConnection().registryAccess(),
                Registries.ENCHANTMENT,
                EnchantmentTags.TOOLTIP_ORDER);
        for (Holder<Enchantment> holder : holderSet) {
            int enchantmentLevel = enchantments.getInt(holder);
            if (enchantmentLevel > 0) {
                this.addTooltipLine(holder, enchantmentLevel, tooltipAdder);
            }
        }
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.object2IntEntrySet()) {
            if (!holderSet.contains(entry.getKey())) {
                this.addTooltipLine(entry.getKey(), entry.getIntValue(), tooltipAdder);
            }
        }
        return !slotData.isEmpty();
    }

    private void addTooltipLine(Holder<Enchantment> holder, int enchantmentLevel, Consumer<Component> tooltipAdder) {
        Component component = Component.empty()
                .append(Enchantment.getFullname(holder, enchantmentLevel))
                .withStyle(ChatFormatting.GRAY);
        if (EasyMagic.CONFIG.get(ServerConfig.class).enchantmentHint == ServerConfig.EnchantmentHint.ALL) {
            tooltipAdder.accept(component);
        } else {
            tooltipAdder.accept(Component.translatable("container.enchant.clue", component));
        }
    }

    private static <T> HolderSet<T> getTagOrEmpty(@Nullable HolderLookup.Provider registries, ResourceKey<Registry<T>> registryKey, TagKey<T> key) {
        if (registries != null) {
            Optional<HolderSet.Named<T>> optional = registries.lookupOrThrow(registryKey).get(key);
            if (optional.isPresent()) {
                return optional.get();
            }
        }

        return HolderSet.direct();
    }

    private void gatherSlotCostsTooltip(int slot, List<Component> slotTooltip, boolean hasValidEnchantment) {
        List<Component> additionalTooltip = Lists.newArrayList();
        if (ModLoaderEnvironment.INSTANCE.getModLoader().isNeoForge() && !hasValidEnchantment) {
            additionalTooltip.add(Component.translatable("neoforge.container.enchant.limitedEnchantability")
                    .withStyle(ChatFormatting.RED));
        } else if (!this.minecraft.player.getAbilities().instabuild) {
            int enchantLevels = this.menu.costs[slot];
            if (this.minecraft.player.experienceLevel < enchantLevels) {
                additionalTooltip.add((Component.translatable("container.enchant.level.requirement",
                        enchantLevels)).withStyle(ChatFormatting.RED));
            } else {
                getEnchantingComponent(slot + 1,
                        this.menu.getGoldCount(),
                        KEY_ONE_ENCHANT_CATALYST,
                        KEY_MANY_ENCHANT_CATALYSTS).ifPresent(additionalTooltip::add);
                getEnchantingComponent(slot + 1,
                        this.minecraft.player.experienceLevel,
                        KEY_ONE_ENCHANTMENT_LEVEL,
                        KEY_MANY_ENCHANTMENT_LEVELS).ifPresent(additionalTooltip::add);
            }
        }
        if (!additionalTooltip.isEmpty()) {
            if (!slotTooltip.isEmpty()) {
                slotTooltip.add(CommonComponents.EMPTY);
            }
            slotTooltip.addAll(additionalTooltip);
        }
    }

    private void gatherRerollTooltip(List<Component> tooltip) {
        tooltip.add(Component.translatable(KEY_REROLL).withStyle(ChatFormatting.GRAY));
        List<Component> additionalTooltip = Lists.newArrayList();
        if (!this.minecraft.player.getAbilities().instabuild) {
            int rerollCatalystCost = EasyMagic.CONFIG.get(ServerConfig.class).rerollCatalystCost;
            if (EasyMagic.CONFIG.get(ServerConfig.class).dedicatedRerollCatalyst) {
                getEnchantingComponent(rerollCatalystCost,
                        this.getMenu().getRerollCatalystCount(),
                        KEY_ONE_REROLL_CATALYST,
                        KEY_MANY_REROLL_CATALYSTS).ifPresent(additionalTooltip::add);
            } else {
                getEnchantingComponent(rerollCatalystCost,
                        this.getMenu().getRerollCatalystCount(),
                        KEY_ONE_ENCHANT_CATALYST,
                        KEY_MANY_ENCHANT_CATALYSTS).ifPresent(additionalTooltip::add);
            }
            String singleKey = EasyMagic.CONFIG.get(ServerConfig.class).rerollingTakesEnchantmentLevels ?
                    KEY_ONE_ENCHANTMENT_LEVEL : KEY_ONE_EXPERIENCE_POINT;
            String manyKey = EasyMagic.CONFIG.get(ServerConfig.class).rerollingTakesEnchantmentLevels ?
                    KEY_MANY_ENCHANTMENT_LEVELS : KEY_MANY_EXPERIENCE_POINTS;
            getEnchantingComponent(EasyMagic.CONFIG.get(ServerConfig.class).rerollExperiencePointsCost,
                    PlayerExperienceHelper.getTotalExperience(this.minecraft.player),
                    singleKey,
                    manyKey).ifPresent(additionalTooltip::add);
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
        if (requiredAmount < 1) {
            return Optional.empty();
        } else {
            return Optional.of(component.withStyle(
                    currentAmount >= requiredAmount ? ChatFormatting.GRAY : ChatFormatting.RED));
        }
    }

    @Override
    public ModEnchantmentMenu getMenu() {
        return (ModEnchantmentMenu) super.getMenu();
    }
}
