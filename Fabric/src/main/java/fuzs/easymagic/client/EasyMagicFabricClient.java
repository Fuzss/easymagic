package fuzs.easymagic.client;

import fuzs.easymagic.EasyMagic;
import fuzs.puzzleslib.client.core.ClientFactories;
import net.fabricmc.api.ClientModInitializer;

public class EasyMagicFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientFactories.INSTANCE.clientModConstructor(EasyMagic.MOD_ID).accept(new EasyMagicClient());
    }
}
