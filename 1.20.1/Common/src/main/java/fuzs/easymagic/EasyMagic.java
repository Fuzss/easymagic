package fuzs.easymagic;

import fuzs.easymagic.config.ClientConfig;
import fuzs.easymagic.config.ServerConfig;
import fuzs.easymagic.init.ModRegistry;
import fuzs.easymagic.network.S2CEnchantingDataMessage;
import fuzs.puzzleslib.api.config.v3.ConfigHolder;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.network.v2.MessageDirection;
import fuzs.puzzleslib.api.network.v2.NetworkHandlerV2;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasyMagic implements ModConstructor {
    public static final String MOD_ID = "easymagic";
    public static final String MOD_NAME = "Easy Magic";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final NetworkHandlerV2 NETWORK = NetworkHandlerV2.build(MOD_ID);
    public static final ConfigHolder CONFIG = ConfigHolder.builder(MOD_ID).client(ClientConfig.class).server(ServerConfig.class);

    @Override
    public void onConstructMod() {
        ModRegistry.touch();
        registerMessages();
    }

    private static void registerMessages() {
        NETWORK.register(S2CEnchantingDataMessage.class, S2CEnchantingDataMessage::new, MessageDirection.TO_CLIENT);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
