package dev.createport.client;
import dev.createport.trains.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
public final class CurveTrackRenderer extends Render<CurveTrackEntity> {
    public CurveTrackRenderer(RenderManager manager){super(manager);}
    private static void angles(TrackCurve.V normal,TrackCurve.V diff){
        double yaw=Math.atan2(diff.x(),diff.z()),pitch=Math.atan2(Math.hypot(diff.x(),diff.z()),diff.y())-Math.PI/2;
        TrackCurve.V up=new TrackCurve.V(Math.sin(yaw)*Math.sin(pitch),Math.cos(pitch),Math.cos(yaw)*Math.sin(pitch));
        double sign=Math.signum(up.dot(normal));if(Math.abs(sign)<.5)sign=up.sub(normal).length()<Math.sqrt(.5)?-1:1;
        double dot=diff.cross(normal).unit().dot(up),roll=Math.acos(Math.max(-1,Math.min(1,dot)))*sign;
        GlStateManager.rotate((float)Math.toDegrees(yaw),0,1,0);GlStateManager.rotate((float)Math.toDegrees(pitch),1,0,0);GlStateManager.rotate((float)Math.toDegrees(roll),0,0,1);
    }
    @Override public void doRender(CurveTrackEntity entity,double x,double y,double z,float yaw,float partial){
        GlStateManager.pushMatrix();GlStateManager.translate(x-entity.posX,y-entity.posY,z-entity.posZ);GlStateManager.enableRescaleNormal();GlStateManager.disableCull();GlStateManager.color(1,1,1,1);
        int light=entity.getBrightnessForRender();OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,light&65535,light>>16);draw(entity.curve());
        GlStateManager.enableCull();GlStateManager.disableRescaleNormal();GlStateManager.popMatrix();
    }
    public static void draw(TrackCurve curve){
        GlStateManager.pushMatrix();GlStateManager.translate(0,3/16f,0);
        TrackCurve.V prev=curve.a,prevN=curve.normal(0);
        for(int i=1;i<=curve.segments();i++){
            double t=curve.renderT(i);TrackCurve.V p=curve.point(t),normal=curve.normal(t);
            GlStateManager.pushMatrix();GlStateManager.translate(prev.x(),prev.y(),prev.z());angles(normal,p.sub(prev));GlStateManager.translate(-.5,-2/16f-1/256f,0);TrackMeshes.draw("tie");GlStateManager.popMatrix();
            for(int sign:new int[]{1,-1}){
                TrackCurve.V a=prev.add(prevN.scale(.965f*sign)),b=p.add(normal.scale(.965f*sign)),diff=b.sub(a);
                GlStateManager.pushMatrix();GlStateManager.translate(a.x(),a.y(),a.z());angles(normal,diff);GlStateManager.translate(0,-2/16f-1/256f,-1/32f);GlStateManager.scale(1,1,diff.length()*(i==curve.segments()?2.2f:2.1f));TrackMeshes.draw(sign==1?"segment_left":"segment_right");GlStateManager.popMatrix();
            }
            prev=p;prevN=normal;
        }
        GlStateManager.popMatrix();
    }
    @Override protected ResourceLocation getEntityTexture(CurveTrackEntity entity){return new ResourceLocation("create","textures/block/standard_track.png");}
}
