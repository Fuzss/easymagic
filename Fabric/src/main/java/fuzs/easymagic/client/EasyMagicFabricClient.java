package fuzs.easymagic.client;

import fuzs.puzzleslib.client.core.ClientCoreServices;
import net.fabricmc.api.ClientModInitializer;

public class EasyMagicFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientCoreServices.FACTORIES.clientModConstructor().accept(EasyMagicClient.INSTANCE);
    }
}
