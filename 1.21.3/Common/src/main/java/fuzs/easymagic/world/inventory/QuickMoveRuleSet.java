package fuzs.easymagic.world.inventory;

import com.google.common.base.Predicates;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class QuickMoveRuleSet {
    private static final Predicate<Slot> IS_INVENTORY = (Slot slot) -> slot.container instanceof Inventory;
    private static final Predicate<Slot> IS_HOTBAR = (Slot slot) -> IS_INVENTORY.test(slot) &&
            Inventory.isHotbarSlot(slot.getContainerSlot());
    private static final Predicate<Slot> IS_NOT_HOTBAR = (Slot slot) -> IS_INVENTORY.test(slot) &&
            !IS_HOTBAR.test(slot);

    private final List<Rule> rules = new ArrayList<>();
    private final List<Slot> slots;
    private final Action action;
    private final boolean isLenient;

    private QuickMoveRuleSet(AbstractContainerMenu menu, Action action, boolean isLenient) {
        this.slots = menu.slots;
        this.action = action;
        this.isLenient = isLenient;
    }

    public static QuickMoveRuleSet of(AbstractContainerMenu menu, Action action) {
        return of(menu, action, false);
    }

    public static QuickMoveRuleSet of(AbstractContainerMenu menu, Action action, boolean isLenient) {
        return new QuickMoveRuleSet(menu, action, isLenient);
    }

    public ItemStack apply(Player player, int index) {

        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {

            ItemStack itemInSlot = slot.getItem();
            itemStack = itemInSlot.copy();

            for (Rule quickMove : this.rules) {
                if (quickMove.filter().test(slot)) {
                    if (!this.action.moveItemStackTo(itemInSlot,
                            quickMove.startIndex(),
                            quickMove.endIndex(),
                            quickMove.reverseDirection())) {
                        if (!this.isLenient) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.isLenient) {
                        break;
                    }
                    if (itemInSlot.isEmpty()) {
                        break;
                    }
                }
            }

            if (itemInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemInSlot.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemInSlot);
        }

        return itemStack;
    }

    public QuickMoveRuleSet addContainerRule(int index) {
        return this.addContainerRule(index, Predicates.alwaysTrue());
    }

    public QuickMoveRuleSet addContainerRule(int index, Predicate<Slot> filter) {
        return this.addContainerRule(index, false, filter);
    }

    public QuickMoveRuleSet addContainerRule(int index, boolean reverseDirection, Predicate<Slot> filter) {
        this.addRule(new Rule(index, index + 1, reverseDirection, (Slot slot) -> {
            Slot slotAtIndex = this.slots.get(index);
            return slotAtIndex.container != slot.container && slotAtIndex.mayPlace(slot.getItem()) && filter.test(slot);
        }));
        return this;
    }

    public QuickMoveRuleSet addContainerRule(Container container) {
        return this.addContainerRule(this.getInclusiveStartIndex((Slot slot) -> slot.container == container),
                this.getExclusiveEndIndex((Slot slot) -> slot.container == container));
    }

    public QuickMoveRuleSet addContainerRule(int startIndex, int endIndex) {
        this.addRule(new Rule(startIndex, endIndex, false, (Slot slot) -> {
            return slot.index >= this.getInclusiveStartIndex(IS_INVENTORY) &&
                    slot.index < this.getExclusiveEndIndex(IS_INVENTORY);
        }));
        return this;
    }

    public QuickMoveRuleSet addInventoryRule() {
        return this.addInventoryRule(false);
    }

    public QuickMoveRuleSet addInventoryRule(boolean reverseDirection) {
        this.addRule(new Rule(this.getInclusiveStartIndex(IS_INVENTORY),
                this.getExclusiveEndIndex(IS_INVENTORY),
                reverseDirection,
                (Slot slot) -> {
                    return !(slot.container instanceof Inventory);
                }));
        return this;
    }

    public QuickMoveRuleSet addHotbarRule() {
        this.addRule(new Rule(this.getInclusiveStartIndex(IS_HOTBAR),
                this.getExclusiveEndIndex(IS_HOTBAR),
                false,
                (Slot slot) -> {
                    return slot.index >= this.getInclusiveStartIndex(IS_NOT_HOTBAR) &&
                            slot.index < this.getExclusiveEndIndex(IS_NOT_HOTBAR);
                }));
        this.addRule(new Rule(this.getInclusiveStartIndex(IS_NOT_HOTBAR),
                this.getExclusiveEndIndex(IS_NOT_HOTBAR),
                false,
                (Slot slot) -> {
                    return slot.index >= this.getInclusiveStartIndex(IS_HOTBAR) &&
                            slot.index < this.getExclusiveEndIndex(IS_HOTBAR);
                }));
        return this;
    }

    private void addRule(Rule rule) {
        this.rules.add(rule);
    }

    private int getInclusiveStartIndex(Predicate<Slot> predicate) {
        for (int i = 0; i < this.slots.size(); i++) {
            if (predicate.test(this.slots.get(i))) {
                return i;
            }
        }

        return -1;
    }

    private int getExclusiveEndIndex(Predicate<Slot> predicate) {
        for (int i = this.slots.size() - 1; i >= 0; i--) {
            if (predicate.test(this.slots.get(i))) {
                return i + 1;
            }
        }

        return -1;
    }

    @FunctionalInterface
    public interface Action {

        boolean moveItemStackTo(ItemStack itemStack, int startIndex, int endIndex, boolean reverseDirection);
    }

    private record Rule(int startIndex, int endIndex, boolean reverseDirection, Predicate<Slot> filter) {

    }
}
