package dev.createport.trains;

public final class TrainMotion {
    public static final double WHEEL_RADIUS=6.5/16d;
    public static final double MAX_SPEED=28d/20*.75, TURN_SPEED=14d/20, ACCELERATION=3d/400, BRAKING=2*ACCELERATION, COASTING=ACCELERATION;
    public static double approach(double speed,double throttle,boolean brake) {
        return approach(speed,throttle,brake,MAX_SPEED);
    }
    public static double approach(double speed,double throttle,boolean brake,double cap) {
        if (!Double.isFinite(speed) || !Double.isFinite(throttle)) throw new IllegalArgumentException("Invalid train input");
        speed=Math.max(-MAX_SPEED,Math.min(MAX_SPEED,speed));
        double target=brake ? 0:Math.max(-1,Math.min(1,throttle))*cap;
        if(target<0)target=Math.max(target,-MAX_SPEED/4);
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
