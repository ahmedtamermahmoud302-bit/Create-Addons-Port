package dev.createport.trains;

import java.io.*;
import java.util.*;

/** Small immutable OBJ adapter for the original supplied Create bogey meshes. */
public final class ObjGeometry {
    public record Vertex(float x,float y,float z,float u,float v,float nx,float ny,float nz) {}
    public static Map<String,List<Vertex>> read(Reader input) throws IOException {
        List<float[]> positions=new ArrayList<>(),uvs=new ArrayList<>(),normals=new ArrayList<>();
        Map<String,List<Vertex>> groups=new LinkedHashMap<>();String material="default";
        BufferedReader reader=new BufferedReader(input);String line;
        try {
            while ((line=reader.readLine())!=null) {
                String[] fields=line.trim().split("\\s+");
                switch (fields[0]) {
                    case "v" -> positions.add(new float[]{Float.parseFloat(fields[1]),Float.parseFloat(fields[2]),Float.parseFloat(fields[3])});
                    case "vt" -> uvs.add(new float[]{Float.parseFloat(fields[1]),Float.parseFloat(fields[2])});
                    case "vn" -> normals.add(new float[]{Float.parseFloat(fields[1]),Float.parseFloat(fields[2]),Float.parseFloat(fields[3])});
                    case "usemtl" -> material=fields[1];
                    case "f" -> {
                        if (fields.length<4) throw new IOException("OBJ face needs at least three vertices");
                        Vertex[] face=new Vertex[fields.length-1];
                        for (int i=1;i<fields.length;i++) {
                            String[] refs=fields[i].split("/",-1);
                            float[] p=positions.get(index(refs[0],positions.size()));
                            float[] uv=refs.length>1 && !refs[1].isEmpty() ? uvs.get(index(refs[1],uvs.size())):new float[2];
                            float[] n=refs.length>2 && !refs[2].isEmpty() ? normals.get(index(refs[2],normals.size())):new float[]{0,1,0};
                            face[i-1]=new Vertex(p[0],p[1],p[2],uv[0],1-uv[1],n[0],n[1],n[2]);
                        }
                        List<Vertex> triangles=groups.computeIfAbsent(material,key -> new ArrayList<>());
                        for (int i=1;i<face.length-1;i++) { triangles.add(face[0]);triangles.add(face[i]);triangles.add(face[i+1]); }
                    }
                    default -> { }
                }
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException invalid) { throw new IOException("Invalid OBJ geometry",invalid); }
        Map<String,List<Vertex>> result=new LinkedHashMap<>();groups.forEach((key,value)->result.put(key,List.copyOf(value)));
        return Collections.unmodifiableMap(result);
    }
    private static int index(String text,int size) { int index=Integer.parseInt(text);return index<0 ? size+index:index-1; }
    private ObjGeometry() {}
}
