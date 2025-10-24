package fuzs.easymagic.network;

import fuzs.easymagic.world.inventory.ModEnchantmentMenu;
import fuzs.puzzleslib.api.network.v3.ClientMessageListener;
import fuzs.puzzleslib.api.network.v3.ClientboundMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.List;

public record ClientboundEnchantmentCluesMessage(int containerId,
                                                 List<List<EnchantmentInstance>> slotData) implements ClientboundMessage<ClientboundEnchantmentCluesMessage> {
    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantmentInstance> ENCHANTMENT_INSTANCE_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ENCHANTMENT),
            (EnchantmentInstance enchantmentInstance) -> enchantmentInstance.enchantment,
            ByteBufCodecs.VAR_INT,
            (EnchantmentInstance enchantmentInstance) -> enchantmentInstance.level,
            EnchantmentInstance::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundEnchantmentCluesMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ClientboundEnchantmentCluesMessage::containerId,
            ENCHANTMENT_INSTANCE_STREAM_CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs.list(3)),
            ClientboundEnchantmentCluesMessage::slotData,
            ClientboundEnchantmentCluesMessage::new);

    @Override
    public ClientMessageListener<ClientboundEnchantmentCluesMessage> getHandler() {
        return new ClientMessageListener<ClientboundEnchantmentCluesMessage>() {
            @Override
            public void handle(ClientboundEnchantmentCluesMessage message, Minecraft minecraft, ClientPacketListener handler, LocalPlayer player, ClientLevel level) {
                if (player.containerMenu instanceof ModEnchantmentMenu menu
                        && menu.containerId == ClientboundEnchantmentCluesMessage.this.containerId) {
                    for (int i = 0; i < ClientboundEnchantmentCluesMessage.this.slotData.size(); i++) {
                        menu.clues.set(i, ClientboundEnchantmentCluesMessage.this.slotData.get(i));
                    }
                }
            }
        };
    }
}
