package fuzs.easymagic.neoforge;

import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.data.ModItemTagsProvider;
import fuzs.easymagic.data.client.ModLanguageProvider;
import fuzs.easymagic.data.client.ModModelProvider;
import fuzs.easymagic.init.ModRegistry;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;

@Mod(EasyMagic.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class EasyMagicNeoForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ModConstructor.construct(EasyMagic.MOD_ID, EasyMagic::new);
        DataProviderHelper.registerDataProviders(EasyMagic.MOD_ID, ModItemTagsProvider::new, ModLanguageProvider::new, ModModelProvider::new);
    }

    @SubscribeEvent
    public static void onCommonSetup(final FMLCommonSetupEvent evt) {
        NeoForgeCapabilityHelper.registerWorldlyBlockEntityContainer(ModRegistry.ENCHANTING_TABLE_BLOCK_ENTITY_TYPE.value());
    }
}
