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
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.LockCode;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
    public void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider registries) {
        super.loadAdditional(compoundTag, registries);
        this.code = LockCode.fromTag(compoundTag);
        this.items.clear();
        ContainerHelper.loadAllItems(compoundTag, this.items, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider registries) {
        super.saveAdditional(compoundTag, registries);
        this.code.addToTag(compoundTag);
        ContainerHelper.saveAllItems(compoundTag, this.items, true, registries);
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
        return EasyMagic.CONFIG.get(ServerConfig.class).dedicatedRerollCatalyst ? 3 : 2;
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level != null && this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return !(player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) > 64.0);
        }
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack itemStack) {
        if (index == 2) {
            return itemStack.is(ModRegistry.REROLL_CATALYSTS_ITEM_TAG);
        } else if (index == 1) {
            return itemStack.is(ModRegistry.ENCHANTING_CATALYSTS_ITEM_TAG);
        } else if (index == 0) {
            return this.items.get(0).isEmpty();
        }
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        if (direction.getAxis().isHorizontal()) {
            return EasyMagic.CONFIG.get(ServerConfig.class).dedicatedRerollCatalyst ? new int[]{1, 2} : new int[]{1};
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
        return index == 0 && (itemStack.isEnchanted() || itemStack.getItem() instanceof EnchantedBookItem);
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
        return new ModEnchantmentMenu(id, playerInventory, this, ContainerLevelAccess.create(this.level, this.worldPosition));
    }

    @Override
    public void clientTick() {
        bookAnimationTick(this.getLevel(), this.getBlockPos(), this.getBlockState(), this);
    }
}
