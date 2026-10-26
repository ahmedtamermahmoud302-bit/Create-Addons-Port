package dev.createport.kinetics;
/** A belt lies in the plane perpendicular to its equal-axis pulley shafts. */
public final class BeltGeometry {
    public record Step(int x,int y,int z,int length){}
    public static Step connect(int shaftAxis,int x,int y,int z){
        int[] d={x,y,z};if(d[shaftAxis]!=0)throw new IllegalArgumentException("Pulley shafts must share one plane");int length=Math.max(Math.abs(x),Math.max(Math.abs(y),Math.abs(z)));if(length<1||length>20)throw new IllegalArgumentException("Belt length must be between 1 and 20 blocks");
        for(int value:d)if(value!=0&&Math.abs(value)!=length)throw new IllegalArgumentException("Use straight, vertical or 45-degree belt endpoints");if(shaftAxis==1&&x!=0&&z!=0)throw new IllegalArgumentException("Sideways belts must run straight");return new Step(Integer.signum(x),Integer.signum(y),Integer.signum(z),length);
    }
    public static double speed(double rpm){return rpm/480d;}
    /** Create's positive rotation moves west on X belts and south on Z belts. */
    public static double travel(double rpm,Step step){return speed(rpm)*(step.x()!=0?-step.x():step.z());}
    private BeltGeometry(){}
}
