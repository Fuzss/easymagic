package com.fuzs.easymagic.network.message;

import com.fuzs.easymagic.client.gui.screen.EnchantmentInventoryScreen;
import com.fuzs.puzzleslib_em.network.message.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import java.util.ArrayList;
import java.util.List;

public class S2CEnchantingInfoMessage extends Message {

    private int windowId;
    private List<EnchantmentData> firstSlotData;
    private List<EnchantmentData> secondSlotData;
    private List<EnchantmentData> thirdSlotData;

    public S2CEnchantingInfoMessage() {

    }

    public S2CEnchantingInfoMessage(int windowId, List<EnchantmentData> firstSlotData, List<EnchantmentData> secondSlotData, List<EnchantmentData> thirdSlotData) {

        this.windowId = windowId;
        this.firstSlotData = firstSlotData;
        this.secondSlotData = secondSlotData;
        this.thirdSlotData = thirdSlotData;
    }

    @Override
    public void write(PacketBuffer buf) {

        buf.writeByte(this.windowId);
        this.writeEnchantmentData(buf, this.firstSlotData);
        this.writeEnchantmentData(buf, this.secondSlotData);
        this.writeEnchantmentData(buf, this.thirdSlotData);
    }

    @Override
    public void read(PacketBuffer buf) {

        this.windowId = buf.readByte();
        this.firstSlotData = this.readEnchantmentData(buf);
        this.secondSlotData = this.readEnchantmentData(buf);
        this.thirdSlotData = this.readEnchantmentData(buf);
    }

    private void writeEnchantmentData(PacketBuffer buf, List<EnchantmentData> dataList) {

        buf.writeByte(dataList.size());
        for (EnchantmentData data : dataList) {

            buf.writeInt(((ForgeRegistry<Enchantment>) ForgeRegistries.ENCHANTMENTS).getID(data.enchantment));
            buf.writeShort(data.enchantmentLevel);
        }
    }

    private List<EnchantmentData> readEnchantmentData(PacketBuffer buf) {

        int listSize = buf.readByte();
        List<EnchantmentData> slotData = new ArrayList<>(listSize);
        for (int i = 0; i < listSize; i++) {

            Enchantment enchantment = ((ForgeRegistry<Enchantment>) ForgeRegistries.ENCHANTMENTS).getValue(buf.readInt());
            slotData.add(new EnchantmentData(enchantment, buf.readShort()));
        }

        return slotData;
    }

    @Override
    protected MessageProcessor createProcessor() {

        return new EnchantingInfoProcessor();
    }

    private class EnchantingInfoProcessor implements MessageProcessor {

        @Override
        public void accept(PlayerEntity playerEntity) {

            if (Minecraft.getInstance().currentScreen instanceof EnchantmentInventoryScreen) {

                if (playerEntity.openContainer.windowId == S2CEnchantingInfoMessage.this.windowId) {

                    EnchantmentInventoryScreen screen = (EnchantmentInventoryScreen) Minecraft.getInstance().currentScreen;
                    screen.setSlotTooltip(0, S2CEnchantingInfoMessage.this.firstSlotData);
                    screen.setSlotTooltip(1, S2CEnchantingInfoMessage.this.secondSlotData);
                    screen.setSlotTooltip(2, S2CEnchantingInfoMessage.this.thirdSlotData);
                }
            }
        }

    }

}
