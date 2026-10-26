package dev.createport.client;
import dev.createport.Create;
import dev.createport.kinetics.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.util.*;
import net.minecraftforge.client.model.BakedModelWrapper;
/** Original kinetic models; each split shaft renders its own synchronized face speed. */
public final class MechanicalRenderer {
    public static final List<String> PARTS=List.of("cogwheel","large_cogwheel","hand_crank_handle","belt_pulley","belt_start","belt_middle","belt_end","belt_start_bottom","belt_middle_bottom","belt_end_bottom","belt_diagonal_start","belt_diagonal_middle","belt_diagonal_end");
    private static void align(EnumFacing face){switch(face){case NORTH->GlStateManager.rotate(180,0,1,0);case EAST->GlStateManager.rotate(90,0,1,0);case WEST->GlStateManager.rotate(-90,0,1,0);case UP->GlStateManager.rotate(-90,1,0,0);case DOWN->GlStateManager.rotate(90,1,0,0);default->{}}}
    private static IBakedModel model(String name){return Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getModel(new ModelResourceLocation("create:partial_"+name,"inventory"));}
    private static void draw(IBakedModel model){GlStateManager.color(1,1,1,1);Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(model,1,1,1,1);}
    private static void partial(String name){draw(model(name));}
    public static void render(KineticTile tile,MechanicalBlock block,double x,double y,double z,float partial){
        var mc=Minecraft.getMinecraft();var state=tile.getWorld().getBlockState(tile.getPos());float time=tile.getWorld().getTotalWorldTime()+partial;
        GlStateManager.pushMatrix();GlStateManager.translate(x+.5,y+.5,z+.5);GlStateManager.disableCull();
        if(block.cog()){
            if(tile.axis()==EnumFacing.Axis.X)GlStateManager.rotate(-90,0,0,1);if(tile.axis()==EnumFacing.Axis.Z)GlStateManager.rotate(90,1,0,0);float offset=(tile.getPos().getX()+tile.getPos().getY()+tile.getPos().getZ())%2==0?22.5f:0;GlStateManager.rotate(time*tile.rpm*.3f+offset,0,1,0);GlStateManager.translate(-.5,-.5,-.5);partial(block.kind==MechanicalBlock.Kind.COG?"cogwheel":"large_cogwheel");
        }else if(block.kind==MechanicalBlock.Kind.BELT){
            if(tile.beltFirst!=null){GlStateManager.pushMatrix();var step=BeltTransport.plan(tile);double horizontal=Math.hypot(step.x(),step.z());boolean diagonal=step.y()!=0&&horizontal!=0,up=diagonal&&step.y()>0;GlStateManager.rotate((float)Math.toDegrees(Math.atan2(step.x(),step.z()))+(up?180:0),0,1,0);if(!diagonal)GlStateManager.rotate((float)-Math.toDegrees(Math.atan2(step.y(),horizontal)),1,0,0);if(tile.axis()==EnumFacing.Axis.Y)GlStateManager.rotate(90,0,0,1);GlStateManager.translate(-.5,-.5,-.5);String part=tile.beltIndex==0?(up?"end":"start"):tile.beltIndex==tile.beltLength?(up?"start":"end"):"middle";
                double scroll=time*tile.rpm/(31.5*16);scroll-=Math.floor(scroll);draw(scrolling(model("belt_"+(diagonal?"diagonal_":"")+part),scroll,diagonal));if(!diagonal)draw(scrolling(model("belt_"+part+"_bottom"),(scroll+.5)%1,false));GlStateManager.popMatrix();
                if(tile.beltIndex==0||tile.beltIndex==tile.beltLength){if(tile.axis()==EnumFacing.Axis.Y)GlStateManager.rotate(90,0,0,1);if(tile.axis()==EnumFacing.Axis.Z)GlStateManager.rotate(-90,0,1,0);GlStateManager.rotate(time*tile.rpm*.3f,1,0,0);GlStateManager.translate(-.5,-.5,-.5);partial("belt_pulley");}
            }
        }else if(block.kind==MechanicalBlock.Kind.CRANK){
            align(tile.facing().getOpposite());GlStateManager.rotate(tile.crankAngle+partial*tile.crankVelocity,0,0,1);GlStateManager.translate(-.5,-.5,-.5);partial("hand_crank_handle");
        }else{
            for(var side:EnumFacing.values())if(KineticTopology.hasPort(state,side)){GlStateManager.pushMatrix();align(side);GlStateManager.rotate(time*tile.portRpm(side)*.3f*side.getAxisDirection().getOffset(),0,0,1);var shaft=mc.getRenderItem().getItemModelMesher().getModelManager().getModel(new ModelResourceLocation("create:shaft_half","inventory"));mc.getRenderItem().renderItem(new net.minecraft.item.ItemStack(Create.SHAFT),shaft);GlStateManager.popMatrix();}
        }
        GlStateManager.enableCull();GlStateManager.popMatrix();GlStateManager.color(1,1,1,1);
    }
    private static IBakedModel scrolling(IBakedModel base,double scroll,boolean diagonal){return new BakedModelWrapper<IBakedModel>(base){
        @Override public List<BakedQuad> getQuads(net.minecraft.block.state.IBlockState state,EnumFacing face,long random){List<BakedQuad> result=new ArrayList<>();var target=Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("create:block/"+(diagonal?"belt_diagonal_scroll":"belt_scroll"));
            for(var q:originalModel.getQuads(state,face,random)){var source=q.getSprite();int[] data=q.getVertexData().clone();int stride=data.length/4;for(int i=0;i<4;i++){double u=(Float.intBitsToFloat(data[i*stride+4])-source.getMinU())/(source.getMaxU()-source.getMinU()),v=(Float.intBitsToFloat(data[i*stride+5])-source.getMinV())/(source.getMaxV()-source.getMinV());data[i*stride+4]=Float.floatToRawIntBits(target.getInterpolatedU(u*16));data[i*stride+5]=Float.floatToRawIntBits(target.getInterpolatedV((v+scroll)*(diagonal?6:8)));}result.add(new BakedQuad(data,q.getTintIndex(),q.getFace(),target,q.shouldApplyDiffuseLighting(),q.getFormat()));}return result;
        }};
    }
    private MechanicalRenderer(){}
}
