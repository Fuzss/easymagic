package fuzs.easymagic.fabric.client;

import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.client.EasyMagicClient;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import net.fabricmc.api.ClientModInitializer;

public class EasyMagicFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientModConstructor.construct(EasyMagic.MOD_ID, EasyMagicClient::new);
    }
}
