package dev.createport.glue;

import java.util.*;

/** Undirected face bonds. Coordinates belong to one world dimension. */
public final class GlueGraph {
    public record Pos(int x,int y,int z) {}
    public record Bond(Pos a,Pos b) {}
    private final Map<Pos,Set<Pos>> neighbours=new HashMap<>();
    public boolean connect(Pos a,Pos b) {
        if (Math.abs((long)a.x-b.x)+Math.abs((long)a.y-b.y)+Math.abs((long)a.z-b.z)!=1)
            throw new IllegalArgumentException("Glue joins adjacent block faces");
        boolean added=neighbours.computeIfAbsent(a,p->new HashSet<>()).add(b);
        neighbours.computeIfAbsent(b,p->new HashSet<>()).add(a);return added;
    }
    public boolean connected(Pos pos) { return neighbours.containsKey(pos) && !neighbours.get(pos).isEmpty(); }
    public boolean disconnect(Pos a,Pos b) {
        boolean removed=neighbours.containsKey(a) && neighbours.get(a).remove(b);
        if (neighbours.containsKey(b)) neighbours.get(b).remove(a);
        if (neighbours.containsKey(a) && neighbours.get(a).isEmpty()) neighbours.remove(a);
        if (neighbours.containsKey(b) && neighbours.get(b).isEmpty()) neighbours.remove(b);
        return removed;
    }
    public boolean remove(Pos pos) {
        if (!neighbours.containsKey(pos)) return false;
        for (Pos other:new HashSet<>(neighbours.get(pos))) disconnect(pos,other);
        return true;
    }
    public Set<Pos> component(Pos start,int limit) {
        if (limit<1) throw new IllegalArgumentException("Invalid glue limit");
        Set<Pos> visited=new HashSet<>();ArrayDeque<Pos> queue=new ArrayDeque<>();queue.add(start);
        while(!queue.isEmpty()) {
            Pos pos=queue.remove();if (!visited.add(pos)) continue;
            if (visited.size()>limit) throw new IllegalArgumentException("Glued structure exceeds block limit");
            queue.addAll(neighbours.getOrDefault(pos,Set.of()));
        }
        return Set.copyOf(visited);
    }
    public List<Bond> bonds() {
        List<Bond> result=new ArrayList<>();Set<Pos> done=new HashSet<>();
        for (var entry:neighbours.entrySet()) {
            for (Pos other:entry.getValue()) if (!done.contains(other)) result.add(new Bond(entry.getKey(),other));
            done.add(entry.getKey());
        }
        return List.copyOf(result);
    }
}
