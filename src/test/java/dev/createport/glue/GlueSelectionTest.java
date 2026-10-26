package dev.createport.glue;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class GlueSelectionTest {
    @Test void oppositeCornersCaptureTheWholeConnectedBody(){Set<GlueGraph.Pos> blocks=new HashSet<>();for(int x=0;x<6;x++)blocks.add(new GlueGraph.Pos(x,0,0));blocks.add(new GlueGraph.Pos(4,1,0));blocks.add(new GlueGraph.Pos(5,1,0));assertEquals(blocks,GlueSelection.collect(new GlueGraph.Pos(0,0,0),new GlueGraph.Pos(5,1,0),blocks::contains));}
    @Test void gapsCannotReachDisconnectedCorners(){var a=new GlueGraph.Pos(0,0,0);var b=new GlueGraph.Pos(2,0,0);var blocks=Set.of(a,b);assertEquals(Set.of(a),GlueSelection.collect(a,b,blocks::contains));}
    @Test void blocksOutsideTheSelectionDoNotBridgeAGap(){var a=new GlueGraph.Pos(0,0,0);var b=new GlueGraph.Pos(2,0,0);assertFalse(GlueSelection.collect(a,b,p->p.z()==1||p.equals(a)||p.equals(b)).contains(b));}
    @Test void excessiveSelectionsAreRejectedBeforeLookingUpWorldBlocks(){assertThrows(IllegalArgumentException.class,()->GlueSelection.collect(new GlueGraph.Pos(0,0,0),new GlueGraph.Pos(25,0,0),p->{fail("Out-of-range world lookup");return true;}));}
}
