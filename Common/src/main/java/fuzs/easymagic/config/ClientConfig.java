package fuzs.easymagic.config;

import fuzs.puzzleslib.config.ConfigCore;
import fuzs.puzzleslib.config.annotation.Config;

public class ClientConfig implements ConfigCore {
    @Config(name = "render_enchanting_table_contents", description = "Render inventory contents of an enchanting table.")
    public ItemRenderType renderContentsType = ItemRenderType.FANCY_FLOATING;
    @Config(name = "disappearing_enchanting_table_contents", description = "Makes inventory contents of enchanting tables vanish from rendering when the player gets to far away.")
    public boolean disappearingContents = false;

    public enum ItemRenderType {
        NONE, FLAT, FLOATING, FANCY_FLOATING
    }
}
