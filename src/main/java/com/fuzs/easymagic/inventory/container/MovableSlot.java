package com.fuzs.easymagic.inventory.container;

import com.fuzs.easymagic.mixin.accessor.SlotAccessor;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MovableSlot extends Slot {

    private boolean visible = false;

    public MovableSlot(IInventory inventoryIn, int index) {

        super(inventoryIn, index, 0, 0);
    }

    public void moveTo(int xPos, int yPos) {

        ((SlotAccessor) this).setXPos(xPos);
        ((SlotAccessor) this).setYPos(yPos);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public final boolean isEnabled() {

        return this.visible;
    }

    public void setVisible(boolean visible) {

        this.visible = visible;
    }

}
