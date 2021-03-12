package com.fuzs.easymagic.mixin;

import com.fuzs.easymagic.EasyMagic;
import com.fuzs.easymagic.element.EasyEnchantingElement;
import com.fuzs.easymagic.inventory.container.IEnchantmentContainer;
import com.fuzs.easymagic.mixin.accessor.IEnchantmentContainerAccessor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.IntReferenceHolder;
import net.minecraftforge.common.Tags;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("unused")
@Mixin(EnchantmentContainer.class)
public abstract class EnchantmentContainerMixin extends Container implements IEnchantmentContainer {

    @Shadow
    @Final
    private IInventory tableInventory;
    @Shadow
    @Final
    private IntReferenceHolder xpSeed;

    protected EnchantmentContainerMixin(@Nullable ContainerType<?> type, int id) {

        super(type, id);
    }

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;)V", at = @At("TAIL"))
    public void onInit(int id, PlayerInventory playerInventory, CallbackInfo callbackInfo) {

        this.updateInventory(this.tableInventory, playerInventory);
    }

    @Inject(method = "onContainerClosed", at = @At("HEAD"), cancellable = true)
    public void onContainerClosed(PlayerEntity playerIn, CallbackInfo callbackInfo) {

        EasyEnchantingElement element = (EasyEnchantingElement) EasyMagic.EASY_ENCHANTING;
        if (element.isEnabled()) {

            // update seed when container is closed
            if (element.rerollEnchantments) {

                playerIn.onEnchant(ItemStack.EMPTY, 0);
            }

            if (element.itemsStay) {

                super.onContainerClosed(playerIn);
                callbackInfo.cancel();
            }
        }
    }

    @Inject(method = "canInteractWith", at = @At("HEAD"), cancellable = true)
    public void canInteractWith(PlayerEntity playerIn, CallbackInfoReturnable<Boolean> callbackInfo) {

        EasyEnchantingElement element = (EasyEnchantingElement) EasyMagic.EASY_ENCHANTING;
        if (element.isEnabled() && element.itemsStay) {

            callbackInfo.setReturnValue(this.tableInventory.isUsableByPlayer(playerIn));
        }
    }

    @Override
    public void updateInventory(IInventory tileEntityInventory, PlayerInventory playerInventory) {

        ((IEnchantmentContainerAccessor) this).setTableInventory(tileEntityInventory);
        this.inventorySlots.clear();
        this.addSlot(new Slot(this.tableInventory, 0, 15, 47) {

            @Override
            public boolean isItemValid(@Nonnull ItemStack stack) {

                return stack.isEnchantable() || stack.getItem() == Items.BOOK;
            }

            @Override
            public int getSlotStackLimit() {

                return 1;
            }

            @Override
            @Nonnull
            public ItemStack onTake(@Nonnull PlayerEntity thePlayer, @Nonnull ItemStack stack) {

                // generate new enchantment seed and set it in the container
                thePlayer.onEnchant(ItemStack.EMPTY, 0);
                EnchantmentContainerMixin.this.xpSeed.set(thePlayer.getXPSeed());
                return super.onTake(thePlayer, stack);
            }

            @Override
            public void onSlotChange(@Nonnull ItemStack oldStackIn, @Nonnull ItemStack newStackIn) {

                super.onSlotChange(oldStackIn, newStackIn);
                EnchantmentContainerMixin.this.onCraftMatrixChanged(EnchantmentContainerMixin.this.tableInventory);
            }

        });
        this.addSlot(new Slot(this.tableInventory, 1, 35, 47) {

            @Override
            public boolean isItemValid(@Nonnull ItemStack stack) {

                return Tags.Items.GEMS_LAPIS.contains(stack.getItem());
            }

            @Override
            public void onSlotChange(@Nonnull ItemStack oldStackIn, @Nonnull ItemStack newStackIn) {

                super.onSlotChange(oldStackIn, newStackIn);
                EnchantmentContainerMixin.this.onCraftMatrixChanged(EnchantmentContainerMixin.this.tableInventory);
            }

        });

        for (int i = 0; i < 3; ++i) {

            for(int j = 0; j < 9; ++j) {

                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {

            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

}
