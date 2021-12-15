package fuzs.easymagic.config;

import fuzs.puzzleslib.config.AbstractConfig;
import fuzs.puzzleslib.config.ConfigHolder;
import fuzs.puzzleslib.config.annotation.Config;
import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig extends AbstractConfig {
    public ReRollEnchantments rerollEnchantments;
    @Config(description = {"Amount of lapis lazuli taken as cost for rerolling enchantments. Set to 0 to disable this kind of cost.", "Requires \"reroll_enchantments\" to be set to \"WITH_COST\"."})
    @Config.IntRange(min = 0, max = 64)
    public int rerollLapisCost = 3;
    @Config(description = {"Amount of enchantment levels taken as cost for rerolling enchantments. Set to 0 to disable this kind of cost.", "Requires \"reroll_enchantments\" to be set to \"WITH_COST\"."})
    @Config.IntRange(min = 0)
    public int rerollLevelCost = 0;
    public ShowEnchantments showEnchantments;
    public int maxPower;
    public boolean lenientBookshelves;
    public boolean filterTable;
    
    public ServerConfig() {
        super("");
    }

    @Override
    protected void addToBuilder(ForgeConfigSpec.Builder builder, ConfigHolder.ConfigCallback saveCallback) {
        saveCallback.accept(builder.comment("Re-roll possible enchantments in an enchanting table every time an item is placed into the enchanting slot.").defineEnum("reroll_enchantments", ReRollEnchantments.FREE), v -> this.rerollEnchantments = v);
        saveCallback.accept(builder.comment("Choose how many enchantments are shown on the enchanting tooltip, if any at all.").defineEnum("show_enchantments", ShowEnchantments.SINGLE), v -> this.showEnchantments = v);
        saveCallback.accept(builder.comment("Amount of bookshelves required to perform enchantments at the highest level.").defineInRange("enchanting_power", 15, 0, Integer.MAX_VALUE), v -> this.maxPower = v);
        saveCallback.accept(builder.comment("Blocks without a collision shape do not block bookshelves placed behind from counting towards current enchanting power.").define("lenient_bookshelves", true), v -> this.lenientBookshelves = v);
        saveCallback.accept(builder.comment("Only allow items that can be enchanted to be placed into the enchanting slot of an enchanting table. Useful when inserting items automatically with a hopper.").define("filter_enchanting_input", false), v -> this.filterTable = v);
    }

    public enum ShowEnchantments {
        NONE, SINGLE, ALL
    }

    public enum ReRollEnchantments {
        FREE, WITH_COST, NEVER
    }
}