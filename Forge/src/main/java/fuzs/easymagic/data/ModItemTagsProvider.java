package fuzs.easymagic.data;

import fuzs.easymagic.init.ModRegistry;
import fuzs.puzzleslib.api.data.v1.AbstractTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends AbstractTagsProvider.Items {

    public ModItemTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, ExistingFileHelper fileHelper) {
        super(packOutput, lookupProvider, modId, fileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ModRegistry.ENCHANTING_CATALYSTS_ITEM_TAG).add(Items.LAPIS_LAZULI).addOptionalTag(Tags.Items.ENCHANTING_FUELS.location());
        this.tag(ModRegistry.REROLL_CATALYSTS_ITEM_TAG).add(Items.AMETHYST_SHARD);
    }
}
