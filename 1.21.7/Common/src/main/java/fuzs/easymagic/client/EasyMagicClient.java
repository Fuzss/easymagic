package fuzs.easymagic.client;

import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.client.gui.screens.inventory.ModEnchantmentScreen;
import fuzs.easymagic.client.handler.BlockStateTranslator;
import fuzs.easymagic.client.handler.ChiseledBookshelfTooltipHandler;
import fuzs.easymagic.client.renderer.blockentity.ModEnchantTableRenderer;
import fuzs.easymagic.handler.BlockConversionHandler;
import fuzs.easymagic.init.ModRegistry;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.*;
import fuzs.puzzleslib.api.client.event.v1.gui.RenderTooltipCallback;
import fuzs.puzzleslib.api.client.renderer.v1.model.ModelLoadingHelper;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

public class EasyMagicClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        RenderTooltipCallback.EVENT.register(ModEnchantmentScreen::onRenderTooltip);
    }

    @Override
    public void onRegisterBlockStateResolver(BlockStateResolverContext context) {
        BlockConversionHandler.getBlockConversions().forEach((Block oldBlock, Block newBlock) -> {
            context.registerBlockStateResolver(newBlock,
                    (ResourceManager resourceManager, Executor executor) -> {
                        return ModelLoadingHelper.loadBlockState(resourceManager, oldBlock, executor);
                    },
                    (BlockStateModelLoader.LoadedModels loadedModels, BiConsumer<BlockState, BlockStateModel.UnbakedRoot> blockStateConsumer) -> {
                        Map<BlockState, BlockState> blockStates = BlockStateTranslator.INSTANCE.convertAllBlockStates(
                                newBlock,
                                oldBlock);
                        for (BlockState blockState : newBlock.getStateDefinition().getPossibleStates()) {
                            BlockStateModel.UnbakedRoot model = loadedModels.models().get(blockStates.get(blockState));
                            if (model != null) {
                                blockStateConsumer.accept(blockState, model);
                            } else {
                                EasyMagic.LOGGER.warn("Missing model for variant: '{}'", blockState);
                                blockStateConsumer.accept(blockState, ModelLoadingHelper.missingModel());
                            }
                        }
                    });
        });
    }

    @Override
    public void onRegisterMenuScreens(MenuScreensContext context) {
        context.registerMenuScreen(ModRegistry.ENCHANTING_MENU_TYPE.value(), ModEnchantmentScreen::new);
    }

    @Override
    public void onRegisterBlockEntityRenderers(BlockEntityRenderersContext context) {
        context.registerBlockEntityRenderer(ModRegistry.ENCHANTING_TABLE_BLOCK_ENTITY_TYPE.value(),
                ModEnchantTableRenderer::new);
    }

    @Override
    public void onRegisterBlockRenderTypes(RenderTypesContext<Block> context) {
        // this runs deferred by default, so we should have all entries from other mods available to us
        for (Map.Entry<Block, Block> entry : BlockConversionHandler.getBlockConversions().entrySet()) {
            context.registerChunkRenderType(entry.getValue(), context.getChunkRenderType(entry.getKey()));
        }
    }

    @Override
    public void onRegisterGuiLayers(GuiLayersContext context) {
        context.registerGuiLayer(GuiLayersContext.HELD_ITEM_TOOLTIP,
                EasyMagic.id("chiseled_bookshelf_tooltip"),
                ChiseledBookshelfTooltipHandler::render);
    }
}
