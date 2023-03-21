package fuzs.easymagic.world.inventory;

import com.google.common.collect.Lists;
import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.config.ServerConfig;
import fuzs.easymagic.core.CommonAbstractions;
import fuzs.easymagic.init.ModRegistry;
import fuzs.easymagic.mixin.accessor.EnchantmentMenuAccessor;
import fuzs.easymagic.mixin.accessor.PlayerAccessor;
import fuzs.easymagic.network.S2CEnchantingDataMessage;
import fuzs.easymagic.util.ExperienceUtil;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.List;

public class ModEnchantmentMenu extends EnchantmentMenu implements ContainerListener {
    public static final int REROLL_CATALYST_SLOT = 38;

    private final Container enchantSlots;
    private final ContainerLevelAccess access;
    private final Player player;
    private final RandomSource random;
    private final DataSlot enchantmentSeed;

    public ModEnchantmentMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(EasyMagic.CONFIG.get(ServerConfig.class).dedicatedRerollCatalyst ? 3 : 2), ContainerLevelAccess.NULL);
    }

    public ModEnchantmentMenu(int id, Inventory inventory, Container container, ContainerLevelAccess access) {
        super(id, inventory, access);
        this.enchantSlots = container;
        this.access = access;
        this.player = inventory.player;
        this.random = ((EnchantmentMenuAccessor) this).getRandom();
        this.enchantmentSeed = ((EnchantmentMenuAccessor) this).getEnchantmentSeed();

        ((EnchantmentMenuAccessor) this).setEnchantSlots(container);
        boolean dedicatedRerollCatalyst = EasyMagic.CONFIG.get(ServerConfig.class).dedicatedRerollCatalyst;

        // do these before the override slots, as remote slot list isn't reset properly and this will use the wrong index
        if (dedicatedRerollCatalyst) {
            this.addSlot(new Slot(container, 2, 41, 47) {

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(ModRegistry.REROLL_CATALYSTS_ITEM_TAG);
                }
            });
        }

        //  manually set slot index as vanilla's AbstractContainerMenu::addSlot normally does that which we cannot use as we want to override default enchantment menu slots
        this.slots.set(0, Util.make(new Slot(container, 0, dedicatedRerollCatalyst ? 5 : 15, 47) {

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        }, slot -> slot.index = 0));

        this.slots.set(1, Util.make(new Slot(container, 1, dedicatedRerollCatalyst ? 23 : 35, 47) {

            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModRegistry.ENCHANTING_CATALYSTS_ITEM_TAG);
            }
        }, slot -> slot.index = 1));

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
                    int power = EasyMagic.CONFIG.get(ServerConfig.class).maxEnchantingPower == 0 ? 15 : (this.getEnchantingPower(world, pos) * 15) / EasyMagic.CONFIG.get(ServerConfig.class).maxEnchantingPower;
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
                    this.enchantClue[j1] = BuiltInRegistries.ENCHANTMENT.getId(enchantmentdata.enchantment);
                    this.levelClue[j1] = enchantmentdata.level;
                }
            }
        }
    }

    private List<EnchantmentInstance> createEnchantmentInstance(ItemStack enchantedItem, int enchantSlot) {
        return ((EnchantmentMenuAccessor) this).callGetEnchantmentList(enchantedItem, enchantSlot, this.costs[enchantSlot]);
    }

    private List<EnchantmentInstance> getEnchantmentHint(ItemStack enchantedItem, int enchantSlot, ServerConfig.EnchantmentHint enchantmentHint) {
        return switch (enchantmentHint) {
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
        final ServerConfig.EnchantmentHint enchantmentHint = EasyMagic.CONFIG.get(ServerConfig.class).enchantmentHint;
        List<EnchantmentInstance> firstSlotData = this.getEnchantmentHint(enchantedItem, 0, enchantmentHint);
        List<EnchantmentInstance> secondSlotData = this.getEnchantmentHint(enchantedItem, 1, enchantmentHint);
        List<EnchantmentInstance> thirdSlotData = this.getEnchantmentHint(enchantedItem, 2, enchantmentHint);
        EasyMagic.NETWORK.sendTo(new S2CEnchantingDataMessage(this.containerId, firstSlotData, secondSlotData, thirdSlotData), (ServerPlayer) this.player);
    }

    private int getEnchantingPower(Level level, BlockPos pos) {
        float j = 0;
        for(BlockPos blockpos : EnchantmentTableBlock.BOOKSHELF_OFFSETS) {
            if (EnchantmentTableBlock.isValidBookShelf(level, pos, blockpos)) {
                j += CommonAbstractions.INSTANCE.getEnchantPowerBonus(level.getBlockState(pos.offset(blockpos)), level, pos.offset(blockpos));
            }
        }
        return (int) j;
    }

    public static boolean isBlockEmpty(Level world, BlockPos pos) {
        if (EasyMagic.CONFIG.get(ServerConfig.class).lenientBookshelves) {
            return world.getBlockState(pos).getCollisionShape(world, pos) != Shapes.block();
        }
        return world.isEmptyBlock(pos);
    }

    @Override
    public boolean clickMenuButton(Player player, int data) {
        if (data == 4) {
            if (EasyMagic.CONFIG.get(ServerConfig.class).rerollEnchantments && !this.enchantSlots.getItem(0).isEmpty()) {
                int catalystSlot = EasyMagic.CONFIG.get(ServerConfig.class).dedicatedRerollCatalyst ? 2 : 1;
                ItemStack itemstack = this.enchantSlots.getItem(catalystSlot);
                if (itemstack.getCount() >= EasyMagic.CONFIG.get(ServerConfig.class).rerollCatalystCost && ExperienceUtil.getTotalExperience(player) >= EasyMagic.CONFIG.get(ServerConfig.class).rerollExperiencePointsCost || player.getAbilities().instabuild) {
                    this.access.execute((Level level, BlockPos pos) -> {
                        // set a new enchantment seed every time a new item is placed into the enchanting slot
                        this.enchantmentSeed.set(this.player.getRandom().nextInt());
                        ((PlayerAccessor) this.player).setEnchantmentSeed(this.enchantmentSeed.get());
                        if (!player.getAbilities().instabuild) {
                            if (EasyMagic.CONFIG.get(ServerConfig.class).rerollCatalystCost > 0) {
                                itemstack.shrink(EasyMagic.CONFIG.get(ServerConfig.class).rerollCatalystCost);
                                if (itemstack.isEmpty()) {
                                    this.enchantSlots.setItem(catalystSlot, ItemStack.EMPTY);
                                }
                            }
                            if (EasyMagic.CONFIG.get(ServerConfig.class).rerollExperiencePointsCost > 0) {
                                player.giveExperiencePoints(-EasyMagic.CONFIG.get(ServerConfig.class).rerollExperiencePointsCost);
                            }
                        }
                        this.enchantSlots.setChanged();
                        // needed for creative mode since slots don't actually change
                        this.slotsChanged(this.enchantSlots);
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

    public int getRerollCatalystCount() {
        if (EasyMagic.CONFIG.get(ServerConfig.class).dedicatedRerollCatalyst) {
            return this.enchantSlots.getItem(2).getCount();
        }
        return this.getGoldCount();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            boolean override = false;
            boolean dedicatedRerollCatalyst = EasyMagic.CONFIG.get(ServerConfig.class).dedicatedRerollCatalyst;
            if (dedicatedRerollCatalyst && index == REROLL_CATALYST_SLOT) {
                override = true;
                if (!this.moveItemStackTo(itemStack2, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index != 0 && index != 1) {
                if (itemStack2.is(ModRegistry.ENCHANTING_CATALYSTS_ITEM_TAG)) {
                    override = true;
                    if (!this.moveItemStackTo(itemStack2, 1, 2, true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (dedicatedRerollCatalyst && itemStack2.is(ModRegistry.REROLL_CATALYSTS_ITEM_TAG)) {
                    override = true;
                    if (!this.moveItemStackTo(itemStack2, 38, 39, true)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (!override) return super.quickMoveStack(player, index);

            if (itemStack2.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemStack2);
        }

        return itemStack;
    }
}
