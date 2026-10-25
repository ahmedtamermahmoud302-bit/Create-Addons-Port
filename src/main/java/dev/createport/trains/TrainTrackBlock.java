package dev.createport.trains;

import dev.createport.Create;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.BlockRenderLayer;

public final class TrainTrackBlock extends BlockRailBase {
    public static final PropertyEnum<EnumRailDirection> SHAPE=PropertyEnum.create("shape",EnumRailDirection.class);
    public TrainTrackBlock() {
        super(false);setRegistryName(Create.ID,"track");setTranslationKey("create.track");
        setCreativeTab(CreativeTabs.TRANSPORTATION);setHardness(.7f);
        setDefaultState(blockState.getBaseState().withProperty(SHAPE,EnumRailDirection.NORTH_SOUTH));
    }
    @Override public IProperty<EnumRailDirection> getShapeProperty() { return SHAPE; }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this,SHAPE); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(SHAPE,EnumRailDirection.byMetadata(Math.floorMod(meta,10))); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(SHAPE).getMetadata(); }
    @Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT; }
}
