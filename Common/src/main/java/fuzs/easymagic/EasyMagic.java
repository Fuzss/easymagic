package fuzs.easymagic;

import fuzs.easymagic.config.ClientConfig;
import fuzs.easymagic.config.ServerConfig;
import fuzs.easymagic.network.message.S2CEnchantingDataMessage;
import fuzs.easymagic.init.ModRegistry;
import fuzs.puzzleslib.config.ConfigHolder;
import fuzs.puzzleslib.core.CoreServices;
import fuzs.puzzleslib.core.ModConstructor;
import fuzs.puzzleslib.network.MessageDirection;
import fuzs.puzzleslib.network.NetworkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasyMagic implements ModConstructor {
    static final ModConstructor INSTANCE = new EasyMagic();
    public static final String MOD_ID = "easymagic";
    public static final String MOD_NAME = "Easy Magic";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final NetworkHandler NETWORK = CoreServices.FACTORIES.network(MOD_ID);
    @SuppressWarnings("Convert2MethodRef")
    public static final ConfigHolder<ClientConfig, ServerConfig> CONFIG = CoreServices.FACTORIES.config(() -> new ClientConfig(), () -> new ServerConfig());

    @Override
    public void onConstructMod() {
        CONFIG.loadConfigs(MOD_ID);
        ModRegistry.touch();
        registerMessages();
    }

    private static void registerMessages() {
        NETWORK.register(S2CEnchantingDataMessage.class, S2CEnchantingDataMessage::new, MessageDirection.TO_CLIENT);
    }
}
