package fuzs.easymagic.client.gui.components;

import fuzs.easymagic.world.inventory.ModEnchantmentMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

public class EnchantmentSlotButton extends ImageButton implements Tickable {
    private static final WidgetSprites WIDGET_SPRITES = new WidgetSprites(EnchantmentScreen.ENCHANTMENT_SLOT_SPRITE,
            EnchantmentScreen.ENCHANTMENT_SLOT_DISABLED_SPRITE,
            EnchantmentScreen.ENCHANTMENT_SLOT_HIGHLIGHTED_SPRITE);

    private final ResourceLocation disabledLevelSpriteLocation;
    private final ResourceLocation enabledLevelSpriteLocation;
    private final int slotIndex;
    private final ModEnchantmentMenu menu;

    public EnchantmentSlotButton(int x, int y, OnPress onPress, ResourceLocation disabledLevelSpriteLocation, ResourceLocation enabledLevelSpriteLocation, int slotIndex, ModEnchantmentMenu menu) {
        super(x, y, 108, 19, WIDGET_SPRITES, onPress);
        this.disabledLevelSpriteLocation = disabledLevelSpriteLocation;
        this.enabledLevelSpriteLocation = enabledLevelSpriteLocation;
        this.slotIndex = slotIndex;
        this.menu = menu;
    }

    @Override
    public void tick() {
        this.visible = this.getCost() != 0;
        this.active = this.menu.player.getAbilities().instabuild
                || this.menu.getGoldCount() >= this.slotIndex + 1 && this.menu.player.experienceLevel >= this.getCost();
    }

    protected int getCost() {
        return this.menu.costs[this.slotIndex];
    }

    @Override
    public void playDownSound(SoundManager handler) {
        // NO-OP
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        Font font = Minecraft.getInstance().font;
        String string = String.valueOf(this.getCost());
        int textOffsetLeft = this.getX() + 20;
        int textOffsetRight = 86 - font.width(string);
        FormattedText formattedText = EnchantmentNames.getInstance().getRandomName(font, textOffsetRight);

        if (!this.isActive()) {
            guiGraphics.blitSprite(this.disabledLevelSpriteLocation, this.getX() + 1, this.getY() + 1, 16, 16);
            guiGraphics.drawWordWrap(font,
                    formattedText,
                    textOffsetLeft,
                    this.getY() + 2,
                    textOffsetRight,
                    FastColor.ABGR32.opaque(this.getRuneTextColor()));
        } else {
            guiGraphics.blitSprite(this.enabledLevelSpriteLocation, this.getX() + 1, this.getY() + 1, 16, 16);
            guiGraphics.drawWordWrap(font,
                    formattedText,
                    textOffsetLeft,
                    this.getY() + 2,
                    textOffsetRight,
                    FastColor.ABGR32.opaque(this.getRuneTextColor()));
        }

        guiGraphics.drawString(font,
                string,
                textOffsetLeft + textOffsetRight,
                this.getY() + 2 + 7,
                FastColor.ABGR32.opaque(this.getCostTextColor()));
    }

    private int getRuneTextColor() {
        return this.isActive() ? this.isHoveredOrFocused() ? 16777088 : 6839882 : (6839882 & 16711422) >> 1;
    }

    private int getCostTextColor() {
        return this.isActive() ? 8453920 : 4226832;
    }
}
