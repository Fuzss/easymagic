package fuzs.easymagic.data;

import fuzs.easymagic.init.ModRegistry;
import fuzs.puzzleslib.api.data.v2.AbstractTagProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;

public class ModItemTagsProvider extends AbstractTagProvider.Items {

    public ModItemTagsProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTags(HolderLookup.Provider provider) {
        this.tag(ModRegistry.ENCHANTING_CATALYSTS_ITEM_TAG).add(Items.LAPIS_LAZULI);
        this.tag(ModRegistry.REROLL_CATALYSTS_ITEM_TAG).add(Items.AMETHYST_SHARD);
    }
}
