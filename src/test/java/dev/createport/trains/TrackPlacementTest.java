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
    @Test void twoMaximisedQuarterTurnsMakeAConnectedHalfTurn(){
        var first=TrackPlacement.connect(new V(.5,0,.5),new V(12.5,0,12.5),new V(1,0,0),new V(0,0,-1),true);
        var second=TrackPlacement.connect(new V(12.5,0,12.5),new V(.5,0,24.5),new V(0,0,1),new V(1,0,0),true);
        assertEquals(0,first.extendA());assertEquals(0,first.extendB());assertEquals(0,second.extendA());assertEquals(0,second.extendB());
        assertEquals(0,first.curve().tangent(1).sub(second.curve().tangent(0)).length(),1e-9);
        assertEquals(1,first.curve().b.sub(second.curve().a).length(),1e-9);
        assertEquals(0,first.curve().tangent(0).add(second.curve().tangent(1)).length(),1e-9);
    }
}
