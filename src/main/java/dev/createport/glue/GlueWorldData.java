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
    public record Area(java.util.UUID id,BlockPos first,BlockPos second){public net.minecraft.util.math.AxisAlignedBB bounds(){return new net.minecraft.util.math.AxisAlignedBB(first,second).expand(1,1,1);}public boolean contains(BlockPos p){return bounds().contains(new net.minecraft.util.math.Vec3d(p).add(.5,.5,.5));}}
    private final java.util.Map<java.util.UUID,Area> areas=new java.util.LinkedHashMap<>();
    public java.util.Collection<Area> areas(){return java.util.List.copyOf(areas.values());}
    public boolean addArea(BlockPos first,BlockPos second){for(var a:areas.values())if(a.bounds().equals(new Area(null,first,second).bounds()))return false;var id=java.util.UUID.randomUUID();areas.put(id,new Area(id,first,second));markDirty();return true;}
    public Area area(java.util.UUID id){return areas.get(id);}
    public boolean removeArea(java.util.UUID id){boolean changed=areas.remove(id)!=null;if(changed)markDirty();return changed;}
    public java.util.Set<GlueGraph.Pos> neighbours(World world,BlockPos p){
        var result=new java.util.HashSet<>(graph.neighbours(pos(p)));
        for(var area:areas.values())if(area.contains(p))for(var face:net.minecraft.util.EnumFacing.values()){BlockPos n=p.offset(face);if(area.contains(n)&&!world.isAirBlock(n)&&world.getBlockState(n).getMaterial().isSolid())result.add(pos(n));}return result;
    }
    public void captured(java.util.Set<BlockPos> selected){for(var area:java.util.List.copyOf(areas.values()))if(selected.contains(area.first())&&selected.contains(area.second()))areas.remove(area.id());for(var p:selected)graph.remove(pos(p));markDirty();}

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
        areas.clear();var volumes=nbt.getTagList("Areas",10);for(int i=0;i<volumes.tagCount();i++){var a=volumes.getCompoundTagAt(i);var id=a.getUniqueId("Id");areas.put(id,new Area(id,BlockPos.fromLong(a.getLong("A")),BlockPos.fromLong(a.getLong("B"))));}
        graph=new GlueGraph();NBTTagList bonds=nbt.getTagList("Bonds",10);
        for(int i=0;i<bonds.tagCount();i++) {
            NBTTagCompound bond=bonds.getCompoundTagAt(i);
            try { graph.connect(pos(BlockPos.fromLong(bond.getLong("A"))),pos(BlockPos.fromLong(bond.getLong("B")))); }
            catch(IllegalArgumentException ignored) { }
        }
    }
    @Override public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        var volumes=new NBTTagList();for(var a:areas.values()){var n=new NBTTagCompound();n.setUniqueId("Id",a.id());n.setLong("A",a.first().toLong());n.setLong("B",a.second().toLong());volumes.appendTag(n);}nbt.setTag("Areas",volumes);
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
