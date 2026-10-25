package dev.createport.trains;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
class ObjGeometryTest {
    @Test void quadsAreTriangulatedAndTextureVIsFlipped() throws Exception {
        var mesh=ObjGeometry.read(new StringReader("v 0 0 0\nv 1 0 0\nv 1 1 0\nv 0 1 0\nvt 0 0\nvn 0 0 1\nusemtl rail\nf 1/1/1 2/1/1 3/1/1 4/1/1\n"));
        assertEquals(6,mesh.get("rail").size());assertEquals(1,mesh.get("rail").get(0).v());
    }
    @Test void invalidVertexReferencesFailInsteadOfProducingMissingGeometry() {
        assertThrows(IOException.class,()->ObjGeometry.read(new StringReader("v 0 0 0\nf 1 2 3\n")));
    }
    @Test void bothSuppliedCreateBogeyMeshesParseWithTheirMaterialsAndNormals() throws Exception {
        for(String name:new String[]{"bogey_frame","bogey_wheel"}) {
            try(var stream=getClass().getResourceAsStream("/assets/create/models/block/track/bogey/"+name+".obj")) {
                assertNotNull(stream);var mesh=ObjGeometry.read(new InputStreamReader(stream,StandardCharsets.UTF_8));
                assertFalse(mesh.isEmpty());assertTrue(mesh.values().stream().mapToInt(java.util.List::size).sum()>100);
                for(String material:mesh.keySet())assertTrue(java.util.Set.of("Bogey_Body","Bogey_Body_2","wheel_Large","00_Shaft","00_Shaft_end").contains(material),material);
            }
        }
    }
}
