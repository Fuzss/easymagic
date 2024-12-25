package fuzs.easymagic.world.inventory;

import com.mojang.datafixers.util.Pair;
import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.config.ServerConfig;
import fuzs.easymagic.init.ModRegistry;
import fuzs.easymagic.mixin.accessor.EnchantmentMenuAccessor;
import fuzs.easymagic.mixin.accessor.PlayerAccessor;
import fuzs.easymagic.util.ChiseledBookshelfHelper;
import fuzs.easymagic.util.PlayerExperienceHelper;
import fuzs.puzzleslib.api.core.v1.CommonAbstractions;
import fuzs.puzzleslib.api.core.v1.utility.ResourceLocationHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModEnchantmentMenu extends EnchantmentMenu implements ContainerListener {
    static final ResourceLocation EMPTY_SLOT_LAPIS_LAZULI = ResourceLocationHelper.withDefaultNamespace(
            "item/empty_slot_lapis_lazuli");
    public static final int REROLL_CATALYST_SLOT = 38;

    private final Container enchantSlots;
    private final ContainerLevelAccess access;
    private final Player player;
    private final RandomSource random;
    private final DataSlot enchantmentSeed;

    public ModEnchantmentMenu(int id, Inventory playerInventory) {
        this(id,
                playerInventory,
                new SimpleContainer(EasyMagic.CONFIG.get(ServerConfig.class).dedicatedRerollCatalyst ? 3 : 2),
                ContainerLevelAccess.NULL);
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
        this.setSlot(0, new Slot(container, 0, dedicatedRerollCatalyst ? 5 : 15, 47) {

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.setSlot(1, new Slot(container, 1, dedicatedRerollCatalyst ? 23 : 35, 47) {

            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModRegistry.ENCHANTING_CATALYSTS_ITEM_TAG);
            }

            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_SLOT_LAPIS_LAZULI);
            }
        });

        this.addSlotListener(this);
    }

    public Slot setSlot(int index, Slot slot) {
        slot.index = index;
        this.slots.set(index, slot);
        return slot;
    }

    @Override
    public MenuType<?> getType() {
        return ModRegistry.ENCHANTMENT_MENU_TYPE.value();
    }

    @Override
    public void slotsChanged(Container inventory) {
        if (inventory == this.enchantSlots) {
            ItemStack itemStack = inventory.getItem(0);
            if (!itemStack.isEmpty() && itemStack.isEnchantable()) {
                this.access.execute((Level level, BlockPos pos) -> {
                    int power = EasyMagic.CONFIG.get(ServerConfig.class).maxEnchantingPower == 0 ? 15 :
                            (this.getEnchantingPower(level, pos) * 15) /
                                    EasyMagic.CONFIG.get(ServerConfig.class).maxEnchantingPower;
                    this.random.setSeed(this.enchantmentSeed.get());
                    this.updateLevels(itemStack, level, pos, power);
                    this.broadcastChanges();
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
    public void dataChanged(AbstractContainerMenu abstractContainerMenu, int dataSlotIndex, int value) {
        if (dataSlotIndex == 3) {
            this.random.setSeed(value);
        }
    }

    private void resetLevelsAndClues() {
        for (int i = 0; i < 3; ++i) {
            this.costs[i] = 0;
            this.enchantClue[i] = -1;
            this.levelClue[i] = -1;
        }
    }

    private void updateLevels(ItemStack itemstack, Level level, BlockPos pos, int power) {
        for (int i1 = 0; i1 < 3; ++i1) {
            this.costs[i1] = EnchantmentHelper.getEnchantmentCost(this.random, i1, power, itemstack);
            if (this.costs[i1] < i1 + 1) {
                this.costs[i1] = 0;
            }
        }
    }

    public List<List<EnchantmentInstance>> getEnchantingData(RegistryAccess registryAccess) {
        ServerConfig.EnchantmentHint enchantmentHint = EasyMagic.CONFIG.get(ServerConfig.class).enchantmentHint;
        List<List<EnchantmentInstance>> slotData = new ArrayList<>(3);
        ItemStack itemStack = this.enchantSlots.getItem(0);
        for (int i = 0; i < 3; i++) {
            slotData.add(i, this.getEnchantmentHint(registryAccess, itemStack, i, enchantmentHint));
        }
        return slotData;
    }

    private List<EnchantmentInstance> createEnchantmentInstance(RegistryAccess registryAccess, ItemStack itemStack, int enchantSlot) {
        return ((EnchantmentMenuAccessor) this).callGetEnchantmentList(registryAccess,
                itemStack,
                enchantSlot,
                this.costs[enchantSlot]);
    }

    private List<EnchantmentInstance> getEnchantmentHint(RegistryAccess registryAccess, ItemStack itemStack, int enchantSlot, ServerConfig.EnchantmentHint enchantmentHint) {
        return switch (enchantmentHint) {
            case NONE -> Collections.emptyList();
            case SINGLE -> {
                List<EnchantmentInstance> enchantmentData = this.createEnchantmentInstance(registryAccess,
                        itemStack,
                        enchantSlot);
                if (enchantmentData.isEmpty()) {
                    yield Collections.emptyList();
                } else {
                    yield Collections.singletonList(enchantmentData.get(this.random.nextInt(enchantmentData.size())));
                }
            }
            case ALL -> this.createEnchantmentInstance(registryAccess, itemStack, enchantSlot);
        };
    }

    private int getEnchantingPower(Level level, BlockPos pos) {
        float enchantingPower = 0;
        int chiseledBookshelfBooks = 0;
        for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            if (EnchantingTableBlock.isValidBookShelf(level, pos, offset)) {
                enchantingPower += CommonAbstractions.INSTANCE.getEnchantPowerBonus(level.getBlockState(pos.offset(
                        offset)), level, pos.offset(offset));
                chiseledBookshelfBooks += ChiseledBookshelfHelper.findValidBooks(level, pos, offset);
            }
        }
        return (int) enchantingPower + chiseledBookshelfBooks / 3;
    }

    @Override
    public boolean clickMenuButton(Player player, int data) {
        if (data == 4) {
            if (EasyMagic.CONFIG.get(ServerConfig.class).rerollEnchantments && this.canUseReroll()) {
                int catalystSlot = EasyMagic.CONFIG.get(ServerConfig.class).dedicatedRerollCatalyst ? 2 : 1;
                ItemStack itemstack = this.enchantSlots.getItem(catalystSlot);
                if (player.getAbilities().instabuild ||
                        itemstack.getCount() >= EasyMagic.CONFIG.get(ServerConfig.class).rerollCatalystCost) {
                    int totalExperience = EasyMagic.CONFIG.get(ServerConfig.class).rerollingTakesEnchantmentLevels ?
                            player.experienceLevel : PlayerExperienceHelper.getTotalExperience(player);
                    if (player.getAbilities().instabuild ||
                            totalExperience >= EasyMagic.CONFIG.get(ServerConfig.class).rerollExperiencePointsCost) {
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
                                    if (EasyMagic.CONFIG.get(ServerConfig.class).rerollingTakesEnchantmentLevels) {
                                        player.giveExperienceLevels(-EasyMagic.CONFIG.get(ServerConfig.class).rerollExperiencePointsCost);
                                    } else {
                                        player.giveExperiencePoints(-EasyMagic.CONFIG.get(ServerConfig.class).rerollExperiencePointsCost);
                                    }
                                }
                            }
                            this.enchantSlots.setChanged();
                            // needed for creative mode since slots don't actually change
                            this.slotsChanged(this.enchantSlots);
                        });
                        return true;
                    }
                }
            }
            return false;
        }
        return super.clickMenuButton(player, data);
    }

    public boolean canUseReroll() {
        ItemStack itemToEnchant = this.enchantSlots.getItem(0);
        if (!itemToEnchant.isEmpty() && itemToEnchant.isEnchantable()) {
            for (int cost : this.costs) {
                if (cost > 0) return true;
            }
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.enchantSlots.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        // copied from container super method
        if (player instanceof ServerPlayer) {
            ItemStack itemstack = this.getCarried();
            if (!itemstack.isEmpty()) {
                if (player.isAlive() && !((ServerPlayer) player).hasDisconnected()) {
                    player.getInventory().placeItemBackInInventory(itemstack);
                } else {
                    player.drop(itemstack, false);
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
        if (slot.hasItem()) {
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
