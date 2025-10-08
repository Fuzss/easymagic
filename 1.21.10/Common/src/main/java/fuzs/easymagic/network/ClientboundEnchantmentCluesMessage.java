package fuzs.easymagic.network;

import fuzs.easymagic.client.gui.screens.inventory.ModEnchantmentScreen;
import fuzs.easymagic.world.inventory.ModEnchantmentMenu;
import fuzs.puzzleslib.api.network.v4.message.MessageListener;
import fuzs.puzzleslib.api.network.v4.message.play.ClientboundPlayMessage;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.List;

public record ClientboundEnchantmentCluesMessage(int containerId,
                                                 List<List<EnchantmentInstance>> slotData) implements ClientboundPlayMessage {
    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantmentInstance> ENCHANTMENT_INSTANCE_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ENCHANTMENT),
            EnchantmentInstance::enchantment,
            ByteBufCodecs.VAR_INT,
            EnchantmentInstance::level,
            EnchantmentInstance::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundEnchantmentCluesMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ClientboundEnchantmentCluesMessage::containerId,
            ENCHANTMENT_INSTANCE_STREAM_CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs.list(3)),
            ClientboundEnchantmentCluesMessage::slotData,
            ClientboundEnchantmentCluesMessage::new);

    @Override
    public MessageListener<Context> getListener() {
        return new MessageListener<Context>() {
            @Override
            public void accept(Context context) {
                if (context.player().containerMenu instanceof ModEnchantmentMenu menu
                        && menu.containerId == ClientboundEnchantmentCluesMessage.this.containerId) {
                    for (int i = 0; i < ClientboundEnchantmentCluesMessage.this.slotData.size(); i++) {
                        menu.clues.set(i, ClientboundEnchantmentCluesMessage.this.slotData.get(i));
                    }
                    if (context.client().screen instanceof ModEnchantmentScreen screen) {
                        screen.tickChildren();
                    }
                }
            }
        };
    }
}