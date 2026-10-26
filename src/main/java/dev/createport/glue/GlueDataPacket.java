package dev.createport.glue;
import dev.createport.Create;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.*;
public final class GlueDataPacket implements IMessage {
    public NBTTagCompound data=new NBTTagCompound();
    public GlueDataPacket(){}public GlueDataPacket(NBTTagCompound data){this.data=data;}
    @Override public void toBytes(ByteBuf buffer){ByteBufUtils.writeTag(buffer,data);}
    @Override public void fromBytes(ByteBuf buffer){data=ByteBufUtils.readTag(buffer);if(data==null)data=new NBTTagCompound();}
    public static final class Server implements IMessageHandler<GlueDataPacket,IMessage>{
        @Override public IMessage onMessage(GlueDataPacket packet,MessageContext context){
            var player=context.getServerHandler().player;player.getServerWorld().addScheduledTask(()->{
                if(player.getHeldItemMainhand().getItem()!=Create.SUPER_GLUE||player.isSpectator())return;
                var saved=GlueWorldData.get(player.world);
                if(packet.data.hasUniqueId("Remove")){
                    var area=saved.area(packet.data.getUniqueId("Remove"));
                    if(area!=null){Vec3d eye=player.getPositionEyes(1),end=eye.add(player.getLookVec().scale(6));
                        if((area.bounds().contains(eye)||area.bounds().calculateIntercept(eye,end)!=null)&&player.world.isBlockModifiable(player,area.first())&&player.world.isBlockModifiable(player,area.second())&&player.canPlayerEdit(area.first(),EnumFacing.UP,player.getHeldItemMainhand())&&player.canPlayerEdit(area.second(),EnumFacing.UP,player.getHeldItemMainhand()))saved.removeArea(area.id());
                    }
                }else if(packet.data.hasKey("RemoveA")&&packet.data.hasKey("RemoveB")){
                    BlockPos a=BlockPos.fromLong(packet.data.getLong("RemoveA")),b=BlockPos.fromLong(packet.data.getLong("RemoveB"));var box=new AxisAlignedBB(a,b).expand(1,1,1);
                    if(box.calculateIntercept(player.getPositionEyes(1),player.getPositionEyes(1).add(player.getLookVec().scale(6)))!=null&&player.world.isBlockModifiable(player,a)&&player.world.isBlockModifiable(player,b)&&player.canPlayerEdit(a,EnumFacing.UP,player.getHeldItemMainhand())&&player.canPlayerEdit(b,EnumFacing.UP,player.getHeldItemMainhand()))saved.disconnect(a,b);
                }
                var n=new NBTTagCompound();n.setInteger("Dimension",player.dimension);var list=new NBTTagList();
                for(var area:saved.areas())if(area.bounds().grow(32,16,32).contains(player.getPositionVector())&&list.tagCount()<512){var item=new NBTTagCompound();item.setUniqueId("Id",area.id());item.setLong("A",area.first().toLong());item.setLong("B",area.second().toLong());list.appendTag(item);}
                for(var bond:saved.graph().bonds())if(list.tagCount()<512){var a=GlueWorldData.block(bond.a());if(a.distanceSq(player.getPosition())>48*48)continue;var item=new NBTTagCompound();item.setLong("A",a.toLong());item.setLong("B",GlueWorldData.block(bond.b()).toLong());list.appendTag(item);}
                n.setTag("Areas",list);Create.NETWORK.sendTo(new GlueDataPacket(n),player);
            });return null;
        }
    }
    public static final class Client implements IMessageHandler<GlueDataPacket,IMessage>{public IMessage onMessage(GlueDataPacket packet,MessageContext context){Create.proxy.acceptGlueData(packet.data);return null;}}
}
