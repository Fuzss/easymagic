package com.fuzs.easymagic.network.message;

import com.fuzs.easymagic.EasyMagic;
import com.fuzs.easymagic.element.GearSlotsEverywhereElement;
import com.fuzs.puzzleslib_em.network.message.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;

import java.util.Arrays;

public class S2CGearSlotsMessage extends Message {

    private int windowId;
    private int[] slotNumbers;

    public S2CGearSlotsMessage() {

    }

    public S2CGearSlotsMessage(int windowId, int[] slotNumbers) {

        this.windowId = windowId;
        this.slotNumbers = slotNumbers;
    }

    @Override
    public void write(PacketBuffer buf) {

        buf.writeByte(this.windowId);
        buf.writeVarIntArray(this.slotNumbers);
    }

    @Override
    public void read(PacketBuffer buf) {

        this.windowId = buf.readByte();
        this.slotNumbers = buf.readVarIntArray();
    }

    @Override
    protected MessageProcessor createProcessor() {

        return new GearSlotsProcessor();
    }

    private class GearSlotsProcessor implements MessageProcessor {

        @Override
        public void accept(PlayerEntity playerEntity) {

            Container container = playerEntity.openContainer;
            if (S2CGearSlotsMessage.this.windowId == container.windowId) {

                int[] slotNumbers = GearSlotsEverywhereElement.addGearSlots(container, playerEntity);
                if (!Arrays.equals(slotNumbers, S2CGearSlotsMessage.this.slotNumbers)) {

                    EasyMagic.LOGGER.error("unable to add additional container slots on client: slot numbers mismatched");
                    return;
                }

                Screen currentScreen = Minecraft.getInstance().currentScreen;
                if (currentScreen instanceof ContainerScreen<?> && ((ContainerScreen<?>) currentScreen).getContainer() == container) {

                    int[] slotNumberToPosition = new int[slotNumbers.length * 3];
                    for (int i = 0; i < slotNumberToPosition.length; i += 3) {

                        slotNumberToPosition[i] = slotNumbers[i / 3];
                        slotNumberToPosition[i + 1] = -26;
                        slotNumberToPosition[i + 2] = 8 + (i / 3) * 18;
                    }

                    GearSlotsEverywhereElement.setupSlotPositions(container, slotNumberToPosition);
                    GearSlotsEverywhereElement.setSlotVisibilities(container, slotNumbers, true);
//                    PuzzlesLib.getNetworkHandler().sendToServer(new C2SMoveGearSlotsMessage(container.windowId, slotNumberToPosition));
                }
            }
        }

    }

}
