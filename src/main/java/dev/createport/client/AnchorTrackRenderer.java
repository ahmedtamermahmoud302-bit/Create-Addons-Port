package dev.createport.client;
import dev.createport.trains.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
public final class AnchorTrackRenderer extends TileEntitySpecialRenderer<TrackRenderTile> {
    @Override public boolean isGlobalRenderer(TrackRenderTile tile){return tile.connections.tagCount()>0;}
    @Override public void render(TrackRenderTile tile,double x,double y,double z,float partial,int destroy,float alpha){
        var state=tile.getWorld().getBlockState(tile.getPos());if(!(state.getBlock() instanceof TrainTrackBlock))return;
        var diagonal=state.getValue(TrainTrackBlock.DIAGONAL);var shape=state.getValue(TrainTrackBlock.SHAPE);
        int light=state.getPackedLightmapCoords(tile.getWorld(),tile.getPos());OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,light&65535,light>>16);GlStateManager.color(1,1,1,1);
        if(tile.connections.tagCount()>0){
            GlStateManager.pushMatrix();GlStateManager.translate(x-tile.getPos().getX(),y-tile.getPos().getY(),z-tile.getPos().getZ());GlStateManager.disableCull();GlStateManager.enableRescaleNormal();
            for(int i=0;i<tile.connections.tagCount();i++){
                var n=tile.connections.getCompoundTagAt(i);var primary=net.minecraft.util.math.BlockPos.fromLong(n.getLong("First"));
                if(!primary.equals(tile.getPos()) && tile.getWorld().isBlockLoaded(primary))continue;
                if(i<tile.renderedCurves.size() && tile.renderedCurves.get(i)!=null)CurveTrackRenderer.draw(tile.renderedCurves.get(i));
            }
            GlStateManager.disableRescaleNormal();GlStateManager.enableCull();GlStateManager.popMatrix();
        }
        if(diagonal==TrainTrackBlock.Diagonal.NONE && !shape.isAscending())return;
        GlStateManager.pushMatrix();GlStateManager.translate(x,y,z);GlStateManager.disableCull();GlStateManager.enableRescaleNormal();
        if(diagonal!=TrainTrackBlock.Diagonal.NONE)TrackMeshes.draw(diagonal==TrainTrackBlock.Diagonal.POSITIVE?"diag":"diag_2");
        else {GlStateManager.translate(.5,0,.5);GlStateManager.rotate(switch(shape){case ASCENDING_EAST->90;case ASCENDING_WEST->-90;case ASCENDING_NORTH->180;default->0;},0,1,0);GlStateManager.translate(-.5,0,-.5);TrackMeshes.draw("ascending");}
        GlStateManager.disableRescaleNormal();GlStateManager.enableCull();GlStateManager.popMatrix();
    }
}
