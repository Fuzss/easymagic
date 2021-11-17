package fuzs.easymagic.config;

import fuzs.puzzleslib.config.AbstractConfig;
import fuzs.puzzleslib.config.ConfigHolder;
import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig extends AbstractConfig {
    public boolean itemsStay;
    public boolean reRollEnchantments;
    public ShowEnchantments showEnchantments;
    public int maxPower;
    public boolean lenientBookshelves;
    public boolean filterTable;
    
    public ServerConfig() {
        super("");
    }

    @Override
    protected void addToBuilder(ForgeConfigSpec.Builder builder, ConfigHolder.ConfigCallback saveCallback) {
        saveCallback.accept(builder.comment("Inventory contents stay in their slot after closing the enchanting screen. Also makes hoppers able to input and output items.").define("Inventory Contents Stay", true), v -> this.itemsStay = v);
        saveCallback.accept(builder.comment("Re-roll possible enchantments in an enchanting table every time an item is placed into the enchanting slot.").define("Re-Roll Enchantments", true), v -> this.reRollEnchantments = v);
        saveCallback.accept(builder.comment("Choose how many enchantments are shown on the enchanting tooltip, if any at all.").defineEnum("Show Enchantments", ShowEnchantments.SINGLE), v -> this.showEnchantments = v);
        saveCallback.accept(builder.comment("Amount of bookshelves required to perform enchantments at the highest level.").defineInRange("Enchanting Power", 15, 0, Integer.MAX_VALUE), v -> this.maxPower = v);
        saveCallback.accept(builder.comment("Blocks without a collision shape do not block bookshelves placed behind from counting towards current enchanting power.").define("Lenient Bookshelves", true), v -> this.lenientBookshelves = v);
        saveCallback.accept(builder.comment("Only allow items that can be enchanted to be placed into the enchanting slot of an enchanting table. Useful when inserting items automatically with a hopper.").define("Filter Enchanting Input", false), v -> this.filterTable = v);
    }

    public enum ShowEnchantments {
        NONE, SINGLE, ALL
    }
}