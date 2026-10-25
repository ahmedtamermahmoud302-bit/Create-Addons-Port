package dev.createport.trains;

import dev.createport.trains.RailPath.*;

/** Advances by distance rather than by tick fraction, including reversal across shape boundaries. */
public final class RailNavigator {
    public record Cell(int x,int y,int z) { public Cell add(int x,int y,int z) { return new Cell(this.x+x,this.y+y,this.z+z); } }
    public record Cursor(Cell cell,double progress,int forward) {
        public Cursor {
            if (cell==null || !Double.isFinite(progress) || progress<0 || progress>1 || Math.abs(forward)!=1)
                throw new IllegalArgumentException("Invalid train cursor");
        }
    }
    @FunctionalInterface public interface Lookup { Shape get(Cell cell); }
    public record Advance(Cursor cursor,boolean blocked,double distance) {}
    public static Point position(Cursor cursor,Lookup tracks) {
        Shape shape=tracks.get(cursor.cell());
        if (shape==null) throw new IllegalArgumentException("Missing track");
        return worldPoint(cursor.cell(),RailPath.of(shape).sample(cursor.progress()));
    }
    private static Point worldPoint(Cell cell,Point point) { return point.add(cell.x,cell.y,cell.z); }
    public static Advance advance(Cursor cursor,double distance,Lookup tracks) {
        if (!Double.isFinite(distance) || Math.abs(distance)>16) throw new IllegalArgumentException("Invalid travel distance");
        double remaining=Math.abs(distance),moved=0; int sign=distance<0 ? -1:1;
        for (int transitions=0;transitions<64;transitions++) {
            Shape shape=tracks.get(cursor.cell());
            if (shape==null) return new Advance(cursor,true,sign*moved);
            RailPath path=RailPath.of(shape); int travel=cursor.forward()*sign;
            double available=(travel>0 ? 1-cursor.progress() : cursor.progress())*path.length();
            if (remaining<=available+1e-9) {
                double progress=Math.max(0,Math.min(1,cursor.progress()+travel*remaining/path.length()));
                return new Advance(new Cursor(cursor.cell(),progress,cursor.forward()),false,sign*(moved+remaining));
            }
            remaining-=available; moved+=available;
            int exit=travel>0 ? 1:0;
            Point endpoint=path.sample(exit),absolute=worldPoint(cursor.cell(),endpoint);
            int dx=endpoint.x()<1e-7 ? -1 : endpoint.x()>1-1e-7 ? 1:0;
            int dz=endpoint.z()<1e-7 ? -1 : endpoint.z()>1-1e-7 ? 1:0;
            Cursor next=null;
            for (int dy=-1;dy<=1 && next==null;dy++) {
                Cell candidate=cursor.cell().add(dx,dy,dz); Shape candidateShape=tracks.get(candidate);
                if (candidateShape==null) continue;
                RailPath neighbour=RailPath.of(candidateShape);
                for (int entry=0;entry<=1;entry++) {
                    if (absolute.distance(worldPoint(candidate,neighbour.sample(entry)))<1e-6) {
                        next=new Cursor(candidate,entry,(entry==0 ? 1:-1)*sign); break;
                    }
                }
            }
            if (next==null) return new Advance(new Cursor(cursor.cell(),exit,cursor.forward()),true,sign*moved);
            cursor=next;
        }
        return new Advance(cursor,true,sign*moved);
    }
    private RailNavigator() {}
}
