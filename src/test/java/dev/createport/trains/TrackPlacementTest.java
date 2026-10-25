package dev.createport.trains;
import dev.createport.trains.TrackCurve.V;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TrackPlacementTest {
    @Test void defaultQuarterTurnAddsStandardStraightExtensions(){
        var p=TrackPlacement.connect(new V(0,0,0),new V(12,0,12),new V(1,0,0),new V(0,0,-1),false);
        assertEquals(4,p.extendA());assertEquals(4,p.extendB());assertFalse(p.straight());
        assertEquals(new V(4.5,0,0),p.curve().a);assertEquals(new V(12,0,7.5),p.curve().b);
    }
    @Test void sprintKeepsTheLargestTurnThatFits(){
        var p=TrackPlacement.connect(new V(0,0,0),new V(12,0,12),new V(1,0,0),new V(0,0,-1),true);
        assertEquals(0,p.extendA());assertEquals(0,p.extendB());assertEquals(new V(.5,0,0),p.curve().a);
    }
    @Test void straightRunPlacesOrdinaryTrackInsteadOfACurve(){
        var p=TrackPlacement.connect(new V(0,0,0),new V(12,0,0),new V(-1,0,0),new V(1,0,0),false);
        assertTrue(p.straight());assertNull(p.curve());assertEquals(12,p.extendA());assertEquals(0,new V(1,0,0).sub(p.axisA()).length(),1e-9);
    }
    @Test void sBendSplitsExcessLengthBetweenBothEnds(){
        var p=TrackPlacement.connect(new V(0,0,0),new V(14,0,4),new V(1,0,0),new V(-1,0,0),false);
        assertEquals(3,p.extendA());assertEquals(2,p.extendB());assertNotNull(p.curve());
    }
    @Test void invalidSharpAndAscendingSBendsAreRejected(){
        assertThrows(IllegalArgumentException.class,()->TrackPlacement.connect(new V(0,0,0),new V(6,0,6),new V(1,0,0),new V(0,0,-1),false));
        assertThrows(IllegalArgumentException.class,()->TrackPlacement.connect(new V(0,0,0),new V(14,1,4),new V(1,0,0),new V(-1,0,0),false));
    }
}
