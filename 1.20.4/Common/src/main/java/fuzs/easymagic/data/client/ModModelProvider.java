package fuzs.easymagic.data.client;

import fuzs.easymagic.init.ModRegistry;
import fuzs.puzzleslib.api.client.data.v2.AbstractModelProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.world.level.block.Blocks;

public class ModModelProvider extends AbstractModelProvider {

    public ModModelProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addBlockModels(BlockModelGenerators builder) {
        // dynamically generate a basic stone block model for every diagonal block so the model bakery logs no missing model
        // don't use an air model so the blocks remain visible
        Variant variant = Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.STONE));
        builder.blockStateOutput.accept(MultiVariantGenerator.multiVariant(ModRegistry.ENCHANTMENT_TABLE_BLOCK.value(), variant));
        builder.skipAutoItemBlock(ModRegistry.ENCHANTMENT_TABLE_BLOCK.value());
    }
}
