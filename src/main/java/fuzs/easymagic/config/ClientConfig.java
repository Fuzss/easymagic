package fuzs.easymagic.config;

import fuzs.puzzleslib.config.AbstractConfig;
import fuzs.puzzleslib.config.annotation.Config;

public class ClientConfig extends AbstractConfig {
    @Config(name = "render_enchanting_table_contents", description = "Render inventory contents of an enchanting table.")
    public ItemRenderType renderContentsType = ItemRenderType.FANCY_FLOATING;

    public ClientConfig() {
        super("");
    }

    public enum ItemRenderType {
        NONE, FLAT, FLOATING, FANCY_FLOATING
    }
}
