package fuzs.easymagic.config;

import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;

public class ServerConfig implements ConfigCore {
    static final String ENCHANTING_POWER_CATEGORY = "enchanting_power";
    static final String REROLLING_CATEGORY = "rerolling";

    @Config(category = REROLLING_CATEGORY, description = {"Add a button in the enchanting screen to allow for re-rolling enchantments.", "This costs experience levels as well as lapis lazuli, or can be free when the costs are set to 0."})
    public boolean rerollEnchantments = true;
    @Config(category = REROLLING_CATEGORY, description = {"Amount of catalyst item taken as a cost for re-rolling enchantments. Set to 0 to disable this kind of cost.", "The default re-roll catalyst is simply lapis lazuli as defined in 'easymagic:enchanting_catalysts'.", "Requires the re-rolling option to be enabled."})
    @Config.IntRange(min = 0, max = 64)
    public int rerollCatalystCost = 1;
    @Config(category = REROLLING_CATEGORY, description = {"Amount of experience points (not enchantment levels) taken as a cost for re-rolling enchantments. Set to 0 to disable this kind of cost.", "Requires the re-rolling option to be enabled."})
    @Config.IntRange(min = 0)
    public int rerollExperiencePointsCost = 5;
    @Config(category = REROLLING_CATEGORY, description = "Switch re-rolling cost from experience points to full enchantment levels, making re-rolls considerable more expensive.")
    public boolean rerollingTakesEnchantmentLevels = false;
    @Config(description = "Choose how many enchantments are shown on the enchanting tooltip, if any at all.")
    public EnchantmentHint enchantmentHint = EnchantmentHint.SINGLE;
    @Config(category = ENCHANTING_POWER_CATEGORY, description = "Amount of bookshelves required to perform enchantments at the highest level.")
    @Config.IntRange(min = 0, max = 50)
    public int maxEnchantingPower = 15;
    @Config(category = ENCHANTING_POWER_CATEGORY, description = "Blocks without a full collision shape (e.g. torches & carpet) do not block bookshelves placed behind from counting towards current enchanting power.")
    public boolean lenientBookshelves = true;
    @Config(category = REROLLING_CATEGORY, description = {"Catalyst items for re-rolling are defined by the 'easymagic:reroll_catalysts' item tag instead of 'easymagic:enchanting_catalysts' (which includes just lapis lazuli by default).", "Unlocks an additional slot for providing those items in the enchanting table interface."})
    public boolean dedicatedRerollCatalyst = false;
    @Config(category = ENCHANTING_POWER_CATEGORY, description = {"Do chiseled bookshelves provide enchanting power to an enchanting table, one for every three contained books.", "NONE: Vanilla behavior, no power is provided.", "FACING: The bookshelf must face the enchanting table to provide any power.", "ALL: Chiseled bookshelves provide enchanting power regardless of where they are facing."})
    public ChiseledBookshelfPower chiseledBookshelfEnchantingPower = ChiseledBookshelfPower.FACING;

    public boolean dedicatedRerollCatalyst() {
        return this.rerollEnchantments && this.dedicatedRerollCatalyst;
    }

    public enum EnchantmentHint {
        NONE, SINGLE, ALL
    }

    public enum ChiseledBookshelfPower {
        NONE, FACING, ALL
    }
}