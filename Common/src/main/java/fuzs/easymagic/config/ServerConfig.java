package fuzs.easymagic.config;

import fuzs.puzzleslib.config.ConfigCore;
import fuzs.puzzleslib.config.annotation.Config;

public class ServerConfig implements ConfigCore {
    @Config(description = {"Add a button in the enchanting screen to allow for re-rolling enchantments.", "This costs experience levels as well as lapis lazuli, or can be free when the costs are set to 0."})
    public boolean rerollEnchantments = true;
    @Config(description = {"Amount of lapis lazuli taken as a cost for re-rolling enchantments. Set to 0 to disable this kind of cost.", "Requires the re-rolling option to be enabled."})
    @Config.IntRange(min = 0, max = 64)
    public int rerollLapisLazuliCost = 1;
    @Config(description = {"Amount of experience points (not enchantment levels) taken as a cost for re-rolling enchantments. Set to 0 to disable this kind of cost.", "Requires the re-rolling option to be enabled."})
    @Config.IntRange(min = 0)
    public int rerollExperiencePointsCost = 5;
    @Config(description = "Choose how many enchantments are shown on the enchanting tooltip, if any at all.")
    public EnchantmentHint enchantmentHint = EnchantmentHint.SINGLE;
    @Config(description = "Amount of bookshelves required to perform enchantments at the highest level.")
    @Config.IntRange(min = 0)
    public int maxEnchantingPower = 15;
    @Config(description = "Blocks without a collision shape (e.g. grass or torches) do not block bookshelves placed behind from counting towards current enchanting power.")
    public boolean lenientBookshelves = true;

    public enum EnchantmentHint {
        NONE, SINGLE, ALL
    }
}