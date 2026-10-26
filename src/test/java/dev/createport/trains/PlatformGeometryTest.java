package dev.createport.trains;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PlatformGeometryTest {
    private final PlatformGeometry.Box cube=new PlatformGeometry.Box(-.5,0,-.5,.5,1,.5);
    @Test void worldAndLocalCoordinatesRoundTripThroughCurves(){for(double yaw:new double[]{0,45,90,179,-72}){var world=PlatformGeometry.world(3,-7,100,-30,yaw);var local=PlatformGeometry.local(world.x(),world.z(),100,-30,yaw);assertEquals(3,local.x(),1e-10);assertEquals(-7,local.z(),1e-10);}}
    @Test void emptyCornerOfRotatedBoundingBoxIsNotASolidBlock(){var empty=new PlatformGeometry.Box(.6,.5,.6,.69,1.5,.69);assertNull(PlatformGeometry.clip(cube,0,0,0,45,empty));}
    @Test void realRotatedFootprintSupportsStandingPlayer(){var feet=new PlatformGeometry.Box(.25,.99,-.15,.55,1.1,.15);var clipped=PlatformGeometry.clip(cube,0,0,0,45,feet);assertNotNull(clipped);assertEquals(1,clipped.maxY());}
    @Test void movementCarriesAPointAroundTheBogeyRatherThanOnlyTranslatingIt(){var local=PlatformGeometry.local(0,5,0,0,0);var carried=PlatformGeometry.world(local.x(),local.z(),1,2,90);assertEquals(-4,carried.x(),1e-10);assertEquals(2,carried.z(),1e-10);}
    @Test void verticalGapRemainsPassable(){var above=new PlatformGeometry.Box(-.1,1.01,-.1,.1,2,.1);assertNull(PlatformGeometry.clip(cube,0,0,0,0,above));}
}
