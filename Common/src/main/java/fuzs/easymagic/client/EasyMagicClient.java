package fuzs.easymagic.client;

import fuzs.easymagic.client.gui.screens.inventory.ModEnchantmentScreen;
import fuzs.easymagic.client.renderer.blockentity.ModEnchantTableRenderer;
import fuzs.easymagic.init.ModRegistry;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.BlockEntityRenderersContext;
import fuzs.puzzleslib.api.core.v1.context.ModLifecycleContext;
import net.minecraft.client.gui.screens.MenuScreens;

public class EasyMagicClient implements ClientModConstructor {

    @Override
    public void onClientSetup(ModLifecycleContext context) {
        MenuScreens.register(ModRegistry.ENCHANTMENT_MENU_TYPE.get(), ModEnchantmentScreen::new);
    }

    @Override
    public void onRegisterBlockEntityRenderers(BlockEntityRenderersContext context) {
        context.registerBlockEntityRenderer(ModRegistry.ENCHANTING_TABLE_BLOCK_ENTITY_TYPE.get(), ModEnchantTableRenderer::new);
    }
}
