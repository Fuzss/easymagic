package fuzs.easymagic.world.level.block.entity;

import fuzs.puzzleslib.api.container.v1.ListBackedContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.LockCode;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Basically an implementation of {@link BaseContainerBlockEntity} on top of {@link EnchantingTableBlockEntity}.
 */
public abstract class BaseEnchantingTableContainerBlockEntity extends EnchantingTableBlockEntity implements MenuProvider, ListBackedContainer {
    private LockCode lockKey = LockCode.NO_LOCK;

    public BaseEnchantingTableContainerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(pos, state);
        this.type = blockEntityType;
    }

    @Override
    public void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.lockKey = LockCode.fromTag(valueInput);
        this.getContainerItems().clear();
        ContainerHelper.loadAllItems(valueInput, this.getContainerItems());
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        this.lockKey.addToTag(valueOutput);
        ContainerHelper.saveAllItems(valueOutput, this.getContainerItems(), true);
    }

    @Override
    public Component getDisplayName() {
        return this.getName();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    public boolean canOpen(Player player) {
        return this.lockKey.canUnlock(player);
    }

    public boolean isLocked() {
        return !this.lockKey.equals(LockCode.NO_LOCK);
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        if (this.canOpen(player)) {
            return this.createMenu(containerId, inventory);
        } else {
            BaseContainerBlockEntity.sendChestLockedNotifications(this.getBlockPos().getCenter(),
                    player,
                    this.getDisplayName());
            return null;
        }
    }

    protected abstract AbstractContainerMenu createMenu(int containerId, Inventory inventory);

    @Override
    protected void applyImplicitComponents(DataComponentGetter componentGetter) {
        super.applyImplicitComponents(componentGetter);
        this.lockKey = componentGetter.getOrDefault(DataComponents.LOCK, LockCode.NO_LOCK);
        componentGetter.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
                .copyInto(this.getContainerItems());
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (this.isLocked()) {
            components.set(DataComponents.LOCK, this.lockKey);
        }

        components.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.getContainerItems()));
    }

    @Override
    public void removeComponentsFromTag(ValueOutput valueOutput) {
        super.removeComponentsFromTag(valueOutput);
        valueOutput.discard("lock");
        valueOutput.discard("Items");
    }
}
