package fuzs.easymagic.world.inventory;

import com.google.common.collect.Lists;
import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.config.ServerConfig;
import fuzs.easymagic.mixin.accessor.EnchantmentMenuAccessor;
import fuzs.easymagic.network.message.S2CEnchantingDataMessage;
import fuzs.easymagic.registry.ModRegistry;
import fuzs.puzzleslib.util.PuzzlesUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import java.util.List;

@SuppressWarnings("NullableProblems")
public class ModEnchantmentMenu extends EnchantmentMenu implements ContainerListener {
    private final Container enchantSlots;
    private final ContainerLevelAccess access;
    private final Player player;

    public ModEnchantmentMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(2), ContainerLevelAccess.NULL);
    }

    public ModEnchantmentMenu(int id, Inventory playerInventory, Container inventory, ContainerLevelAccess worldPosCallable) {
        super(id, playerInventory, worldPosCallable);
        this.enchantSlots = inventory;
        this.access = worldPosCallable;
        this.player = playerInventory.player;
        ((EnchantmentMenuAccessor) this).setEnchantSlots(inventory);
        this.slots.set(0, PuzzlesUtil.make(new EnchantableSlot(inventory, 0, 15, 47), slot -> slot.index = 0));
        this.slots.set(1, PuzzlesUtil.make(new LapisSlot(inventory, 1, 35, 47), slot -> slot.index = 1));
        this.addSlotListener(this);
    }

    @Override
    public MenuType<?> getType() {
        return ModRegistry.ENCHANTMENT_MENU_TYPE.get();
    }

    @Override
    public void slotsChanged(Container inventory) {
        if (inventory == this.enchantSlots) {
            ItemStack enchantedItem = inventory.getItem(0);
            if (!enchantedItem.isEmpty() && enchantedItem.isEnchantable()) {
                this.access.execute((world, pos) -> {
                    int power = EasyMagic.CONFIG.server().maxPower == 0 ? 15 : (this.getEnchantingPower(world, pos) * 15) / EasyMagic.CONFIG.server().maxPower;
                    ((EnchantmentMenuAccessor) this).getRandom().setSeed(((EnchantmentMenuAccessor) this).getEnchantmentSeed().get());
                    this.updateLevels(enchantedItem, world, pos, power);
                    // need to run this always as enchanting buttons will otherwise be greyed out
                    this.createClues(enchantedItem);
                    this.broadcastChanges();
                    this.sendEnchantingData(enchantedItem);
                });
            } else {
                this.resetLevelsAndClues();
            }
        }
    }

    @Override
    public void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {
        // we use this method instead of AbstractContainerMenu::slotsChanged as this automatically triggers when the block entity inventory changes on the server,
        // e.g. when inserting items using a hopper or from another player,
        // while slotsChanged is often only triggered on the client when it is sent updated inventory contents
        if (abstractContainerMenu == this) {
            // this is only executed on server anyways as slot listeners are only processed there by default, but might as well use the access here
            this.access.execute((Level, BlockPos) -> {
                // need to do this before calling AbstractContainerMenu::slotsChanged
                if (i == 0 && !itemStack.isEmpty() && EasyMagic.CONFIG.server().reRollEnchantments) {
                    // set a new enchantment seed every time a new item is placed into the enchanting slot
                    ((EnchantmentMenuAccessor) ModEnchantmentMenu.this).getEnchantmentSeed().set(this.player.getRandom().nextInt());
                }
                if (i >= 0 && i < 2) {
                    this.slotsChanged(this.enchantSlots);
                }
            });
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu abstractContainerMenu, int i, int j) {

    }

    private void resetLevelsAndClues() {
        for (int i = 0; i < 3; ++i) {
            this.costs[i] = 0;
            this.enchantClue[i] = -1;
            this.levelClue[i] = -1;
        }
    }

    private void updateLevels(ItemStack itemstack, Level world, BlockPos pos, int power) {
        for (int i1 = 0; i1 < 3; ++i1) {
            this.costs[i1] = EnchantmentHelper.getEnchantmentCost(((EnchantmentMenuAccessor) this).getRandom(), i1, power, itemstack);
            if (this.costs[i1] < i1 + 1) {
                this.costs[i1] = 0;
            }
        }
    }

    private void createClues(ItemStack itemstack) {
        for (int j1 = 0; j1 < 3; ++j1) {
            if (this.costs[j1] > 0) {
                List<EnchantmentInstance> list = this.createEnchantmentInstance(itemstack, j1);
                if (list != null && !list.isEmpty()) {
                    EnchantmentInstance enchantmentdata = list.get(((EnchantmentMenuAccessor) this).getRandom().nextInt(list.size()));
                    this.enchantClue[j1] = ((ForgeRegistry<Enchantment>) ForgeRegistries.ENCHANTMENTS).getID(enchantmentdata.enchantment);
                    this.levelClue[j1] = enchantmentdata.level;
                }
            }
        }
    }

    private List<EnchantmentInstance> createEnchantmentInstance(ItemStack enchantedItem, int enchantSlot) {
        return ((EnchantmentMenuAccessor) this).callGetEnchantmentList(enchantedItem, enchantSlot, this.costs[enchantSlot]);
    }

    private List<EnchantmentInstance> getEnchantmentHint(ItemStack enchantedItem, int enchantSlot, ServerConfig.ShowEnchantments showEnchantments) {
        return switch (showEnchantments) {
            case NONE -> Lists.newArrayList();
            case SINGLE -> {
                List<EnchantmentInstance> enchantmentData = this.createEnchantmentInstance(enchantedItem, enchantSlot);
                if (enchantmentData.isEmpty()) yield Lists.newArrayList();
                yield Lists.newArrayList(enchantmentData.get(((EnchantmentMenuAccessor) this).getRandom().nextInt(enchantmentData.size())));
            }
            case ALL -> this.createEnchantmentInstance(enchantedItem, enchantSlot);
        };
    }

    private void sendEnchantingData(ItemStack enchantedItem) {
        final ServerConfig.ShowEnchantments showEnchantments = EasyMagic.CONFIG.server().showEnchantments;
        List<EnchantmentInstance> firstSlotData = this.getEnchantmentHint(enchantedItem, 0, showEnchantments);
        List<EnchantmentInstance> secondSlotData = this.getEnchantmentHint(enchantedItem, 1, showEnchantments);
        List<EnchantmentInstance> thirdSlotData = this.getEnchantmentHint(enchantedItem, 2, showEnchantments);
        EasyMagic.NETWORK.sendTo(new S2CEnchantingDataMessage(this.containerId, firstSlotData, secondSlotData, thirdSlotData), (ServerPlayer) this.player);
    }

    private int getEnchantingPower(Level world, BlockPos pos) {
        int power = 0;
        for (int k = -1; k <= 1; ++k) {
            for (int l = -1; l <= 1; ++l) {
                if ((k != 0 || l != 0) && isBlockEmpty(world, pos.offset(l, 0, k)) && isBlockEmpty(world, pos.offset(l, 1, k))) {
                    power += this.getPower(world, pos.offset(l * 2, 0, k * 2));
                    power += this.getPower(world, pos.offset(l * 2, 1, k * 2));
                    if (l != 0 && k != 0) {
                        power += this.getPower(world, pos.offset(l * 2, 0, k));
                        power += this.getPower(world, pos.offset(l * 2, 1, k));
                        power += this.getPower(world, pos.offset(l, 0, k * 2));
                        power += this.getPower(world, pos.offset(l, 1, k * 2));
                    }
                }
            }
        }
        return power;
    }

    public static boolean isBlockEmpty(Level world, BlockPos pos) {
        if (EasyMagic.CONFIG.server().lenientBookshelves) {
            return world.getBlockState(pos).getCollisionShape(world, pos).isEmpty();
        }
        return world.isEmptyBlock(pos);
    }

    private float getPower(Level world, BlockPos pos) {
        return world.getBlockState(pos).is(Blocks.BOOKSHELF) ? 1.0F : 0.0F;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return this.enchantSlots.stillValid(playerIn);
    }

    @Override
    public void removed(Player playerIn) {
        // copied from container super method
        if (playerIn instanceof ServerPlayer) {
            ItemStack itemstack = this.getCarried();
            if (!itemstack.isEmpty()) {
                if (playerIn.isAlive() && !((ServerPlayer) playerIn).hasDisconnected()) {
                    playerIn.getInventory().placeItemBackInInventory(itemstack);
                } else {
                    playerIn.drop(itemstack, false);
                }
                this.setCarried(ItemStack.EMPTY);
            }
        }
    }

    private class EnchantableSlot extends Slot {
        public EnchantableSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (EasyMagic.CONFIG.server().filterTable) {
                // can't exchange items directly while holding replacement otherwise, this seems to do the trick
                return stack.isEnchantable() || stack.getItem() instanceof BookItem && !this.hasItem();
            }
            return true;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private class LapisSlot extends Slot {
        public LapisSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(Tags.Items.ENCHANTING_FUELS);
        }
    }
}
