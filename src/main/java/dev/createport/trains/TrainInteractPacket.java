package dev.createport.trains;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.*;
/** Reach is checked against the actual carried block, including controls far from the rear bogey. */
public final class TrainInteractPacket implements IMessage {
    private int entityId;
    public TrainInteractPacket(){}public TrainInteractPacket(int entityId){this.entityId=entityId;}
    @Override public void toBytes(ByteBuf buffer){buffer.writeInt(entityId);}
    @Override public void fromBytes(ByteBuf buffer){entityId=buffer.readInt();}
    public static final class Handler implements IMessageHandler<TrainInteractPacket,IMessage>{
        @Override public IMessage onMessage(TrainInteractPacket packet,MessageContext context){
            var player=context.getServerHandler().player;player.getServerWorld().addScheduledTask(()->{
                if(!player.isSpectator() && player.capabilities.allowEdit && player.world.getEntityByID(packet.entityId) instanceof TrainEntity train && !train.isDead && train.worldBuilt())
                    train.applyPlayerInteraction(player,Vec3d.ZERO,EnumHand.MAIN_HAND);
            });return null;
        }
    }
}
