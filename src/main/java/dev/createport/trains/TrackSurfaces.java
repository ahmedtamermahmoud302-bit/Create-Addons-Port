package dev.createport.trains;
import java.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
/** Collision follows each curve's actual strip, not the empty interior of its bounding box. */
public final class TrackSurfaces {
    public static Collection<TrackCurve> curves(World world){
        if(!world.isRemote)return CurveGraphData.get(world).curves(world);
        Map<UUID,TrackCurve> found=new HashMap<>();
        for(var tile:world.loadedTileEntityList)if(tile instanceof TrackRenderTile track)for(int i=0;i<track.connections.tagCount()&&i<track.renderedCurves.size();i++){var c=track.renderedCurves.get(i);if(c!=null)found.put(track.connections.getCompoundTagAt(i).getUniqueId("Id"),c);}return found.values();
    }
    public static void collisions(TrackCurve curve,AxisAlignedBB query,List<AxisAlignedBB> out){
        var previous=curve.a;for(int i=1;i<=curve.segments();i++){
            var next=curve.point(curve.renderT(i));double dx=next.x()-previous.x(),dz=next.z()-previous.z(),length=Math.hypot(dx,dz);
            double x=(next.x()+previous.x())/2,z=(next.z()+previous.z())/2,y=Math.min(previous.y(),next.y());
            if(Math.abs(x-(query.minX+query.maxX)/2)>length+2+query.maxX-query.minX||Math.abs(z-(query.minZ+query.maxZ)/2)>length+2+query.maxZ-query.minZ){previous=next;continue;}
            var strip=new PlatformGeometry.Box(-1.125,0,-length/2-.04,1.125,5/16d+Math.abs(next.y()-previous.y()),length/2+.04);
            var clipped=PlatformGeometry.clip(strip,x,y,z,-Math.toDegrees(Math.atan2(dx,dz)),TrainCollisions.box(query));if(clipped!=null)out.add(TrainCollisions.box(clipped));previous=next;
        }
    }
    private TrackSurfaces(){}
}
