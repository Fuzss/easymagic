package com.fuzs.easymagic.network.message;

import com.fuzs.easymagic.EasyMagic;
import com.fuzs.easymagic.client.gui.widget.LineTabWidget;
import com.fuzs.easymagic.client.gui.widget.TabManager;
import com.fuzs.easymagic.element.EasyEnchantingElement;
import com.fuzs.easymagic.inventory.container.EnchantmentInventoryContainer;
import com.fuzs.puzzleslib_em.network.message.Message;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class SEnchantingPowerMessage extends Message {

    private int windowId;
    private int enchPower;

    public SEnchantingPowerMessage() {

    }

    public SEnchantingPowerMessage(int windowId, int enchPower) {

        this.windowId = windowId;
        this.enchPower = enchPower;
    }

    @Override
    public void write(PacketBuffer buf) {

        buf.writeByte(this.windowId);
        buf.writeByte(this.enchPower);
    }

    @Override
    public void read(PacketBuffer buf) {

        this.windowId = buf.readByte();
        this.enchPower = buf.readByte();
    }

    @Override
    protected MessageProcessor createProcessor() {

        return new EnchantingPowerProcessor();
    }

    private class EnchantingPowerProcessor implements MessageProcessor {

        @Override
        public void accept(PlayerEntity playerEntity) {

            Container container = playerEntity.openContainer;
            if (SEnchantingPowerMessage.this.windowId == container.windowId && container instanceof EnchantmentInventoryContainer) {

                TabManager.INSTANCE.getTab(new ResourceLocation(EasyMagic.MODID, "information")).ifPresent(tab -> ((LineTabWidget) tab).setLineContent(new StringTextComponent("Enchanting Power:"), new StringTextComponent(String.format("    %d / %d", SEnchantingPowerMessage.this.enchPower, ((EasyEnchantingElement) EasyMagic.EASY_ENCHANTING).maxPower))));
            }
        }

    }

}
