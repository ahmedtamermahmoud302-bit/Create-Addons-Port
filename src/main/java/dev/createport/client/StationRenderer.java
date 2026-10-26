package dev.createport.client;
import dev.createport.Create;
import dev.createport.trains.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
public final class StationRenderer extends TileEntitySpecialRenderer<TrainStationTile> {
    public static void partial(String name,int color){
        var mc=Minecraft.getMinecraft();mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);GlStateManager.color(1,1,1,1);
        var model=mc.getRenderItem().getItemModelMesher().getModelManager().getModel(new ModelResourceLocation("create:partial_"+name,"inventory"));mc.getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(model,1,(color>>16&255)/255f,(color>>8&255)/255f,(color&255)/255f);GlStateManager.color(1,1,1,1);
    }
    public static void flag(TrainStationTile tile,float partial,boolean gui){
        if(tile.target==null)return;var dir=tile.direction;int rotation=(int)(-dir.getHorizontalAngle()-90);
        var diff=new Vec3d(tile.target.subtract(tile.getPos()));var right=dir.rotateY();boolean flipped=diff.dotProduct(new Vec3d(right.getXOffset(),0,right.getZOffset()))>0;
        float value=tile.previousFlag+(tile.flag-tile.previousFlag)*partial;if(gui&&tile.assembling)value=1;
        double progress=Math.pow(Math.min(value*5,1),2);if(value>.2&&value<1){double wiggle=(value-.2)/.8;progress+=Math.sin(wiggle*2*Math.PI*4)/8/Math.max(1,8*wiggle);}
        float nudge=1/512f;GlStateManager.pushMatrix();GlStateManager.translate(.5,.5,.5);GlStateManager.rotate(rotation,0,1,0);GlStateManager.translate(nudge,9.5/16f,flipped?14/16f-nudge:2/16f+nudge);GlStateManager.translate(-.5,-.5,-.5);GlStateManager.rotate((float)((flipped?1:-1)*(progress*90+270)),1,0,0);GlStateManager.translate(.5/16f,0,0);GlStateManager.rotate(flipped?0:180,0,1,0);GlStateManager.translate(-.5/16f,0,0);
        partial("track_station_flag_"+(tile.assembling?"assemble":value>.75?"on":"off"),0xffffff);GlStateManager.popMatrix();
    }
    public static void overlay(BlockPos pos,EnumFacing direction,String kind,int color){
        GlStateManager.pushMatrix();GlStateManager.translate(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5);GlStateManager.rotate((float)Math.toDegrees(Math.atan2(direction.getXOffset(),direction.getZOffset())),0,1,0);GlStateManager.translate(-.5,-.5,-.5);partial("track_overlay_"+kind,color);GlStateManager.popMatrix();
    }
    @Override public void render(TrainStationTile tile,double x,double y,double z,float partial,int destroy,float alpha){
        if(tile.target==null)return;GlStateManager.pushMatrix();GlStateManager.translate(x,y,z);GlStateManager.enableRescaleNormal();GlStateManager.disableCull();GlStateManager.color(1,1,1,1);
        int light=tile.getWorld().getCombinedLight(tile.getPos(),0);OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,light&65535,light>>16);flag(tile,partial,false);
        GlStateManager.translate(-tile.getPos().getX(),-tile.getPos().getY(),-tile.getPos().getZ());
        if(!tile.assembling||tile.trainPresent)overlay(tile.target,tile.direction,"station",0xffffff);
        else for(int i=0;i<tile.assemblyLength;i++){
            BlockPos p=tile.target.offset(tile.direction,i+1);int color=tile.validBogeyOffset(i)?0x96B5FF:-1;if(Bogeys.is(tile.getWorld().getBlockState(p.up())))color=0xCAFF96;if(color!=-1)overlay(p,tile.direction,"assembling",color);
        }
        GlStateManager.enableCull();GlStateManager.disableRescaleNormal();GlStateManager.color(1,1,1,1);GlStateManager.popMatrix();
    }
    @Override public boolean isGlobalRenderer(TrainStationTile tile){return true;}
}
