package fuzs.easymagic;

import fuzs.puzzleslib.core.CommonFactories;
import net.fabricmc.api.ModInitializer;

public class EasyMagicFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        CommonFactories.INSTANCE.modConstructor(EasyMagic.MOD_ID).accept(new EasyMagic());
    }
}
