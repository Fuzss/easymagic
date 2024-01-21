package fuzs.easymagic.data.client;

import fuzs.easymagic.client.gui.screens.inventory.ModEnchantmentScreen;
import fuzs.easymagic.handler.BlockConversionHandler;
import fuzs.puzzleslib.api.client.data.v2.AbstractLanguageProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;

public class ModLanguageProvider extends AbstractLanguageProvider {

    public ModLanguageProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTranslations(TranslationBuilder builder) {
        builder.add(ModEnchantmentScreen.KEY_REROLL, "Reroll enchantments");
        builder.add(ModEnchantmentScreen.KEY_ONE_EXPERIENCE_POINT, "1 Experience Point");
        builder.add(ModEnchantmentScreen.KEY_MANY_EXPERIENCE_POINTS, "%s Experience Points");
        builder.add(BlockConversionHandler.INVALID_BLOCK_COMPONENT, "Unable to open. Break and replace to use.");
    }
}
