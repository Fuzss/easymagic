package fuzs.easymagic.client;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.client.handler.ChiseledBookshelfTooltipHandler;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;

public class EasyMagicFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientModConstructor.construct(EasyMagic.MOD_ID, EasyMagicClient::new);
        registerHandlers();
    }

    private static void registerHandlers() {
        HudRenderCallback.EVENT.register((PoseStack matrixStack, float tickDelta) -> {
            Minecraft minecraft = Minecraft.getInstance();
            Window window = minecraft.getWindow();
            ChiseledBookshelfTooltipHandler.setupOverlayRenderState();
            minecraft.gui.setBlitOffset(-90);
            ChiseledBookshelfTooltipHandler.tryRenderBookTooltip(minecraft, matrixStack, window.getGuiScaledWidth(), window.getGuiScaledHeight());
        });
    }
}
