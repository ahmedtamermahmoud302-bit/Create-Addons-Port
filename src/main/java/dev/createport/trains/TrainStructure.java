package dev.createport.trains;

import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.*;

/** Deliberately limited to ordinary solid block states; tile entities are not captured. */
public final class TrainStructure {
    public record Part(int x,int y,int z,IBlockState state) {}
    public static NBTTagCompound write(List<Part> parts) {
        NBTTagCompound data=new NBTTagCompound();NBTTagList list=new NBTTagList();
        for(Part part:parts) {
            NBTTagCompound n=new NBTTagCompound();n.setInteger("X",part.x());n.setInteger("Y",part.y());n.setInteger("Z",part.z());
            n.setString("Block",part.state().getBlock().getRegistryName().toString());
            n.setInteger("Meta",part.state().getBlock().getMetaFromState(part.state()));list.appendTag(n);
        }
        data.setTag("Blocks",list);return data;
    }
    public static List<Part> read(NBTTagCompound data) {
        List<Part> result=new ArrayList<>();NBTTagList list=data.getTagList("Blocks",10);
        for(int i=0;i<Math.min(256,list.tagCount());i++) {
            NBTTagCompound n=list.getCompoundTagAt(i);int x=n.getInteger("X"),y=n.getInteger("Y"),z=n.getInteger("Z");
            Block block=Block.getBlockFromName(n.getString("Block"));
            if(block==null || block==Blocks.AIR || Math.abs(x)>8 || y<0 || y>12 || Math.abs(z)>24)continue;
            IBlockState state=block.getStateFromMeta(Math.floorMod(n.getInteger("Meta"),16));
            if(state.getMaterial().isSolid() && (!block.hasTileEntity(state) || block==dev.createport.Create.SMALL_BOGEY))result.add(new Part(x,y,z,state));
        }
        return List.copyOf(result);
    }
    private TrainStructure() {}
}
