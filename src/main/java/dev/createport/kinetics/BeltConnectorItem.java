package dev.createport.kinetics;
import dev.createport.Create;
import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
public final class BeltConnectorItem extends Item {
    public BeltConnectorItem(){setRegistryName(Create.ID,"belt_connector");setTranslationKey("create.belt_connector");setCreativeTab(CreativeTabs.REDSTONE);}
    @Override public boolean hasEffect(ItemStack s){return s.hasTagCompound()&&s.getTagCompound().hasKey("Pulley");}
    @Override public EnumActionResult onItemUse(EntityPlayer player,World world,BlockPos pos,EnumHand hand,EnumFacing face,float x,float y,float z){
        var stack=player.getHeldItem(hand);if(player.isSneaking()){if(stack.hasTagCompound())stack.getTagCompound().removeTag("Pulley");return EnumActionResult.SUCCESS;}if(world.isRemote)return EnumActionResult.SUCCESS;
        if(world.getBlockState(pos).getBlock()!=Create.SHAFT)return EnumActionResult.FAIL;var tag=stack.getTagCompound();if(tag==null){tag=new NBTTagCompound();stack.setTagCompound(tag);}
        if(!tag.hasKey("Pulley")){tag.setLong("Pulley",pos.toLong());tag.setInteger("Dimension",player.dimension);player.sendStatusMessage(new TextComponentString("First pulley selected. Use another parallel shaft to connect the belt."),true);return EnumActionResult.SUCCESS;}
        BlockPos first=BlockPos.fromLong(tag.getLong("Pulley"));if(player.dimension!=tag.getInteger("Dimension")||!world.isBlockLoaded(first)||world.getBlockState(first).getBlock()!=Create.SHAFT){tag.removeTag("Pulley");return EnumActionResult.FAIL;}
        var axis=world.getBlockState(first).getValue(KineticBlock.AXIS);if(world.getBlockState(pos).getValue(KineticBlock.AXIS)!=axis)return EnumActionResult.FAIL;
        try{var delta=pos.subtract(first);var plan=BeltGeometry.connect(axis.ordinal(),delta.getX(),delta.getY(),delta.getZ());Map<BlockPos,IBlockState> before=new LinkedHashMap<>();
            for(int i=0;i<=plan.length();i++){BlockPos p=first.add(plan.x()*i,plan.y()*i,plan.z()*i);if(!world.isBlockLoaded(p)||!world.isBlockModifiable(player,p)||!player.canPlayerEdit(p,face,stack)||i>0&&i<plan.length()&&!world.getBlockState(p).getBlock().isReplaceable(world,p))throw new IllegalArgumentException("Belt path is obstructed or protected");before.put(p,world.getBlockState(p));}
            for(var e:before.entrySet())if(!world.setBlockState(e.getKey(),Create.BELT.getDefaultState().withProperty(KineticBlock.AXIS,axis),2)){for(var old:before.entrySet())world.setBlockState(old.getKey(),old.getValue(),2);return EnumActionResult.FAIL;}
            int index=0;for(var p:before.keySet()){var tile=(KineticTile)world.getTileEntity(p);tile.beltFirst=first;tile.beltLast=pos;tile.beltIndex=index++;tile.beltLength=plan.length();tile.beltPaid=!player.capabilities.isCreativeMode;tile.markDirty();tile.sync();}
            tag.removeTag("Pulley");if(!player.capabilities.isCreativeMode)stack.shrink(1);KineticTopology.invalidate(world);KineticTile.refreshAround(world,first);KineticTile.refreshAround(world,pos);world.playSound(null,pos,net.minecraft.init.SoundEvents.BLOCK_CLOTH_PLACE,SoundCategory.BLOCKS,1,1);return EnumActionResult.SUCCESS;
        }catch(IllegalArgumentException invalid){player.sendStatusMessage(new TextComponentString(invalid.getMessage()),true);return EnumActionResult.FAIL;}
    }
}
