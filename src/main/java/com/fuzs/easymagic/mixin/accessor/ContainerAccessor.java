package com.fuzs.easymagic.mixin.accessor;

import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Container.class)
public interface ContainerAccessor {

    @Invoker
    Slot callAddSlot(Slot slotIn);

}
