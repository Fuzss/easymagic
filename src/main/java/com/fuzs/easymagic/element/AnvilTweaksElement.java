package com.fuzs.easymagic.element;

import com.fuzs.puzzleslib_em.PuzzlesLib;
import com.fuzs.puzzleslib_em.element.AbstractElement;
import com.fuzs.puzzleslib_em.element.side.ICommonElement;
import com.fuzs.easymagic.EasyMagic;
import com.fuzs.easymagic.network.message.SAnvilRepairMessage;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nullable;

public class AnvilTweaksElement extends AbstractElement implements ICommonElement {

    public static final Tags.IOptionalNamedTag<Item> ANVIL_REPAIR_MATERIALS = ItemTags.createOptional(new ResourceLocation(EasyMagic.MODID, "anvil_repair_materials"));
    public static final Tags.IOptionalNamedTag<Item> ANVIL_REPAIR_CHANCE_MATERIALS = ItemTags.createOptional(new ResourceLocation(EasyMagic.MODID, "anvil_repair_chance_materials"));

    private boolean repairAnvilWithIron;
    private int anvilRepairChance;
    
    @Override
    public String getDescription() {

        return "Some convenient tweaks for working with anvils.";
    }

    @Override
    public void setupCommon() {

        this.addListener(this::onRightClickBlock);
    }

    @Override
    public void loadCommon() {

        PuzzlesLib.getNetworkHandler().registerMessage(SAnvilRepairMessage::new, LogicalSide.CLIENT);
    }

    @Override
    public void setupCommonConfig(ForgeConfigSpec.Builder builder) {

        addToConfig(builder.comment("Using an iron ingot on a damaged anvil has a chance to repair it.").define("Iron Repairs Anvil", true), v -> this.repairAnvilWithIron = v);
        addToConfig(builder.comment("Chance one out of set value an attempt at repairing an anvil will be successful.").defineInRange("Anvil Repair Chance", 5, 1, Integer.MAX_VALUE), v -> this.anvilRepairChance = v);
    }

    private void onRightClickBlock(final PlayerInteractEvent.RightClickBlock evt) {

        boolean isRepairMaterial = evt.getItemStack().getItem().isIn(ANVIL_REPAIR_MATERIALS);
        boolean isRepairChanceMaterial = evt.getItemStack().getItem().isIn(ANVIL_REPAIR_CHANCE_MATERIALS);
        if (this.repairAnvilWithIron && evt.getSide().isServer() && (isRepairMaterial || isRepairChanceMaterial)) {

            World world = evt.getWorld();
            BlockPos blockPos = evt.getPos();
            BlockState blockState = repair(world.getBlockState(blockPos));
            if (blockState != null) {

                // fully repair when using an iron block
                if (isRepairMaterial && blockState.isIn(Blocks.CHIPPED_ANVIL)) {

                    blockState = repair(blockState);
                }

                evt.setUseBlock(Event.Result.DENY);
                evt.setUseItem(Event.Result.DENY);

                if (!evt.getPlayer().abilities.isCreativeMode) {

                    evt.getItemStack().shrink(1);
                }

                if (isRepairMaterial || world.getRandom().nextInt(this.anvilRepairChance) == 0) {

                    assert blockState != null;
                    world.setBlockState(blockPos, blockState, 2);
                    // don't call IWorld::playEvent as it also plays a block breaking sound
                    SAnvilRepairMessage message = new SAnvilRepairMessage(blockPos, blockState);
                    PuzzlesLib.getNetworkHandler().sendToAllNear(message, world, blockPos);
                } else if (world instanceof ServerWorld) {

                    double randomOffset = 0.33;
                    double posX = blockPos.getX() + 0.5;
                    double posY = blockPos.getY() + 0.5;
                    double posZ = blockPos.getZ() + 0.5;
                    ((ServerWorld) world).spawnParticle(ParticleTypes.SMOKE, posX, posY, posZ, 20, randomOffset, randomOffset, randomOffset, 0.0);
                }
            }
        }
    }

    @Nullable
    private static BlockState repair(BlockState state) {

        if (state.isIn(Blocks.DAMAGED_ANVIL)) {

            return Blocks.CHIPPED_ANVIL.getDefaultState().with(AnvilBlock.FACING, state.get(AnvilBlock.FACING));
        } else {

            return state.isIn(Blocks.CHIPPED_ANVIL) ? Blocks.ANVIL.getDefaultState().with(AnvilBlock.FACING, state.get(AnvilBlock.FACING)) : null;
        }
    }

}
