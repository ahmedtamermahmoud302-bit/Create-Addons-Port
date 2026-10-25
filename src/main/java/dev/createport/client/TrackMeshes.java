package dev.createport.client;

import dev.createport.trains.ObjGeometry;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class TrackMeshes {
    private static final Map<String,Map<String,List<ObjGeometry.Vertex>>> CACHE=new HashMap<>();
    public static void draw(String name){
        Map<String,List<ObjGeometry.Vertex>> mesh=CACHE.computeIfAbsent(name,key->{
            try(var in=Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("create","models/block/track/"+key+".obj")).getInputStream()){
                return ObjGeometry.read(new InputStreamReader(in,StandardCharsets.UTF_8));
            }catch(IOException e){throw new IllegalStateException("Could not load original track mesh "+key,e);}
        });
        for(var group:mesh.entrySet()){
            String texture=switch(group.getKey()){case "m_0","m_1_0"->"standard_track";case "m_1"->"standard_track_mip";case "m_2"->name.equals("ascending")?"standard_track_mip":"standard_track_crossing";default->null;};if(texture==null)continue;
            Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("create","textures/block/"+texture+".png"));
            BufferBuilder buffer=Tessellator.getInstance().getBuffer();buffer.begin(GL11.GL_TRIANGLES,DefaultVertexFormats.POSITION_TEX_NORMAL);
            for(var v:group.getValue())buffer.pos(v.x(),v.y(),v.z()).tex(v.u(),v.v()).normal(v.nx(),v.ny(),v.nz()).endVertex();
            Tessellator.getInstance().draw();
        }
    }
    private TrackMeshes(){}
}
