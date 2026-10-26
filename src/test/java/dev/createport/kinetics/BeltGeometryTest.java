package dev.createport.kinetics;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BeltGeometryTest {
    @Test void flatAndInclinedBeltsUseIntegralSegmentSteps(){assertEquals(new BeltGeometry.Step(1,0,0,8),BeltGeometry.connect(2,8,0,0));assertEquals(new BeltGeometry.Step(0,1,-1,6),BeltGeometry.connect(0,0,6,-6));}
    @Test void verticalBeltsRemainInThePulleyPlane(){assertEquals(new BeltGeometry.Step(0,1,0,4),BeltGeometry.connect(0,0,4,0));}
    @Test void skewAndUnequalSlopesAreRejected(){assertThrows(IllegalArgumentException.class,()->BeltGeometry.connect(0,1,4,4));assertThrows(IllegalArgumentException.class,()->BeltGeometry.connect(0,0,3,4));}
    @Test void maximumLengthIsBounded(){assertThrows(IllegalArgumentException.class,()->BeltGeometry.connect(2,21,0,0));assertThrows(IllegalArgumentException.class,()->BeltGeometry.connect(2,0,0,0));}
    @Test void sidewaysBeltsDoNotCrossTwoHorizontalAxes(){assertThrows(IllegalArgumentException.class,()->BeltGeometry.connect(1,3,0,3));}
    @Test void itemMovementUsesCreateRpmConversion(){assertEquals(.1,BeltGeometry.speed(48),1e-12);assertEquals(-.5,BeltGeometry.speed(-240),1e-12);}
    @Test void selectionOrderDoesNotReverseWorldMovement(){
        var south=BeltGeometry.connect(0,0,0,6);var north=BeltGeometry.connect(0,0,0,-6);
        assertEquals(.1,BeltGeometry.travel(48,south)*south.z(),1e-12);
        assertEquals(.1,BeltGeometry.travel(48,north)*north.z(),1e-12);
    }
    @Test void positiveRotationCarriesWestOnAnXAlignedBelt(){
        var east=BeltGeometry.connect(2,6,0,0);var west=BeltGeometry.connect(2,-6,0,0);
        assertEquals(-.1,BeltGeometry.travel(48,east)*east.x(),1e-12);
        assertEquals(-.1,BeltGeometry.travel(48,west)*west.x(),1e-12);
    }
}
