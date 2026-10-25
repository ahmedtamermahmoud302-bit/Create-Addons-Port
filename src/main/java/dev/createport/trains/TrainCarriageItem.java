package dev.createport.trains;

import dev.createport.Create;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

public final class TrainCarriageItem extends Item {
    public TrainCarriageItem() {
        setRegistryName(Create.ID,"train_carriage");setTranslationKey("create.train_carriage");
        setCreativeTab(CreativeTabs.TRANSPORTATION);setMaxStackSize(1);
    }
    @Override public EnumActionResult onItemUse(EntityPlayer player,World world,BlockPos pos,EnumHand hand,EnumFacing face,float x,float y,float z) {
        ItemStack stack=player.getHeldItem(hand);
        if (!(world.getBlockState(pos).getBlock() instanceof TrainTrackBlock)
                || !player.canPlayerEdit(pos,face,stack)) return EnumActionResult.FAIL;
        if (world.isRemote) return EnumActionResult.SUCCESS;
        RailPath.Shape shape=RailPath.Shape.valueOf(world.getBlockState(pos).getValue(TrainTrackBlock.SHAPE).name());
        RailPath.Point tangent=RailPath.of(shape).tangent(.5);Vec3d look=player.getLookVec();
        TrainEntity train=new TrainEntity(world);train.place(pos,look.x*tangent.x()+look.z*tangent.z()<0 ? -1:1);
        boolean glued=dev.createport.glue.GlueWorldData.get(world).graph().connected(dev.createport.glue.GlueWorldData.pos(pos.up()));
        if (glued) {
            if (!TrainAssembly.assemble(world,pos,player,train))return EnumActionResult.FAIL;
            if (!player.capabilities.isCreativeMode)stack.shrink(1);
            return EnumActionResult.SUCCESS;
        }
        if (!world.getCollisionBoxes(train,train.getEntityBoundingBox()).isEmpty()
                || !world.getEntitiesWithinAABB(TrainEntity.class,train.getEntityBoundingBox()).isEmpty()) return EnumActionResult.FAIL;
        if (!world.spawnEntity(train)) return EnumActionResult.FAIL;
        if (!player.capabilities.isCreativeMode) stack.shrink(1);
        return EnumActionResult.SUCCESS;
    }
}
