package dev.createport.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import org.lwjgl.opengl.GL11;
/** Source-sized world outlines and textured selection faces shared by placement tools. */
public final class WorldVisuals {
    public static void begin(float partial){
        var e=Minecraft.getMinecraft().getRenderViewEntity();GlStateManager.pushMatrix();
        GlStateManager.translate(-(e.lastTickPosX+(e.posX-e.lastTickPosX)*partial),-(e.lastTickPosY+(e.posY-e.lastTickPosY)*partial),-(e.lastTickPosZ+(e.posZ-e.lastTickPosZ)*partial));
        GlStateManager.disableLighting();GlStateManager.enableBlend();GlStateManager.blendFunc(770,771);GlStateManager.depthMask(false);GlStateManager.disableCull();GlStateManager.color(1,1,1,1);
    }
    public static void end(){GlStateManager.color(1,1,1,1);GlStateManager.enableTexture2D();GlStateManager.enableCull();GlStateManager.depthMask(true);GlStateManager.disableBlend();GlStateManager.popMatrix();}
    public static void line(Vec3d a,Vec3d b,double width,int color){
        Vec3d d=b.subtract(a);if(d.lengthSquared()<1e-10)return;d=d.normalize();Vec3d right=d.crossProduct(Math.abs(d.y)>.9?new Vec3d(1,0,0):new Vec3d(0,1,0)).normalize().scale(width/2),up=d.crossProduct(right).normalize().scale(width/2);
        Vec3d[] v={a.add(right).add(up),a.subtract(right).add(up),a.subtract(right).subtract(up),a.add(right).subtract(up),b.add(right).add(up),b.subtract(right).add(up),b.subtract(right).subtract(up),b.add(right).subtract(up)};
        int[][] faces={{0,1,2,3},{4,7,6,5},{0,4,5,1},{1,5,6,2},{2,6,7,3},{3,7,4,0}};
        GlStateManager.disableTexture2D();var out=Tessellator.getInstance().getBuffer();out.begin(GL11.GL_QUADS,DefaultVertexFormats.POSITION_COLOR);
        for(var face:faces)for(int index:face){var p=v[index];out.pos(p.x,p.y,p.z).color(color>>16&255,color>>8&255,color&255,255).endVertex();}Tessellator.getInstance().draw();GlStateManager.enableTexture2D();
    }
    public static void outline(AxisAlignedBB b,double width,int color){
        Vec3d[] v={new Vec3d(b.minX,b.minY,b.minZ),new Vec3d(b.maxX,b.minY,b.minZ),new Vec3d(b.maxX,b.minY,b.maxZ),new Vec3d(b.minX,b.minY,b.maxZ),new Vec3d(b.minX,b.maxY,b.minZ),new Vec3d(b.maxX,b.maxY,b.minZ),new Vec3d(b.maxX,b.maxY,b.maxZ),new Vec3d(b.minX,b.maxY,b.maxZ)};
        for(int i=0;i<4;i++){line(v[i],v[(i+1)%4],width,color);line(v[i+4],v[(i+1)%4+4],width,color);line(v[i],v[i+4],width,color);}
    }
    public static void top(BlockPos p,int color){textured(new AxisAlignedBB(p).offset(0,.003,0),"thin_checkerboard",color,1f,true);}
    public static void textured(AxisAlignedBB b,String name,int color,float alpha,boolean topOnly){
        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("create","textures/special/"+name+".png"));GlStateManager.enableTexture2D();GlStateManager.color(1,1,1,1);
        double x=b.minX,y=b.minY,z=b.minZ,X=b.maxX,Y=b.maxY,Z=b.maxZ;double[][][] faces={{{x,Y,z},{x,Y,Z},{X,Y,Z},{X,Y,z}},{{x,y,z},{X,y,z},{X,y,Z},{x,y,Z}},{{x,y,z},{x,Y,z},{X,Y,z},{X,y,z}},{{X,y,Z},{X,Y,Z},{x,Y,Z},{x,y,Z}},{{x,y,Z},{x,Y,Z},{x,Y,z},{x,y,z}},{{X,y,z},{X,Y,z},{X,Y,Z},{X,y,Z}}};
        var out=Tessellator.getInstance().getBuffer();out.begin(GL11.GL_QUADS,DefaultVertexFormats.POSITION_TEX_COLOR);
        for(int i=0;i<(topOnly?1:6);i++){double u=i<2?X-x:i<4?X-x:Z-z,v=i<2?Z-z:Y-y;double[][] uv={{0,0},{0,v},{u,v},{u,0}};for(int j=0;j<4;j++){var p=faces[i][j];out.pos(p[0],p[1],p[2]).tex(uv[j][0],uv[j][1]).color(color>>16&255,color>>8&255,color&255,(int)(alpha*255)).endVertex();}}
        Tessellator.getInstance().draw();GlStateManager.color(1,1,1,1);
    }
    private WorldVisuals(){}
}
