package dev.createport.glue;

import dev.createport.Create;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.*;

public final class SuperGlueItem extends Item {
    public SuperGlueItem() {
        setRegistryName(Create.ID,"super_glue");setTranslationKey("create.super_glue");
        setCreativeTab(CreativeTabs.TOOLS);setMaxStackSize(1);setMaxDamage(99);
    }
    private void tell(EntityPlayer player,String text) { player.sendStatusMessage(new TextComponentString(text),true); }
    @Override public EnumActionResult onItemUse(EntityPlayer player,World world,BlockPos pos,EnumHand hand,EnumFacing face,float x,float y,float z) {
        if(world.isRemote)return EnumActionResult.SUCCESS;
        ItemStack stack=player.getHeldItem(hand);
        if(!player.canPlayerEdit(pos,face,stack) || !world.isBlockModifiable(player,pos))return EnumActionResult.FAIL;
        NBTTagCompound data=stack.getTagCompound();if(data==null){ data=new NBTTagCompound();stack.setTagCompound(data); }
        if(player.isSneaking()) {
            boolean removed=false;
            if(data.hasKey("FirstBlock") && data.getInteger("GlueDimension")==world.provider.getDimension()) {
                BlockPos first=BlockPos.fromLong(data.getLong("FirstBlock"));
                if(world.isBlockLoaded(first) && world.isBlockModifiable(player,first)
                        && player.canPlayerEdit(first,face,stack))removed=GlueWorldData.get(world).disconnect(first,pos);
            }
            data.removeTag("FirstBlock");
            tell(player,removed ? "Glue removed from this joint.":"Selection cleared. Select one block, then Sneak-use its neighbor to remove glue.");
            return EnumActionResult.SUCCESS;
        }
        if(!data.hasKey("FirstBlock")) {
            data.setLong("FirstBlock",pos.toLong());data.setInteger("GlueDimension",world.provider.getDimension());
            tell(player,"First block selected. Use glue on an adjacent block.");return EnumActionResult.SUCCESS;
        }
        BlockPos first=BlockPos.fromLong(data.getLong("FirstBlock"));
        if(data.getInteger("GlueDimension")!=world.provider.getDimension() || !world.isBlockLoaded(first) || world.isAirBlock(first)) {
            data.removeTag("FirstBlock");tell(player,"First block is unavailable. Select it again.");return EnumActionResult.FAIL;
        }
        if(first.equals(pos)) { data.removeTag("FirstBlock");tell(player,"Glue selection cleared.");return EnumActionResult.SUCCESS; }
        if(Math.abs(first.getX()-pos.getX())+Math.abs(first.getY()-pos.getY())+Math.abs(first.getZ()-pos.getZ())!=1) {
            tell(player,"Select a block directly next to the first block.");return EnumActionResult.FAIL;
        }
        if(!player.canPlayerEdit(first,face,stack) || !world.isBlockModifiable(player,first))return EnumActionResult.FAIL;
        boolean added=GlueWorldData.get(world).connect(first,pos);data.removeTag("FirstBlock");
        if(added && !player.capabilities.isCreativeMode)stack.damageItem(1,player);
        ((WorldServer)world).spawnParticle(EnumParticleTypes.SLIME,(first.getX()+pos.getX()+1)/2d,(first.getY()+pos.getY()+1)/2d,(first.getZ()+pos.getZ()+1)/2d,8,.1,.1,.1,0);
        tell(player,added ? "Blocks glued together.":"These blocks are already glued.");return EnumActionResult.SUCCESS;
    }
}
