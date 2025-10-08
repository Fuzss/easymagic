package fuzs.easymagic.client.renderer.blockentity.state;

import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

import java.util.ArrayList;
import java.util.List;

public class ModEnchantTableRenderState extends EnchantTableRenderState {
    public List<ItemStackRenderState> items = new ArrayList<>();
}
