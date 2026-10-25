package dev.createport.client;

import dev.createport.trains.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public final class TrainRenderer extends Render<TrainEntity> {
    private static final Map<String,String> MATERIALS=Map.of("Bogey_Body","block/bogey/frame","Bogey_Body_2","block/bogey/frame2","wheel_Large","block/bogey/wheel","00_Shaft","block/axis","00_Shaft_end","block/axis_top");
    private Map<String,List<ObjGeometry.Vertex>> frame,wheels;
    public TrainRenderer(RenderManager manager) { super(manager);shadowSize=1.2f; }
    private Map<String,List<ObjGeometry.Vertex>> load(String name) {
        ResourceLocation resource=new ResourceLocation("create","models/block/track/bogey/"+name+".obj");
        try (InputStream stream=Minecraft.getMinecraft().getResourceManager().getResource(resource).getInputStream();
             Reader reader=new InputStreamReader(stream,StandardCharsets.UTF_8)) { return ObjGeometry.read(reader); }
        catch (IOException failure) { throw new IllegalStateException("Could not load Create bogey model "+resource,failure); }
    }
    private void mesh(Map<String,List<ObjGeometry.Vertex>> mesh) {
        for (Map.Entry<String,List<ObjGeometry.Vertex>> entry:mesh.entrySet()) {
            String texture=MATERIALS.get(entry.getKey());if (texture==null) continue;
            bindTexture(new ResourceLocation("create","textures/"+texture+".png"));
            BufferBuilder buffer=Tessellator.getInstance().getBuffer();buffer.begin(GL11.GL_TRIANGLES,DefaultVertexFormats.POSITION_TEX_NORMAL);
            for (ObjGeometry.Vertex v:entry.getValue()) buffer.pos(v.x(),v.y(),v.z()).tex(v.u(),v.v()).normal(v.nx(),v.ny(),v.nz()).endVertex();
            Tessellator.getInstance().draw();
        }
    }
    @Override public void doRender(TrainEntity train,double x,double y,double z,float yaw,float partialTicks) {
        if (frame==null) { frame=load("bogey_frame");wheels=load("bogey_wheel"); }
        GlStateManager.pushMatrix();GlStateManager.translate(x,y-.3,z);
        float heading=train.prevRotationYaw+MathHelper.wrapDegrees(train.rotationYaw-train.prevRotationYaw)*partialTicks;
        GlStateManager.rotate(-heading,0,1,0);GlStateManager.rotate(train.rotationPitch,1,0,0);
        GlStateManager.enableRescaleNormal();GlStateManager.disableCull();GlStateManager.color(1,1,1,1);
        int light=train.getBrightnessForRender();OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,light&65535,light>>16);
        mesh(frame);
        float wheelAngle=train.wheels()+(float)TrainMotion.wheelAngle(train.speed()*partialTicks,TrainMotion.WHEEL_RADIUS);
        for (int side:new int[]{-1,1}) {
            GlStateManager.pushMatrix();GlStateManager.translate(0,.75,side);GlStateManager.rotate(wheelAngle,1,0,0);mesh(wheels);GlStateManager.popMatrix();
        }
        GlStateManager.pushMatrix();GlStateManager.translate(-.9,1.4,-1.5);GlStateManager.scale(1.8,.2,3);
        bindTexture(net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE);
        BlockRendererDispatcher blocks=Minecraft.getMinecraft().getBlockRendererDispatcher();
        blocks.getBlockModelRenderer().renderModelBrightnessColor(blocks.getModelForState(Blocks.PLANKS.getDefaultState()),1,1,1,1);
        GlStateManager.popMatrix();
        for (TrainStructure.Part part:train.structure()) {
            GlStateManager.pushMatrix();GlStateManager.translate(part.x()-.5,1.6+part.y(),part.z()-.5);
            blocks.getBlockModelRenderer().renderModelBrightnessColor(blocks.getModelForState(part.state()),1,1,1,1);
            GlStateManager.popMatrix();
        }
        GlStateManager.enableCull();GlStateManager.disableRescaleNormal();GlStateManager.popMatrix();
        super.doRender(train,x,y,z,yaw,partialTicks);
    }
    @Override protected ResourceLocation getEntityTexture(TrainEntity train) { return new ResourceLocation("create","textures/block/bogey/frame.png"); }
}
