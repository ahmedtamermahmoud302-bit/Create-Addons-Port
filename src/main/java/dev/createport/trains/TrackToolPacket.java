package dev.createport.trains;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraftforge.fml.common.network.simpleimpl.*;
/** Carries the Sprint key state, which differs from the player's actual sprinting state. */
public final class TrackToolPacket implements IMessage {
    private BlockPos clicked;private EnumFacing face;private boolean maximise;
    public TrackToolPacket(){}public TrackToolPacket(BlockPos clicked,EnumFacing face,boolean maximise){this.clicked=clicked;this.face=face;this.maximise=maximise;}
    @Override public void toBytes(ByteBuf b){b.writeLong(clicked.toLong());b.writeByte(face.getIndex());b.writeBoolean(maximise);}
    @Override public void fromBytes(ByteBuf b){clicked=BlockPos.fromLong(b.readLong());face=EnumFacing.byIndex(b.readUnsignedByte());maximise=b.readBoolean();}
    public static final class Handler implements IMessageHandler<TrackToolPacket,IMessage>{public IMessage onMessage(TrackToolPacket packet,MessageContext context){var p=context.getServerHandler().player;p.getServerWorld().addScheduledTask(()->{
        var stack=p.getHeldItemMainhand();if(!(stack.getItem() instanceof TrackItem item)||!item.hasEffect(stack)||p.isSpectator()||!p.world.isBlockLoaded(packet.clicked))return;
        Vec3d eye=p.getPositionEyes(1);var ray=p.world.rayTraceBlocks(eye,eye.add(p.getLookVec().scale(6)),false,false,false);
        if(ray==null||!ray.getBlockPos().equals(packet.clicked)||ray.sideHit!=packet.face)return;
        stack.getTagCompound().setBoolean("ExtendCurve",packet.maximise);
        try{item.onItemUse(p,p.world,packet.clicked,EnumHand.MAIN_HAND,packet.face,.5f,.5f,.5f);}finally{if(stack.hasTagCompound())stack.getTagCompound().removeTag("ExtendCurve");}
    });return null;}}
}
