package fuzs.easymagic.client.util;

import com.google.common.collect.Lists;
import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.config.ServerConfig;
import fuzs.easymagic.util.PlayerExperienceHelper;
import fuzs.easymagic.world.inventory.ModEnchantmentMenu;
import fuzs.puzzleslib.api.chat.v1.ComponentHelper;
import fuzs.puzzleslib.api.client.gui.v2.components.tooltip.ClientComponentSplitter;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class EnchantmentTooltipHelper {
    public static final String KEY_ONE_ENCHANT_CATALYST = "container.enchant.lapis.one";
    public static final String KEY_MANY_ENCHANT_CATALYSTS = "container.enchant.lapis.many";
    public static final String KEY_ONE_REROLL_CATALYST = "container.enchant.reroll.one";
    public static final String KEY_MANY_REROLL_CATALYSTS = "container.enchant.reroll.many";
    public static final String KEY_ONE_ENCHANTMENT_LEVEL = "container.enchant.level.one";
    public static final String KEY_MANY_ENCHANTMENT_LEVELS = "container.enchant.level.many";
    public static final String KEY_ONE_EXPERIENCE_POINT = "container.enchant.experience.one";
    public static final String KEY_MANY_EXPERIENCE_POINTS = "container.enchant.experience.many";
    public static final String KEY_REROLL = "container.enchant.reroll";

    private EnchantmentTooltipHelper() {
        // NO-OP
    }

    public static void gatherSlotEnchantmentsTooltip(List<EnchantmentInstance> slotData, Consumer<Component> tooltipAdder, HolderLookup.Provider registries) {
        Object2IntMap<Holder<Enchantment>> enchantments = slotData.stream()
                .collect(Collectors.toMap((EnchantmentInstance enchantmentInstance) -> enchantmentInstance.enchantment,
                        (EnchantmentInstance enchantmentInstance) -> enchantmentInstance.level,
                        (Integer o1, Integer o2) -> o2,
                        Object2IntLinkedOpenHashMap::new));
        HolderSet<Enchantment> holderSet = getTagOrEmpty(registries,
                Registries.ENCHANTMENT,
                EnchantmentTags.TOOLTIP_ORDER);
        for (Holder<Enchantment> holder : holderSet) {
            int enchantmentLevel = enchantments.getInt(holder);
            if (enchantmentLevel > 0) {
                addTooltipLine(holder, enchantmentLevel, tooltipAdder);
            }
        }
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.object2IntEntrySet()) {
            if (!holderSet.contains(entry.getKey())) {
                addTooltipLine(entry.getKey(), entry.getIntValue(), tooltipAdder);
            }
        }
    }

    private static void addTooltipLine(Holder<Enchantment> holder, int enchantmentLevel, Consumer<Component> tooltipAdder) {
        Component enchantmentComponent = Component.empty()
                .append(Enchantment.getFullname(holder, enchantmentLevel))
                .withStyle(ChatFormatting.GRAY);
        if (EasyMagic.CONFIG.get(ServerConfig.class).enchantmentHint == ServerConfig.EnchantmentHint.ALL) {
            tooltipAdder.accept(enchantmentComponent);
        } else {
            tooltipAdder.accept(Component.translatable("container.enchant.clue", enchantmentComponent));
        }

        getEnchantmentDescriptionKey(holder).ifPresent((String translationKey) -> {
            Component descriptionComponent = Component.literal(" \u25C6 ")
                    .append(Component.translatable(translationKey))
                    .withStyle(ChatFormatting.GRAY);
            ClientComponentSplitter.splitTooltipLines(descriptionComponent)
                    .map(ComponentHelper::toComponent)
                    .forEach(tooltipAdder);
        });
    }

    /**
     * Copied from
     * {@link net.minecraft.world.item.enchantment.ItemEnchantments#getTagOrEmpty(HolderLookup.Provider, ResourceKey,
     * TagKey)}.
     */
    private static <T> HolderSet<T> getTagOrEmpty(@Nullable HolderLookup.Provider registries, ResourceKey<Registry<T>> registryKey, TagKey<T> key) {
        if (registries != null) {
            Optional<HolderSet.Named<T>> optional = registries.lookupOrThrow(registryKey).get(key);
            if (optional.isPresent()) {
                return optional.get();
            }
        }

        return HolderSet.direct();
    }

    private static Optional<String> getEnchantmentDescriptionKey(Holder<Enchantment> enchantment) {
        String translationKey = enchantment.unwrapKey().map((ResourceKey<Enchantment> resourceKey) -> {
            return Util.makeDescriptionId(resourceKey.registry().getPath(), resourceKey.location());
        }).orElse(null);
        if (translationKey == null) {
            return Optional.empty();
        } else if (Language.getInstance().has(translationKey + ".desc")) {
            return Optional.of(translationKey + ".desc");
        } else if (Language.getInstance().has(translationKey + ".description")) {
            return Optional.of(translationKey + ".description");
        } else {
            return Optional.empty();
        }
    }

    public static void gatherSlotCostsTooltip(int slotIndex, List<Component> tooltipLines, Player player, ModEnchantmentMenu menu) {
        List<Component> additionalTooltip = Lists.newArrayList();
        if (!player.getAbilities().instabuild) {
            int enchantLevels = menu.costs[slotIndex];
            if (player.experienceLevel < enchantLevels) {
                additionalTooltip.add((Component.translatable("container.enchant.level.requirement",
                        enchantLevels)).withStyle(ChatFormatting.RED));
            } else {
                getEnchantingComponent(slotIndex + 1,
                        menu.getGoldCount(),
                        KEY_ONE_ENCHANT_CATALYST,
                        KEY_MANY_ENCHANT_CATALYSTS).ifPresent(additionalTooltip::add);
                getEnchantingComponent(slotIndex + 1,
                        player.experienceLevel,
                        KEY_ONE_ENCHANTMENT_LEVEL,
                        KEY_MANY_ENCHANTMENT_LEVELS).ifPresent(additionalTooltip::add);
            }
        }
        if (!additionalTooltip.isEmpty()) {
            if (!tooltipLines.isEmpty()) {
                tooltipLines.add(CommonComponents.EMPTY);
            }
            tooltipLines.addAll(additionalTooltip);
        }
    }

    public static void gatherRerollTooltip(List<Component> tooltipLines, Player player, ModEnchantmentMenu menu) {
        tooltipLines.add(Component.translatable(KEY_REROLL).withStyle(ChatFormatting.GRAY));
        List<Component> additionalTooltip = Lists.newArrayList();
        if (!player.getAbilities().instabuild) {
            int rerollCatalystCost = EasyMagic.CONFIG.get(ServerConfig.class).rerollCatalystCost;
            if (EasyMagic.CONFIG.get(ServerConfig.class).dedicatedRerollCatalyst) {
                getEnchantingComponent(rerollCatalystCost,
                        menu.getRerollCatalystCount(),
                        KEY_ONE_REROLL_CATALYST,
                        KEY_MANY_REROLL_CATALYSTS).ifPresent(additionalTooltip::add);
            } else {
                getEnchantingComponent(rerollCatalystCost,
                        menu.getRerollCatalystCount(),
                        KEY_ONE_ENCHANT_CATALYST,
                        KEY_MANY_ENCHANT_CATALYSTS).ifPresent(additionalTooltip::add);
            }
            String singleKey = EasyMagic.CONFIG.get(ServerConfig.class).rerollingTakesEnchantmentLevels ?
                    KEY_ONE_ENCHANTMENT_LEVEL : KEY_ONE_EXPERIENCE_POINT;
            String manyKey = EasyMagic.CONFIG.get(ServerConfig.class).rerollingTakesEnchantmentLevels ?
                    KEY_MANY_ENCHANTMENT_LEVELS : KEY_MANY_EXPERIENCE_POINTS;
            getEnchantingComponent(EasyMagic.CONFIG.get(ServerConfig.class).rerollExperiencePointsCost,
                    PlayerExperienceHelper.getTotalExperience(player),
                    singleKey,
                    manyKey).ifPresent(additionalTooltip::add);
        }
        if (!additionalTooltip.isEmpty()) {
            tooltipLines.add(CommonComponents.EMPTY);
            tooltipLines.addAll(additionalTooltip);
        }
    }

    private static Optional<Component> getEnchantingComponent(int requiredAmount, int currentAmount, String singleKey, String manyKey) {
        if (requiredAmount < 1) {
            return Optional.empty();
        } else {
            MutableComponent component;
            if (requiredAmount == 1) {
                component = Component.translatable(singleKey);
            } else {
                component = Component.translatable(manyKey, requiredAmount);
            }
            return getEnchantingComponent(requiredAmount, currentAmount, component);
        }
    }

    private static Optional<Component> getEnchantingComponent(int requiredAmount, int currentAmount, MutableComponent component) {
        if (requiredAmount < 1) {
            return Optional.empty();
        } else {
            return Optional.of(component.withStyle(
                    currentAmount >= requiredAmount ? ChatFormatting.GRAY : ChatFormatting.RED));
        }
    }
}
