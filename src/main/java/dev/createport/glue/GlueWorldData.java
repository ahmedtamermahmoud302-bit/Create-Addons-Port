package dev.createport.glue;

import dev.createport.Create;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.*;

@Mod.EventBusSubscriber(modid=Create.ID)
public final class GlueWorldData extends WorldSavedData {
    private GlueGraph graph=new GlueGraph();
    public GlueWorldData() { super("create_glue"); }
    public GlueWorldData(String name) { super(name); }
    public GlueGraph graph() { return graph; }
    public static GlueGraph.Pos pos(BlockPos p) { return new GlueGraph.Pos(p.getX(),p.getY(),p.getZ()); }
    public static BlockPos block(GlueGraph.Pos p) { return new BlockPos(p.x(),p.y(),p.z()); }
    public static GlueWorldData get(World world) {
        var storage=world.getPerWorldStorage();
        GlueWorldData data=(GlueWorldData)storage.getOrLoadData(GlueWorldData.class,"create_glue");
        if (data==null) { data=new GlueWorldData();storage.setData("create_glue",data); }
        return data;
    }
    public boolean connect(BlockPos a,BlockPos b) { boolean changed=graph.connect(pos(a),pos(b));if(changed)markDirty();return changed; }
    public boolean disconnect(BlockPos a,BlockPos b) { boolean changed=graph.disconnect(pos(a),pos(b));if(changed)markDirty();return changed; }
    public void remove(BlockPos pos) { if(graph.remove(pos(pos)))markDirty(); }
    @Override public void readFromNBT(NBTTagCompound nbt) {
        graph=new GlueGraph();NBTTagList bonds=nbt.getTagList("Bonds",10);
        for(int i=0;i<bonds.tagCount();i++) {
            NBTTagCompound bond=bonds.getCompoundTagAt(i);
            try { graph.connect(pos(BlockPos.fromLong(bond.getLong("A"))),pos(BlockPos.fromLong(bond.getLong("B")))); }
            catch(IllegalArgumentException ignored) { }
        }
    }
    @Override public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList bonds=new NBTTagList();
        for(GlueGraph.Bond bond:graph.bonds()) {
            NBTTagCompound tag=new NBTTagCompound();tag.setLong("A",block(bond.a()).toLong());tag.setLong("B",block(bond.b()).toLong());bonds.appendTag(tag);
        }
        nbt.setTag("Bonds",bonds);return nbt;
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void broken(BlockEvent.BreakEvent event) {
        if(!event.getWorld().isRemote && !event.isCanceled())get(event.getWorld()).remove(event.getPos());
    }
}
