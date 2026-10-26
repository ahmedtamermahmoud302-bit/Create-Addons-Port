package dev.createport.trains;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.*;
public final class StationActionPacket implements IMessage {
    private BlockPos pos;private int action;private String name="";
    public StationActionPacket(){}public StationActionPacket(BlockPos pos,int action,String name){this.pos=pos;this.action=action;this.name=name;}
    @Override public void toBytes(ByteBuf buffer){buffer.writeLong(pos.toLong());buffer.writeByte(action);ByteBufUtils.writeUTF8String(buffer,name);}
    @Override public void fromBytes(ByteBuf buffer){pos=BlockPos.fromLong(buffer.readLong());action=buffer.readUnsignedByte();name=ByteBufUtils.readUTF8String(buffer);}
    public static final class Handler implements IMessageHandler<StationActionPacket,IMessage>{
        @Override public IMessage onMessage(StationActionPacket packet,MessageContext context){var player=context.getServerHandler().player;player.getServerWorld().addScheduledTask(()->{
            if(packet.action>12 || packet.name.length()>35 || player.isSpectator() || !player.capabilities.allowEdit || player.getDistanceSq(packet.pos)>64 || !player.world.isBlockLoaded(packet.pos) || !player.world.isBlockModifiable(player,packet.pos))return;
            if(player.world.getTileEntity(packet.pos) instanceof TrainStationTile station)station.action(player,packet.action,packet.name);
        });return null;}
    }
}
