package fuzs.easymagic.handler;

import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.config.ServerConfig;
import fuzs.easymagic.init.ModRegistry;
import fuzs.puzzleslib.api.block.v1.BlockConversionHelper;
import fuzs.puzzleslib.api.event.v1.core.EventResultHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

public class BlockConversionHandler {
    public static final Component INVALID_BLOCK_COMPONENT = Component.translatable("container.invalidBlock");

    public static void onTagsUpdated(RegistryAccess registryAccess, boolean client) {
        if (!client) {
            BlockConversionHelper.copyBoundTags(Blocks.ENCHANTING_TABLE, ModRegistry.ENCHANTMENT_TABLE_BLOCK.value());
        }
    }

    public static EventResultHolder<InteractionResult> onUseBlock(Player player, Level level, InteractionHand interactionHand, BlockHitResult hitResult) {
        if (!EasyMagic.CONFIG.get(ServerConfig.class).disableVanillaEnchantingTable) return EventResultHolder.pass();
        if (level.getBlockState(hitResult.getBlockPos()).is(Blocks.ENCHANTING_TABLE)) {
            player.displayClientMessage(Component.empty().append(INVALID_BLOCK_COMPONENT).withStyle(ChatFormatting.RED), true);
            return EventResultHolder.interrupt(InteractionResult.sidedSuccess(level.isClientSide));
        } else {
            return EventResultHolder.pass();
        }
    }
}
