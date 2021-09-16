package com.fuzs.easymagic.network.message;

import com.fuzs.easymagic.element.GearSlotsEverywhereElement;
import com.fuzs.easymagic.network.client.message.C2SMoveGearSlotsMessage;
import com.fuzs.puzzleslib_em.PuzzlesLib;
import com.fuzs.puzzleslib_em.network.message.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;

public class S2CGearSlotsMessage extends Message {

    private int windowId;

    public S2CGearSlotsMessage() {

    }

    public S2CGearSlotsMessage(int windowId) {

        this.windowId = windowId;
    }

    @Override
    public void write(PacketBuffer buf) {

        buf.writeByte(this.windowId);
    }

    @Override
    public void read(PacketBuffer buf) {

        this.windowId = buf.readByte();
    }

    @Override
    protected MessageProcessor createProcessor() {

        return new AddArmorSlotsProcessor();
    }

    private class AddArmorSlotsProcessor implements MessageProcessor {

        @Override
        public void accept(PlayerEntity playerEntity) {

            Container container = playerEntity.openContainer;
            if (S2CGearSlotsMessage.this.windowId == container.windowId) {

                int[] slotNumbers = GearSlotsEverywhereElement.addPlayerSlots(container, playerEntity);
                Screen currentScreen = Minecraft.getInstance().currentScreen;
                if (currentScreen instanceof ContainerScreen<?> && ((ContainerScreen<?>) currentScreen).getContainer() == container) {

                    int[] slotNumberToPosition = new int[slotNumbers.length * 3];
                    for (int i = 0; i < slotNumberToPosition.length; i += 3) {

                        slotNumberToPosition[i] = slotNumbers[i / 3];
                        slotNumberToPosition[i + 1] = -26;
                        slotNumberToPosition[i + 2] = 8 + (i / 3) * 18;
                    }

                    GearSlotsEverywhereElement.setupSlotPositions(container, slotNumberToPosition);
                    // don't need to make slots visible on server as parameter is only used for rendering
                    GearSlotsEverywhereElement.setSlotVisibilities(container, slotNumbers, true);
                    PuzzlesLib.getNetworkHandler().sendToServer(new C2SMoveGearSlotsMessage(container.windowId, slotNumberToPosition));
                }
            }
        }

    }

}
