package dev.createport.trains;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
/** Create's standard style has two sizes; add-on styles are separate integrations. */
public final class Bogeys {
    public static boolean is(Block block){return block instanceof RailwayBlock railway&&railway.kind==RailwayBlock.Kind.BOGEY;}
    public static boolean is(IBlockState state){return is(state.getBlock());}
    public static boolean large(IBlockState state){return state.getBlock()==dev.createport.Create.LARGE_BOGEY;}
    public static double radius(IBlockState state){return (large(state)?12.5:6.5)/16d;}
    private Bogeys(){}
}
