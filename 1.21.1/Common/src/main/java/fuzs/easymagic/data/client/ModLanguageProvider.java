package fuzs.easymagic.data.client;

import fuzs.easymagic.client.util.EnchantmentTooltipHelper;
import fuzs.easymagic.handler.BlockConversionHandler;
import fuzs.easymagic.init.ModRegistry;
import fuzs.puzzleslib.api.client.data.v2.AbstractLanguageProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;

public class ModLanguageProvider extends AbstractLanguageProvider {

    public ModLanguageProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTranslations(TranslationBuilder builder) {
        builder.add(EnchantmentTooltipHelper.KEY_REROLL, "Reroll enchantments");
        builder.add(EnchantmentTooltipHelper.KEY_ONE_REROLL_CATALYST, "1 Amethyst Shard");
        builder.add(EnchantmentTooltipHelper.KEY_MANY_REROLL_CATALYSTS, "%s Amethyst Shards");
        builder.add(EnchantmentTooltipHelper.KEY_ONE_EXPERIENCE_POINT, "1 Experience Point");
        builder.add(EnchantmentTooltipHelper.KEY_MANY_EXPERIENCE_POINTS, "%s Experience Points");
        builder.add(BlockConversionHandler.INVALID_BLOCK_COMPONENT, "Unable to open. Break and replace to use.");
        builder.add(ModRegistry.ENCHANTING_CATALYSTS_ITEM_TAG, "Enchanting Catalysts");
        builder.add(ModRegistry.REROLL_CATALYSTS_ITEM_TAG, "Reroll Catalysts");
        builder.add(ModRegistry.UNALTERED_ENCHANTING_TABLES_BLOCK_TAG, "Unaltered Enchanting Tables");
    }
}
