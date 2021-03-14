package com.fuzs.easymagic.inventory.container;

import com.fuzs.easymagic.EasyMagic;
import com.fuzs.easymagic.element.EasyEnchantingElement;
import com.fuzs.easymagic.mixin.accessor.IEnchantmentContainerAccessor;
import com.fuzs.easymagic.network.message.SEnchantingInfoMessage;
import com.fuzs.puzzleslib_em.PuzzlesLib;
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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("NullableProblems")
public class EnchantmentInventoryContainer extends EnchantmentContainer {

    private Consumer<SEnchantingInfoMessage> sendEnchantingInfo;

    public EnchantmentInventoryContainer(int id, PlayerInventory playerInventory) {

        super(id, playerInventory);
        this.updateInventory(playerInventory, new Inventory(2));
    }

    public EnchantmentInventoryContainer(int id, PlayerInventory playerInventory, IInventory inventory, IWorldPosCallable worldPosCallable) {

        super(id, playerInventory, worldPosCallable);
        this.updateInventory(playerInventory, inventory);
        if (playerInventory.player instanceof ServerPlayerEntity) {

            this.sendEnchantingInfo = message -> PuzzlesLib.getNetworkHandler().sendTo(message, (ServerPlayerEntity) playerInventory.player);
        }

        if (((EasyEnchantingElement) EasyMagic.EASY_ENCHANTING).reRollEnchantments) {

            // set random seed right from the beginning
            this.get().getXpSeed().set(playerInventory.player.getRNG().nextInt());
        }
    }

    public void onCraftMatrixChanged() {

        this.onCraftMatrixChanged(this.get().getTableInventory());
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {
        
        if (inventoryIn == this.get().getTableInventory()) {

            ItemStack enchantedItem = inventoryIn.getStackInSlot(0);
            if (!enchantedItem.isEmpty() && enchantedItem.isEnchantable()) {
                
                this.get().getWorldPosCallable().consume((world, pos) -> {

                    EasyEnchantingElement element = (EasyEnchantingElement) EasyMagic.EASY_ENCHANTING;
                    int power = element.maxPower == 0 ? 15 : (this.getEnchantingPower(world, pos) * 15) / element.maxPower;
                    this.get().getRand().setSeed(this.get().getXpSeed().get());
                    this.updateLevels(enchantedItem, world, pos, power);
                    // need to run this always as enchanting buttons will otherwise be greyed out
                    this.createClues(enchantedItem);
                    this.detectAndSendChanges();
                    if (element.allEnchantments) {

                        List<EnchantmentData> firstSlotData = this.createEnchantmentData(enchantedItem, 0);
                        List<EnchantmentData> secondSlotData = this.createEnchantmentData(enchantedItem, 1);
                        List<EnchantmentData> thirdSlotData = this.createEnchantmentData(enchantedItem, 2);
                        this.sendEnchantingInfo.accept(new SEnchantingInfoMessage(this.windowId, firstSlotData, secondSlotData, thirdSlotData));
                    }
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

            this.enchantLevels[i1] = EnchantmentHelper.calcItemStackEnchantability(this.get().getRand(), i1, power, itemstack);
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

                    EnchantmentData enchantmentdata = list.get(this.get().getRand().nextInt(list.size()));
                    this.enchantClue[j1] = ((ForgeRegistry<Enchantment>) ForgeRegistries.ENCHANTMENTS).getID(enchantmentdata.enchantment);
                    this.worldClue[j1] = enchantmentdata.enchantmentLevel;
                }
            }
        }
    }

    private List<EnchantmentData> createEnchantmentData(ItemStack enchantedItem, int enchantSlot) {

        return this.get().callGetEnchantmentList(enchantedItem, enchantSlot, this.enchantLevels[enchantSlot]);
    }

    private int getEnchantingPower(World world, BlockPos pos) {
        
        int power = 0;
        for (int k = -1; k <= 1; ++k) {
            
            for (int l = -1; l <= 1; ++l) {
                
                if ((k != 0 || l != 0) && this.isBlockEmpty(world, pos.add(l, 0, k)) && this.isBlockEmpty(world, pos.add(l, 1, k))) {
                    
                    power += this.getPower(world, pos.add(l * 2, 0, k * 2));
                    power += this.getPower(world, pos.add(l * 2, 1, k * 2));
                    if (l != 0 && k != 0) {
                        
                        power += this.getPower(world, pos.add(l * 2, 0, k));
                        power += this.getPower(world, pos.add(l * 2, 1, k));
                        power += this.getPower(world, pos.add(l, 0, k * 2));
                        power += this.getPower(world, pos.add(l, 1, k * 2));
                    }
                }
            }
        }
        
        return power;
    }

    private boolean isBlockEmpty(World world, BlockPos pos) {

        if (((EasyEnchantingElement) EasyMagic.EASY_ENCHANTING).lenientBookshelves) {

            return world.getBlockState(pos).getCollisionShape(world, pos).isEmpty();
        }

        return world.isAirBlock(pos);
    }
    
    private float getPower(World world, BlockPos pos) {

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
        
        return this.get().getTableInventory().isUsableByPlayer(playerIn);
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

        if (((EasyEnchantingElement) EasyMagic.EASY_ENCHANTING).itemsStay) {

            this.get().setTableInventory(inventory);
        }

        this.inventorySlots.clear();
        this.addSlot(new EnchantableSlot(this.get().getTableInventory(), 0, 15, 47));
        this.addSlot(new LapisSlot(this.get().getTableInventory(), 1, 35, 47));
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
    
    private IEnchantmentContainerAccessor get() {
        
        return (IEnchantmentContainerAccessor) this;
    }

    private class EnchantableSlot extends Slot {

        public EnchantableSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {

            super(inventoryIn, index, xPosition, yPosition);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {

            if (((EasyEnchantingElement) EasyMagic.EASY_ENCHANTING).itemsStay) {

                return stack.isEnchantable() || stack.getItem() == Items.BOOK;
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

                // set a random seed whenever the item is taken out
                ((IEnchantmentContainerAccessor) EnchantmentInventoryContainer.this).getXpSeed().set(thePlayer.getRNG().nextInt());
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
