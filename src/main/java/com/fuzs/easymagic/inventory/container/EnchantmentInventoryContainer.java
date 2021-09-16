package com.fuzs.easymagic.inventory.container;

import com.fuzs.easymagic.EasyMagic;
import com.fuzs.easymagic.element.EasyEnchantingElement;
import com.fuzs.easymagic.mixin.accessor.EnchantmentContainerAccessor;
import com.fuzs.easymagic.network.message.S2CEnchantingInfoMessage;
import com.fuzs.easymagic.tileentity.EnchantingTableInventoryTileEntity;
import com.fuzs.puzzleslib_em.PuzzlesLib;
import com.google.common.collect.Lists;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import javax.annotation.Nonnull;
import java.util.List;

@SuppressWarnings("NullableProblems")
public class EnchantmentInventoryContainer extends EnchantmentContainer {

    private final PlayerEntity user;

    public EnchantmentInventoryContainer(int id, PlayerInventory playerInventory) {

        super(id, playerInventory);
        this.updateInventory(playerInventory, new Inventory(2));
        this.user = playerInventory.player;
    }

    public EnchantmentInventoryContainer(int id, PlayerInventory playerInventory, IInventory inventory, IWorldPosCallable worldPosCallable) {

        super(id, playerInventory, worldPosCallable);
        this.updateInventory(playerInventory, inventory);
        this.user = playerInventory.player;
        if (((EasyEnchantingElement) EasyMagic.EASY_ENCHANTING).reRollEnchantments) {

            // set random seed right from the beginning
            ((EnchantmentContainerAccessor) this).getXpSeed().set(playerInventory.player.getRNG().nextInt());
        }
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {

        EnchantmentContainerAccessor accessor = (EnchantmentContainerAccessor) this;
        if (inventoryIn == accessor.getTableInventory()) {

            ItemStack enchantedItem = inventoryIn.getStackInSlot(0);
            if (!enchantedItem.isEmpty() && enchantedItem.isEnchantable()) {

                accessor.getWorldPosCallable().consume((world, pos) -> {

                    EasyEnchantingElement element = (EasyEnchantingElement) EasyMagic.EASY_ENCHANTING;
                    int power = element.maxPower == 0 ? 15 : (getEnchantingPower(world, pos) * 15) / element.maxPower;

                    accessor.getRand().setSeed(accessor.getXpSeed().get());
                    this.updateLevels(enchantedItem, world, pos, power);
                    // need to run this always as enchanting buttons will otherwise be greyed out
                    this.createClues(enchantedItem);
                    this.detectAndSendChanges();
                    this.sendEnchantingInfo(enchantedItem, element.showEnchantments);
                });
            } else {

                this.resetLevelsAndClues();
            }
        }
    }

    private void resetLevelsAndClues() {

        for (int i = 0; i < 3; ++i) {

            this.enchantLevels[i] = 0;
            this.enchantClue[i] = -1;
            this.worldClue[i] = -1;
        }
    }

    private void updateLevels(ItemStack itemstack, World world, BlockPos pos, int power) {

        for (int i1 = 0; i1 < 3; ++i1) {

            this.enchantLevels[i1] = EnchantmentHelper.calcItemStackEnchantability(((EnchantmentContainerAccessor) this).getRand(), i1, power, itemstack);
            if (this.enchantLevels[i1] < i1 + 1) {

                this.enchantLevels[i1] = 0;
            }

            this.enchantLevels[i1] = ForgeEventFactory.onEnchantmentLevelSet(world, pos, i1, power, itemstack, this.enchantLevels[i1]);
        }
    }

    private void createClues(ItemStack itemstack) {

        for (int j1 = 0; j1 < 3; ++j1) {

            if (this.enchantLevels[j1] > 0) {

                List<EnchantmentData> list = this.createEnchantmentData(itemstack, j1);
                if (list != null && !list.isEmpty()) {

                    EnchantmentData enchantmentdata = list.get(((EnchantmentContainerAccessor) this).getRand().nextInt(list.size()));
                    this.enchantClue[j1] = ((ForgeRegistry<Enchantment>) ForgeRegistries.ENCHANTMENTS).getID(enchantmentdata.enchantment);
                    this.worldClue[j1] = enchantmentdata.enchantmentLevel;
                }
            }
        }
    }

    private List<EnchantmentData> createEnchantmentData(ItemStack enchantedItem, int enchantSlot) {

        return ((EnchantmentContainerAccessor) this).callGetEnchantmentList(enchantedItem, enchantSlot, this.enchantLevels[enchantSlot]);
    }

    private List<EnchantmentData> getEnchantmentHint(ItemStack enchantedItem, int enchantSlot, EasyEnchantingElement.ShowEnchantments showEnchantments) {

        switch (showEnchantments) {

            case NONE:

                return Lists.newArrayList();
            case SINGLE:

                List<EnchantmentData> enchantmentData = this.createEnchantmentData(enchantedItem, enchantSlot);

                return Lists.newArrayList(enchantmentData.get(((EnchantmentContainerAccessor) this).getRand().nextInt(enchantmentData.size())));
            case ALL:

                return this.createEnchantmentData(enchantedItem, enchantSlot);
        }

        throw new IllegalStateException("no enum types left");
    }

    private void sendEnchantingInfo(ItemStack enchantedItem, EasyEnchantingElement.ShowEnchantments showEnchantments) {

        List<EnchantmentData> firstSlotData = this.getEnchantmentHint(enchantedItem, 0, showEnchantments);
        List<EnchantmentData> secondSlotData = this.getEnchantmentHint(enchantedItem, 1, showEnchantments);
        List<EnchantmentData> thirdSlotData = this.getEnchantmentHint(enchantedItem, 2, showEnchantments);
        PuzzlesLib.getNetworkHandler().sendTo(new S2CEnchantingInfoMessage(this.windowId, firstSlotData, secondSlotData, thirdSlotData), (ServerPlayerEntity) this.user);
    }

    public static int getEnchantingPower(World world, BlockPos pos) {
        
        int power = 0;
        for (int k = -1; k <= 1; ++k) {
            
            for (int l = -1; l <= 1; ++l) {
                
                if ((k != 0 || l != 0) && isBlockEmpty(world, pos.add(l, 0, k)) && isBlockEmpty(world, pos.add(l, 1, k))) {
                    
                    power += getPower(world, pos.add(l * 2, 0, k * 2));
                    power += getPower(world, pos.add(l * 2, 1, k * 2));
                    if (l != 0 && k != 0) {
                        
                        power += getPower(world, pos.add(l * 2, 0, k));
                        power += getPower(world, pos.add(l * 2, 1, k));
                        power += getPower(world, pos.add(l, 0, k * 2));
                        power += getPower(world, pos.add(l, 1, k * 2));
                    }
                }
            }
        }
        
        return power;
    }

    public static boolean isBlockEmpty(World world, BlockPos pos) {

        if (((EasyEnchantingElement) EasyMagic.EASY_ENCHANTING).lenientBookshelves) {

            return world.getBlockState(pos).getCollisionShape(world, pos).isEmpty();
        }

        return world.isAirBlock(pos);
    }
    
    private static float getPower(World world, BlockPos pos) {

        return world.getBlockState(pos).getEnchantPowerBonus(world, pos);
    }

    @Override
    public ContainerType<?> getType() {
        
        return EasyEnchantingElement.ENCHANTMENT_CONTAINER;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {

        EasyEnchantingElement element = (EasyEnchantingElement) EasyMagic.EASY_ENCHANTING;
        if (!element.itemsStay) {

            return super.canInteractWith(playerIn);
        }

        return ((EnchantmentContainerAccessor) this).getTableInventory().isUsableByPlayer(playerIn);
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {

        EasyEnchantingElement element = (EasyEnchantingElement) EasyMagic.EASY_ENCHANTING;
        if (!element.itemsStay) {

            super.onContainerClosed(playerIn);
            return;
        }

        // copied from container super method
        PlayerInventory playerinventory = playerIn.inventory;
        if (!playerinventory.getItemStack().isEmpty()) {
            
            playerIn.dropItem(playerinventory.getItemStack(), false);
            playerinventory.setItemStack(ItemStack.EMPTY);
        }
    }
    
    private void updateInventory(PlayerInventory playerInventory, IInventory inventory) {

        EnchantmentContainerAccessor accessor = (EnchantmentContainerAccessor) this;
        if (((EasyEnchantingElement) EasyMagic.EASY_ENCHANTING).itemsStay) {

            accessor.setTableInventory(inventory);
        }

        this.inventorySlots.clear();
        this.addSlot(new EnchantableSlot(accessor.getTableInventory(), 0, 15, 47));
        this.addSlot(new LapisSlot(accessor.getTableInventory(), 1, 35, 47));
        this.addPlayerSlots(playerInventory);
    }

    private void addPlayerSlots(PlayerInventory playerInventory) {

        for (int i = 0; i < 3; ++i) {

            for(int j = 0; j < 9; ++j) {

                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {

            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    public PlayerEntity getUser() {

        return this.user;
    }

    public void reseed() {

        ((EnchantmentContainerAccessor) EnchantmentInventoryContainer.this).getXpSeed().set(this.user.getRNG().nextInt());
    }

    private class EnchantableSlot extends Slot {

        public EnchantableSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {

            super(inventoryIn, index, xPosition, yPosition);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {

            if (((EasyEnchantingElement) EasyMagic.EASY_ENCHANTING).itemsStay) {

                // can't exchange items directly while holding replacement otherwise, this seems to do the trick
                return stack.isEnchantable() || stack.getItem() instanceof BookItem && !this.getHasStack();
            }

            return true;
        }

        @Override
        public int getSlotStackLimit() {

            return 1;
        }

        @Override
        @Nonnull
        public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {

            if (((EasyEnchantingElement) EasyMagic.EASY_ENCHANTING).reRollEnchantments) {

                // set a random seed for everyone using same tile entity whenever the item is taken out
                IInventory tableInventory = ((EnchantmentContainerAccessor) EnchantmentInventoryContainer.this).getTableInventory();
                if (tableInventory instanceof EnchantingTableInventoryTileEntity) {

                    ((EnchantingTableInventoryTileEntity) tableInventory).updateReferences(EnchantmentInventoryContainer::reseed);
                }
            }

            return super.onTake(thePlayer, stack);
        }

    }

    private static class LapisSlot extends Slot {

        public LapisSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {

            super(inventoryIn, index, xPosition, yPosition);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {

            return Tags.Items.GEMS_LAPIS.contains(stack.getItem());
        }

    }

}
