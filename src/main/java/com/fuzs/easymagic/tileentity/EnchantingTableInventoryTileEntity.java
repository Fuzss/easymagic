package com.fuzs.easymagic.tileentity;

import com.fuzs.easymagic.element.EasyEnchantingElement;
import com.fuzs.easymagic.inventory.container.IEnchantmentContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.EnchantingTableTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.LockCode;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;

@SuppressWarnings("NullableProblems")
public class EnchantingTableInventoryTileEntity extends EnchantingTableTileEntity implements IInventory, INamedContainerProvider, ISidedInventory {

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);
    private LockCode code = LockCode.EMPTY_CODE;
    
    private final LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.UP, Direction.DOWN, Direction.NORTH);

    @Override
    public TileEntityType<?> getType() {

        // set in super constructor, so just override the whole method
        return EasyEnchantingElement.ENCHANTING_TABLE_TILE_ENTITY;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {

        super.read(state, nbt);
        this.inventory.clear();
        ItemStackHelper.loadAllItems(nbt, this.inventory);
        this.code = LockCode.read(nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {

        this.writeItems(compound);
        this.code.write(compound);
        return compound;
    }

    private CompoundNBT writeItems(CompoundNBT compound) {

        super.write(compound);
        ItemStackHelper.saveAllItems(compound, this.inventory, true);
        return compound;
    }

    @Override
    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket() {

        return new SUpdateTileEntityPacket(this.pos, -1, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {

        return this.writeItems(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt){

        CompoundNBT tag = pkt.getNbtCompound();
        this.inventory.clear();
        ItemStackHelper.loadAllItems(tag, this.inventory);
    }

    @Override
    public void markDirty() {

        super.markDirty();

        assert this.world != null;
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 3);
    }

    @Override
    public int getSizeInventory() {

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
    public ItemStack getStackInSlot(int index) {

        return index >= 0 && index < this.inventory.size() ? this.inventory.get(index) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {

        return ItemStackHelper.getAndSplit(this.inventory, index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {

        return ItemStackHelper.getAndRemove(this.inventory, index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {

        if (index >= 0 && index < this.inventory.size()) {

            this.inventory.set(index, stack);
        }
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {

        assert this.world != null;
        if (this.world.getTileEntity(this.pos) != this) {

            return false;
        } else {

            return !(player.getDistanceSq((double)this.pos.getX() + 0.5, (double)this.pos.getY() + 0.5, (double)this.pos.getZ() + 0.5) > 64.0);
        }
    }

    @Override
    public void clear() {

        this.inventory.clear();
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {

        if (index == 1) {

            return Tags.Items.GEMS_LAPIS.contains(stack.getItem());
        } else if (index == 0) {

            return (stack.isEnchantable() || stack.getItem() == Items.BOOK) && this.inventory.get(index).isEmpty();
        }

        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {

        return side == Direction.UP || side == Direction.DOWN ? new int[]{0} : new int[]{1};
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, @Nullable Direction direction) {

        return this.isItemValidForSlot(index, itemStackIn);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, Direction direction) {

        // only allow extracting of enchantable item
        return index == 0 && (stack.isEnchanted() || stack.getItem() == Items.ENCHANTED_BOOK);
    }

    @Override
    public ITextComponent getDisplayName() {

        return this.getName();
    }

    public boolean canOpen(PlayerEntity p_213904_1_) {

        return LockableTileEntity.canUnlock(p_213904_1_, this.code, this.getDisplayName());
    }

    @Nullable
    @Override
    public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {

        return this.canOpen(p_createMenu_3_) ? this.createMenu(p_createMenu_1_, p_createMenu_2_) : null;
    }

    @SuppressWarnings("ConstantConditions")
    protected Container createMenu(int id, PlayerInventory player) {

        EnchantmentContainer container = new EnchantmentContainer(id, player, IWorldPosCallable.of(this.world, this.pos));
        ((IEnchantmentContainer) container).updateInventory(this, player);
        return container;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {

        if (!this.removed && facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {

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
    protected void invalidateCaps() {

        super.invalidateCaps();
        for (LazyOptional<? extends IItemHandler> handler : this.handlers) {

            handler.invalidate();
        }
    }
    
}
