package dev.createport.client;
import dev.createport.trains.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
public final class WorldBogeyRenderer extends TileEntitySpecialRenderer<BogeyTile>{
    @Override public void render(BogeyTile tile,double x,double y,double z,float partial,int destroy,float alpha){
        var state=tile.getWorld().getBlockState(tile.getPos());if(!(state.getBlock() instanceof RailwayBlock))return;
        GlStateManager.pushMatrix();GlStateManager.translate(x+.5,y-1.15,z+.5);
        if(state.getValue(RailwayBlock.FACING).getAxis()==net.minecraft.util.EnumFacing.Axis.X)GlStateManager.rotate(90,0,1,0);
        int light=state.getPackedLightmapCoords(tile.getWorld(),tile.getPos().down());OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,light&65535,light>>16);
        GlStateManager.disableCull();GlStateManager.enableRescaleNormal();GlStateManager.color(1,1,1,1);TrainRenderer.drawBogey(0,Bogeys.large(state));GlStateManager.disableRescaleNormal();GlStateManager.enableCull();GlStateManager.popMatrix();
    }
}
