package fuzs.easymagic.world.inventory;

import com.google.common.collect.Lists;
import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.config.ServerConfig;
import fuzs.easymagic.core.ModCoreServices;
import fuzs.easymagic.init.ModRegistry;
import fuzs.easymagic.mixin.accessor.EnchantmentMenuAccessor;
import fuzs.easymagic.mixin.accessor.PlayerAccessor;
import fuzs.easymagic.network.message.S2CEnchantingDataMessage;
import fuzs.puzzleslib.util.PuzzlesUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantmentTableBlock;

import java.util.List;

public class ModEnchantmentMenu extends EnchantmentMenu implements ContainerListener {
    private final Container enchantSlots;
    private final ContainerLevelAccess access;
    private final Player player;
    private final RandomSource random;
    private final DataSlot enchantmentSeed;

    public ModEnchantmentMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(2), ContainerLevelAccess.NULL);
    }

    public ModEnchantmentMenu(int id, Inventory playerInventory, Container inventory, ContainerLevelAccess worldPosCallable) {
        super(id, playerInventory, worldPosCallable);
        this.enchantSlots = inventory;
        this.access = worldPosCallable;
        this.player = playerInventory.player;
        this.random = ((EnchantmentMenuAccessor) this).getRandom();
        this.enchantmentSeed = ((EnchantmentMenuAccessor) this).getEnchantmentSeed();
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
                    int power = EasyMagic.CONFIG.get(ServerConfig.class).maxPower == 0 ? 15 : (this.getEnchantingPower(world, pos) * 15) / EasyMagic.CONFIG.get(ServerConfig.class).maxPower;
                    this.random.setSeed(this.enchantmentSeed.get());
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
            // this is only executed on server anyway as slot listeners are only processed there by default, but might as well use the access here
            this.access.execute((Level, BlockPos) -> {
                // need to do this before calling AbstractContainerMenu::slotsChanged
                if (i == 0 && !itemStack.isEmpty() && EasyMagic.CONFIG.get(ServerConfig.class).rerollEnchantments == ServerConfig.ReRollEnchantments.FREE) {
                    this.reRollEnchantments(false);
                }
                if (i >= 0 && i < 2) {
                    this.slotsChanged(this.enchantSlots);
                }
            });
        }
    }

    private void reRollEnchantments(boolean setPlayerSeed) {
        // set a new enchantment seed every time a new item is placed into the enchanting slot
        this.enchantmentSeed.set(this.player.getRandom().nextInt());
        if (setPlayerSeed) {
            ((PlayerAccessor) this.player).setEnchantmentSeed(this.enchantmentSeed.get());
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
            this.costs[i1] = EnchantmentHelper.getEnchantmentCost(this.random, i1, power, itemstack);
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
                    EnchantmentInstance enchantmentdata = list.get(this.random.nextInt(list.size()));
                    this.enchantClue[j1] = Registry.ENCHANTMENT.getId(enchantmentdata.enchantment);
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
                yield Lists.newArrayList(enchantmentData.get(this.random.nextInt(enchantmentData.size())));
            }
            case ALL -> this.createEnchantmentInstance(enchantedItem, enchantSlot);
        };
    }

    private void sendEnchantingData(ItemStack enchantedItem) {
        final ServerConfig.ShowEnchantments showEnchantments = EasyMagic.CONFIG.get(ServerConfig.class).showEnchantments;
        List<EnchantmentInstance> firstSlotData = this.getEnchantmentHint(enchantedItem, 0, showEnchantments);
        List<EnchantmentInstance> secondSlotData = this.getEnchantmentHint(enchantedItem, 1, showEnchantments);
        List<EnchantmentInstance> thirdSlotData = this.getEnchantmentHint(enchantedItem, 2, showEnchantments);
        EasyMagic.NETWORK.sendTo(new S2CEnchantingDataMessage(this.containerId, firstSlotData, secondSlotData, thirdSlotData), (ServerPlayer) this.player);
    }

    private int getEnchantingPower(Level level, BlockPos pos) {
        float j = 0;
        for(BlockPos blockpos : EnchantmentTableBlock.BOOKSHELF_OFFSETS) {
            if (EnchantmentTableBlock.isValidBookShelf(level, pos, blockpos)) {
                j += ModCoreServices.ABSTRACTIONS.getEnchantPowerBonus(level.getBlockState(pos.offset(blockpos)), level, pos.offset(blockpos));
            }
        }
        return (int) j;
    }

    public static boolean isBlockEmpty(Level world, BlockPos pos) {
        if (EasyMagic.CONFIG.get(ServerConfig.class).lenientBookshelves) {
            return world.getBlockState(pos).getCollisionShape(world, pos).isEmpty();
        }
        return world.isEmptyBlock(pos);
    }

    @Override
    public boolean clickMenuButton(Player player, int data) {
        if (data == 4 && !this.enchantSlots.getItem(0).isEmpty()) {
            if (EasyMagic.CONFIG.get(ServerConfig.class).rerollEnchantments == ServerConfig.ReRollEnchantments.WITH_COST) {
                ItemStack itemstack = this.enchantSlots.getItem(1);
                if (itemstack.getCount() >= EasyMagic.CONFIG.get(ServerConfig.class).rerollLapisCost && player.experienceLevel >= EasyMagic.CONFIG.get(ServerConfig.class).rerollLevelCost || player.getAbilities().instabuild) {
                    this.access.execute((Level level, BlockPos pos) -> {
                        this.reRollEnchantments(true);
                        if (!player.getAbilities().instabuild) {
                            if (EasyMagic.CONFIG.get(ServerConfig.class).rerollLapisCost > 0) {
                                itemstack.shrink(EasyMagic.CONFIG.get(ServerConfig.class).rerollLapisCost);
                                if (itemstack.isEmpty()) {
                                    this.enchantSlots.setItem(1, ItemStack.EMPTY);
                                }
                            }
                            if (EasyMagic.CONFIG.get(ServerConfig.class).rerollLevelCost > 0) {
                                player.giveExperienceLevels(-EasyMagic.CONFIG.get(ServerConfig.class).rerollLevelCost);
                            }
                        }
                        this.enchantSlots.setChanged();
                        // needed for creative mode since slots don't actually change
                        this.slotsChanged(this.enchantSlots);
                        level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F);
                    });
                    return true;
                }
            }
            return false;
        }
        return super.clickMenuButton(player, data);
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

    private static class EnchantableSlot extends Slot {
        public EnchantableSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (EasyMagic.CONFIG.get(ServerConfig.class).filterTable) {
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

    private static class LapisSlot extends Slot {
        public LapisSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return ModCoreServices.ABSTRACTIONS.isStackEnchantingFuel(stack);
        }
    }
}
