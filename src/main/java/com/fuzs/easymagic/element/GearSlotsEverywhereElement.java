package com.fuzs.easymagic.element;

import com.fuzs.easymagic.EasyMagic;
import com.fuzs.easymagic.client.element.GearSlotsEverywhereExtension;
import com.fuzs.easymagic.inventory.container.MovableSlot;
import com.fuzs.easymagic.mixin.accessor.ContainerAccessor;
import com.fuzs.easymagic.network.message.S2CGearSlotsMessage;
import com.fuzs.puzzleslib_em.PuzzlesLib;
import com.fuzs.puzzleslib_em.element.extension.ClientExtensibleElement;
import com.mojang.datafixers.util.Pair;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.LogicalSide;

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
    }

    private void onContainerOpen(final PlayerContainerEvent.Open evt) {

        Container container = evt.getContainer();
        addPlayerSlots(container, evt.getPlayer());
        PuzzlesLib.getNetworkHandler().sendTo(new S2CGearSlotsMessage(container.windowId), (ServerPlayerEntity) evt.getPlayer());
    }

    public static int[] addPlayerSlots(Container container, PlayerEntity player) {

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
                if (slot instanceof MovableSlot) {

                    ((MovableSlot) slot).moveTo(slotNumberToPosition[i + 1], slotNumberToPosition[i + 2]);
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
            if (slot instanceof MovableSlot) {

                ((MovableSlot) slot).setVisible(visible);
            } else {

                EasyMagic.LOGGER.warn("slot unable to change visibility");
            }
        }
    }

}
