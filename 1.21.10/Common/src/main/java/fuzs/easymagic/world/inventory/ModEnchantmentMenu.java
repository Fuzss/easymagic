package fuzs.easymagic.world.inventory;

import com.google.common.collect.ImmutableList;
import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.config.ServerConfig;
import fuzs.easymagic.init.ModRegistry;
import fuzs.easymagic.network.ClientboundEnchantmentCluesMessage;
import fuzs.easymagic.util.ChiseledBookshelfHelper;
import fuzs.easymagic.util.PlayerExperienceHelper;
import fuzs.puzzleslib.api.container.v1.QuickMoveRuleSet;
import fuzs.puzzleslib.api.core.v1.utility.ResourceLocationHelper;
import fuzs.puzzleslib.api.item.v2.EnchantingHelper;
import fuzs.puzzleslib.api.network.v4.MessageSender;
import fuzs.puzzleslib.api.network.v4.PlayerSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ModEnchantmentMenu extends EnchantmentMenu implements ContainerListener {
    static final ResourceLocation EMPTY_SLOT_LAPIS_LAZULI = ResourceLocationHelper.withDefaultNamespace(
            "container/slot/lapis_lazuli");
    static final ResourceLocation EMPTY_SLOT_AMETHYST_SHARD = ResourceLocation.withDefaultNamespace(
            "container/slot/amethyst_shard");
    static final int REROLL_CATALYST_SLOT = 38;
    public static final int REROLL_DATA_SLOT = 4;

    public final Player player;
    public final List<List<EnchantmentInstance>> clues = Arrays.asList(List.of(), List.of(), List.of());

    public ModEnchantmentMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(3), ContainerLevelAccess.NULL);
    }

    public ModEnchantmentMenu(int id, Inventory inventory, Container container, ContainerLevelAccess access) {
        super(id, inventory, access);
        this.enchantSlots = container;
        this.player = inventory.player;
        // do these before the override slots, as remote slot list isn't reset properly and this will use the wrong index
        Slot slot = this.addSlot(new Slot(container, 2, 41, 47) {
            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.is(ModRegistry.REROLL_CATALYSTS_ITEM_TAG);
            }

            @Override
            public ResourceLocation getNoItemIcon() {
                return EMPTY_SLOT_AMETHYST_SHARD;
            }

            @Override
            public boolean isActive() {
                return EasyMagic.CONFIG.get(ServerConfig.class).dedicatedRerollCatalyst();
            }
        });
        //  manually set slot index as vanilla's AbstractContainerMenu::addSlot normally does that which we cannot use as we want to override default enchantment menu slots
        this.setSlot(0, new Slot(container, 0, slot.isActive() ? 5 : 15, 47) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        this.setSlot(1, new Slot(container, 1, slot.isActive() ? 23 : 35, 47) {
            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.is(ModRegistry.ENCHANTING_CATALYSTS_ITEM_TAG);
            }

            @Override
            public ResourceLocation getNoItemIcon() {
                return EMPTY_SLOT_LAPIS_LAZULI;
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
        return ModRegistry.ENCHANTING_MENU_TYPE.value();
    }

    @Override
    public void slotsChanged(Container container) {
        if (container == this.enchantSlots) {
            ItemStack itemStack = container.getItem(0);
            if (!itemStack.isEmpty() && itemStack.isEnchantable()) {
                this.access.execute((Level level, BlockPos pos) -> {
                    int enchantingPower = EasyMagic.CONFIG.get(ServerConfig.class).maxEnchantingPower == 0 ? 15 :
                            (this.getEnchantingPower(level, pos) * 15)
                                    / EasyMagic.CONFIG.get(ServerConfig.class).maxEnchantingPower;
                    this.random.setSeed(this.enchantmentSeed.get());
                    this.updateLevelsAndClues(level.registryAccess(), itemStack, enchantingPower);
                    this.broadcastChanges();
                });
            } else {
                this.resetLevelsAndClues();
            }
        }
    }

    private int getEnchantingPower(Level level, BlockPos blockPos) {
        float enchantingPower = 0;
        int chiseledBookshelfBooks = 0;
        for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            if (EnchantingTableBlock.isValidBookShelf(level, blockPos, offset)) {
                enchantingPower += EnchantingHelper.getEnchantPowerBonus(level.getBlockState(blockPos.offset(offset)),
                        level,
                        blockPos.offset(offset));
                chiseledBookshelfBooks += ChiseledBookshelfHelper.findValidBooks(level, blockPos, offset);
            }
        }

        return (int) enchantingPower + chiseledBookshelfBooks / 3;
    }

    private void resetLevelsAndClues() {
        for (int i = 0; i < 3; ++i) {
            this.costs[i] = 0;
            // we do not want any of vanilla's clue handling, this is otherwise not touched anywhere
            this.enchantClue[i] = -1;
            this.levelClue[i] = -1;
        }
    }

    private void updateLevelsAndClues(RegistryAccess registries, ItemStack itemStack, int enchantingPower) {
        for (int i = 0; i < 3; ++i) {
            this.costs[i] = EnchantmentHelper.getEnchantmentCost(this.random, i, enchantingPower, itemStack);
            if (this.costs[i] < i + 1) {
                this.costs[i] = 0;
            }

            List<EnchantmentInstance> enchantmentList = this.getEnchantmentList(registries,
                    itemStack,
                    i,
                    this.costs[i]);
            // set this to zero, so that the enchantment slot does not show anything
            if (enchantmentList.isEmpty()) {
                this.costs[i] = 0;
            }

            this.clues.set(i, this.createClue(enchantmentList));
        }
    }

    private List<EnchantmentInstance> createClue(List<EnchantmentInstance> enchantmentList) {
        return switch (EasyMagic.CONFIG.get(ServerConfig.class).enchantmentHint) {
            case NONE -> Collections.emptyList();
            case SINGLE -> {
                if (enchantmentList.isEmpty()) {
                    yield Collections.emptyList();
                } else {
                    yield Collections.singletonList(enchantmentList.get(this.random.nextInt(enchantmentList.size())));
                }
            }
            case ALL -> ImmutableList.copyOf(enchantmentList);
        };
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        // will not attempt sending anything on the client when the entity is not a server player
        MessageSender.broadcast(PlayerSet.ofEntity(this.player),
                new ClientboundEnchantmentCluesMessage(this.containerId, this.clues));
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
        // NO-OP
    }

    @Override
    public boolean clickMenuButton(Player player, int data) {
        if (data == REROLL_DATA_SLOT) {
            if (EasyMagic.CONFIG.get(ServerConfig.class).rerollEnchantments && this.canEnchantItem()
                    && this.canUseReroll(player)) {
                this.access.execute((Level level, BlockPos pos) -> {
                    this.enchantmentSeed.set(player.getRandom().nextInt());
                    player.enchantmentSeed = this.enchantmentSeed.get();
                    if (!player.getAbilities().instabuild) {
                        if (EasyMagic.CONFIG.get(ServerConfig.class).rerollCatalystCost > 0) {
                            int catalystSlot = this.getRerollCatalystSlot();
                            ItemStack itemStack = this.enchantSlots.getItem(catalystSlot);
                            itemStack.shrink(EasyMagic.CONFIG.get(ServerConfig.class).rerollCatalystCost);
                            if (itemStack.isEmpty()) {
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
            } else {
                return false;
            }
        } else {
            return super.clickMenuButton(player, data);
        }
    }

    public boolean canEnchantItem() {
        return this.enchantSlots.getItem(0).isEnchantable();
    }

    private int getRerollCatalystSlot() {
        return EasyMagic.CONFIG.get(ServerConfig.class).dedicatedRerollCatalyst ? 2 : 1;
    }

    public int getRerollCatalystCount() {
        return this.enchantSlots.getItem(this.getRerollCatalystSlot()).getCount();
    }

    public boolean canUseReroll(Player player) {
        if (player.getAbilities().instabuild) {
            return true;
        } else {
            int rerollExperiencePointsCost = EasyMagic.CONFIG.get(ServerConfig.class).rerollExperiencePointsCost;
            int rerollCatalystCost = EasyMagic.CONFIG.get(ServerConfig.class).rerollCatalystCost;
            return PlayerExperienceHelper.getTotalExperience(player) >= rerollExperiencePointsCost
                    && this.getRerollCatalystCount() >= rerollCatalystCost;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.enchantSlots.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        // copied from container super method to bypass items being placed back into the player inventory
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack itemStack = this.getCarried();
            if (!itemStack.isEmpty()) {
                if (player.isAlive() && !serverPlayer.hasDisconnected()) {
                    player.getInventory().placeItemBackInInventory(itemStack);
                } else {
                    player.drop(itemStack, false);
                }

                this.setCarried(ItemStack.EMPTY);
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return QuickMoveRuleSet.of(this, this::moveItemStackTo)
                .addContainerSlotRule(1)
                .addContainerSlotRule(REROLL_CATALYST_SLOT, (Slot slot) -> {
                    return EasyMagic.CONFIG.get(ServerConfig.class).dedicatedRerollCatalyst();
                })
                .addContainerSlotRule(0, (Slot slot) -> slot.getItem().isEnchantable())
                .addInventoryRules()
                .addInventoryCompartmentRules()
                .quickMoveStack(player, index);
    }
}
