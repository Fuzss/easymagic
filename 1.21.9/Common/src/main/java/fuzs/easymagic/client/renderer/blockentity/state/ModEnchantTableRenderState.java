package fuzs.easymagic.client.renderer.blockentity.state;

import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

import java.util.List;
import java.util.stream.Stream;

public class ModEnchantTableRenderState extends EnchantTableRenderState {
    public final List<ItemStackRenderState> items = Stream.generate(ItemStackRenderState::new).limit(7).toList();
}
