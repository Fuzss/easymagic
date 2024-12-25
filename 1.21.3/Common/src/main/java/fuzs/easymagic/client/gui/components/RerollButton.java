package fuzs.easymagic.client.gui.components;

import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.config.ServerConfig;
import fuzs.easymagic.world.inventory.ModEnchantmentMenu;
import fuzs.puzzleslib.api.client.gui.v2.components.SpritelessImageButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

public class RerollButton extends SpritelessImageButton implements Tickable {
    public static final ResourceLocation ENCHANTING_TABLE_REROLL_LOCATION = EasyMagic.id(
            "textures/gui/container/enchanting_table_reroll.png");

    private final int rerollExperiencePointsCost = EasyMagic.CONFIG.get(ServerConfig.class).rerollExperiencePointsCost;
    private final int rerollCatalystCost = EasyMagic.CONFIG.get(ServerConfig.class).rerollCatalystCost;
    private final ModEnchantmentMenu menu;

    public RerollButton(int x, int y, OnPress onPress, ModEnchantmentMenu menu) {
        super(x, y, 38, 27, 0, 0, ENCHANTING_TABLE_REROLL_LOCATION, onPress);
        this.setTextureLayout(LEGACY_TEXTURE_LAYOUT);
        this.menu = menu;
    }

    @Override
    public void tick() {
        this.visible = this.menu.canEnchantItem();
        this.active = this.menu.canUseReroll(this.menu.player);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        if (this.rerollExperiencePointsCost == 0 && this.rerollCatalystCost == 0) {
            // arrow circle
            guiGraphics.blit(RenderType::guiTextured,
                    ENCHANTING_TABLE_REROLL_LOCATION,
                    this.getX() + 12,
                    this.getY() + 6,
                    64,
                    !this.isActive() ? 0 : this.isHoveredOrFocused() ? 30 : 15,
                    15,
                    15,
                    256,
                    256);
        } else {
            // arrow circle
            guiGraphics.blit(RenderType::guiTextured,
                    ENCHANTING_TABLE_REROLL_LOCATION,
                    this.getX() + 3,
                    this.getY() + 6,
                    64,
                    !this.isActive() ? 0 : this.isHoveredOrFocused() ? 30 : 15,
                    15,
                    15,
                    256,
                    256);
            if (this.rerollExperiencePointsCost > 0 && this.rerollCatalystCost > 0) {
                // level orb
                this.renderCostOrb(guiGraphics,
                        this.getX() + (this.rerollExperiencePointsCost > 9 ? 17 : 20),
                        this.getY() + 13,
                        38,
                        !this.isActive() ? 39 : 0,
                        this.rerollExperiencePointsCost,
                        !this.isActive() ? ChatFormatting.RED : ChatFormatting.GREEN);
                // lapis orb
                this.renderCostOrb(guiGraphics,
                        this.getX() + (this.rerollCatalystCost > 9 ? 17 : 20),
                        this.getY() + 1,
                        51,
                        !this.isActive() ? 39 : 0,
                        this.rerollCatalystCost,
                        !this.isActive() ? ChatFormatting.RED : ChatFormatting.BLUE);
            } else if (this.rerollExperiencePointsCost > 0) {
                // level orb
                this.renderCostOrb(guiGraphics,
                        this.getX() + (this.rerollExperiencePointsCost > 9 ? 17 : 20),
                        this.getY() + 7,
                        38,
                        !this.isActive() ? 39 : 0,
                        this.rerollExperiencePointsCost,
                        !this.isActive() ? ChatFormatting.RED : ChatFormatting.GREEN);
            } else if (this.rerollCatalystCost > 0) {
                // lapis orb
                this.renderCostOrb(guiGraphics,
                        this.getX() + (this.rerollCatalystCost > 9 ? 17 : 20),
                        this.getY() + 7,
                        51,
                        !this.isActive() ? 39 : 0,
                        this.rerollCatalystCost,
                        !this.isActive() ? ChatFormatting.RED : ChatFormatting.BLUE);
            }
        }
    }

    private void renderCostOrb(GuiGraphics guiGraphics, int posX, int posY, int textureX, int textureY, int cost, ChatFormatting color) {
        guiGraphics.blit(RenderType::guiTextured,
                ENCHANTING_TABLE_REROLL_LOCATION,
                posX,
                posY,
                textureX,
                textureY + Math.min(2, cost / 5) * 13,
                13,
                13,
                256,
                256);
        // render shadow on every side to avoid readability issues with colorful background
        FormattedCharSequence formattedCharSequence = Component.literal(String.valueOf(cost)).getVisualOrderText();
        guiGraphics.drawSpecial(bufferSource -> {
            Minecraft.getInstance().font.drawInBatch8xOutline(formattedCharSequence,
                    posX + 8,
                    posY + 3,
                    color.getColor(),
                    0,
                    guiGraphics.pose().last().pose(),
                    bufferSource,
                    0XF000F0);
        });
    }
}
