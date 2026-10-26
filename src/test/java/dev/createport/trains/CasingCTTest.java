package dev.createport.trains;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CasingCTTest {
    @Test void originalSheetUsesDistinctBordersAndInterior(){assertEquals(0,CasingCT.index(0));assertEquals(54,CasingCT.index(255));assertEquals(1,CasingCT.index(1));assertEquals(8,CasingCT.index(4));assertEquals(19,CasingCT.index(1|2|8));}
    @Test void everyNeighbourCombinationRemainsInsideTheEightByEightSheet(){for(int mask=0;mask<256;mask++){int tile=CasingCT.index(mask);assertTrue(tile>=0&&tile<64,"mask "+mask);}}
    @Test void diagonalConnectionsRequireTheirTwoAdjacentEdges(){assertEquals(CasingCT.index(0),CasingCT.index(16|32|64|128));assertNotEquals(CasingCT.index(1|4),CasingCT.index(1|4|16));}
}
