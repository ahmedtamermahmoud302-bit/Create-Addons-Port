package dev.createport.trains;
import dev.createport.Create;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
public final class SeatBlock extends Block {
    public SeatBlock(String color){super(Material.CLOTH);setRegistryName(Create.ID,color+"_seat");setTranslationKey("create."+color+"_seat");setHardness(.5f);setCreativeTab(CreativeTabs.TRANSPORTATION);}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return new AxisAlignedBB(0,0,0,1,.5,1);}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing face,float x,float y,float z){
        if(player.isSneaking()||player.isSpectator())return false;
        if(!world.isRemote){
            var seats=world.getEntitiesWithinAABB(SeatEntity.class,new AxisAlignedBB(pos));
            if(!seats.isEmpty()){if(seats.get(0).getPassengers().isEmpty())player.startRiding(seats.get(0));return true;}
            var seat=new SeatEntity(world);seat.place(pos);if(world.spawnEntity(seat))player.startRiding(seat);
        }return true;
    }
}
