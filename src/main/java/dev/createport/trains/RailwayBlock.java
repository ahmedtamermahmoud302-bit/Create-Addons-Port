package dev.createport.trains;

import dev.createport.Create;
import dev.createport.equipment.WrenchItem;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.item.Item;
import java.util.Random;

public class RailwayBlock extends Block {
    public enum Kind { CASING,BOGEY,CONTROLS,STATION }
    public static final PropertyDirection FACING=PropertyDirection.create("facing",EnumFacing.Plane.HORIZONTAL);
    public final Kind kind;
    public RailwayBlock(String name,Kind kind){super(Material.IRON);this.kind=kind;setRegistryName(Create.ID,name);setTranslationKey("create."+name);setHardness(1.5f);if(kind!=Kind.BOGEY)setCreativeTab(CreativeTabs.TRANSPORTATION);setDefaultState(blockState.getBaseState().withProperty(FACING,EnumFacing.NORTH));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FACING);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(FACING,EnumFacing.byHorizontalIndex(meta));}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(FACING).getHorizontalIndex();}
    @Override public IBlockState getStateForPlacement(World w,BlockPos p,EnumFacing face,float x,float y,float z,int meta,EntityLivingBase placer){return getDefaultState().withProperty(FACING,placer.getHorizontalFacing().getOpposite());}
    @Override public IBlockState withRotation(IBlockState state,Rotation rotation){return state.withProperty(FACING,rotation.rotate(state.getValue(FACING)));}
    @Override public boolean hasTileEntity(IBlockState state){return kind==Kind.STATION || kind==Kind.BOGEY;}
    @Override public TileEntity createTileEntity(World world,IBlockState state){return kind==Kind.STATION?new TrainStationTile():kind==Kind.BOGEY?new BogeyTile():null;}
    @Override public boolean isOpaqueCube(IBlockState state){return kind==Kind.CASING;}
    @Override public boolean isFullCube(IBlockState state){return kind==Kind.CASING;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return kind==Kind.BOGEY?new AxisAlignedBB(0,.4375,0,1,1,1):kind==Kind.STATION?new AxisAlignedBB(0,0,0,1,.8125,1):FULL_BLOCK_AABB;}
    @Override public Item getItemDropped(IBlockState state,Random random,int fortune){return kind==Kind.BOGEY?Item.getItemFromBlock(Create.RAILWAY_CASING):super.getItemDropped(state,random,fortune);}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing face,float x,float y,float z){
        if(kind!=Kind.STATION || player.isSneaking() || WrenchItem.isHolding(player))return false;
        if(world.isRemote)Create.proxy.openStation(pos);return true;
    }
}
