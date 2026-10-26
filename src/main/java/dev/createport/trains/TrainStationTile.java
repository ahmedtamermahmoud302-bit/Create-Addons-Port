package dev.createport.trains;
import dev.createport.Create;
import java.util.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.nbt.*;
import net.minecraft.network.*;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
public final class TrainStationTile extends TileEntity implements ITickable {
    public BlockPos target;public EnumFacing direction=EnumFacing.EAST;
    public boolean assembling,lastError,trainPresent,canDisassemble;public int bogeyCount,assemblyLength,doorMode;
    public String message="",trainName="Unnamed Train",stationName="Track Station";
    public float flag,previousFlag;
    public void configure(BlockPos target){configure(target,null);}
    public void configure(BlockPos target,EnumFacing direction){this.target=target;if(direction!=null)this.direction=direction;else if(TrainTrackBlock.pathShape(world.getBlockState(target))==RailPath.Shape.NORTH_SOUTH)this.direction=EnumFacing.SOUTH;refresh();sync();}
    public void sync(){markDirty();if(world!=null&&!world.isRemote){var state=world.getBlockState(pos);world.notifyBlockUpdate(pos,state,state,2);}}
    public void status(EntityPlayer player,String text){lastError=false;message=text;refresh();sync();if(player!=null)player.sendStatusMessage(new TextComponentString(text),true);}
    public void error(EntityPlayer player,String text){message=text;lastError=true;refresh();sync();if(player!=null)player.sendStatusMessage(new TextComponentString(text),true);}
    public TrainEntity train(){for(var e:world.loadedEntityList)if(e instanceof TrainEntity train&&!train.isDead&&train.belongsTo(pos))return train;return null;}
    public void refresh(){
        var train=train();trainPresent=train!=null;canDisassemble=train!=null&&Math.abs(train.speed())<.0001&&!train.onCurve()&&Math.abs(train.rotationPitch)<.001&&Math.abs(Math.sin(Math.toRadians(train.rotationYaw*2)))<.001;
        if(canDisassemble&&target!=null){var track=new BlockPos(train.posX,train.posY-.15,train.posZ);canDisassemble=track.getY()==target.getY()&&(direction.getAxis()==EnumFacing.Axis.X?track.getZ()==target.getZ():track.getX()==target.getX())&&track.distanceSq(target)<=32*32;}
        bogeyCount=0;assemblyLength=0;if(target==null)return;
        var state=world.getBlockState(target);if(!(state.getBlock() instanceof TrainTrackBlock))return;var shape=TrainTrackBlock.pathShape(state);
        for(int i=1;i<=48;i++){BlockPos p=target.offset(direction,i);if(!world.isBlockLoaded(p)||!(world.getBlockState(p).getBlock() instanceof TrainTrackBlock)||TrainTrackBlock.pathShape(world.getBlockState(p))!=shape)break;assemblyLength=i;if(world.getBlockState(p.up()).getBlock()==Create.SMALL_BOGEY)bogeyCount++;}
    }
    public int offset(BlockPos p){if(target==null||p.getY()!=target.getY())return -1;int dx=p.getX()-target.getX(),dz=p.getZ()-target.getZ();if(direction.getAxis()==EnumFacing.Axis.X&&dz!=0||direction.getAxis()==EnumFacing.Axis.Z&&dx!=0)return -1;return dx*direction.getXOffset()+dz*direction.getZOffset()-1;}
    public boolean validBogeyOffset(int offset){if(offset<0||offset>=assemblyLength)return false;BlockPos p=target.offset(direction,offset+1).up();for(int i=-2;i<=2;i++)if(world.getBlockState(p.offset(direction,i)).getBlock()==Create.SMALL_BOGEY)return false;return true;}
    @Override public void update(){
        if(world.isRemote){previousFlag=flag;float targetFlag=assembling||trainPresent?1:0;flag+=Math.max(-.1f,Math.min(.1f,targetFlag-flag));return;}
        if((world.getTotalWorldTime()+pos.toLong())%10==0){int oldCount=bogeyCount,oldLength=assemblyLength;boolean oldPresent=trainPresent,oldCanDisassemble=canDisassemble;refresh();if(oldCount!=bogeyCount||oldLength!=assemblyLength||oldPresent!=trainPresent||oldCanDisassemble!=canDisassemble)sync();}
    }
    public void action(EntityPlayer player,int action,String name){
        if(action==5){if(name!=null&&!name.isBlank())stationName=name.substring(0,Math.min(25,name.length()));sync();return;}
        if(action==6){if(name!=null&&!name.isBlank()){trainName=name.substring(0,Math.min(35,name.length()));var t=train();if(t!=null)t.setCustomNameTag(trainName);}sync();return;}
        if(action>=7&&action<=12){doorMode=action-7;sync();return;}
        if(target==null||!world.isBlockLoaded(target)||!(world.getBlockState(target).getBlock() instanceof TrainTrackBlock)){error(player,"The station's selected track is missing.");return;}
        refresh();
        if(action==0){if(train()!=null){error(player,"Disassemble the stopped train before entering assembly mode.");return;}assembling=!assembling;lastError=false;sync();}
        else if(action==1){if(!assembling){error(player,"Enter assembly mode first.");return;}StationAssembly.assemble(this,player);}
        else if(action==2){var train=train();if(train==null){error(player,"No assembled train is present.");return;}StationAssembly.disassemble(this,train,player);}
    }
    public static TrainStationTile assemblyFor(World world,BlockPos track){for(TileEntity tile:world.loadedTileEntityList)if(tile instanceof TrainStationTile station&&station.assembling&&!station.trainPresent&&station.offset(track)>=0&&station.offset(track)<station.assemblyLength)return station;return null;}
    @Override public AxisAlignedBB getRenderBoundingBox(){var box=new AxisAlignedBB(pos).grow(3);return target==null?box:box.union(new AxisAlignedBB(target,target.offset(direction,assemblyLength)).grow(3));}
    @Override public double getMaxRenderDistanceSquared(){return 192*192;}
    @Override public NBTTagCompound writeToNBT(NBTTagCompound n){super.writeToNBT(n);if(target!=null)n.setLong("TrackTarget",target.toLong());n.setInteger("Direction",direction.getIndex());n.setBoolean("Assembling",assembling);n.setString("Message",message);n.setString("TrainName",trainName);n.setString("StationName",stationName);n.setBoolean("LastError",lastError);n.setBoolean("TrainPresent",trainPresent);n.setBoolean("CanDisassemble",canDisassemble);n.setInteger("BogeyCount",bogeyCount);n.setInteger("AssemblyLength",assemblyLength);n.setInteger("DoorMode",doorMode);return n;}
    @Override public void readFromNBT(NBTTagCompound n){super.readFromNBT(n);target=n.hasKey("TrackTarget")?BlockPos.fromLong(n.getLong("TrackTarget")):null;direction=n.hasKey("Direction")?EnumFacing.byIndex(n.getInteger("Direction")):EnumFacing.EAST;if(direction.getAxis()==EnumFacing.Axis.Y)direction=EnumFacing.EAST;assembling=n.getBoolean("Assembling");message=n.getString("Message");trainName=n.hasKey("TrainName")?n.getString("TrainName"):"Unnamed Train";stationName=n.hasKey("StationName")?n.getString("StationName"):"Track Station";lastError=n.getBoolean("LastError");trainPresent=n.getBoolean("TrainPresent");canDisassemble=n.getBoolean("CanDisassemble");bogeyCount=n.getInteger("BogeyCount");assemblyLength=n.getInteger("AssemblyLength");doorMode=n.getInteger("DoorMode");}
    @Override public NBTTagCompound getUpdateTag(){return writeToNBT(new NBTTagCompound());}
    @Override public SPacketUpdateTileEntity getUpdatePacket(){return new SPacketUpdateTileEntity(pos,0,getUpdateTag());}
    @Override public void onDataPacket(NetworkManager manager,SPacketUpdateTileEntity packet){readFromNBT(packet.getNbtCompound());}
}
