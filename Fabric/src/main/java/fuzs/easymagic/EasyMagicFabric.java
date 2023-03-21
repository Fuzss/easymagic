package fuzs.easymagic;

import fuzs.puzzleslib.api.core.v1.ModConstructor;
import net.fabricmc.api.ModInitializer;

public class EasyMagicFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModConstructor.construct(EasyMagic.MOD_ID, EasyMagic::new);
    }
}
