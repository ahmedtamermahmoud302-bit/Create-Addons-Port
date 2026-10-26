package dev.createport.kinetics;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TransmissionRulesTest {
    @Test void adjacentSmallCogsReverseAtOneToOne(){assertEquals(-1,TransmissionRules.cogs(false,false,1,1,1,0,0));assertEquals(0,TransmissionRules.cogs(false,false,1,1,0,1,0));}
    @Test void diagonalLargeAndSmallCogsGiveReciprocalTwoToOne(){assertEquals(-2,TransmissionRules.cogs(true,false,1,1,1,0,1));assertEquals(-.5,TransmissionRules.cogs(false,true,1,1,-1,0,-1));assertEquals(0,TransmissionRules.cogs(true,false,1,1,1,0,0));}
    @Test void bevelGearsDependOnTheTwoAxisOffsets(){assertEquals(1,TransmissionRules.cogs(true,true,0,1,1,1,0));assertEquals(-1,TransmissionRules.cogs(true,true,0,1,-1,1,0));assertEquals(0,TransmissionRules.cogs(true,true,0,1,1,1,1));}
    @Test void coplanarLargeGearsDoNotMeshDirectly(){assertEquals(0,TransmissionRules.cogs(true,true,1,1,1,0,0));}
    @Test void gearboxSignsAreConsistentAroundEveryClosedFacePath(){int[] faces={2,3,4,5};for(int a:faces)for(int b:faces)for(int c:faces)assertEquals(TransmissionRules.gearbox(a,c),TransmissionRules.gearbox(a,b)*TransmissionRules.gearbox(b,c));}
    @Test void oppositeGearboxShaftsReverse(){assertEquals(-1,TransmissionRules.gearbox(2,3));assertEquals(-1,TransmissionRules.gearbox(4,5));}
    @Test void analogChainRatioUsesOriginalSignalSteps(){assertEquals(1,TransmissionRules.chain(0));assertEquals(1.125,TransmissionRules.chain(1));assertEquals(1.5,TransmissionRules.chain(7));assertEquals(2,TransmissionRules.chain(15));}
    @Test void invalidRedstoneStrengthIsRejected(){assertThrows(IllegalArgumentException.class,()->TransmissionRules.chain(16));}
}
