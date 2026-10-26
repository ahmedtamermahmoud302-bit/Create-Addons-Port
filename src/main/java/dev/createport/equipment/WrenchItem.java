package dev.createport.equipment;

import dev.createport.Create;
import dev.createport.kinetics.*;
import dev.createport.trains.TrainTrackBlock;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

public final class WrenchItem extends Item {
    public WrenchItem() { setRegistryName(Create.ID,"wrench");setTranslationKey("create.wrench");setMaxStackSize(1);setCreativeTab(CreativeTabs.TOOLS); }
    public static boolean isHolding(EntityPlayer player) {
        return player.getHeldItemMainhand().getItem()==Create.WRENCH || player.getHeldItemOffhand().getItem()==Create.WRENCH;
    }
    private static boolean pickupAllowed(Block block) {
        if(block instanceof dev.createport.trains.SeatBlock || block instanceof KineticBlock || block instanceof TrainTrackBlock || block instanceof dev.createport.trains.RailwayBlock)return true;
        // The 1.12 equivalents of available vanilla entries in Create 6.0.8's wrench_pickup tag.
        return Set.of(Blocks.REDSTONE_WIRE,Blocks.REDSTONE_TORCH,Blocks.UNLIT_REDSTONE_TORCH,
                Blocks.POWERED_REPEATER,Blocks.UNPOWERED_REPEATER,Blocks.LEVER,Blocks.REDSTONE_LAMP,Blocks.LIT_REDSTONE_LAMP,
                Blocks.POWERED_COMPARATOR,Blocks.UNPOWERED_COMPARATOR,Blocks.OBSERVER,Blocks.PISTON,Blocks.STICKY_PISTON,
                Blocks.TRIPWIRE,Blocks.TRIPWIRE_HOOK,Blocks.DAYLIGHT_DETECTOR,Blocks.DAYLIGHT_DETECTOR_INVERTED,
                Blocks.HOPPER,Blocks.STONE_BUTTON,Blocks.WOODEN_BUTTON,Blocks.STONE_PRESSURE_PLATE,Blocks.WOODEN_PRESSURE_PLATE,
                Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE,Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE,Blocks.RAIL,Blocks.GOLDEN_RAIL,
                Blocks.DETECTOR_RAIL,Blocks.ACTIVATOR_RAIL).contains(block);
    }
    private static EnumFacing turn(EnumFacing direction,EnumFacing.Axis axis) {
        var rotated=WrenchRotation.clockwise(new WrenchRotation.Direction(direction.getXOffset(),direction.getYOffset(),direction.getZOffset()),WrenchRotation.Axis.valueOf(axis.name()));
        return EnumFacing.getFacingFromVector(rotated.x(),rotated.y(),rotated.z());
    }
    public static IBlockState rotated(IBlockState state,EnumFacing face) {
        if(state.getBlock() instanceof dev.createport.trains.RailwayBlock railway){return face.getAxis()==EnumFacing.Axis.Y && railway.kind!=dev.createport.trains.RailwayBlock.Kind.CASING ? state.withProperty(dev.createport.trains.RailwayBlock.FACING,state.getValue(dev.createport.trains.RailwayBlock.FACING).rotateY()):state;}
        if(!(state.getBlock() instanceof KineticBlock block))return state;
        if(block.motor) {
            EnumFacing facing=state.getValue(KineticBlock.FACING);
            if(facing.getAxis()==face.getAxis())return state;
            EnumFacing next=turn(facing,face.getAxis());
            return state.withProperty(KineticBlock.FACING,next).withProperty(KineticBlock.AXIS,next.getAxis());
        }
        EnumFacing.Axis axis=state.getValue(KineticBlock.AXIS);
        EnumFacing positive=EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE,axis);
        return state.withProperty(KineticBlock.AXIS,turn(positive,face.getAxis()).getAxis());
    }
    @Override public EnumActionResult onItemUse(EntityPlayer player,World world,BlockPos pos,EnumHand hand,EnumFacing face,float x,float y,float z) {
        if(player.isSpectator() || !player.capabilities.allowEdit || !world.isBlockLoaded(pos)
                || !player.canPlayerEdit(pos,face,player.getHeldItem(hand)) || !world.isBlockModifiable(player,pos))return EnumActionResult.FAIL;
        IBlockState original=world.getBlockState(pos);Block block=original.getBlock();
        if(player.isSneaking()) {
            if(!pickupAllowed(block))return EnumActionResult.PASS;
            if(world.isRemote)return EnumActionResult.SUCCESS;
            BlockEvent.BreakEvent event=new BlockEvent.BreakEvent(world,pos,original,player);
            if(MinecraftForge.EVENT_BUS.post(event) || !world.getBlockState(pos).equals(original))return EnumActionResult.FAIL;
            NonNullList<ItemStack> drops=NonNullList.create();
            if(!player.capabilities.isCreativeMode) {
                block.getDrops(drops,world,pos,original,0);
                if(block instanceof KineticBlock kinetic && kinetic.motor && world.getTileEntity(pos) instanceof KineticTile tile) {
                    for(ItemStack drop:drops) if(drop.getItem()==Item.getItemFromBlock(block)) {
                        NBTTagCompound settings=new NBTTagCompound();settings.setInteger("GeneratedRPM",tile.generatedRpm);
                        NBTTagCompound tag=drop.getTagCompound();if(tag==null){tag=new NBTTagCompound();drop.setTagCompound(tag);}
                        tag.setTag("BlockEntityTag",settings);
                    }
                }
            }
            if(!world.setBlockToAir(pos))return EnumActionResult.FAIL;
            for(ItemStack drop:drops) if(!player.inventory.addItemStackToInventory(drop) && !drop.isEmpty())player.dropItem(drop,false);
            player.inventory.markDirty();KineticTile.refreshAround(world,pos);
            world.playSound(null,pos,SoundEvents.ENTITY_ITEM_PICKUP,SoundCategory.BLOCKS,.7f,1);
            return EnumActionResult.SUCCESS;
        }
        if(!(block instanceof KineticBlock) && !(block instanceof TrainTrackBlock) && !(block instanceof dev.createport.trains.RailwayBlock))return EnumActionResult.PASS;
        if(world.isRemote)return EnumActionResult.SUCCESS;
        Integer savedSpeed=world.getTileEntity(pos) instanceof KineticTile tile && tile.motor()?tile.generatedRpm:null;
        IBlockState next=rotated(original,face);
        if(!next.equals(original)) {
            if(!world.setBlockState(pos,next,3))return EnumActionResult.FAIL;
            if(savedSpeed!=null && world.getTileEntity(pos) instanceof KineticTile tile)tile.setGeneratedRpm(savedSpeed);
            KineticTile.refreshAround(world,pos);
            world.playSound(null,pos,SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE,SoundCategory.BLOCKS,.4f,1.4f);
        }
        return EnumActionResult.SUCCESS;
    }
}
