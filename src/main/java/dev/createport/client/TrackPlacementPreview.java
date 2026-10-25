package dev.createport.client;
import dev.createport.Create;
import dev.createport.trains.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.util.math.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
@Mod.EventBusSubscriber(modid=Create.ID,value=Side.CLIENT)
public final class TrackPlacementPreview {
    @SubscribeEvent public static void render(RenderWorldLastEvent event){
        var mc=Minecraft.getMinecraft();if(mc.player==null || mc.world==null || mc.currentScreen!=null)return;
        var stack=mc.player.getHeldItemMainhand();if(!(stack.getItem() instanceof TrackItem item) || !item.hasEffect(stack))return;
        var hit=mc.objectMouseOver;if(hit==null || hit.typeOfHit!=RayTraceResult.Type.BLOCK)return;
        BlockPos target=mc.world.getBlockState(hit.getBlockPos()).getBlock() instanceof TrainTrackBlock?hit.getBlockPos():mc.world.getBlockState(hit.getBlockPos()).getBlock().isReplaceable(mc.world,hit.getBlockPos())?hit.getBlockPos():hit.getBlockPos().offset(hit.sideHit);
        try{
            var placement=TrackItem.plan(mc.world,stack,target,mc.player);TrackCurve curve=placement.plan().curve();var camera=mc.getRenderViewEntity();double t=event.getPartialTicks();
            GlStateManager.pushMatrix();GlStateManager.translate(-(camera.lastTickPosX+(camera.posX-camera.lastTickPosX)*t),-(camera.lastTickPosY+(camera.posY-camera.lastTickPosY)*t),-(camera.lastTickPosZ+(camera.posZ-camera.lastTickPosZ)*t));
            GlStateManager.enableBlend();GlStateManager.blendFunc(770,771);GlStateManager.depthMask(false);GlStateManager.disableCull();GlStateManager.color(.6f,1,.6f,.55f);
            mc.getTextureManager().bindTexture(net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE);
            for(var entry:placement.blocks().entrySet())if(mc.world.getBlockState(entry.getKey()).getBlock().isReplaceable(mc.world,entry.getKey())){
                var p=entry.getKey();GlStateManager.pushMatrix();GlStateManager.translate(p.getX(),p.getY()+.01,p.getZ());
                mc.getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(mc.getBlockRendererDispatcher().getModelForState(entry.getValue()),1,.6f,1,.6f);GlStateManager.popMatrix();
            }
            if(curve!=null)CurveTrackRenderer.draw(curve);
            GlStateManager.color(1,1,1,1);GlStateManager.enableCull();GlStateManager.depthMask(true);GlStateManager.disableBlend();GlStateManager.popMatrix();
        }catch(IllegalArgumentException ignored){}
    }
}
