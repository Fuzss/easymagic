package fuzs.easymagic.data;

import fuzs.easymagic.init.ModRegistry;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.puzzleslib.api.data.v2.tags.AbstractTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ModItemTagsProvider extends AbstractTagProvider<Item> {

    public ModItemTagsProvider(DataProviderContext context) {
        super(Registries.ITEM, context);
    }

    @Override
    public void addTags(HolderLookup.Provider provider) {
        this.tag(ModRegistry.ENCHANTING_CATALYSTS_ITEM_TAG).add(Items.LAPIS_LAZULI);
        this.tag(ModRegistry.REROLL_CATALYSTS_ITEM_TAG).add(Items.AMETHYST_SHARD);
    }
}
