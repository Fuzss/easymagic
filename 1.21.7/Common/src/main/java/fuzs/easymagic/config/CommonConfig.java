package fuzs.easymagic.config;

import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;

public class CommonConfig implements ConfigCore {
    @Config(description = "Leftover vanilla enchanting tables in a world become unusable until they are broken and replaced.")
    public boolean disableVanillaEnchantingTable = true;
    @Config(description = "Replace vanilla enchanting tables created in structures during world generation. Does not affect already generated blocks.")
    public boolean convertVanillaEnchantingTableDuringWorldGen = true;
}
