package fuzs.easymagic.client;

import fuzs.easymagic.client.gui.screens.inventory.ModEnchantmentScreen;
import fuzs.easymagic.client.handler.BlockModelHandler;
import fuzs.easymagic.client.handler.ChiseledBookshelfTooltipHandler;
import fuzs.easymagic.client.renderer.blockentity.ModEnchantTableRenderer;
import fuzs.easymagic.init.ModRegistry;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.BlockEntityRenderersContext;
import fuzs.puzzleslib.api.client.core.v1.context.MenuScreensContext;
import fuzs.puzzleslib.api.client.event.v1.ModelEvents;
import fuzs.puzzleslib.api.client.event.v1.renderer.RenderGuiCallback;

public class EasyMagicClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerHandlers();
    }

    private static void registerHandlers() {
        RenderGuiCallback.EVENT.register(ChiseledBookshelfTooltipHandler::tryRenderBookTooltip);
        ModelEvents.MODIFY_UNBAKED_MODEL.register(BlockModelHandler::onModifyUnbakedModel);
    }

    @Override
    public void onRegisterMenuScreens(MenuScreensContext context) {
        context.registerMenuScreen(ModRegistry.ENCHANTMENT_MENU_TYPE.value(), ModEnchantmentScreen::new);
    }

    @Override
    public void onRegisterBlockEntityRenderers(BlockEntityRenderersContext context) {
        context.registerBlockEntityRenderer(ModRegistry.ENCHANTING_TABLE_BLOCK_ENTITY_TYPE.value(), ModEnchantTableRenderer::new);
    }
}
