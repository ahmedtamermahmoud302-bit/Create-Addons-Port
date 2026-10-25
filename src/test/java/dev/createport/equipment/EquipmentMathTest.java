package dev.createport.equipment;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EquipmentMathTest {
    @Test void fourWrenchTurnsRestoreEveryDirection(){
        for(var axis:WrenchRotation.Axis.values())for(int i=0;i<3;i++)for(int sign:new int[]{-1,1}){
            var d=new WrenchRotation.Direction(i==0?sign:0,i==1?sign:0,i==2?sign:0);var result=d;
            for(int j=0;j<4;j++)result=WrenchRotation.clockwise(result,axis);assertEquals(d,result);
        }
    }
    @Test void wrenchTurnsUseTheSourceAxisConvention(){
        assertEquals(new WrenchRotation.Direction(1,0,0),WrenchRotation.clockwise(new WrenchRotation.Direction(0,0,-1),WrenchRotation.Axis.Y));
        assertEquals(new WrenchRotation.Direction(0,0,-1),WrenchRotation.clockwise(new WrenchRotation.Direction(0,1,0),WrenchRotation.Axis.X));
        assertEquals(new WrenchRotation.Direction(0,-1,0),WrenchRotation.clockwise(new WrenchRotation.Direction(1,0,0),WrenchRotation.Axis.Z));
    }
    @Test void readoutHandlesStoppedAndOverloadedNetworks(){
        assertEquals("n/a",new KineticReadout(0,0,0,0,1,"STOPPED").load());
        var overloaded=new KineticReadout(0,16,100,150,3,"OVERLOADED");assertEquals(0,overloaded.remaining());assertEquals("150.0%",overloaded.load());
        assertEquals("262,144",KineticReadout.number(16*16384));
    }
    @Test void invalidReadoutsAndDirectionsAreRejected(){
        assertThrows(IllegalArgumentException.class,()->new WrenchRotation.Direction(1,1,0));
        assertThrows(IllegalArgumentException.class,()->new KineticReadout(Double.NaN,0,0,0,0,"STOPPED"));
    }
}
