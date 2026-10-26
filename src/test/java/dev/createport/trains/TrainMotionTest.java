package dev.createport.trains;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TrainMotionTest {
    @Test void accelerationIsGradualAndSpeedIsBounded() {
        double speed=0;for(int i=0;i<200;i++)speed=TrainMotion.approach(speed,1,false);
        assertEquals(TrainMotion.MAX_SPEED,speed,1e-9);
        assertTrue(TrainMotion.approach(0,1,false)>0);assertTrue(TrainMotion.approach(0,1,false)<TrainMotion.MAX_SPEED);
    }
    @Test void reversingBrakesThroughZeroBeforeMovingBackwards() {
        double speed=.1;
        while(speed>0) { double next=TrainMotion.approach(speed,-1,false);assertTrue(next>=0);assertTrue(next<speed);speed=next; }
        assertTrue(TrainMotion.approach(speed,-1,false)<0);
    }
    @Test void emergencyBrakeStopsWithoutChangingDirection() {
        double speed=-.4;for(int i=0;i<100;i++)speed=TrainMotion.approach(speed,1,true);
        assertEquals(0,speed,1e-9);
    }
    @Test void releasingThrottleCoastsToAStop() {
        double speed=.3;for(int i=0;i<100;i++)speed=TrainMotion.approach(speed,0,false);
        assertEquals(0,speed,1e-9);
    }
    @Test void wheelRotationMatchesTravelledCircumference() {
        assertEquals(-360,TrainMotion.wheelAngle(2*Math.PI*.45,.45),1e-9);
        assertEquals(360,TrainMotion.wheelAngle(-2*Math.PI*.45,.45),1e-9);
    }
    @Test void nonfiniteControlInputIsRejected() {
        assertThrows(IllegalArgumentException.class,()->TrainMotion.approach(0,Double.NaN,false));
    }
}
