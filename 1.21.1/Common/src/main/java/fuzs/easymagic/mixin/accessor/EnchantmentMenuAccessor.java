package fuzs.easymagic.mixin.accessor;

import net.minecraft.core.RegistryAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(EnchantmentMenu.class)
public interface EnchantmentMenuAccessor {

    @Mutable
    @Accessor("enchantSlots")
    void setEnchantSlots(Container enchantSlots);

    @Accessor("enchantmentSeed")
    DataSlot getEnchantmentSeed();

    @Accessor("random")
    RandomSource getRandom();

    @Invoker("getEnchantmentList")
    List<EnchantmentInstance> callGetEnchantmentList(RegistryAccess registryAccess, ItemStack itemStack, int slot, int cost);
}
