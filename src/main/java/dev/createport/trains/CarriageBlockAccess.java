package dev.createport.trains;
import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
/** Neighbour and shape lookup uses the carried palette rather than unrelated world blocks. */
public final class CarriageBlockAccess implements IBlockAccess {
    private final Map<BlockPos,IBlockState> blocks=new HashMap<>();
    public CarriageBlockAccess(List<TrainStructure.Part> parts){for(var p:parts)blocks.put(new BlockPos(p.x(),p.y(),p.z()),p.state());}
    public TileEntity getTileEntity(BlockPos p){return null;}
    public int getCombinedLight(BlockPos p,int light){return 0xf000f0;}
    public IBlockState getBlockState(BlockPos p){return blocks.getOrDefault(p,Blocks.AIR.getDefaultState());}
    public boolean isAirBlock(BlockPos p){return getBlockState(p).getBlock()==Blocks.AIR;}
    public Biome getBiome(BlockPos p){return Biomes.PLAINS;}
    public int getStrongPower(BlockPos p,EnumFacing side){return 0;}
    public WorldType getWorldType(){return WorldType.DEFAULT;}
    public boolean isSideSolid(BlockPos p,EnumFacing side,boolean fallback){return getBlockState(p).isSideSolid(this,p,side);}
}
