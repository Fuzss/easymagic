package fuzs.easymagic;

import fuzs.easymagic.config.ClientConfig;
import fuzs.easymagic.config.CommonConfig;
import fuzs.easymagic.config.ServerConfig;
import fuzs.easymagic.handler.BlockConversionHandler;
import fuzs.easymagic.init.ModRegistry;
import fuzs.easymagic.network.ClientboundEnchantmentCluesMessage;
import fuzs.easymagic.world.level.block.EnchantmentTableWithInventoryBlock;
import fuzs.puzzleslib.api.config.v3.ConfigHolder;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.context.PayloadTypesContext;
import fuzs.puzzleslib.api.core.v1.utility.ResourceLocationHelper;
import fuzs.puzzleslib.api.event.v1.AddBlockEntityTypeBlocksCallback;
import fuzs.puzzleslib.api.event.v1.RegistryEntryAddedCallback;
import fuzs.puzzleslib.api.event.v1.core.EventPhase;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerInteractEvents;
import fuzs.puzzleslib.api.event.v1.server.TagsUpdatedCallback;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnchantingTableBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

public class EasyMagic implements ModConstructor {
    public static final String MOD_ID = "easymagic";
    public static final String MOD_NAME = "Easy Magic";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final ConfigHolder CONFIG = ConfigHolder.builder(MOD_ID)
            .client(ClientConfig.class)
            .common(CommonConfig.class)
            .server(ServerConfig.class);

    public static final Predicate<Block> BLOCK_PREDICATE = (Block block) -> {
        return block instanceof EnchantingTableBlock && !(block instanceof EnchantmentTableWithInventoryBlock);
    };

    @Override
    public void onConstructMod() {
        ModRegistry.bootstrap();
        registerLoadingHandlers();
    }

    private static void registerLoadingHandlers() {
        RegistryEntryAddedCallback.registryEntryAdded(Registries.BLOCK)
                .register(BlockConversionHandler.onRegistryEntryAdded(BLOCK_PREDICATE,
                        EnchantmentTableWithInventoryBlock::new,
                        MOD_ID));
        AddBlockEntityTypeBlocksCallback.EVENT.register(BlockConversionHandler.onAddBlockEntityTypeBlocks(ModRegistry.ENCHANTING_TABLE_BLOCK_ENTITY_TYPE));
    }

    @Override
    public void onCommonSetup() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        PlayerInteractEvents.USE_BLOCK.register(BlockConversionHandler.onUseBlock(ModRegistry.UNALTERED_ENCHANTING_TABLES_BLOCK_TAG,
                () -> CONFIG.get(CommonConfig.class).disableVanillaEnchantingTable));
        TagsUpdatedCallback.EVENT.register(EventPhase.FIRST,
                BlockConversionHandler.onTagsUpdated(ModRegistry.UNALTERED_ENCHANTING_TABLES_BLOCK_TAG,
                        BLOCK_PREDICATE));
    }

    @Override
    public void onRegisterPayloadTypes(PayloadTypesContext context) {
        context.playToClient(ClientboundEnchantmentCluesMessage.class, ClientboundEnchantmentCluesMessage.STREAM_CODEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocationHelper.fromNamespaceAndPath(MOD_ID, path);
    }
}
