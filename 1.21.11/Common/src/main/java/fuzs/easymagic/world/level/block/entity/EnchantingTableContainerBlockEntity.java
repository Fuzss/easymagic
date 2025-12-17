package fuzs.easymagic.world.level.block.entity;

import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.config.ServerConfig;
import fuzs.easymagic.init.ModRegistry;
import fuzs.easymagic.world.inventory.ModEnchantmentMenu;
import fuzs.puzzleslib.api.block.v1.entity.TickingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class EnchantingTableContainerBlockEntity extends BaseEnchantingTableContainerBlockEntity implements WorldlyContainer, TickingBlockEntity {
    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);

    public EnchantingTableContainerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModRegistry.ENCHANTING_TABLE_BLOCK_ENTITY_TYPE.value(), blockPos, blockState);
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModRegistry.ENCHANTING_TABLE_BLOCK_ENTITY_TYPE.value();
    }

    @Override
    public void clientTick() {
        bookAnimationTick(this.getLevel(), this.getBlockPos(), this.getBlockState(), this);
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.hasLevel()) {
            this.getLevel()
                    .sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public NonNullList<ItemStack> getContainerItems() {
        return this.items;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack itemStack) {
        if (index == 2) {
            return itemStack.is(ModRegistry.REROLL_CATALYSTS_ITEM_TAG);
        } else if (index == 1) {
            return itemStack.is(ModRegistry.ENCHANTING_CATALYSTS_ITEM_TAG);
        } else if (index == 0) {
            return this.items.getFirst().isEmpty();
        } else {
            return false;
        }
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        if (direction.getAxis().isHorizontal()) {
            return EasyMagic.CONFIG.get(ServerConfig.class).dedicatedRerollCatalyst() ? new int[]{1, 2} : new int[]{1};
        } else {
            return new int[]{0};
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        return this.canPlaceItem(index, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack itemStack, Direction direction) {
        // only allow extracting of enchantable item
        return index == 0 && (itemStack.isEnchanted() || itemStack.is(Items.ENCHANTED_BOOK));
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
        return new ModEnchantmentMenu(containerId,
                playerInventory,
                this,
                ContainerLevelAccess.create(this.getLevel(), this.worldPosition));
    }
}
