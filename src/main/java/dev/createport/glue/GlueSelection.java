package dev.createport.glue;
import java.util.*;
import java.util.function.Predicate;
/** Bounded face-connected search inside the two selected block corners. */
public final class GlueSelection {
    public static Set<GlueGraph.Pos> collect(GlueGraph.Pos a,GlueGraph.Pos b,Predicate<GlueGraph.Pos> movable){
        long dx=(long)a.x()-b.x(),dy=(long)a.y()-b.y(),dz=(long)a.z()-b.z();if(dx*dx+dy*dy+dz*dz>=25*25)throw new IllegalArgumentException("Glue selection is too far away");
        int minX=Math.min(a.x(),b.x()),maxX=Math.max(a.x(),b.x()),minY=Math.min(a.y(),b.y()),maxY=Math.max(a.y(),b.y()),minZ=Math.min(a.z(),b.z()),maxZ=Math.max(a.z(),b.z());
        Set<GlueGraph.Pos> group=new HashSet<>();ArrayDeque<GlueGraph.Pos> queue=new ArrayDeque<>();if(movable.test(a))queue.add(a);
        int[][] offsets={{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
        while(!queue.isEmpty()){
            var p=queue.remove();if(!group.add(p))continue;
            for(var d:offsets){var n=new GlueGraph.Pos(p.x()+d[0],p.y()+d[1],p.z()+d[2]);if(n.x()<minX||n.x()>maxX||n.y()<minY||n.y()>maxY||n.z()<minZ||n.z()>maxZ||group.contains(n)||!movable.test(n))continue;queue.add(n);}
        }return Set.copyOf(group);
    }
    private GlueSelection(){}
}
