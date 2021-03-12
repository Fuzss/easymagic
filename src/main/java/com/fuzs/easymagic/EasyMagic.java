package com.fuzs.easymagic;

import com.fuzs.easymagic.element.AnvilTweaksElement;
import com.fuzs.easymagic.element.EasyEnchantingElement;
import com.fuzs.puzzleslib_em.PuzzlesLib;
import com.fuzs.puzzleslib_em.config.ConfigManager;
import com.fuzs.puzzleslib_em.element.AbstractElement;
import com.fuzs.puzzleslib_em.element.registry.ElementRegistry;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings({"WeakerAccess", "unused"})
@Mod(EasyMagic.MODID)
public class EasyMagic extends PuzzlesLib {

    public static final String MODID = "easymagic";
    public static final String NAME = "Easy Magic";
    public static final Logger LOGGER = LogManager.getLogger(EasyMagic.NAME);

    public static final AbstractElement ANVIL_TWEAKS = register("anvil_tweaks", AnvilTweaksElement::new);
    public static final AbstractElement EASY_ENCHANTING = register("easy_enchanting", EasyEnchantingElement::new);

    public EasyMagic() {

        super();
        ElementRegistry.setup(MODID);
        ConfigManager.get().load();
    }

}
