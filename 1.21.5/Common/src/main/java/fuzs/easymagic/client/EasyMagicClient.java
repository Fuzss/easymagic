package fuzs.easymagic.client;

import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.client.gui.screens.inventory.ModEnchantmentScreen;
import fuzs.easymagic.client.handler.BlockStateTranslator;
import fuzs.easymagic.client.handler.ChiseledBookshelfTooltipHandler;
import fuzs.easymagic.client.renderer.blockentity.ModEnchantTableRenderer;
import fuzs.easymagic.handler.BlockConversionHandler;
import fuzs.easymagic.init.ModRegistry;
import fuzs.puzzleslib.api.client.core.v1.ClientAbstractions;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.BlockEntityRenderersContext;
import fuzs.puzzleslib.api.client.core.v1.context.BlockStateResolverContext;
import fuzs.puzzleslib.api.client.core.v1.context.MenuScreensContext;
import fuzs.puzzleslib.api.client.event.v1.ClientStartedCallback;
import fuzs.puzzleslib.api.client.event.v1.gui.RenderGuiEvents;
import fuzs.puzzleslib.api.client.event.v1.gui.RenderTooltipCallback;
import fuzs.puzzleslib.api.client.util.v1.ModelLoadingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.UnbakedBlockStateModel;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.ModelResourceLocation;
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
        ClientStartedCallback.EVENT.register((Minecraft minecraft) -> {
            // run a custom implementation here, the appropriate method in client mod constructor runs together with other mods, so we might miss some entries
            for (Map.Entry<Block, Block> entry : BlockConversionHandler.getBlockConversions().entrySet()) {
                RenderType renderType = ClientAbstractions.INSTANCE.getRenderType(entry.getKey());
                ClientAbstractions.INSTANCE.registerRenderType(entry.getValue(), renderType);
            }
        });
        RenderGuiEvents.AFTER.register(ChiseledBookshelfTooltipHandler::onAfterRenderGui);
        RenderTooltipCallback.EVENT.register(ModEnchantmentScreen::onRenderTooltip);
    }

    @Override
    public void onRegisterBlockStateResolver(BlockStateResolverContext context) {
        BlockConversionHandler.getBlockConversions().forEach((Block oldBlock, Block newBlock) -> {
            context.registerBlockStateResolver(newBlock,
                    (ResourceManager resourceManager, Executor executor) -> {
                        return ModelLoadingHelper.loadBlockState(resourceManager, oldBlock, executor);
                    },
                    (BlockStateModelLoader.LoadedModels loadedModels, BiConsumer<BlockState, UnbakedBlockStateModel> consumer) -> {
                        Map<ModelResourceLocation, ModelResourceLocation> modelResourceLocations = BlockStateTranslator.INSTANCE.convertAllBlockStates(
                                newBlock,
                                oldBlock);
                        for (BlockState blockState : newBlock.getStateDefinition().getPossibleStates()) {
                            ModelResourceLocation newModelResourceLocation = BlockModelShaper.stateToModelLocation(
                                    blockState);
                            ModelResourceLocation oldModelResourceLocation = modelResourceLocations.get(
                                    newModelResourceLocation);
                            UnbakedBlockStateModel model = null;
                            if (oldModelResourceLocation != null) {
                                BlockStateModelLoader.LoadedModel loadedModel = loadedModels.models()
                                        .get(oldModelResourceLocation);
                                if (loadedModel != null) {
                                    model = loadedModel.model();
                                }
                            }
                            if (model != null) {
                                consumer.accept(blockState, model);
                            } else {
                                EasyMagic.LOGGER.warn("Missing model for variant: '{}'", newModelResourceLocation);
                                consumer.accept(blockState, ModelLoadingHelper.missingModel());
                            }
                        }
                    });
        });
    }

    @Override
    public void onRegisterMenuScreens(MenuScreensContext context) {
        context.registerMenuScreen(ModRegistry.ENCHANTMENT_MENU_TYPE.value(), ModEnchantmentScreen::new);
    }

    @Override
    public void onRegisterBlockEntityRenderers(BlockEntityRenderersContext context) {
        context.registerBlockEntityRenderer(ModRegistry.ENCHANTING_TABLE_BLOCK_ENTITY_TYPE.value(),
                ModEnchantTableRenderer::new);
    }
}
