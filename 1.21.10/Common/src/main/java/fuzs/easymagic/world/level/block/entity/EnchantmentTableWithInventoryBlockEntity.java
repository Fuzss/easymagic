package fuzs.easymagic.world.level.block.entity;

import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.config.ServerConfig;
import fuzs.easymagic.init.ModRegistry;
import fuzs.easymagic.world.inventory.ModEnchantmentMenu;
import fuzs.puzzleslib.api.block.v1.entity.TickingBlockEntity;
import fuzs.puzzleslib.api.container.v1.ListBackedContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class EnchantmentTableWithInventoryBlockEntity extends EnchantingTableBlockEntity implements ListBackedContainer, MenuProvider, WorldlyContainer, TickingBlockEntity {
    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private LockCode code = LockCode.NO_LOCK;

    public EnchantmentTableWithInventoryBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModRegistry.ENCHANTING_TABLE_BLOCK_ENTITY_TYPE.value();
    }

    @Override
    public void loadAdditional(ValueInput compoundTag) {
        super.loadAdditional(compoundTag);
        this.code = LockCode.fromTag(compoundTag);
        this.items.clear();
        ContainerHelper.loadAllItems(compoundTag, this.items);
    }

    @Override
    protected void saveAdditional(ValueOutput compoundTag) {
        super.saveAdditional(compoundTag);
        this.code.addToTag(compoundTag);
        ContainerHelper.saveAllItems(compoundTag, this.items, true);
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
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public NonNullList<ItemStack> getContainerItems() {
        return this.items;
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack itemStack) {
        if (index == 2) {
            return itemStack.is(ModRegistry.REROLL_CATALYSTS_ITEM_TAG);
        } else if (index == 1) {
            return itemStack.is(ModRegistry.ENCHANTING_CATALYSTS_ITEM_TAG);
        } else if (index == 0) {
            return this.items.get(0).isEmpty();
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
    public Component getDisplayName() {
        return this.getName();
    }

    public boolean canOpen(Player player) {
        return BaseContainerBlockEntity.canUnlock(player, this.code, this.getDisplayName());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return this.canOpen(player) ? this.createMenu(id, playerInventory) : null;
    }

    @SuppressWarnings("ConstantConditions")
    protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        return new ModEnchantmentMenu(id,
                playerInventory,
                this,
                ContainerLevelAccess.create(this.level, this.worldPosition));
    }

    @Override
    public void clientTick() {
        bookAnimationTick(this.getLevel(), this.getBlockPos(), this.getBlockState(), this);
    }
}
