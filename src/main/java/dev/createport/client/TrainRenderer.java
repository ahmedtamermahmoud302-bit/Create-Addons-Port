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
    private static final Map<String,String> MATERIALS=Map.of("Bogey_Body","block/bogey/frame","Bogey_Body_2","block/bogey/frame2","wheel_Large","block/bogey/wheel","00_Shaft","block/axis","00_Shaft_end","block/axis_top","Bogey_Piston","block/bogey/piston","Belts","block/bogey/belt_scroll");
    private static Map<String,List<ObjGeometry.Vertex>> frame,wheels;
    private static final Map<String,Map<String,List<ObjGeometry.Vertex>>> LARGE=new HashMap<>();
    public TrainRenderer(RenderManager manager) { super(manager);shadowSize=1.2f; }
    private static Map<String,List<ObjGeometry.Vertex>> load(String name) {
        ResourceLocation resource=new ResourceLocation("create","models/block/track/bogey/"+name+".obj");
        try (InputStream stream=Minecraft.getMinecraft().getResourceManager().getResource(resource).getInputStream();
             Reader reader=new InputStreamReader(stream,StandardCharsets.UTF_8)) { return ObjGeometry.read(reader); }
        catch (IOException failure) { throw new IllegalStateException("Could not load Create bogey model "+resource,failure); }
    }
    private static void mesh(Map<String,List<ObjGeometry.Vertex>> mesh) { mesh(mesh,0); }
    private static void mesh(Map<String,List<ObjGeometry.Vertex>> mesh,double scroll) {
        for (Map.Entry<String,List<ObjGeometry.Vertex>> entry:mesh.entrySet()) {
            String texture=MATERIALS.get(entry.getKey());if (texture==null) continue;
            Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("create","textures/"+texture+".png"));
            BufferBuilder buffer=Tessellator.getInstance().getBuffer();buffer.begin(GL11.GL_TRIANGLES,DefaultVertexFormats.POSITION_TEX_NORMAL);
            for (ObjGeometry.Vertex v:entry.getValue()) buffer.pos(v.x(),v.y(),v.z()).tex(v.u(),v.v()+(entry.getKey().equals("Belts")?scroll:0)).normal(v.nx(),v.ny(),v.nz()).endVertex();
            Tessellator.getInstance().draw();
        }
    }
    public static void drawBogey(float angle){
        if(frame==null){frame=load("bogey_frame");wheels=load("bogey_wheel");}
        mesh(frame);
        for(int side:new int[]{-1,1}){GlStateManager.pushMatrix();GlStateManager.translate(0,.75,side);GlStateManager.rotate(angle,1,0,0);mesh(wheels);GlStateManager.popMatrix();}
    }
    public static void drawBogey(float smallAngle,boolean large){
        if(!large){drawBogey(smallAngle);return;}
        float angle=smallAngle*6.5f/12.5f;
        java.util.function.Function<String,Map<String,List<ObjGeometry.Vertex>>> part=name->LARGE.computeIfAbsent(name,TrainRenderer::load);
        mesh(part.apply("bogey_drive"));
        mesh(part.apply("bogey_drive_belt"),(5d/16*Math.toRadians(angle)%1)*.5);
        GlStateManager.pushMatrix();GlStateManager.translate(0,0,.25*Math.sin(Math.toRadians(angle)));mesh(part.apply("bogey_drive_piston"));GlStateManager.popMatrix();
        GlStateManager.pushMatrix();GlStateManager.translate(0,1,0);GlStateManager.rotate(angle,1,0,0);mesh(part.apply("bogey_drive_wheel"));GlStateManager.translate(0,.25,0);GlStateManager.rotate(-angle,1,0,0);mesh(part.apply("bogey_drive_wheel_pin"));GlStateManager.popMatrix();
    }
    @Override public boolean shouldRender(TrainEntity train,net.minecraft.client.renderer.culling.ICamera camera,double x,double y,double z){return train.isInRangeToRender3d(x,y,z)&&camera.isBoundingBoxInFrustum(train.visualBounds());}
    @Override public void doRender(TrainEntity train,double x,double y,double z,float yaw,float partialTicks) {
        if (frame==null) { frame=load("bogey_frame");wheels=load("bogey_wheel"); }
        GlStateManager.pushMatrix();GlStateManager.translate(x,y-.3,z);
        float heading=train.prevRotationYaw+MathHelper.wrapDegrees(train.rotationYaw-train.prevRotationYaw)*partialTicks;
        GlStateManager.rotate(-heading,0,1,0);GlStateManager.rotate(train.rotationPitch,1,0,0);
        GlStateManager.enableRescaleNormal();GlStateManager.disableCull();GlStateManager.color(1,1,1,1);
        int light=train.getBrightnessForRender();OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,light&65535,light>>16);
        float wheelAngle=train.wheels()+(float)TrainMotion.wheelAngle(train.speed()*partialTicks,TrainMotion.WHEEL_RADIUS);
        if(!train.worldBuilt())drawBogey(wheelAngle);
        else for(var part:train.structure())if(Bogeys.is(part.state())){GlStateManager.pushMatrix();GlStateManager.translate(part.x(),part.y(),part.z());GlStateManager.rotate(-train.bogeyYaw(part.z()>0),0,1,0);drawBogey(wheelAngle,Bogeys.large(part.state()));GlStateManager.popMatrix();}
        bindTexture(net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE);
        BlockRendererDispatcher blocks=Minecraft.getMinecraft().getBlockRendererDispatcher();
        java.util.Map<net.minecraft.util.math.BlockPos,net.minecraft.block.state.IBlockState> carried=new java.util.HashMap<>();
        for(var p:train.structure())carried.put(new net.minecraft.util.math.BlockPos(p.x(),p.y(),p.z()),p.state());
        for (TrainStructure.Part part:train.structure()) {
            GlStateManager.pushMatrix();GlStateManager.translate(part.x()-.5,train.bodyOffset()+part.y(),part.z()-.5);
            var state=part.state();if(state.getBlock() instanceof TrainCasingBlock)state=TrainCasingBlock.withConnections(state,new net.minecraft.util.math.BlockPos(part.x(),part.y(),part.z()),p->carried.getOrDefault(p,Blocks.AIR.getDefaultState()));
            if(state.getBlock() instanceof ControlsBlock)state=state.withProperty(ControlsBlock.VIRTUAL,true);
            var model=blocks.getModelForState(state instanceof net.minecraftforge.common.property.IExtendedBlockState extended?extended.getClean():state);if(model instanceof CasingModel casing)model=casing.forState(state);
            blocks.getBlockModelRenderer().renderModelBrightnessColor(model,1,1,1,1);
            GlStateManager.popMatrix();
        }
        GlStateManager.enableCull();GlStateManager.disableRescaleNormal();GlStateManager.popMatrix();
        super.doRender(train,x,y,z,yaw,partialTicks);
    }
    @Override protected ResourceLocation getEntityTexture(TrainEntity train) { return new ResourceLocation("create","textures/block/bogey/frame.png"); }
}
