package dev.createport.trains;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.*;

public final class TrainControlPacket implements IMessage {
    private int entityId,input;
    private boolean brake;
    public TrainControlPacket() {}
    public TrainControlPacket(int entityId,int input,boolean brake) { this.entityId=entityId;this.input=input;this.brake=brake; }
    @Override public void fromBytes(ByteBuf buf) { entityId=buf.readInt();input=buf.readByte();brake=buf.readBoolean(); }
    @Override public void toBytes(ByteBuf buf) { buf.writeInt(entityId);buf.writeByte(input);buf.writeBoolean(brake); }
    public static final class Handler implements IMessageHandler<TrainControlPacket,IMessage> {
        @Override public IMessage onMessage(TrainControlPacket packet,MessageContext context) {
            EntityPlayerMP player=context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.isSpectator() || packet.input < -1 || packet.input > 1) return;
                if (player.getRidingEntity() instanceof TrainEntity train && train.getEntityId()==packet.entityId
                        && train.getControllingPassenger()==player) train.control(packet.input,packet.brake);
            });
            return null;
        }
    }
}
