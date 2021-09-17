package com.fuzs.easymagic.inventory.container;

import com.fuzs.easymagic.mixin.accessor.SlotAccessor;
import net.minecraft.inventory.container.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IMovableSlot {

    Slot getSlot();

    default void moveTo(int xPos, int yPos) {

        ((SlotAccessor) this.getSlot()).setXPos(xPos);
        ((SlotAccessor) this.getSlot()).setYPos(yPos);
    }

    @OnlyIn(Dist.CLIENT)
    boolean isEnabled();

    void setVisible(boolean visible);

}
