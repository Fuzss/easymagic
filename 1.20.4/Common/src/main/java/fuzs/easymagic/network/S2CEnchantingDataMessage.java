package fuzs.easymagic.network;

import fuzs.easymagic.client.gui.screens.inventory.ModEnchantmentScreen;
import fuzs.puzzleslib.api.network.v2.WritableMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.ArrayList;
import java.util.List;

public class S2CEnchantingDataMessage implements WritableMessage<S2CEnchantingDataMessage> {
    private final int containerId;
    private final List<EnchantmentInstance> firstSlotData;
    private final List<EnchantmentInstance> secondSlotData;
    private final List<EnchantmentInstance> thirdSlotData;

    public S2CEnchantingDataMessage(int containerId, List<EnchantmentInstance> firstSlotData, List<EnchantmentInstance> secondSlotData, List<EnchantmentInstance> thirdSlotData) {
        this.containerId = containerId;
        this.firstSlotData = firstSlotData;
        this.secondSlotData = secondSlotData;
        this.thirdSlotData = thirdSlotData;
    }

    public S2CEnchantingDataMessage(FriendlyByteBuf buf) {
        this.containerId = buf.readByte();
        this.firstSlotData = this.readEnchantmentInstance(buf);
        this.secondSlotData = this.readEnchantmentInstance(buf);
        this.thirdSlotData = this.readEnchantmentInstance(buf);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(this.containerId);
        this.writeEnchantmentInstance(buf, this.firstSlotData);
        this.writeEnchantmentInstance(buf, this.secondSlotData);
        this.writeEnchantmentInstance(buf, this.thirdSlotData);
    }

    private void writeEnchantmentInstance(FriendlyByteBuf buf, List<EnchantmentInstance> dataList) {
        buf.writeByte(dataList.size());
        for (EnchantmentInstance data : dataList) {
            buf.writeInt(BuiltInRegistries.ENCHANTMENT.getId(data.enchantment));
            buf.writeShort(data.level);
        }
    }

    private List<EnchantmentInstance> readEnchantmentInstance(FriendlyByteBuf buf) {
        int listSize = buf.readByte();
        List<EnchantmentInstance> slotData = new ArrayList<>(listSize);
        for (int i = 0; i < listSize; i++) {
            Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.byId(buf.readInt());
            slotData.add(new EnchantmentInstance(enchantment, buf.readShort()));
        }
        return slotData;
    }

    @Override
    public MessageHandler<S2CEnchantingDataMessage> makeHandler() {
        return new MessageHandler<>() {

            @Override
            public void handle(S2CEnchantingDataMessage message, Player player, Object gameInstance) {
                if (((Minecraft) gameInstance).screen instanceof ModEnchantmentScreen screen) {
                    if (player.containerMenu.containerId == message.containerId) {
                        screen.setSlotData(0, message.firstSlotData);
                        screen.setSlotData(1, message.secondSlotData);
                        screen.setSlotData(2, message.thirdSlotData);
                    }
                }
            }
        };
    }
}
