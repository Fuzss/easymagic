package com.fuzs.easymagic.element;

import com.fuzs.easymagic.EasyMagic;
import com.fuzs.easymagic.client.element.GearSlotsEverywhereExtension;
import com.fuzs.easymagic.inventory.container.IMovableSlot;
import com.fuzs.easymagic.inventory.container.MovableCraftingSlot;
import com.fuzs.easymagic.inventory.container.MovableSlot;
import com.fuzs.easymagic.mixin.accessor.ContainerAccessor;
import com.fuzs.easymagic.network.message.S2CCraftingSlotsMessage;
import com.fuzs.easymagic.network.message.S2CGearSlotsMessage;
import com.fuzs.easymagic.network.message.S2COpenTabMessage;
import com.fuzs.puzzleslib_em.PuzzlesLib;
import com.fuzs.puzzleslib_em.element.extension.ClientExtensibleElement;
import com.mojang.datafixers.util.Pair;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.LogicalSide;

import java.util.Optional;

public class GearSlotsEverywhereElement extends ClientExtensibleElement<GearSlotsEverywhereExtension> {

    private static final ResourceLocation[] ARMOR_SLOT_TEXTURES = new ResourceLocation[]{PlayerContainer.EMPTY_ARMOR_SLOT_BOOTS, PlayerContainer.EMPTY_ARMOR_SLOT_LEGGINGS, PlayerContainer.EMPTY_ARMOR_SLOT_CHESTPLATE, PlayerContainer.EMPTY_ARMOR_SLOT_HELMET};
    private static final EquipmentSlotType[] VALID_EQUIPMENT_SLOTS = new EquipmentSlotType[]{EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET};

    public GearSlotsEverywhereElement() {

        super(element -> new GearSlotsEverywhereExtension((GearSlotsEverywhereElement) element));
    }

    @Override
    public String getDescription() {

        return "Armor slots and off-hand accessible in every container.";
    }

    @Override
    public void setupCommon() {

        this.addListener(this::onContainerOpen);
    }

    @Override
    public void initCommon() {

        PuzzlesLib.getNetworkHandler().registerMessage(S2CGearSlotsMessage::new, LogicalSide.CLIENT);
        PuzzlesLib.getNetworkHandler().registerMessage(S2CCraftingSlotsMessage::new, LogicalSide.CLIENT);
        PuzzlesLib.getNetworkHandler().registerMessage(S2COpenTabMessage::new, LogicalSide.CLIENT);
    }

    private void onContainerOpen(final PlayerContainerEvent.Open evt) {

        Container container = evt.getContainer();
        PlayerEntity player = evt.getPlayer();

        int[] gearSlotNumbers = addGearSlots(container, player);
        PuzzlesLib.getNetworkHandler().sendTo(new S2CGearSlotsMessage(container.windowId, gearSlotNumbers), (ServerPlayerEntity) player);

        if (!(container instanceof RecipeBookContainer) || ((RecipeBookContainer<?>) container).getWidth() != 3 || ((RecipeBookContainer<?>) container).getHeight() != 3) {

            int[] craftingSlotNumbers = addCraftingSlots(container, player);
            PuzzlesLib.getNetworkHandler().sendTo(new S2CCraftingSlotsMessage(container.windowId, craftingSlotNumbers), (ServerPlayerEntity) player);
        }
    }

    private void onContainerClose(final PlayerContainerEvent.Close evt, IInventory craftMatrix, Container container, Runnable unregister) {

        if (container == evt.getContainer()) {

            this.clearContainer(evt.getPlayer(), evt.getPlayer().world, craftMatrix);
        }

        unregister.run();
    }

    private void clearContainer(PlayerEntity playerIn, World worldIn, IInventory inventoryIn) {

        if (!playerIn.isAlive() || playerIn instanceof ServerPlayerEntity && ((ServerPlayerEntity)playerIn).hasDisconnected()) {

            for (int j = 0; j < inventoryIn.getSizeInventory(); ++j) {

                playerIn.dropItem(inventoryIn.removeStackFromSlot(j), false);
            }
        } else {

            for (int i = 0; i < inventoryIn.getSizeInventory(); ++i) {

                playerIn.inventory.placeItemBackInInventory(worldIn, inventoryIn.removeStackFromSlot(i));
            }
        }
    }

