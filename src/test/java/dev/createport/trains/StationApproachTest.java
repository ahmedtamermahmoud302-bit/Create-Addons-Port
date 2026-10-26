package dev.createport.trains;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class StationApproachTest {
    @Test void fullSpeedApproachStopsExactlyAtMarkerWithoutOvershoot(){double distance=90,speed=TrainMotion.MAX_SPEED;for(int tick=0;tick<10000&&distance>0;tick++){speed=StationApproach.next(speed,distance,TrainMotion.MAX_SPEED,TrainMotion.ACCELERATION);assertTrue(speed>=0&&speed<=distance);distance-=speed;}assertEquals(0,distance,1e-8);assertEquals(0,StationApproach.next(speed,0,TrainMotion.MAX_SPEED,TrainMotion.ACCELERATION));}
    @Test void stationaryTrainCanCreepIntoNearbyStation(){assertTrue(StationApproach.next(0,.5,1,.0075)>0);}
    @Test void lookingAheadGrowsWithTheBrakingDistance(){assertEquals(32,StationApproach.maximumSearchDistance(0));assertTrue(StationApproach.maximumSearchDistance(1)>StationApproach.maximumSearchDistance(.1));assertTrue(StationApproach.minimumSearchDistance(1)>0);}
    @Test void lastMovementCannotCrossTheStopPlane(){assertEquals(.001,StationApproach.next(1,.001,1,.0075),1e-12);}
    @Test void invalidDistanceIsRejected(){assertThrows(IllegalArgumentException.class,()->StationApproach.next(0,-1,1,.0075));}
}
