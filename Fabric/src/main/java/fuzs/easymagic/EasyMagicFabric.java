package fuzs.easymagic;

import fuzs.easymagic.init.FabricModRegistry;
import fuzs.puzzleslib.core.CoreServices;
import net.fabricmc.api.ModInitializer;

public class EasyMagicFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        CoreServices.FACTORIES.modConstructor().accept(EasyMagic.INSTANCE);
        FabricModRegistry.touch();
    }
}
