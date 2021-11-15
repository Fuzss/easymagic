package fuzs.easymagic.world.inventory;

import fuzs.easymagic.EasyMagic;
import fuzs.easymagic.config.ServerConfig;
import fuzs.easymagic.mixin.accessor.EnchantmentMenuAccessor;
import fuzs.easymagic.network.message.S2CEnchantingInfoMessage;
import fuzs.easymagic.registry.ModRegistry;
import fuzs.easymagic.world.level.block.entity.ModEnchantmentTableBlockEntity;
import com.google.common.collect.Lists;
import fuzs.puzzleslib.util.PuzzlesUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import javax.annotation.Nonnull;
import java.util.List;

@SuppressWarnings("NullableProblems")
public class ModEnchantmentMenu extends EnchantmentMenu {
    private final Player user;

    public ModEnchantmentMenu(int id, Inventory playerInventory) {
        super(id, playerInventory);
        this.updateInventory(new SimpleContainer(2));
        this.user = playerInventory.player;
    }

    public ModEnchantmentMenu(int id, Inventory playerInventory, Container inventory, ContainerLevelAccess worldPosCallable) {
        super(id, playerInventory, worldPosCallable);
        this.updateInventory(inventory);
        this.user = playerInventory.player;
        if (EasyMagic.CONFIG.server().reRollEnchantments) {
            // set random seed right from the beginning
            ((EnchantmentMenuAccessor) this).getEnchantmentSeed().set(playerInventory.player.getRandom().nextInt());
        }
    }

    @Override
    public MenuType<?> getType() {
        return ModRegistry.ENCHANTMENT_MENU_TYPE.get();
    }

    @Override
    public void slotsChanged(Container inventoryIn) {
        EnchantmentMenuAccessor accessor = (EnchantmentMenuAccessor) this;
        if (inventoryIn == accessor.getEnchantSlots()) {
            ItemStack enchantedItem = inventoryIn.getItem(0);
            if (!enchantedItem.isEmpty() && enchantedItem.isEnchantable()) {
                accessor.getAccess().execute((world, pos) -> {
                    int power = EasyMagic.CONFIG.server().maxPower == 0 ? 15 : (this.getEnchantingPower(world, pos) * 15) / EasyMagic.CONFIG.server().maxPower;
                    accessor.getRandom().setSeed(accessor.getEnchantmentSeed().get());
                    this.updateLevels(enchantedItem, world, pos, power);
                    // need to run this always as enchanting buttons will otherwise be greyed out
                    this.createClues(enchantedItem);
                    this.broadcastChanges();
                    this.sendEnchantingInfo(enchantedItem);
                });
            } else {
                this.resetLevelsAndClues();
            }
        }
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
            this.costs[i1] = ForgeEventFactory.onEnchantmentLevelSet(world, pos, i1, power, itemstack, this.costs[i1]);
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
                yield Lists.newArrayList(enchantmentData.get(((EnchantmentMenuAccessor) this).getRandom().nextInt(enchantmentData.size())));
            }
            case ALL -> this.createEnchantmentInstance(enchantedItem, enchantSlot);
        };
    }

    private void sendEnchantingInfo(ItemStack enchantedItem) {
        final ServerConfig.ShowEnchantments showEnchantments = EasyMagic.CONFIG.server().showEnchantments;
        List<EnchantmentInstance> firstSlotData = this.getEnchantmentHint(enchantedItem, 0, showEnchantments);
        List<EnchantmentInstance> secondSlotData = this.getEnchantmentHint(enchantedItem, 1, showEnchantments);
        List<EnchantmentInstance> thirdSlotData = this.getEnchantmentHint(enchantedItem, 2, showEnchantments);
        EasyMagic.NETWORK.sendTo(new S2CEnchantingInfoMessage(this.containerId, firstSlotData, secondSlotData, thirdSlotData), (ServerPlayer) this.user);
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
        return world.getBlockState(pos).getEnchantPowerBonus(world, pos);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        if (!EasyMagic.CONFIG.server().itemsStay) {
            return super.stillValid(playerIn);
        }
        return ((EnchantmentMenuAccessor) this).getEnchantSlots().stillValid(playerIn);
    }

    @Override
    public void removed(Player playerIn) {
        if (!EasyMagic.CONFIG.server().itemsStay) {
            super.removed(playerIn);
            return;
        }
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
    
    private void updateInventory(Container inventory) {
        EnchantmentMenuAccessor accessor = (EnchantmentMenuAccessor) this;
        if (EasyMagic.CONFIG.server().itemsStay) accessor.setEnchantSlots(inventory);
        this.slots.set(0, PuzzlesUtil.make(new EnchantableSlot(accessor.getEnchantSlots(), 0, 15, 47), slot -> slot.index = 0));
        this.slots.set(1, PuzzlesUtil.make(new LapisSlot(accessor.getEnchantSlots(), 1, 35, 47), slot -> slot.index = 1));
    }

    public Player getUser() {
        return this.user;
    }

    public void reseed() {
        ((EnchantmentMenuAccessor) ModEnchantmentMenu.this).getEnchantmentSeed().set(this.user.getRandom().nextInt());
    }

    private class EnchantableSlot extends Slot {
        public EnchantableSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (EasyMagic.CONFIG.server().itemsStay) {
                // can't exchange items directly while holding replacement otherwise, this seems to do the trick
                return stack.isEnchantable() || stack.getItem() instanceof BookItem && !this.hasItem();
            }
            return true;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        @Nonnull
        public void onTake(Player thePlayer, ItemStack stack) {
            if (EasyMagic.CONFIG.server().reRollEnchantments) {
                // set a random seed for everyone using same tile entity whenever the item is taken out
                Container tableInventory = ((EnchantmentMenuAccessor) ModEnchantmentMenu.this).getEnchantSlots();
                if (tableInventory instanceof ModEnchantmentTableBlockEntity) {
                    ((ModEnchantmentTableBlockEntity) tableInventory).updateReferences(ModEnchantmentMenu::reseed);
                }
            }
            super.onTake(thePlayer, stack);
        }
    }

    private static class LapisSlot extends Slot {
        public LapisSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return Tags.Items.GEMS_LAPIS.contains(stack.getItem());
        }
    }
}
