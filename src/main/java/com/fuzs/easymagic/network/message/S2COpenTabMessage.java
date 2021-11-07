package com.fuzs.easymagic.network.message;

import com.fuzs.easymagic.client.gui.widget.TabManager;
import com.fuzs.puzzleslib_em.network.message.Message;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import java.util.Optional;

public class S2COpenTabMessage extends Message {

    private int windowId;
    private int tabId;
    private int tabTypeId;

    public S2COpenTabMessage() {

    }

    public S2COpenTabMessage(int windowId, int tabId, ContainerType<?> tabTypeId) {

        this.windowId = windowId;
        this.tabId = tabId;
        this.tabTypeId = ((ForgeRegistry<ContainerType<?>>) ForgeRegistries.CONTAINERS).getID(tabTypeId);
    }

    @Override
    public void write(PacketBuffer buf) {

        buf.writeVarInt(this.windowId);
        buf.writeVarInt(this.tabId);
        buf.writeVarInt(this.tabTypeId);
    }

    @Override
    public void read(PacketBuffer buf) {

        this.windowId = buf.readVarInt();
        this.tabId = buf.readVarInt();
        this.tabTypeId = buf.readVarInt();
    }

    @Override
    protected MessageProcessor createProcessor() {

        return new OpenTabProcessor();
    }

    private class OpenTabProcessor implements MessageProcessor {

        @Override
        public void accept(PlayerEntity playerEntity) {

            Container container = playerEntity.openContainer;
            if (S2COpenTabMessage.this.windowId == container.windowId) {

                Optional<? extends ScreenManager.IScreenFactory<?, ?>> screenFactory = TabManager.getScreenFactory(((ForgeRegistry<ContainerType<?>>) ForgeRegistries.CONTAINERS).getValue(S2COpenTabMessage.this.tabTypeId));

            }
        }

    }

}
