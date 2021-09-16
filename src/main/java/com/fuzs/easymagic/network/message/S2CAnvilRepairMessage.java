package com.fuzs.easymagic.network.message;

import com.fuzs.puzzleslib_em.network.message.Message;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

public class S2CAnvilRepairMessage extends Message {

    private BlockPos blockPos;
    private int stateId;

    public S2CAnvilRepairMessage() {

    }

    public S2CAnvilRepairMessage(BlockPos blockPos, BlockState blockState) {

        this.blockPos = blockPos;
        this.stateId = Block.getStateId(blockState);
    }

    public void write(PacketBuffer buf) {

        buf.writeBlockPos(this.blockPos);
        buf.writeInt(this.stateId);
    }

    public void read(PacketBuffer buf) {

        this.blockPos = buf.readBlockPos();
        this.stateId = buf.readInt();
    }

    @Override
    public MessageProcessor createProcessor() {

        return new AnvilRepairProcessor();
    }

    private class AnvilRepairProcessor implements MessageProcessor {

        @Override
        public void accept(PlayerEntity playerEntity) {

            // play repair sound
            playerEntity.world.playEvent(Constants.WorldEvents.ANVIL_USE_SOUND, S2CAnvilRepairMessage.this.blockPos, 0);

            // show block breaking particles for anvil without playing breaking sound
            BlockState blockstate = Block.getStateById(S2CAnvilRepairMessage.this.stateId);
            Minecraft.getInstance().particles.addBlockDestroyEffects(S2CAnvilRepairMessage.this.blockPos, blockstate);
        }

    }

}
