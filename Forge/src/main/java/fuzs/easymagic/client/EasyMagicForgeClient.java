package fuzs.easymagic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.client.handler.ChiseledBookshelfTooltipHandler;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = EasyMagic.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EasyMagicForgeClient {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ClientModConstructor.construct(EasyMagic.MOD_ID, EasyMagicClient::new);
    }

    @SubscribeEvent
    public static void onRegisterGuiOverlays(final RegisterGuiOverlaysEvent evt) {
        evt.registerBelow(VanillaGuiOverlay.CROSSHAIR.id(), "chiseled_bookshelf_tooltip", (ForgeGui gui, PoseStack poseStack, float partialTicks, int screenWidth, int screenHeight) -> {
            gui.setupOverlayRenderState(true, false);
            gui.setBlitOffset(-90);
            ChiseledBookshelfTooltipHandler.tryRenderBookTooltip(gui.getMinecraft(), poseStack, screenWidth, screenHeight);
        });
    }
}