    public static int[] addCraftingSlots(Container container, PlayerEntity player) {

        int[] slotNumbers = new int[10];
        CraftResultInventory craftResult = new CraftResultInventory();
        CraftingInventory craftMatrix = new CraftingInventory(container, 3, 3) {

            @Override
            public ItemStack decrStackSize(int index, int count) {

                ItemStack itemstack = super.decrStackSize(index, count);
                if (!itemstack.isEmpty()) {

                    updateCraftingResult(container.windowId, player.world, player, this, craftResult, slotNumbers);
                }

                return itemstack;
            }

            @Override
            public void setInventorySlotContents(int index, ItemStack stack) {

                super.setInventorySlotContents(index, stack);
                updateCraftingResult(container.windowId, player.world, player, this, craftResult, slotNumbers);
            }

        };

        slotNumbers[0] = ((ContainerAccessor) container).callAddSlot(new MovableCraftingSlot(player, craftMatrix, craftResult, 0)).slotNumber;
        for (int i = 0; i < 3; ++i) {

            for (int j = 0; j < 3; ++j) {

                slotNumbers[j + i * 3 + 1] = ((ContainerAccessor) container).callAddSlot(new MovableSlot(craftMatrix, j + i * 3)).slotNumber;
            }
        }

        return slotNumbers;
    }

    private static void updateCraftingResult(int id, World world, PlayerEntity player, CraftingInventory inventory, CraftResultInventory inventoryResult, int[] slotNumbers) {

        // copied from WorkbenchContainer, only SSetSlotPacket slot has been changed
        if (!world.isRemote) {

            ServerPlayerEntity serverplayerentity = (ServerPlayerEntity) player;
            ItemStack itemstack = ItemStack.EMPTY;
            Optional<ICraftingRecipe> optional = world.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, inventory, world);
            if (optional.isPresent()) {

                ICraftingRecipe icraftingrecipe = optional.get();
                if (inventoryResult.canUseRecipe(world, serverplayerentity, icraftingrecipe)) {

                    itemstack = icraftingrecipe.getCraftingResult(inventory);
                }
            }

            inventoryResult.setInventorySlotContents(0, itemstack);
            serverplayerentity.connection.sendPacket(new SSetSlotPacket(id, slotNumbers[0], itemstack));
        }
    }

    public static int[] addGearSlots(Container container, PlayerEntity player) {

        int[] slotNumbers = new int[5];
        PlayerInventory playerInventory = player.inventory;
        // copied from player inventory
        for (int i = 0; i < 4; i++) {

            final EquipmentSlotType equipmentslottype = VALID_EQUIPMENT_SLOTS[i];
            slotNumbers[i] = ((ContainerAccessor) container).callAddSlot(new MovableSlot(playerInventory, 39 - i) {

                @Override
                public int getSlotStackLimit() {

                    return 1;
                }

                @Override
                public boolean isItemValid(ItemStack stack) {

                    return stack.canEquip(equipmentslottype, player);
                }

                @Override
                public boolean canTakeStack(PlayerEntity playerIn) {

                    ItemStack itemstack = this.getStack();
                    return (itemstack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(itemstack)) && super.canTakeStack(playerIn);
                }

                @Override
                @OnlyIn(Dist.CLIENT)
                public Pair<ResourceLocation, ResourceLocation> getBackground() {

                    return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, ARMOR_SLOT_TEXTURES[equipmentslottype.getIndex()]);
                }

            }).slotNumber;
        }

        slotNumbers[4] = ((ContainerAccessor) container).callAddSlot(new MovableSlot(playerInventory, 40) {

            @Override
            @OnlyIn(Dist.CLIENT)
            public Pair<ResourceLocation, ResourceLocation> getBackground() {

                return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_SHIELD);
            }

        }).slotNumber;

        return slotNumbers;
    }

    public static void setupSlotPositions(Container container, int[] slotNumberToPosition) {

        for (int i = 0; i < slotNumberToPosition.length; i += 3) {

            int slotIndex = slotNumberToPosition[i];
            if (slotIndex < container.inventorySlots.size()) {

                Slot slot = container.getSlot(slotIndex);
                if (slot instanceof IMovableSlot) {

                    ((IMovableSlot) slot).moveTo(slotNumberToPosition[i + 1], slotNumberToPosition[i + 2]);
                } else {

                    EasyMagic.LOGGER.warn("slot is not movable");
                }
            } else {

                EasyMagic.LOGGER.warn("slot index exceeds inventory size: {} >= {}", slotIndex, container.inventorySlots.size());
            }
        }
    }

    public static void setSlotVisibilities(Container container, int[] slotNumbers, boolean visible) {

        for (int slotNumber : slotNumbers) {

            Slot slot = container.getSlot(slotNumber);
            if (slot instanceof IMovableSlot) {

                ((IMovableSlot) slot).setVisible(visible);
            } else {

                EasyMagic.LOGGER.warn("slot unable to change visibility");
            }
        }
    }

}
