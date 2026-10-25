package dev.createport.trains;

import dev.createport.trains.RailNavigator.*;
import dev.createport.trains.RailPath.*;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class RailNavigatorTest {
    @Test void straightTravelPreservesRemainingDistanceAcrossBlocks() {
        Lookup tracks=c -> c.y()==0 && c.z()==0 && c.x()>=0 && c.x()<4 ? Shape.EAST_WEST:null;
        Advance moved=RailNavigator.advance(new Cursor(new Cell(0,0,0),.5,1),1.25,tracks);
        assertEquals(new Cell(1,0,0),moved.cursor().cell());assertEquals(.75,moved.cursor().progress(),1e-9);
        assertFalse(moved.blocked());assertEquals(1.25,moved.distance(),1e-9);
    }
    @Test void travelCanTurnThenReverseToItsStartingPoint() {
        Map<Cell,Shape> rails=Map.of(new Cell(0,0,0),Shape.EAST_WEST,new Cell(1,0,0),Shape.SOUTH_WEST,new Cell(1,0,1),Shape.NORTH_SOUTH);
        Cursor start=new Cursor(new Cell(0,0,0),.5,1);double distance=.5+Math.PI/4+.25;
        Advance moved=RailNavigator.advance(start,distance,rails::get);
        Point p=RailNavigator.position(moved.cursor(),rails::get);
        assertEquals(1.5,p.x(),1e-9);assertEquals(1.25,p.z(),1e-9);
        Advance reversed=RailNavigator.advance(moved.cursor(),-distance,rails::get);
        assertEquals(start.cell(),reversed.cursor().cell());assertEquals(start.progress(),reversed.cursor().progress(),1e-9);
        assertEquals(start.forward(),reversed.cursor().forward());
    }
    @Test void slopeConnectsToTrackAtTheHigherElevation() {
        Map<Cell,Shape> rails=Map.of(new Cell(0,0,0),Shape.ASCENDING_EAST,new Cell(1,1,0),Shape.EAST_WEST);
        Cursor start=new Cursor(new Cell(0,0,0),.5,1);double distance=Math.sqrt(2)/2+.25;
        Advance moved=RailNavigator.advance(start,distance,rails::get);
        assertEquals(new Cell(1,1,0),moved.cursor().cell());assertEquals(.25,moved.cursor().progress(),1e-9);
        Advance reverse=RailNavigator.advance(moved.cursor(),-distance,rails::get);
        assertEquals(start.cell(),reverse.cursor().cell());assertEquals(.5,reverse.cursor().progress(),1e-9);
    }
    @Test void disconnectedOrUnloadedTrackStopsAtTheEdge() {
        Lookup track=c -> c.equals(new Cell(0,0,0)) ? Shape.EAST_WEST:null;
        Advance moved=RailNavigator.advance(new Cursor(new Cell(0,0,0),.5,1),2,track);
        assertTrue(moved.blocked());assertEquals(.5,moved.distance(),1e-9);assertEquals(1,moved.cursor().progress(),1e-9);
    }
    @Test void neighbourWithTheWrongAxisDoesNotConnect() {
        Lookup tracks=c -> c.equals(new Cell(0,0,0)) ? Shape.EAST_WEST:c.equals(new Cell(1,0,0)) ? Shape.NORTH_SOUTH:null;
        assertTrue(RailNavigator.advance(new Cursor(new Cell(0,0,0),.5,1),1,tracks).blocked());
    }
    @Test void backwardsFacingCarriageCanTravelBothWays() {
        Lookup track=c -> c.y()==0 && c.z()==0 && c.x()>=-2 && c.x()<=2 ? Shape.EAST_WEST:null;
        Cursor start=new Cursor(new Cell(0,0,0),.5,-1);
        Advance moved=RailNavigator.advance(start,.75,track);
        assertEquals(-.25,RailNavigator.position(moved.cursor(),track).x(),1e-9);
        Advance reverse=RailNavigator.advance(moved.cursor(),-.75,track);
        assertEquals(start.cell(),reverse.cursor().cell());assertEquals(.5,reverse.cursor().progress(),1e-9);
    }
    @Test void curvedDistanceIsArcLengthInsteadOfChordLength() {
        for (Shape shape:Shape.values()) {
            RailPath path=RailPath.of(shape);double measured=0;
            for(int i=1;i<=1000;i++) measured+=path.sample((i-1)/1000d).distance(path.sample(i/1000d));
            assertEquals(path.length(),measured,1e-5,shape.name());
            Point tangent=path.tangent(.4);assertEquals(1,Math.sqrt(tangent.x()*tangent.x()+tangent.y()*tangent.y()+tangent.z()*tangent.z()),1e-9);
        }
    }
    @Test void missingCurrentTrackStopsWithoutTravel() {
        Advance moved=RailNavigator.advance(new Cursor(new Cell(0,0,0),.5,1),.2,c->null);
        assertTrue(moved.blocked());assertEquals(0,moved.distance());
    }
    @Test void invalidStateAndNonfiniteTravelAreRejected() {
        assertThrows(IllegalArgumentException.class,()->new Cursor(new Cell(0,0,0),Double.NaN,1));
        assertThrows(IllegalArgumentException.class,()->new Cursor(new Cell(0,0,0),.5,0));
        assertThrows(IllegalArgumentException.class,()->RailNavigator.advance(new Cursor(new Cell(0,0,0),.5,1),Double.POSITIVE_INFINITY,c->Shape.EAST_WEST));
    }
}
