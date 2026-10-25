package dev.createport.trains;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.*;
/** Uses the original opened/virtual console model while carried by a train. */
public final class ControlsBlock extends RailwayBlock {
    public static final PropertyBool VIRTUAL=PropertyBool.create("virtual");
    public ControlsBlock(){super("controls",Kind.CONTROLS);setDefaultState(getDefaultState().withProperty(VIRTUAL,false));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FACING,VIRTUAL);}
    @Override public IBlockState getStateFromMeta(int meta){return super.getStateFromMeta(meta&3).withProperty(VIRTUAL,(meta&4)!=0);}
    @Override public int getMetaFromState(IBlockState state){return super.getMetaFromState(state)|(state.getValue(VIRTUAL)?4:0);}
}
