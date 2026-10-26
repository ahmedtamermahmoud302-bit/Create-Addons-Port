package dev.createport.trains;
import java.util.*;
import java.util.function.Function;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.*;
public final class TrainCasingBlock extends RailwayBlock {
    public record Faces(List<Integer> tiles){}
    public static final IUnlistedProperty<Faces> CONNECTIONS=new IUnlistedProperty<>(){
        public String getName(){return "connections";}public boolean isValid(Faces value){return value!=null&&value.tiles.size()==6;}
        public Class<Faces> getType(){return Faces.class;}public String valueToString(Faces value){return value.toString();}
    };
    public TrainCasingBlock(){super("railway_casing",Kind.CASING);}
    @Override protected BlockStateContainer createBlockState(){return new ExtendedBlockState(this,new IProperty[]{FACING},new IUnlistedProperty[]{CONNECTIONS});}
    @Override public IBlockState getExtendedState(IBlockState state,IBlockAccess world,BlockPos pos){return withConnections(state,pos,world::getBlockState);}
    public static IBlockState withConnections(IBlockState state,BlockPos pos,Function<BlockPos,IBlockState> lookup){
        if(!(state instanceof IExtendedBlockState extended))return state;
        List<Integer> tiles=new ArrayList<>();
        for(EnumFacing face:EnumFacing.values()){
            EnumFacing h=face.getAxis()==EnumFacing.Axis.X?EnumFacing.SOUTH:EnumFacing.WEST,v=face.getAxis()==EnumFacing.Axis.Y?EnumFacing.NORTH:EnumFacing.UP;
            if(face.getAxisDirection()==EnumFacing.AxisDirection.POSITIVE)h=h.getOpposite();
            if(face==EnumFacing.DOWN){v=v.getOpposite();h=h.getOpposite();}
            int mask=0;int[][] offsets={{0,1},{0,-1},{-1,0},{1,0},{-1,1},{1,1},{-1,-1},{1,-1}};
            for(int i=0;i<8;i++){
                BlockPos other=pos.offset(h,offsets[i][0]).offset(v,offsets[i][1]);
                if(lookup.apply(other).getBlock()==state.getBlock() && lookup.apply(other.offset(face)).getBlock()!=state.getBlock())mask|=1<<i;
            }tiles.add(CasingCT.index(mask));
        }return extended.withProperty(CONNECTIONS,new Faces(List.copyOf(tiles)));
    }
}
