package fuzs.easymagic.config;

import fuzs.puzzleslib.config.ConfigCore;
import fuzs.puzzleslib.config.annotation.Config;

public class ClientConfig implements ConfigCore {
    @Config(name = "render_enchanting_table_contents", description = "Render inventory contents of an enchanting table.")
    public ItemRenderType renderContentsType = ItemRenderType.FLOATING;
    @Config(description = {"Keep the vanilla book inside the enchantment screen instead of rendering a reroll button.", "The button is simply not visible, rerolling still works by clicking the book. Disabling the reroll feature itself is possible in the server config."})
    public boolean keepEnchantmentScreenBook = false;

    public enum ItemRenderType {
        NONE, FLAT, FLOATING
    }
}
