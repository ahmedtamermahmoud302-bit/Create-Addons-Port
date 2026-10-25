package dev.createport.trains;

public final class TrainMotion {
    public static final double WHEEL_RADIUS=6.5/16d;
    public static final double MAX_SPEED=.4, ACCELERATION=.008, BRAKING=.04, COASTING=.006;
    public static double approach(double speed,double throttle,boolean brake) {
        if (!Double.isFinite(speed) || !Double.isFinite(throttle)) throw new IllegalArgumentException("Invalid train input");
        speed=Math.max(-MAX_SPEED,Math.min(MAX_SPEED,speed));
        double target=brake ? 0:Math.max(-1,Math.min(1,throttle))*MAX_SPEED;
        boolean reversing=speed*target<0;
        if (reversing) target=0;
        double step=brake || reversing ? BRAKING : target==0 ? COASTING:ACCELERATION;
        return speed<target ? Math.min(target,speed+step) : Math.max(target,speed-step);
    }
    public static double wheelAngle(double distance,double radius) {
        if (!Double.isFinite(distance) || !Double.isFinite(radius) || radius<=0) throw new IllegalArgumentException("Invalid wheel input");
        return -360*distance/(Math.PI*2*radius);
    }
    private TrainMotion() {}
}
