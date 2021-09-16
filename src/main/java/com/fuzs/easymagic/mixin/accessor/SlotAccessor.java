package com.fuzs.easymagic.mixin.accessor;

import net.minecraft.inventory.container.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slot.class)
public interface SlotAccessor {

    @Mutable
    @Accessor
    void setXPos(int xPos);

    @Mutable
    @Accessor
    void setYPos(int yPos);

}
