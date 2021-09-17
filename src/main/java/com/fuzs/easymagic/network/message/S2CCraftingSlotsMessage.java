package com.fuzs.easymagic.network.message;

import com.fuzs.easymagic.EasyMagic;
import com.fuzs.easymagic.element.GearSlotsEverywhereElement;
import com.fuzs.puzzleslib_em.network.message.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.network.PacketBuffer;

import java.util.Arrays;

public class S2CCraftingSlotsMessage extends Message {

    private int windowId;
    private int[] slotNumbers;

    public S2CCraftingSlotsMessage() {

    }

    public S2CCraftingSlotsMessage(int windowId, int[] slotNumbers) {

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

        return new CraftingSlotsProcessor();
    }

    private class CraftingSlotsProcessor implements MessageProcessor {

        @Override
        public void accept(PlayerEntity playerEntity) {

            Container container = playerEntity.openContainer;
            if (S2CCraftingSlotsMessage.this.windowId == container.windowId) {

                if (!(container instanceof RecipeBookContainer) || ((RecipeBookContainer<?>) container).getWidth() != 3 || ((RecipeBookContainer<?>) container).getHeight() != 3) {

                    int[] slotNumbers = GearSlotsEverywhereElement.addCraftingSlots(container, playerEntity);
                    if (!Arrays.equals(slotNumbers, S2CCraftingSlotsMessage.this.slotNumbers)) {

                        EasyMagic.LOGGER.error("unable to add additional container slots on client: slot numbers mismatched");
                        return;
                    }

                    Screen currentScreen = Minecraft.getInstance().currentScreen;
                    if (currentScreen instanceof ContainerScreen<?> && ((ContainerScreen<?>) currentScreen).getContainer() == container) {

                        final int xOffset = ((ContainerScreen<?>) currentScreen).getXSize() + 8;
                        final int yOffset = 8;
                        int[] slotNumberToPosition = new int[slotNumbers.length * 3];
                        slotNumberToPosition[0] = slotNumbers[0];
                        slotNumberToPosition[1] = xOffset + 18;
                        slotNumberToPosition[2] = yOffset + 3 * 18 + 20;
                        for (int i = 3; i < slotNumberToPosition.length; i += 3) {

                            slotNumberToPosition[i] = slotNumbers[i / 3];
                            int craftingSlot = i / 3 - 1;
                            slotNumberToPosition[i + 1] = craftingSlot % 3 * 18 + xOffset;
                            slotNumberToPosition[i + 2] = craftingSlot / 3 * 18 + yOffset;
                        }

                        GearSlotsEverywhereElement.setupSlotPositions(container, slotNumberToPosition);
                        GearSlotsEverywhereElement.setSlotVisibilities(container, slotNumbers, true);
                    }
                }
            }
        }

    }

}
