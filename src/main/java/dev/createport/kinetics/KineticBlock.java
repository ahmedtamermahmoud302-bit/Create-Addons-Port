package dev.createport.kinetics;

import dev.createport.Create;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class KineticBlock extends Block {
    public static final PropertyEnum<EnumFacing.Axis> AXIS = PropertyEnum.create("axis", EnumFacing.Axis.class);
    public static final PropertyEnum<EnumFacing> FACING = PropertyEnum.create("facing", EnumFacing.class);
    public final boolean motor;
    public KineticBlock(String name, boolean motor) {
        super(Material.IRON); this.motor = motor;
        setRegistryName(Create.ID, name); setTranslationKey("create." + name);
        setCreativeTab(CreativeTabs.REDSTONE); setHardness(1.5f);
        setDefaultState(blockState.getBaseState().withProperty(AXIS, EnumFacing.Axis.Y).withProperty(FACING, EnumFacing.UP));
    }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, AXIS, FACING); }
    @Override public IBlockState getStateFromMeta(int meta) { return motor ? getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta)).withProperty(AXIS, EnumFacing.byIndex(meta).getAxis()) : getDefaultState().withProperty(AXIS, EnumFacing.Axis.values()[Math.floorMod(meta, 3)]); }
    @Override public int getMetaFromState(IBlockState state) { return motor ? state.getValue(FACING).getIndex() : state.getValue(AXIS).ordinal(); }
    @Override public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float x, float y, float z, int meta, EntityLivingBase placer) {
        return getDefaultState().withProperty(AXIS, facing.getAxis()).withProperty(FACING, motor ? facing : EnumFacing.UP);
    }
    @Override public boolean hasTileEntity(IBlockState state) { return true; }
    @Override public TileEntity createTileEntity(World world, IBlockState state) { return new KineticTile(); }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return motor; }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return motor ? EnumBlockRenderType.MODEL : EnumBlockRenderType.INVISIBLE; }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (motor) return FULL_BLOCK_AABB;
        return switch (state.getValue(AXIS)) {
            case X -> new AxisAlignedBB(0, .375, .375, 1, .625, .625);
            case Y -> new AxisAlignedBB(.375, 0, .375, .625, 1, .625);
            case Z -> new AxisAlignedBB(.375, .375, 0, .625, .625, 1);
        };
    }
}
