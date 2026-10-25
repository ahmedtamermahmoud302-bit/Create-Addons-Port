package dev.createport.trains;

/** Arc-length paths for the initial block-based track adapter; Create's Bezier graph is not ported yet. */
public final class RailPath {
    public enum Shape {
        NORTH_SOUTH, EAST_WEST, ASCENDING_EAST, ASCENDING_WEST, ASCENDING_NORTH,
        ASCENDING_SOUTH, SOUTH_EAST, SOUTH_WEST, NORTH_WEST, NORTH_EAST
    }
    public record Point(double x, double y, double z) {
        public Point add(double dx, double dy, double dz) { return new Point(x+dx,y+dy,z+dz); }
        public double distance(Point p) { return Math.sqrt(Math.pow(x-p.x,2)+Math.pow(y-p.y,2)+Math.pow(z-p.z,2)); }
    }
    private final Point a, b;
    private final double cx, cz, angle, sweep;
    private RailPath(Point a, Point b) { this.a=a; this.b=b; cx=cz=angle=sweep=0; }
    private RailPath(double cx,double cz,double angle,double sweep) {
        this.cx=cx; this.cz=cz; this.angle=angle; this.sweep=sweep; a=b=null;
    }
    public static RailPath of(Shape shape) {
        return switch (shape) {
            case NORTH_SOUTH -> new RailPath(new Point(.5,0,0),new Point(.5,0,1));
            case EAST_WEST -> new RailPath(new Point(0,0,.5),new Point(1,0,.5));
            case ASCENDING_EAST -> new RailPath(new Point(0,0,.5),new Point(1,1,.5));
            case ASCENDING_WEST -> new RailPath(new Point(0,1,.5),new Point(1,0,.5));
            case ASCENDING_NORTH -> new RailPath(new Point(.5,1,0),new Point(.5,0,1));
            case ASCENDING_SOUTH -> new RailPath(new Point(.5,0,0),new Point(.5,1,1));
            case SOUTH_EAST -> new RailPath(1,1,Math.PI,Math.PI/2);
            case SOUTH_WEST -> new RailPath(0,1,0,-Math.PI/2);
            case NORTH_WEST -> new RailPath(0,0,0,Math.PI/2);
            case NORTH_EAST -> new RailPath(1,0,Math.PI,-Math.PI/2);
        };
    }
    public double length() { return a == null ? Math.PI/4 : a.distance(b); }
    public Point sample(double t) {
        t=Math.max(0,Math.min(1,t));
        return a == null ? new Point(cx+.5*Math.cos(angle+sweep*t),0,cz+.5*Math.sin(angle+sweep*t))
                : new Point(a.x+(b.x-a.x)*t,a.y+(b.y-a.y)*t,a.z+(b.z-a.z)*t);
    }
    public Point tangent(double t) {
        if (a != null) return new Point((b.x-a.x)/length(),(b.y-a.y)/length(),(b.z-a.z)/length());
        double direction=Math.signum(sweep);
        return new Point(-Math.sin(angle+sweep*t)*direction,0,Math.cos(angle+sweep*t)*direction);
    }
}
