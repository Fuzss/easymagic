package fuzs.easymagic.network;

import fuzs.easymagic.client.gui.screens.inventory.ModEnchantmentScreen;
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

public record ClientboundCluesMessage(int containerId,
                                      List<List<EnchantmentInstance>> slotData) implements ClientboundMessage<ClientboundCluesMessage> {
    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantmentInstance> ENCHANTMENT_INSTANCE_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ENCHANTMENT),
            EnchantmentInstance::enchantment,
            ByteBufCodecs.VAR_INT,
            EnchantmentInstance::level,
            EnchantmentInstance::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundCluesMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ClientboundCluesMessage::containerId,
            ENCHANTMENT_INSTANCE_STREAM_CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs.list(3)),
            ClientboundCluesMessage::slotData,
            ClientboundCluesMessage::new);

    @Override
    public ClientMessageListener<ClientboundCluesMessage> getHandler() {
        return new ClientMessageListener<>() {

            @Override
            public void handle(ClientboundCluesMessage message, Minecraft client, ClientPacketListener handler, LocalPlayer player, ClientLevel level) {
                if (player.containerMenu instanceof ModEnchantmentMenu menu &&
                        menu.containerId == message.containerId) {
                    for (int i = 0; i < message.slotData.size(); i++) {
                        menu.clues.set(i, message.slotData.get(i));
                    }
                    if (client.screen instanceof ModEnchantmentScreen screen) {
                        screen.tickChildren();
                    }
                }
            }
        };
    }
}