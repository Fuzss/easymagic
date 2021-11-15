package fuzs.easymagic.world.level.block.entity;

import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.world.inventory.ModEnchantmentMenu;
import fuzs.easymagic.registry.ModRegistry;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("NullableProblems")
public class ModEnchantmentTableBlockEntity extends EnchantmentTableBlockEntity implements Container, MenuProvider, WorldlyContainer {
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);
    private final List<WeakReference<ModEnchantmentMenu>> containerReferences = Lists.newArrayList();
    private LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.UP, Direction.DOWN, Direction.NORTH);
    private LockCode code = LockCode.NO_LOCK;

    public ModEnchantmentTableBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(pWorldPosition, pBlockState);
    }

    @Override
    public BlockEntityType<?> getType() {
        // set in super constructor, so just override the whole method
        return ModRegistry.ENCHANTING_TABLE_BLOCK_ENTITY_TYPE.get();
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.inventory.clear();
        ContainerHelper.loadAllItems(nbt, this.inventory);
        this.code = LockCode.fromTag(nbt);
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        this.saveItems(compound);
        this.code.addToTag(compound);
        return compound;
    }

    private CompoundTag saveItems(CompoundTag compound) {
        super.save(compound);
        ContainerHelper.saveAllItems(compound, this.inventory, true);
        return compound;
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, -1, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveItems(new CompoundTag());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt){
        CompoundTag tag = pkt.getTag();
        this.inventory.clear();
        ContainerHelper.loadAllItems(tag, this.inventory);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            this.updateReferences(container -> container.slotsChanged(this));
        }
    }

    public void updateReferences(Consumer<ModEnchantmentMenu> action) {
        Iterator<WeakReference<ModEnchantmentMenu>> iterator = this.containerReferences.iterator();
        while (iterator.hasNext()) {
            ModEnchantmentMenu container = iterator.next().get();
            if (container != null && container.getUser().containerMenu == container) {
                action.accept(container);
            } else {
                iterator.remove();
            }
        }
    }

    @Override
    public int getContainerSize() {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.inventory) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        return index >= 0 && index < this.inventory.size() ? this.inventory.get(index) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return ContainerHelper.removeItem(this.inventory, index, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(this.inventory, index);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index >= 0 && index < this.inventory.size()) {
            this.inventory.set(index, stack);
        }
        this.setChanged();
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
    public void clearContent() {
        this.inventory.clear();
        this.setChanged();
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (EasyMagic.CONFIG.server().itemsStay) {
            if (index == 1) {
                return Tags.Items.GEMS_LAPIS.contains(stack.getItem());
            } else if (index == 0) {
                return (stack.isEnchantable() || stack.getItem() instanceof BookItem) && this.inventory.get(index).isEmpty();
            }
        }
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.UP || side == Direction.DOWN ? new int[]{0} : new int[]{1};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, @Nullable Direction direction) {
        if (EasyMagic.CONFIG.server().itemsStay) {
            return this.canPlaceItem(index, itemStackIn);
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        if (EasyMagic.CONFIG.server().itemsStay) {
            // only allow extracting of enchantable item
            return index == 0 && (stack.isEnchanted() || stack.getItem() instanceof EnchantedBookItem);
        }
        return false;
    }

    @Override
    public Component getDisplayName() {
        return this.getName();
    }

    public boolean canOpen(Player p_213904_1_) {
        return BaseContainerBlockEntity.canUnlock(p_213904_1_, this.code, this.getDisplayName());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return this.canOpen(player) ? this.createMenu(id, playerInventory) : null;
    }

    @SuppressWarnings("ConstantConditions")
    protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        ModEnchantmentMenu container = new ModEnchantmentMenu(id, playerInventory, this, ContainerLevelAccess.create(this.level, this.worldPosition));
        this.containerReferences.add(new WeakReference<>(container));
        return container;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (!this.remove && facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == Direction.UP) {
                return this.handlers[0].cast();
            } else if (facing == Direction.DOWN) {
                return this.handlers[1].cast();
            } else {
                return this.handlers[2].cast();
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        for (LazyOptional<? extends IItemHandler> handler : this.handlers) {
            handler.invalidate();
        }
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        this.handlers = SidedInvWrapper.create(this, Direction.UP, Direction.DOWN, Direction.NORTH);
    }
}
