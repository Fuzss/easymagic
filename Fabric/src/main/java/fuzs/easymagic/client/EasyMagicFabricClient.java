package fuzs.easymagic.client;

import fuzs.easymagic.EasyMagic;
import fuzs.puzzleslib.client.core.ClientCoreServices;
import net.fabricmc.api.ClientModInitializer;

public class EasyMagicFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientCoreServices.FACTORIES.clientModConstructor(EasyMagic.MOD_ID).accept(new EasyMagicClient());
    }
}
