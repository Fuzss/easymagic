package fuzs.easymagic.client;

import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.client.gui.screens.inventory.ModEnchantmentScreen;
import fuzs.easymagic.client.renderer.blockentity.ModEnchantTableRenderer;
import fuzs.easymagic.registry.ModRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = EasyMagic.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EasyMagicClient {
    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent evt) {
        MenuScreens.register(ModRegistry.ENCHANTMENT_MENU_TYPE.get(), ModEnchantmentScreen::new);
        BlockEntityRenderers.register(ModRegistry.ENCHANTING_TABLE_BLOCK_ENTITY_TYPE.get(), ModEnchantTableRenderer::new);
    }
}
