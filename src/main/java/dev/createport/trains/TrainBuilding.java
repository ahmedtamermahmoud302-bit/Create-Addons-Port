package dev.createport.trains;
import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
/** Palette edits remain bounded, server-owned and restorable at the station. */
public final class TrainBuilding {
    public static EnumActionResult place(TrainEntity train,EntityPlayer player,TrainStructure.Part against,EnumFacing face,Vec3d hit){
        var stack=player.getHeldItemMainhand();if(!(stack.getItem() instanceof ItemBlock item)||player.isSpectator()||!player.capabilities.allowEdit)return EnumActionResult.PASS;
        var local=new BlockPos(against.x(),against.y(),against.z()).offset(face);var world=train.world;
        if(Math.abs(local.getX())>8||Math.abs(local.getZ())>24||local.getY()<0||local.getY()>12||train.structure().size()>=256)return EnumActionResult.FAIL;
        for(var p:train.structure())if(p.x()==local.getX()&&p.y()==local.getY()&&p.z()==local.getZ())return EnumActionResult.FAIL;
        BlockPos target=new BlockPos(train.toWorld(new Vec3d(local.getX(),.85+local.getY(),local.getZ())));
        if(!world.isBlockLoaded(target)||!world.isBlockModifiable(player,target)||!player.canPlayerEdit(target,face,stack)||!world.getBlockState(target).getBlock().isReplaceable(world,target))return EnumActionResult.FAIL;
        var block=item.getBlock();var state=block.getStateForPlacement(world,target,face,(float)(hit.x-against.x()+.5),(float)(hit.y-.85-against.y()),(float)(hit.z-against.z()+.5),item.getMetadata(stack.getMetadata()),player,EnumHand.MAIN_HAND);
        state=state.withRotation(Rotation.values()[Math.floorMod(Math.round(-train.rotationYaw/90f),4)]);
        if(!state.getMaterial().isSolid()||block.hasTileEntity(state)||block.getBlockHardness(state,world,target)<0)return EnumActionResult.FAIL;
        var test=new ArrayList<>(train.structure());test.add(new TrainStructure.Part(local.getX(),local.getY(),local.getZ(),state));var access=new CarriageBlockAccess(test);var box=state.getCollisionBoundingBox(access,local);
        if(box!=null){var localBox=box.offset(local.getX()-.5,.85+local.getY(),local.getZ()-.5);var bounds=TrainCollisions.box(PlatformGeometry.bounds(TrainCollisions.box(localBox),train.posX,train.posY,train.posZ,train.rotationYaw));
            for(var e:world.getEntitiesWithinAABB(Entity.class,bounds))if(e!=train&&!e.isDead&&!e.noClip&&e.canBeCollidedWith()&&PlatformGeometry.clip(TrainCollisions.box(localBox),train.posX,train.posY,train.posZ,train.rotationYaw,TrainCollisions.box(e.getEntityBoundingBox()))!=null)return EnumActionResult.FAIL;
            if(!world.getCollisionBoxes(train,bounds.shrink(.01)).isEmpty())return EnumActionResult.FAIL;
        }
        if(world.isRemote)return EnumActionResult.SUCCESS;
        train.setStructure(test);train.linkCarriedBlocks(new BlockPos(against.x(),against.y(),against.z()),local);
        if(!player.capabilities.isCreativeMode)stack.shrink(1);player.inventory.markDirty();var sound=block.getSoundType(state,world,target,player);world.playSound(null,target,sound.getPlaceSound(),SoundCategory.BLOCKS,(sound.getVolume()+1)/2,sound.getPitch()*.8f);return EnumActionResult.SUCCESS;
    }
    private TrainBuilding(){}
}
