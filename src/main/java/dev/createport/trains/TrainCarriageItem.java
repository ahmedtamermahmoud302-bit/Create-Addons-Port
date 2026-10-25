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
        setMaxStackSize(1);
    }
    @Override public EnumActionResult onItemUse(EntityPlayer player,World world,BlockPos pos,EnumHand hand,EnumFacing face,float x,float y,float z) {
        if(!world.isRemote)player.sendStatusMessage(new net.minecraft.util.text.TextComponentString("Build bogeys and a glued carriage at a Train Station, then assemble it there."),true);
        return EnumActionResult.FAIL;
    }
}
