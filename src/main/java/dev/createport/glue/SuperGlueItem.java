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
    @Override public void onUpdate(ItemStack stack,World world,net.minecraft.entity.Entity entity,int slot,boolean selected){if(!world.isRemote&&entity instanceof EntityPlayer&&!selected&&stack.hasTagCompound())stack.getTagCompound().removeTag("FirstBlock");}
    @Override public ActionResult<ItemStack> onItemRightClick(World world,EntityPlayer player,EnumHand hand){var stack=player.getHeldItem(hand);if(player.isSneaking()&&stack.hasTagCompound()&&stack.getTagCompound().hasKey("FirstBlock")){stack.getTagCompound().removeTag("FirstBlock");return new ActionResult<>(EnumActionResult.SUCCESS,stack);}return super.onItemRightClick(world,player,hand);}
    private void tell(EntityPlayer player,String text) { player.sendStatusMessage(new TextComponentString(text),true); }
    @Override public EnumActionResult onItemUse(EntityPlayer player,World world,BlockPos pos,EnumHand hand,EnumFacing face,float x,float y,float z) {
        if(world.isRemote)return EnumActionResult.SUCCESS;
        ItemStack stack=player.getHeldItem(hand);
        if(!player.canPlayerEdit(pos,face,stack) || !world.isBlockModifiable(player,pos))return EnumActionResult.FAIL;
        NBTTagCompound data=stack.getTagCompound();if(data==null){ data=new NBTTagCompound();stack.setTagCompound(data); }
        if(player.isSneaking()) {
            data.removeTag("FirstBlock");tell(player,"Glue selection discarded.");return EnumActionResult.SUCCESS;
        }
        if(!data.hasKey("FirstBlock")) {
            data.setLong("FirstBlock",pos.toLong());data.setInteger("GlueDimension",world.provider.getDimension());
            tell(player,"First corner selected. Select the opposite corner of the glued area.");return EnumActionResult.SUCCESS;
        }
        BlockPos first=BlockPos.fromLong(data.getLong("FirstBlock"));
        if(data.getInteger("GlueDimension")!=world.provider.getDimension() || !world.isBlockLoaded(first) || world.isAirBlock(first)) {
            data.removeTag("FirstBlock");tell(player,"First block is unavailable. Select it again.");return EnumActionResult.FAIL;
        }
        if(first.equals(pos)) { data.removeTag("FirstBlock");tell(player,"Glue selection cleared.");return EnumActionResult.SUCCESS; }
        java.util.Set<GlueGraph.Pos> selected;
        try{selected=GlueSelection.collect(GlueWorldData.pos(first),GlueWorldData.pos(pos),p->{var target=GlueWorldData.block(p);return world.isBlockLoaded(target)&&!world.isAirBlock(target)&&world.getBlockState(target).getMaterial().isSolid();});}
        catch(IllegalArgumentException invalid){tell(player,invalid.getMessage());return EnumActionResult.FAIL;}
        if(selected.size()<2||!selected.contains(GlueWorldData.pos(pos))){tell(player,"The selected corners must be connected by blocks inside this area.");return EnumActionResult.FAIL;}
        for(var p:selected){var target=GlueWorldData.block(p);if(!world.isBlockModifiable(player,target)||!player.canPlayerEdit(target,face,stack))return EnumActionResult.FAIL;}
        boolean added=GlueWorldData.get(world).addArea(first,pos);data.removeTag("FirstBlock");
        if(added&&!player.capabilities.isCreativeMode)stack.damageItem(1,player);
        ((WorldServer)world).spawnParticle(EnumParticleTypes.SLIME,(first.getX()+pos.getX()+1)/2d,(first.getY()+pos.getY()+1)/2d,(first.getZ()+pos.getZ()+1)/2d,8,.1,.1,.1,0);
        tell(player,added ? "Area glued together.":"These blocks are already glued.");return EnumActionResult.SUCCESS;
    }
}
