package dev.createport.trains;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.storage.WorldSavedData;
public final class CurveGraphData extends WorldSavedData {
    private final Map<UUID,NBTTagCompound> saved=new LinkedHashMap<>();
    private final Map<UUID,CurveTrackEntity> cache=new HashMap<>();
    public CurveGraphData(){super("create_tracks");}public CurveGraphData(String name){super(name);}
    public static CurveGraphData get(World world){var storage=world.getPerWorldStorage();var data=(CurveGraphData)storage.getOrLoadData(CurveGraphData.class,"create_tracks");if(data==null){data=new CurveGraphData();storage.setData("create_tracks",data);}return data;}
    public CurveTrackEntity node(World world,UUID id){if(!saved.containsKey(id))return null;return cache.computeIfAbsent(id,key->{var e=new CurveTrackEntity(world);e.loadSpec(saved.get(key));e.setUniqueId(key);return e;});}
    public CurveTrackEntity at(World world,TrackCurve.V point){for(UUID id:saved.keySet()){var e=node(world,id);if(e.curve().a.sub(point).length()<1e-5 || e.curve().b.sub(point).length()<1e-5)return e;}return null;}
    public void add(World world,CurveTrackEntity e){saved.put(e.getUniqueID(),e.spec());cache.put(e.getUniqueID(),e);markDirty();update(world,e.anchor(true));update(world,e.anchor(false));}
    public NBTTagList connections(BlockPos pos){NBTTagList list=new NBTTagList();for(var entry:saved.entrySet()){var n=entry.getValue();if(n.getLong("First")==pos.toLong() || n.getLong("Second")==pos.toLong()){var copy=n.copy();copy.setUniqueId("Id",entry.getKey());list.appendTag(copy);}}return list;}
    public void removeAt(World world,BlockPos pos){
        List<UUID> remove=new ArrayList<>();for(var entry:saved.entrySet())if(entry.getValue().getLong("First")==pos.toLong() || entry.getValue().getLong("Second")==pos.toLong())remove.add(entry.getKey());
        for(UUID id:remove){var e=node(world,id);saved.remove(id);cache.remove(id);if(e.spec().getBoolean("Paid") && world.getGameRules().getBoolean("doTileDrops")){int count=(e.curve().segments()+1)/2;while(count>0){int amount=Math.min(64,count);var item=new net.minecraft.entity.item.EntityItem(world,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,new net.minecraft.item.ItemStack(dev.createport.Create.TRACK,amount));world.spawnEntity(item);count-=amount;}}update(world,e.anchor(true));update(world,e.anchor(false));}if(!remove.isEmpty())markDirty();
    }
    private static void update(World world,BlockPos pos){if(world.isBlockLoaded(pos)){var state=world.getBlockState(pos);world.notifyBlockUpdate(pos,state,state,2);}}
    @Override public NBTTagCompound writeToNBT(NBTTagCompound n){NBTTagList list=new NBTTagList();for(var entry:saved.entrySet()){var copy=entry.getValue().copy();copy.setUniqueId("Id",entry.getKey());list.appendTag(copy);}n.setTag("Connections",list);return n;}
    @Override public void readFromNBT(NBTTagCompound n){saved.clear();cache.clear();var list=n.getTagList("Connections",10);for(int i=0;i<list.tagCount();i++){var spec=list.getCompoundTagAt(i);if(spec.hasUniqueId("Id"))saved.put(spec.getUniqueId("Id"),spec);}}
}
