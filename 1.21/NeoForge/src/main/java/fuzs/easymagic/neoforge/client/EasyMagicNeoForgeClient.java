package fuzs.easymagic.neoforge.client;

import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.client.EasyMagicClient;
import fuzs.easymagic.data.client.ModLanguageProvider;
import fuzs.easymagic.data.client.ModModelProvider;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = EasyMagic.MOD_ID, dist = Dist.CLIENT)
public class EasyMagicNeoForgeClient {

    public EasyMagicNeoForgeClient() {
        ClientModConstructor.construct(EasyMagic.MOD_ID, EasyMagicClient::new);
        DataProviderHelper.registerDataProviders(EasyMagic.MOD_ID, ModLanguageProvider::new, ModModelProvider::new);
    }
}
