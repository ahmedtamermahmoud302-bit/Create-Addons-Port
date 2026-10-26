package dev.createport.kinetics;
import dev.createport.Create;
import net.minecraft.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
/** Separate placement item; avoids replacing the gearbox block's ordinary ItemBlock mapping. */
public final class VerticalGearboxItem extends Item {
    public VerticalGearboxItem(){setRegistryName(Create.ID,"vertical_gearbox");setTranslationKey("create.vertical_gearbox");setCreativeTab(net.minecraft.creativetab.CreativeTabs.REDSTONE);}
    @Override public EnumActionResult onItemUse(EntityPlayer player,World world,BlockPos pos,EnumHand hand,EnumFacing face,float x,float y,float z){return Item.getItemFromBlock(Create.GEARBOX).onItemUse(player,world,pos,hand,face,x,y,z);}
}
