package fuzs.easymagic.neoforge;

import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.data.ModItemTagsProvider;
import fuzs.easymagic.init.ModRegistry;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import fuzs.puzzleslib.neoforge.api.init.v3.capability.NeoForgeCapabilityHelper;
import net.neoforged.fml.common.Mod;

@Mod(EasyMagic.MOD_ID)
public class EasyMagicNeoForge {

    public EasyMagicNeoForge() {
        ModConstructor.construct(EasyMagic.MOD_ID, EasyMagic::new);
        DataProviderHelper.registerDataProviders(EasyMagic.MOD_ID, ModItemTagsProvider::new);
        NeoForgeCapabilityHelper.registerWorldlyBlockEntityContainer(ModRegistry.ENCHANTING_TABLE_BLOCK_ENTITY_TYPE);
    }
}
