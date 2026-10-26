package dev.createport.trains;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.storage.WorldSavedData;
/** Cross-dimension rail endpoints. Explicit links allow installed mod dimensions without guessing their portal rules. */
public final class PortalTrackLinks extends WorldSavedData {
    public record Endpoint(int dimension,BlockPos track,EnumFacing direction){}
    private final Map<Endpoint,Endpoint> links=new HashMap<>();
    public PortalTrackLinks(){super("create_portal_tracks");}public PortalTrackLinks(String name){super(name);}
    public static PortalTrackLinks get(World world){var storage=world.getMinecraftServer().getWorld(0).getMapStorage();var data=(PortalTrackLinks)storage.getOrLoadData(PortalTrackLinks.class,"create_portal_tracks");if(data==null){data=new PortalTrackLinks();storage.setData("create_portal_tracks",data);}return data;}
    public Endpoint exit(Endpoint entry){return links.get(entry);}
    public void connect(Endpoint entry,Endpoint exit){remove(entry);remove(reverse(exit));links.put(entry,exit);links.put(reverse(exit),reverse(entry));markDirty();}
    public boolean remove(Endpoint entry){var exit=links.remove(entry);if(exit==null)return false;links.remove(reverse(exit));markDirty();return true;}
    public static Endpoint reverse(Endpoint e){return new Endpoint(e.dimension,e.track,e.direction.getOpposite());}
    private static NBTTagCompound write(Endpoint e){var n=new NBTTagCompound();n.setInteger("Dimension",e.dimension);n.setLong("Track",e.track.toLong());n.setInteger("Direction",e.direction.getIndex());return n;}
    private static Endpoint read(NBTTagCompound n){return new Endpoint(n.getInteger("Dimension"),BlockPos.fromLong(n.getLong("Track")),EnumFacing.byIndex(n.getInteger("Direction")));}
    @Override public NBTTagCompound writeToNBT(NBTTagCompound n){var list=new NBTTagList();for(var e:links.entrySet()){var item=new NBTTagCompound();item.setTag("Entry",write(e.getKey()));item.setTag("Exit",write(e.getValue()));list.appendTag(item);}n.setTag("Links",list);return n;}
    @Override public void readFromNBT(NBTTagCompound n){links.clear();var list=n.getTagList("Links",10);for(int i=0;i<list.tagCount();i++){var item=list.getCompoundTagAt(i);var a=read(item.getCompoundTag("Entry"));var b=read(item.getCompoundTag("Exit"));if(a.direction.getAxis()!=EnumFacing.Axis.Y&&b.direction.getAxis()!=EnumFacing.Axis.Y&&a.dimension!=b.dimension)links.put(a,b);}}
}
