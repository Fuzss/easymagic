package fuzs.easymagic.data;

import fuzs.easymagic.init.ModRegistry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModItemTagsProvider extends ItemTagsProvider {

    public ModItemTagsProvider(DataGenerator dataGenerator, String modId, ExistingFileHelper fileHelper) {
        super(dataGenerator, new BlockTagsProvider(dataGenerator, modId, fileHelper), modId, fileHelper);
    }

    @Override
    protected void addTags() {
        this.tag(ModRegistry.ENCHANTING_CATALYSTS_ITEM_TAG).addOptionalTag(Tags.Items.ENCHANTING_FUELS.location()).add(Items.LAPIS_LAZULI);
        this.tag(ModRegistry.REROLL_CATALYSTS_ITEM_TAG).add(Items.AMETHYST_SHARD);
    }
}
