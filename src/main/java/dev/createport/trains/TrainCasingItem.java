package dev.createport.trains;
import dev.createport.Create;
import net.minecraft.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
public final class TrainCasingItem extends ItemBlock {
    public TrainCasingItem(){super(Create.RAILWAY_CASING);setRegistryName(Create.RAILWAY_CASING.getRegistryName());}
    @Override public EnumActionResult onItemUse(EntityPlayer player,World world,BlockPos pos,EnumHand hand,EnumFacing face,float x,float y,float z){
        if(!(world.getBlockState(pos).getBlock() instanceof TrainTrackBlock))return super.onItemUse(player,world,pos,hand,face,x,y,z);
        if(world.isRemote)return EnumActionResult.SUCCESS;
        var station=TrainStationTile.assemblyFor(world,pos);if(station==null){player.sendStatusMessage(new TextComponentString("Enter assembly mode at a nearby Train Station first."),true);return EnumActionResult.FAIL;}
        var shape=TrainTrackBlock.pathShape(world.getBlockState(pos));if(shape!=RailPath.Shape.EAST_WEST && shape!=RailPath.Shape.NORTH_SOUTH)return EnumActionResult.FAIL;
        station.refresh();
        // The adjacent rail remains reachable when the bogey/body occludes its own rail.
        BlockPos bogeyPos=pos.up();
        if(!station.validBogeyOffset(station.offset(pos)))for(int i=-1;i<=1;i++){
            var candidate=pos.offset(station.direction,i).up();if(Bogeys.is(world.getBlockState(candidate))){bogeyPos=candidate;break;}
        }
        var existing=world.getBlockState(bogeyPos);
        if(Bogeys.is(existing)){
            if(!world.isBlockModifiable(player,bogeyPos)||!player.canPlayerEdit(bogeyPos,face,player.getHeldItem(hand)))return EnumActionResult.FAIL;
            var next=(Bogeys.large(existing)?Create.SMALL_BOGEY:Create.LARGE_BOGEY).getDefaultState().withProperty(RailwayBlock.FACING,existing.getValue(RailwayBlock.FACING));
            if(!world.setBlockState(bogeyPos,next,3))return EnumActionResult.FAIL;
            station.status(player,Bogeys.large(next)?"Standard Bogey: Large":"Standard Bogey: Small");return EnumActionResult.SUCCESS;
        }
        if(!station.validBogeyOffset(station.offset(pos)))return EnumActionResult.FAIL;
        BlockPos top=pos.up();if(!world.isAirBlock(top) || !world.isBlockModifiable(player,top) || !player.canPlayerEdit(top,face,player.getHeldItem(hand)))return EnumActionResult.FAIL;
        for(int i=-2;i<=2;i++)if(Bogeys.is(world.getBlockState(top.add(shape==RailPath.Shape.EAST_WEST?i:0,0,shape==RailPath.Shape.NORTH_SOUTH?i:0))))return EnumActionResult.FAIL;
        var facing=shape==RailPath.Shape.EAST_WEST?EnumFacing.EAST:EnumFacing.SOUTH;
        if(!world.setBlockState(top,Create.SMALL_BOGEY.getDefaultState().withProperty(RailwayBlock.FACING,facing),3))return EnumActionResult.FAIL;
        if(!player.capabilities.isCreativeMode)player.getHeldItem(hand).shrink(1);
        station.status(player,"Bogey placed. Build and glue the carriage, then add Train Controls.");return EnumActionResult.SUCCESS;
    }
}
