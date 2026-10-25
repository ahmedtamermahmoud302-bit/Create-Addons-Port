package dev.createport.kinetics;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.*;

public final class SpeedPacket implements IMessage {
    private BlockPos pos; private int speed;
    public SpeedPacket() {}
    public SpeedPacket(BlockPos pos, int speed) { this.pos = pos; this.speed = speed; }
    @Override public void fromBytes(ByteBuf buffer) { pos = BlockPos.fromLong(buffer.readLong()); speed = buffer.readInt(); }
    @Override public void toBytes(ByteBuf buffer) { buffer.writeLong(pos.toLong()); buffer.writeInt(speed); }
    public static final class Handler implements IMessageHandler<SpeedPacket, IMessage> {
        @Override public IMessage onMessage(SpeedPacket message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.isSpectator() || !player.capabilities.allowEdit || message.speed < -256 || message.speed > 256
                        || player.getDistanceSq(message.pos) > 64 || !player.world.isBlockLoaded(message.pos)
                        || !player.world.isBlockModifiable(player, message.pos)) return;
                if (player.world.getTileEntity(message.pos) instanceof KineticTile tile && tile.motor()) tile.setGeneratedRpm(message.speed);
            });
            return null;
        }
    }
}
