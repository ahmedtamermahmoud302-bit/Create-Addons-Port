package dev.createport.trains;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.*;
/** Driver ownership and eight-block console reach are checked on the server, for seated or standing drivers. */
public final class TrainControlPacket implements IMessage {
    private int entityId,input,steering;private boolean brake,stop;private float throttle=1;
    public TrainControlPacket(){}
    public TrainControlPacket(int id,int input,boolean brake,int steering,float throttle,boolean stop){entityId=id;this.input=input;this.brake=brake;this.steering=steering;this.throttle=throttle;this.stop=stop;}
    @Override public void fromBytes(ByteBuf b){entityId=b.readInt();input=b.readByte();brake=b.readBoolean();steering=b.readByte();throttle=b.readFloat();stop=b.readBoolean();}
    @Override public void toBytes(ByteBuf b){b.writeInt(entityId);b.writeByte(input);b.writeBoolean(brake);b.writeByte(steering);b.writeFloat(throttle);b.writeBoolean(stop);}
    public static final class Handler implements IMessageHandler<TrainControlPacket,IMessage>{public IMessage onMessage(TrainControlPacket packet,MessageContext context){var p=context.getServerHandler().player;p.getServerWorld().addScheduledTask(()->{
        if(p.isSpectator()||Math.abs(packet.input)>1||Math.abs(packet.steering)>1||!Float.isFinite(packet.throttle)||packet.throttle<1/18f||packet.throttle>1)return;
        if(p.world.getEntityByID(packet.entityId) instanceof TrainEntity train&&train.driver()==p&&train.driverInReach(p)){if(packet.stop)train.stopControlling();else train.control(packet.input,packet.brake,packet.steering,packet.throttle);}
    });return null;}}
}
