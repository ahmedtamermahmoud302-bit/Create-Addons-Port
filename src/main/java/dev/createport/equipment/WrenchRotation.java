package dev.createport.equipment;

/** Create's clockwise turn about the targeted face's axis (face sign does not change the turn). */
public final class WrenchRotation {
    public enum Axis { X,Y,Z }
    public record Direction(int x,int y,int z) {
        public Direction { if(Math.abs((long)x)+Math.abs((long)y)+Math.abs((long)z)!=1)throw new IllegalArgumentException("Expected a unit axis direction"); }
    }
    public static Direction clockwise(Direction direction,Axis axis) {
        return switch(axis) {
            case X -> new Direction(direction.x(),direction.z(),-direction.y());
            case Y -> new Direction(-direction.z(),direction.y(),direction.x());
            case Z -> new Direction(direction.y(),-direction.x(),direction.z());
        };
    }
    private WrenchRotation() {}
}
