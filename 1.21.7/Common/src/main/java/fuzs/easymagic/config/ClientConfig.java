package fuzs.easymagic.config;

import fuzs.easymagic.EasyMagic;
import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;

public class ClientConfig implements ConfigCore {
    @Config(description = "Render inventory contents of an enchanting table.")
    public ItemRenderType renderEnchantingTableContents = ItemRenderType.FLOATING;
    @Config(description = {"Keep the vanilla book inside the enchantment screen instead of rendering a reroll button.", "The button is simply not visible, rerolling still works by clicking the book. Disabling the reroll feature itself is possible in the server config."})
    public boolean keepEnchantmentScreenBook = false;
    @Config(description = {"Show a tooltip for the hovered book in a chiseled bookshelf.", "DISABLED: Vanilla behavior, no tooltip shows.", "CROUCHING: A tooltip only shows while the player is crouching.", "ENABLED: A tooltip always shows when a book is looked at."})
    public ChiseledBookshelfTooltip chiseledBookshelfTooltip = ChiseledBookshelfTooltip.ENABLED;
    @Config(description = "Offset on x-axis for chiseled bookshelf book tooltips from original position.")
    public int chiseledBookshelfTooltipOffsetX = 0;
    @Config(description = "Offset on y-axis for chiseled bookshelf book tooltips from original position.")
    public int chiseledBookshelfTooltipOffsetY = 0;

    public boolean keepEnchantmentScreenBook() {
        return this.keepEnchantmentScreenBook || !EasyMagic.CONFIG.get(ServerConfig.class).rerollEnchantments;
    }

    public enum ItemRenderType {
        NONE, FLAT, FLOATING
    }

    public enum ChiseledBookshelfTooltip {
        DISABLED, CROUCHING, ENABLED
    }
}
