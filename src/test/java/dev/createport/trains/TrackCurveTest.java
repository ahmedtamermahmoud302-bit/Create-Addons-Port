package dev.createport.trains;
import dev.createport.trains.TrackCurve.V;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TrackCurveTest {
    private static void near(V a,V b){assertEquals(0,a.sub(b).length(),1e-8);}
    @Test void straightConnectionHasConstantDistanceAndNativeSegmentCount(){
        var c=new TrackCurve(new V(0,0,0),new V(12,0,0),new V(1,0,0),new V(-1,0,0));
        assertEquals(4,c.handle,1e-9);assertEquals(12,c.length,1e-9);assertEquals(24,c.segments());
        near(new V(6,0,0),c.point(.5));assertEquals(.25,c.parameterAtDistance(3),1e-9);
    }
    @Test void quarterTurnUsesTheOriginalCircleHandleRule(){
        var c=new TrackCurve(new V(0,0,0),new V(8,0,8),new V(1,0,0),new V(0,0,-1));
        assertEquals(8*4/3d*Math.tan(Math.PI/8),c.handle,1e-9);
        near(new V(1,0,0),c.tangent(0));near(new V(0,0,1),c.tangent(1));
        assertEquals(Math.PI*4,c.distanceLength(),.004);assertEquals(25,c.segments());
    }
    @Test void parallelOffsetUsesTheOriginalSCurveHandleRule(){
        var c=new TrackCurve(new V(0,0,0),new V(10,0,6),new V(1,0,0),new V(-1,0,0));
        assertEquals(4,c.handle,1e-9);near(new V(5,0,3),c.point(.5));
        near(new V(1,0,0),c.tangent(0));near(new V(1,0,0),c.tangent(1));
    }
    @Test void reversingAConnectionPreservesThePhysicalCurve(){
        var c=new TrackCurve(new V(1,0,2),new V(9,0,10),new V(1,0,0),new V(0,0,-1));
        var reverse=new TrackCurve(c.b,c.a,c.axisB,c.axisA);
        for(int i=0;i<=100;i++)near(c.point(i/100d),reverse.point(1-i/100d));
        assertEquals(c.distanceLength(),reverse.distanceLength(),1e-9);
    }
    @Test void distanceLookupIsMonotoneAndClampedOnSlopedCurves(){
        var c=new TrackCurve(new V(0,0,0),new V(10,5,6),new V(1,1,0),new V(-1,0,0));
        double last=-1;for(int i=0;i<=100;i++){double t=c.parameterAtDistance(i*c.distanceLength()/100);assertTrue(t>last);last=t;}
        assertEquals(0,c.parameterAtDistance(-10));assertEquals(1,c.parameterAtDistance(1000));
        near(c.a,c.point(c.renderT(0)));near(c.b,c.point(c.renderT(c.segments())));
    }
    @Test void invalidGeometryIsRejectedBeforeItReachesTheRenderer(){
        assertThrows(IllegalArgumentException.class,()->new V(Double.NaN,0,0));
        assertThrows(IllegalArgumentException.class,()->new TrackCurve(new V(0,0,0),new V(0,0,0),new V(1,0,0),new V(-1,0,0)));
        assertThrows(IllegalArgumentException.class,()->new TrackCurve(new V(0,0,0),new V(5,0,0),new V(0,0,0),new V(-1,0,0)));
    }
}
