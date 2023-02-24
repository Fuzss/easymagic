package fuzs.easymagic.client;

import fuzs.easymagic.EasyMagic;
import fuzs.puzzleslib.client.core.ClientFactories;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = EasyMagic.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EasyMagicForgeClient {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ClientFactories.INSTANCE.clientModConstructor(EasyMagic.MOD_ID).accept(new EasyMagicClient());
    }
}
