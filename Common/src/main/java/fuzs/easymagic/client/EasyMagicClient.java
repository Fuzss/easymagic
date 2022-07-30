package fuzs.easymagic.client;

import fuzs.easymagic.client.gui.screens.inventory.ModEnchantmentScreen;
import fuzs.easymagic.client.renderer.blockentity.ModEnchantTableRenderer;
import fuzs.easymagic.init.ModRegistry;
import fuzs.puzzleslib.client.core.ClientModConstructor;

public class EasyMagicClient implements ClientModConstructor {

    @Override
    public void onRegisterBlockEntityRenderers(BlockEntityRenderersContext context) {
        context.registerBlockEntityRenderer(ModRegistry.ENCHANTING_TABLE_BLOCK_ENTITY_TYPE.get(), ModEnchantTableRenderer::new);
    }

    @Override
    public void onRegisterMenuScreens(MenuScreensContext context) {
        context.registerMenuScreen(ModRegistry.ENCHANTMENT_MENU_TYPE.get(), ModEnchantmentScreen::new);
    }
}
