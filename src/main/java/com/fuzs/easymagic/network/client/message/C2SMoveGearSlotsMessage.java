package com.fuzs.easymagic.network.client.message;

import com.fuzs.easymagic.element.GearSlotsEverywhereElement;
import com.fuzs.puzzleslib_em.network.message.Message;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;

public class C2SMoveGearSlotsMessage extends Message {

    private int windowId;
    private int[] slotNumberToPosition;

    public C2SMoveGearSlotsMessage() {

    }

    public C2SMoveGearSlotsMessage(int windowId, int[] slotNumberToPosition) {

        this.windowId = windowId;
        assert slotNumberToPosition.length % 3 == 0 : "slot number to position doesn't consist of pairs of three";
        this.slotNumberToPosition = slotNumberToPosition;
    }

    @Override
    public void write(PacketBuffer buf) {

        buf.writeByte(this.windowId);
        buf.writeByte(this.slotNumberToPosition.length);
        for (int slotNumber : this.slotNumberToPosition) {

            buf.writeByte(slotNumber);
        }
    }

    @Override
    public void read(PacketBuffer buf) {

        this.windowId = buf.readByte();
        int[] slotNumberToPosition = new int[buf.readByte()];
        for (int i = 0; i < slotNumberToPosition.length; i++) {

            slotNumberToPosition[i] = buf.readByte();
        }

        this.slotNumberToPosition = slotNumberToPosition;

    }

    @Override
    protected MessageProcessor createProcessor() {

        return new PositionPlayerSlotsProcessor();
    }

    private class PositionPlayerSlotsProcessor implements MessageProcessor {

        @Override
        public void accept(PlayerEntity playerEntity) {

            Container container = playerEntity.openContainer;
            if (C2SMoveGearSlotsMessage.this.windowId == container.windowId) {

                GearSlotsEverywhereElement.setupSlotPositions(container, C2SMoveGearSlotsMessage.this.slotNumberToPosition);
            }
        }

    }

}
