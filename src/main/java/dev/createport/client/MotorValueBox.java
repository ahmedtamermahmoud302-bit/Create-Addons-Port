package dev.createport.client;
import dev.createport.Create;
import dev.createport.kinetics.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;
/** The in-world value control; kinetic statistics remain in the goggles-only HUD. */
@Mod.EventBusSubscriber(modid=Create.ID,value=Side.CLIENT)
public final class MotorValueBox {
    @SubscribeEvent public static void render(RenderWorldLastEvent event){
        var mc=Minecraft.getMinecraft();if(mc.player==null||mc.world==null||mc.currentScreen!=null)return;var hit=mc.objectMouseOver;
        if(hit==null||hit.typeOfHit!=RayTraceResult.Type.BLOCK||!(mc.world.getTileEntity(hit.getBlockPos()) instanceof KineticTile tile)||!tile.motor()||!MotorSettings.active(tile,hit.sideHit))return;
        var center=MotorSettings.center(tile,hit.sideHit);boolean highlighted=MotorSettings.hit(tile,hit.sideHit,hit.hitVec.subtract(new Vec3d(tile.getPos())));
        WorldVisuals.begin(event.getPartialTicks());GlStateManager.disableDepth();try{
            GlStateManager.translate(tile.getPos().getX()+center.x,tile.getPos().getY()+center.y,tile.getPos().getZ()+center.z);
            switch(hit.sideHit){case SOUTH->GlStateManager.rotate(180,0,1,0);case EAST->GlStateManager.rotate(-90,0,1,0);case WEST->GlStateManager.rotate(90,0,1,0);case UP->{GlStateManager.rotate(90,1,0,0);if(tile.axis()!=EnumFacing.Axis.Y)GlStateManager.rotate(-tile.facing().getHorizontalAngle()+180,0,0,1);}case DOWN->GlStateManager.rotate(-90,1,0,0);default->{}}
            if(highlighted){mc.getTextureManager().bindTexture(new ResourceLocation("create","textures/gui/icons.png"));var out=Tessellator.getInstance().getBuffer();out.begin(GL11.GL_QUADS,DefaultVertexFormats.POSITION_TEX);double a=-.5,b=.5,z=-1/64d;out.pos(a,a,z).tex(32/256d,128/256d).endVertex();out.pos(a,b,z).tex(32/256d,144/256d).endVertex();out.pos(b,b,z).tex(48/256d,144/256d).endVertex();out.pos(b,a,z).tex(48/256d,128/256d).endVertex();Tessellator.getInstance().draw();}
            String value=Integer.toString(Math.abs(tile.generatedRpm));float scale=.2109375f/Math.max(9,mc.fontRenderer.getStringWidth(value));GlStateManager.translate(0,0,-.0390625);GlStateManager.scale(-scale,-scale,scale);GlStateManager.color(1,1,1,1);mc.fontRenderer.drawString(value,-mc.fontRenderer.getStringWidth(value)/2f,-4,0xededed,false);
        }finally{GlStateManager.enableDepth();WorldVisuals.end();}
    }
}
