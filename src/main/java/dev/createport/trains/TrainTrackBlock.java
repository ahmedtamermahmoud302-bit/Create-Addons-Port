package dev.createport.trains;

import dev.createport.Create;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.BlockRenderLayer;

public final class TrainTrackBlock extends BlockRailBase {
    public enum Diagonal implements net.minecraft.util.IStringSerializable { NONE,POSITIVE,NEGATIVE; public String getName(){return name().toLowerCase(java.util.Locale.ROOT);} }
    public static final PropertyEnum<Diagonal> DIAGONAL=PropertyEnum.create("diagonal",Diagonal.class);
    public static final PropertyEnum<EnumRailDirection> SHAPE=PropertyEnum.create("shape",EnumRailDirection.class);
    public TrainTrackBlock() {
        super(false);setRegistryName(Create.ID,"track");setTranslationKey("create.track");
        setCreativeTab(CreativeTabs.TRANSPORTATION);setHardness(.7f);
        setDefaultState(blockState.getBaseState().withProperty(SHAPE,EnumRailDirection.NORTH_SOUTH).withProperty(DIAGONAL,Diagonal.NONE));
    }
    @Override public IProperty<EnumRailDirection> getShapeProperty() { return SHAPE; }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this,SHAPE,DIAGONAL); }
    @Override public IBlockState getStateFromMeta(int meta) { return meta==10 || meta==11 ? getDefaultState().withProperty(DIAGONAL,meta==10?Diagonal.POSITIVE:Diagonal.NEGATIVE):getDefaultState().withProperty(SHAPE,EnumRailDirection.byMetadata(Math.floorMod(meta,10))); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(DIAGONAL)==Diagonal.NONE ? state.getValue(SHAPE).getMetadata():state.getValue(DIAGONAL)==Diagonal.POSITIVE?10:11; }
    public static RailPath.Shape pathShape(IBlockState state){return state.getValue(DIAGONAL)==Diagonal.NONE ? RailPath.Shape.valueOf(state.getValue(SHAPE).name()):state.getValue(DIAGONAL)==Diagonal.POSITIVE?RailPath.Shape.DIAGONAL_POS:RailPath.Shape.DIAGONAL_NEG;}
    @Override public IBlockState getStateForPlacement(net.minecraft.world.World world,net.minecraft.util.math.BlockPos pos,net.minecraft.util.EnumFacing face,float x,float y,float z,int meta,net.minecraft.entity.EntityLivingBase player){
        int direction=Math.floorMod(Math.round(player.rotationYaw/45),4);
        return direction==0?getDefaultState():direction==2?getDefaultState().withProperty(SHAPE,EnumRailDirection.EAST_WEST):getDefaultState().withProperty(DIAGONAL,direction==1?Diagonal.NEGATIVE:Diagonal.POSITIVE);
    }
    @Override public void onBlockAdded(net.minecraft.world.World world,net.minecraft.util.math.BlockPos pos,IBlockState state) { }
    @Override public void neighborChanged(IBlockState state,net.minecraft.world.World world,net.minecraft.util.math.BlockPos pos,net.minecraft.block.Block block,net.minecraft.util.math.BlockPos from) { }
    @Override public void breakBlock(net.minecraft.world.World world,net.minecraft.util.math.BlockPos pos,IBlockState state){if(!world.isRemote)CurveGraphData.get(world).removeAt(world,pos);super.breakBlock(world,pos,state);}
    @Override public boolean canPlaceBlockAt(net.minecraft.world.World world,net.minecraft.util.math.BlockPos pos){return world.getBlockState(pos).getBlock().isReplaceable(world,pos);}
    @Override public boolean hasTileEntity(IBlockState state){return true;}
    @Override public net.minecraft.tileentity.TileEntity createTileEntity(net.minecraft.world.World world,IBlockState state){return new TrackRenderTile();}
    @Override public net.minecraft.util.EnumBlockRenderType getRenderType(IBlockState state){return state.getValue(DIAGONAL)!=Diagonal.NONE || state.getValue(SHAPE).isAscending() ? net.minecraft.util.EnumBlockRenderType.INVISIBLE:net.minecraft.util.EnumBlockRenderType.MODEL;}
    @Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT; }
}
