package fuzs.easymagic.client.handler;

import fuzs.easymagic.init.ModRegistry;
import fuzs.puzzleslib.api.event.v1.core.EventResultHolder;
import net.minecraft.Util;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BlockModelHandler {
    private static final Map<ResourceLocation, ResourceLocation> BLOCK_CONVERSIONS;

    static {
        BLOCK_CONVERSIONS = Map.of(ModRegistry.ENCHANTMENT_TABLE_BLOCK.value(), Blocks.ENCHANTING_TABLE).entrySet().stream().map(BlockModelHandler::getAnyBlockStateConversion).collect(Util.toMap());
    }

    public static EventResultHolder<UnbakedModel> onModifyUnbakedModel(ResourceLocation modelLocation, Supplier<UnbakedModel> unbakedModel, Function<ResourceLocation, UnbakedModel> modelGetter, BiConsumer<ResourceLocation, UnbakedModel> modelAdder) {
        modelLocation = new ResourceLocation(modelLocation.getNamespace(), modelLocation.getPath());
        if (BLOCK_CONVERSIONS.containsKey(modelLocation)) {
            return EventResultHolder.interrupt(modelGetter.apply(BLOCK_CONVERSIONS.get(modelLocation)));
        } else {
            return EventResultHolder.pass();
        }
    }

    private static Map.Entry<ResourceLocation, ResourceLocation> getAnyBlockStateConversion(Map.Entry<Block, Block> blocks) {
        return Map.entry(BuiltInRegistries.BLOCK.getKey(blocks.getKey()), BlockModelShaper.stateToModelLocation(blocks.getValue().defaultBlockState()));
    }
}
