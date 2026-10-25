package dev.createport.trains;
import dev.createport.Create;
import net.minecraft.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
public final class StationItem extends ItemBlock {
    public StationItem(){super(Create.TRACK_STATION);setRegistryName(Create.TRACK_STATION.getRegistryName());}
    @Override public boolean hasEffect(ItemStack stack){return stack.hasTagCompound() && stack.getTagCompound().hasKey("TrackTarget");}
    @Override public EnumActionResult onItemUse(EntityPlayer player,World world,BlockPos pos,EnumHand hand,EnumFacing face,float x,float y,float z){
        var stack=player.getHeldItem(hand);
        if(player.isSneaking() && hasEffect(stack)){if(!world.isRemote)stack.getTagCompound().removeTag("TrackTarget");return EnumActionResult.SUCCESS;}
        if(world.getBlockState(pos).getBlock() instanceof TrainTrackBlock){
            var shape=TrainTrackBlock.pathShape(world.getBlockState(pos));if(shape!=RailPath.Shape.NORTH_SOUTH && shape!=RailPath.Shape.EAST_WEST){if(!world.isRemote)player.sendStatusMessage(new TextComponentString("Choose straight track for the assembly station."),true);return EnumActionResult.FAIL;}
            if(!world.isRemote){var tag=stack.getTagCompound();if(tag==null){tag=new NBTTagCompound();stack.setTagCompound(tag);}tag.setLong("TrackTarget",pos.toLong());tag.setInteger("Dimension",world.provider.getDimension());player.sendStatusMessage(new TextComponentString("Track selected. Place the station beside it."),true);}return EnumActionResult.SUCCESS;
        }
        if(!hasEffect(stack)){if(!world.isRemote)player.sendStatusMessage(new TextComponentString("First use the Train Station item on a straight track."),true);return EnumActionResult.FAIL;}
        return super.onItemUse(player,world,pos,hand,face,x,y,z);
    }
    @Override public boolean placeBlockAt(ItemStack stack,EntityPlayer player,World world,BlockPos pos,EnumFacing face,float x,float y,float z,IBlockState state){
        if(!hasEffect(stack) || stack.getTagCompound().getInteger("Dimension")!=world.provider.getDimension())return false;
        BlockPos target=BlockPos.fromLong(stack.getTagCompound().getLong("TrackTarget"));if(target.distanceSq(pos)>16*16 || !world.isBlockLoaded(target))return false;
        if(!super.placeBlockAt(stack,player,world,pos,face,x,y,z,state))return false;
        if(world.getTileEntity(pos) instanceof TrainStationTile station)station.configure(target);
        stack.getTagCompound().removeTag("TrackTarget");return true;
    }
}
