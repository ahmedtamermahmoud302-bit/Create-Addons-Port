package dev.createport.kinetics;
import dev.createport.Create;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
public class MechanicalBlock extends KineticBlock {
    public enum Kind { COG,LARGE_COG,CRANK,GEARBOX,CLUTCH,GEARSHIFT,CHAIN,ADJUSTABLE_CHAIN,BELT }
    public enum Part implements IStringSerializable { START,MIDDLE,END,NONE;public String getName(){return name().toLowerCase(java.util.Locale.ROOT);} }
    public static final PropertyBool POWERED=PropertyBool.create("powered"),ALONG=PropertyBool.create("axis_along_first");
    public static final PropertyEnum<Part> PART=PropertyEnum.create("part",Part.class);
    public final Kind kind;
    public MechanicalBlock(String name,Kind kind){super(name,false);this.kind=kind;setDefaultState(blockState.getBaseState().withProperty(AXIS,EnumFacing.Axis.Y).withProperty(FACING,EnumFacing.UP).withProperty(POWERED,false).withProperty(ALONG,false).withProperty(PART,Part.NONE));}
    public boolean chain(){return kind==Kind.CHAIN||kind==Kind.ADJUSTABLE_CHAIN;}
    public boolean cog(){return kind==Kind.COG||kind==Kind.LARGE_COG;}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,AXIS,FACING,POWERED,ALONG,PART);}
    @Override public IBlockState getStateFromMeta(int meta){return kind==Kind.CRANK?getDefaultState().withProperty(FACING,EnumFacing.byIndex(meta%6)).withProperty(AXIS,EnumFacing.byIndex(meta%6).getAxis()):getDefaultState().withProperty(AXIS,EnumFacing.Axis.values()[Math.floorMod(meta,4)%3]).withProperty(POWERED,(meta&4)!=0);}
    @Override public int getMetaFromState(IBlockState s){return kind==Kind.CRANK?s.getValue(FACING).getIndex():s.getValue(AXIS).ordinal()+(s.getValue(POWERED)?4:0);}
    @Override public IBlockState getStateForPlacement(World world,BlockPos pos,EnumFacing face,float x,float y,float z,int meta,EntityLivingBase player){
        var s=super.getStateForPlacement(world,pos,face,x,y,z,meta,player);if(kind==Kind.GEARBOX){boolean vertical=player instanceof EntityPlayer p&&p.getHeldItemMainhand().getItem()==Create.VERTICAL_GEARBOX;s=s.withProperty(AXIS,vertical?player.getHorizontalFacing().getAxis():EnumFacing.Axis.Y);}
        if(kind==Kind.CRANK)s=s.withProperty(FACING,face).withProperty(AXIS,face.getAxis());return s.withProperty(POWERED,world.isBlockPowered(pos));
    }
    @Override public IBlockState getActualState(IBlockState s,IBlockAccess world,BlockPos pos){
        if(!chain())return s;EnumFacing.Axis axis=KineticTopology.chainAxis(world,pos,s);boolean negative=false,positive=false;
        for(var face:EnumFacing.values())if(face.getAxis()==axis){var other=world.getBlockState(pos.offset(face));if(other.getBlock() instanceof MechanicalBlock m&&m.chain()&&other.getValue(AXIS)!=axis){if(face.getAxisDirection()==EnumFacing.AxisDirection.POSITIVE)positive=true;else negative=true;}}
        return s.withProperty(ALONG,s.getValue(AXIS)==EnumFacing.Axis.X?axis==EnumFacing.Axis.Y:axis==EnumFacing.Axis.X).withProperty(PART,negative&&positive?Part.MIDDLE:positive?Part.START:negative?Part.END:Part.NONE);
    }
    @Override public IBlockState getStateForPlacement(World world,BlockPos pos,EnumFacing face,float x,float y,float z,int meta,EntityLivingBase player,EnumHand hand){
        var state=getStateForPlacement(world,pos,face,x,y,z,meta,player);
        if(kind==Kind.GEARBOX&&player instanceof EntityPlayer p)state=state.withProperty(AXIS,p.getHeldItem(hand).getItem()==Create.VERTICAL_GEARBOX?player.getHorizontalFacing().getAxis():EnumFacing.Axis.Y);
        return state;
    }
    @Override public Item getItemDropped(IBlockState state,java.util.Random random,int fortune){if(kind==Kind.GEARBOX&&state.getValue(AXIS)!=EnumFacing.Axis.Y)return Create.VERTICAL_GEARBOX;if(kind==Kind.BELT)return Create.BELT_CONNECTOR;return super.getItemDropped(state,random,fortune);}
    @Override public EnumBlockRenderType getRenderType(IBlockState s){return cog()||kind==Kind.BELT?EnumBlockRenderType.INVISIBLE:EnumBlockRenderType.MODEL;}
    @Override public boolean isOpaqueCube(IBlockState s){return kind!=null&&!cog()&&kind!=Kind.CRANK&&kind!=Kind.BELT;}
    @Override public boolean isFullCube(IBlockState s){return isOpaqueCube(s);}
    @Override public AxisAlignedBB getBoundingBox(IBlockState s,IBlockAccess world,BlockPos pos){if(!cog())return FULL_BLOCK_AABB;double r=kind==Kind.LARGE_COG?.9375:.5;return switch(s.getValue(AXIS)){case X->new AxisAlignedBB(.375,.5-r,.5-r,.625,.5+r,.5+r);case Y->new AxisAlignedBB(.5-r,.375,.5-r,.5+r,.625,.5+r);case Z->new AxisAlignedBB(.5-r,.5-r,.375,.5+r,.5+r,.625);};}
    @Override public void breakBlock(World world,BlockPos pos,IBlockState state){if(kind==Kind.BELT&&world.getTileEntity(pos) instanceof KineticTile tile)BeltTransport.remove(world,tile,pos,state.getValue(AXIS));super.breakBlock(world,pos,state);}
    @Override public void addCollisionBoxToList(IBlockState state,World world,BlockPos pos,AxisAlignedBB query,java.util.List<AxisAlignedBB> boxes,net.minecraft.entity.Entity entity,boolean actual){
        if(kind!=Kind.BELT){super.addCollisionBoxToList(state,world,pos,query,boxes,entity,actual);return;}
        if(!(world.getTileEntity(pos) instanceof KineticTile tile)||tile.beltFirst==null)return;var step=BeltTransport.plan(tile);if(step.x()==0&&step.z()==0)return;
        for(int i=0;i<8;i++){double start=i/8d,end=(i+1)/8d,top=.8125+step.y()*(step.x()!=0?step.x():step.z())*(start-.5);AxisAlignedBB b=step.x()!=0?new AxisAlignedBB(start,Math.max(0,top-.25),.125,end,Math.max(.125,top),.875):new AxisAlignedBB(.125,Math.max(0,top-.25),start,.875,Math.max(.125,top),end);if(b.offset(pos).intersects(query))boxes.add(b.offset(pos));}
    }
    @Override public void neighborChanged(IBlockState s,World world,BlockPos p,net.minecraft.block.Block block,BlockPos from){if(world.isRemote)return;boolean powered=world.isBlockPowered(p);if(s.getValue(POWERED)!=powered)world.setBlockState(p,s.withProperty(POWERED,powered),2);KineticTopology.invalidate(world);KineticTile.refreshAround(world,p);world.markBlockRangeForRenderUpdate(p.add(-1,-1,-1),p.add(1,1,1));}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing side,float x,float y,float z){if(kind!=Kind.CRANK||dev.createport.equipment.WrenchItem.isHolding(player))return false;if(!world.isRemote&&world.getTileEntity(pos) instanceof KineticTile tile){tile.crankTicks=10;tile.generatedRpm=player.isSneaking()?-32:32;KineticTopology.invalidate(world);tile.recompute();if(!player.capabilities.isCreativeMode)player.addExhaustion(.01f);}return true;}
}
