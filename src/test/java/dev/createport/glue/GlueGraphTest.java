package dev.createport.glue;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class GlueGraphTest {
    private final GlueGraph.Pos a=new GlueGraph.Pos(0,0,0),b=new GlueGraph.Pos(1,0,0),c=new GlueGraph.Pos(2,0,0);
    @Test void bondsWorkFromEitherBlockAndDuplicateUseDoesNotAddAnEdge() {
        GlueGraph graph=new GlueGraph();assertTrue(graph.connect(a,b));assertFalse(graph.connect(b,a));
        assertEquals(java.util.Set.of(a,b),graph.component(b,64));assertEquals(1,graph.bonds().size());
    }
    @Test void diagonalAndDistantBlocksCannotBeGlued() {
        GlueGraph graph=new GlueGraph();assertThrows(IllegalArgumentException.class,()->graph.connect(a,c));
        assertThrows(IllegalArgumentException.class,()->graph.connect(a,new GlueGraph.Pos(1,1,0)));
    }
    @Test void removingAJointSplitsTheStructure() {
        GlueGraph graph=new GlueGraph();graph.connect(a,b);graph.connect(b,c);assertTrue(graph.disconnect(a,b));
        assertEquals(java.util.Set.of(a),graph.component(a,64));assertEquals(java.util.Set.of(b,c),graph.component(c,64));
    }
    @Test void breakingABlockRemovesAllItsBonds() {
        GlueGraph graph=new GlueGraph();graph.connect(a,b);graph.connect(b,c);assertTrue(graph.remove(b));
        assertFalse(graph.connected(a));assertFalse(graph.connected(c));assertTrue(graph.bonds().isEmpty());
    }
    @Test void componentLimitsPreventPartialAssembly() {
        GlueGraph graph=new GlueGraph();graph.connect(a,b);graph.connect(b,c);
        assertThrows(IllegalArgumentException.class,()->graph.component(a,2));assertEquals(3,graph.component(a,3).size());
    }
    @Test void savedBondPairsRestoreCyclesAndComponents() {
        GlueGraph graph=new GlueGraph();var d=new GlueGraph.Pos(1,1,0);var e=new GlueGraph.Pos(0,1,0);
        graph.connect(a,b);graph.connect(b,d);graph.connect(d,e);graph.connect(e,a);
        GlueGraph restored=new GlueGraph();for(var bond:graph.bonds())restored.connect(bond.a(),bond.b());
        assertEquals(graph.component(a,64),restored.component(a,64));assertEquals(4,restored.bonds().size());
    }
}
