package fuzs.easymagic;

import fuzs.puzzleslib.core.CoreServices;
import net.fabricmc.api.ModInitializer;

public class EasyMagicFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        CoreServices.FACTORIES.modConstructor(EasyMagic.MOD_ID).accept(new EasyMagic());
    }
}
