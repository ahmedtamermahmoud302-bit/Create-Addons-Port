package dev.createport.trains;

import dev.createport.Create;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.nbt.*;
import net.minecraft.network.*;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public final class TrainStationTile extends TileEntity {
    public BlockPos target;
    public boolean assembling;
    public String message="Select a straight track with the station item before placing it.";
    public String trainName="Unnamed Train";
    public void configure(BlockPos target){this.target=target;message="Open assembly mode, then use Train Casing on the marked track.";sync();}
    public void sync(){markDirty();if(world!=null && !world.isRemote){var state=world.getBlockState(pos);world.notifyBlockUpdate(pos,state,state,2);}}
    public void status(EntityPlayer player,String text){message=text;sync();if(player!=null)player.sendStatusMessage(new TextComponentString(text),true);}
    public TrainEntity train(){for(var e:world.loadedEntityList)if(e instanceof TrainEntity train && !train.isDead && train.belongsTo(pos))return train;return null;}
    public void action(EntityPlayer player,int action,String name){
        if(name!=null && !name.isBlank())trainName=name.substring(0,Math.min(32,name.length()));
        if(target==null || !world.isBlockLoaded(target) || !(world.getBlockState(target).getBlock() instanceof TrainTrackBlock)){status(player,"The station's selected track is missing.");return;}
        if(action==0){if(train()!=null){status(player,"Disassemble the stopped train before returning to assembly mode.");return;}assembling=!assembling;status(player,assembling?"Assembly mode: use Train Casing on the track to place bogeys.":"Assembly mode closed.");}
        else if(action==1){if(!assembling){status(player,"Enter assembly mode first.");return;}StationAssembly.assemble(this,player);}
        else if(action==2){var train=train();if(train==null){status(player,"No assembled train belongs to this station.");return;}StationAssembly.disassemble(this,train,player);}
    }
    public static TrainStationTile assemblyFor(World world,BlockPos track){
        for(TileEntity tile:world.loadedTileEntityList)if(tile instanceof TrainStationTile station && station.assembling && station.target!=null
                && station.target.getY()==track.getY() && station.target.distanceSq(track)<32*32){
            var state=world.getBlockState(station.target);if(!(state.getBlock() instanceof TrainTrackBlock))continue;
            var shape=TrainTrackBlock.pathShape(state);
            if(shape==RailPath.Shape.EAST_WEST && station.target.getZ()==track.getZ() || shape==RailPath.Shape.NORTH_SOUTH && station.target.getX()==track.getX())return station;
        }return null;
    }
    @Override public NBTTagCompound writeToNBT(NBTTagCompound n){super.writeToNBT(n);if(target!=null)n.setLong("TrackTarget",target.toLong());n.setBoolean("Assembling",assembling);n.setString("Message",message);n.setString("TrainName",trainName);return n;}
    @Override public void readFromNBT(NBTTagCompound n){super.readFromNBT(n);target=n.hasKey("TrackTarget")?BlockPos.fromLong(n.getLong("TrackTarget")):null;assembling=n.getBoolean("Assembling");message=n.getString("Message");trainName=n.getString("TrainName");}
    @Override public NBTTagCompound getUpdateTag(){return writeToNBT(new NBTTagCompound());}
    @Override public SPacketUpdateTileEntity getUpdatePacket(){return new SPacketUpdateTileEntity(pos,0,getUpdateTag());}
    @Override public void onDataPacket(NetworkManager manager,SPacketUpdateTileEntity packet){readFromNBT(packet.getNbtCompound());}
}
