package dev.createport.kinetics;

import org.junit.jupiter.api.Test;
import java.util.List;
import static dev.createport.kinetics.KineticNetwork.*;
import static org.junit.jupiter.api.Assertions.*;

class KineticNetworkTest {
    private Node motor(int id, double rpm) { return new Node(id, rpm, 16, 0); }
    @Test void shaftPreservesSignedSpeed() {
        Result r = solve(List.of(motor(0, -16), new Node(1,0,0,0)), List.of(new Edge(0,1,1)),256);
        assertEquals(-16,r.speed(1)); assertEquals(Status.RUNNING,r.status());
    }
    @Test void gearsReverseAndMultiplySpeed() {
        Result r = solve(List.of(motor(0,16),new Node(1,0,0,4)),List.of(new Edge(0,1,-2)),256);
        assertEquals(-32,r.speed(1)); assertEquals(256,r.capacity()); assertEquals(128,r.stress());
    }
    @Test void overloadStopsRotationButRetainsTheoreticalSpeed() {
        Result r = solve(List.of(motor(0,16),new Node(1,0,0,20)),List.of(new Edge(0,1,1)),256);
        assertEquals(Status.OVERSTRESSED,r.status()); assertEquals(0,r.speed(1)); assertEquals(16,r.theoreticalRpm().get(1));
    }
    @Test void exactCapacityCanRun() {
        Result r = solve(List.of(motor(0,16),new Node(1,0,0,16)),List.of(new Edge(0,1,1)),256);
        assertEquals(Status.RUNNING,r.status());
    }
    @Test void opposingSourcesConflict() {
        Result r = solve(List.of(motor(0,16),motor(1,-16)),List.of(new Edge(0,1,1)),256);
        assertEquals(Status.CONFLICT,r.status()); assertEquals(0,r.speed(0));
    }
    @Test void matchingSourcesAddCapacity() {
        Result r = solve(List.of(motor(0,16),motor(1,16)),List.of(new Edge(0,1,1)),256);
        assertEquals(512,r.capacity()); assertEquals(16,r.speed(1));
    }
    @Test void inconsistentGearLoopConflicts() {
        Result r = solve(List.of(motor(0,16),new Node(1,0,0,0),new Node(2,0,0,0)),
            List.of(new Edge(0,1,-1),new Edge(1,2,-1),new Edge(2,0,-1)),256);
        assertEquals(Status.CONFLICT,r.status());
    }
    @Test void unpoweredComponentsStop() {
        assertEquals(Status.UNPOWERED,solve(List.of(new Node(0,0,0,4)),List.of(),256).status());
    }
    @Test void multipliedSpeedCannotExceedLimit() {
        Result r = solve(List.of(motor(0,256),new Node(1,0,0,0)),List.of(new Edge(0,1,2)),256);
        assertEquals(Status.OVERSPEED,r.status()); assertEquals(0,r.speed(1));
    }
    @Test void disconnectedComponentsMustBeSolvedSeparately() {
        assertThrows(IllegalArgumentException.class,()->solve(List.of(motor(0,16),new Node(1,0,0,0)),List.of(),256));
    }
    @Test void invalidInputsAreRejected() {
        assertThrows(IllegalArgumentException.class,()->new Edge(0,1,0));
        assertThrows(IllegalArgumentException.class,()->new Node(0,Double.NaN,1,0));
        assertThrows(IllegalArgumentException.class,()->solve(List.of(motor(0,16)),List.of(new Edge(0,2,1)),256));
    }
}
