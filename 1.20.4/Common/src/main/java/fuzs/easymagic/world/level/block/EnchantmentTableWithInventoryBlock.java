package fuzs.easymagic.world.level.block;

import fuzs.easymagic.init.ModRegistry;
import fuzs.easymagic.world.inventory.ModEnchantmentMenu;
import fuzs.easymagic.world.level.block.entity.EnchantmentTableWithInventoryBlockEntity;
import fuzs.puzzleslib.api.block.v1.entity.TickingEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class EnchantmentTableWithInventoryBlock extends EnchantmentTableBlock implements TickingEntityBlock<EnchantmentTableWithInventoryBlockEntity> {

    public EnchantmentTableWithInventoryBlock(Properties properties) {
        super(properties);
    }

    @Override
    public String getDescriptionId() {
        return Blocks.ENCHANTING_TABLE.getDescriptionId();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return TickingEntityBlock.super.newBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return TickingEntityBlock.super.getTicker(level, state, blockEntityType);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            if (level.getBlockEntity(pos) instanceof EnchantmentTableWithInventoryBlockEntity blockEntity) {
                player.openMenu(blockEntity);
                if (player.containerMenu instanceof ModEnchantmentMenu) {
                    // items might still be in inventory slots, so this needs to update so that enchantment buttons are shown
                    player.containerMenu.slotsChanged(blockEntity);
                }
            }
            return InteractionResult.CONSUME;
        }
    }

    @Nullable
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        // copied from BaseEntityBlock, used for spectator mode
        return level.getBlockEntity(pos) instanceof MenuProvider menuProvider ? menuProvider : null;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof Container container) {
            return Math.min(container.getItem(1).getCount(), 3);
        }
        return 0;
    }

    @Override
    public BlockEntityType<? extends EnchantmentTableWithInventoryBlockEntity> getBlockEntityType() {
        return ModRegistry.ENCHANTING_TABLE_BLOCK_ENTITY_TYPE.value();
    }
}
