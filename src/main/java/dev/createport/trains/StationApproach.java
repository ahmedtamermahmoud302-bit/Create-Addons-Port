package dev.createport.trains;
/** Discrete stopping envelope: the next movement cannot cross the station marker. */
public final class StationApproach {
    public static double next(double speed,double distance,double cap,double acceleration){
        if(!Double.isFinite(speed)||!Double.isFinite(distance)||distance<0||cap<0||acceleration<=0)throw new IllegalArgumentException("Invalid station approach");
        double target=Math.min(cap,Math.sqrt(2*acceleration*distance+acceleration*acceleration/4)-acceleration/2);
        double next=speed<target?Math.min(target,speed+acceleration):Math.max(target,speed-acceleration);
        return Math.min(distance,Math.max(0,next));
    }
    public static double minimumSearchDistance(double speed){return .75*speed*speed/(2*TrainMotion.ACCELERATION);}
    public static double maximumSearchDistance(double speed){return Math.max(32,1.5*speed*speed/(2*TrainMotion.ACCELERATION));}
    private StationApproach(){}
}
