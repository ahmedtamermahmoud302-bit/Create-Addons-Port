package dev.createport.client;

import dev.createport.Create;
import dev.createport.equipment.*;
import dev.createport.kinetics.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

/** GeneratingKineticBlockEntity's three-line tooltip, vanilla border and original icon. */
@Mod.EventBusSubscriber(modid=Create.ID,value=Side.CLIENT)
public final class GogglesOverlay {
    private static boolean drawing;private static float tooltipFade;
    private static BlockPos last;private static int hover;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){if(event.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getMinecraft();var hit=mc.objectMouseOver;
        BlockPos pos=mc.player!=null&&GogglesItem.isWearing(mc.player)&&hit!=null&&hit.typeOfHit==RayTraceResult.Type.BLOCK?hit.getBlockPos():null;
        if(!Objects.equals(pos,last)){hover=0;last=pos;}else if(pos!=null)hover++;
    }
    @SubscribeEvent public static void colors(net.minecraftforge.client.event.RenderTooltipEvent.Color event){if(drawing){event.setBackground(((int)(180*tooltipFade)<<24)|0x100010);event.setBorderStart(((int)(80*tooltipFade)<<24)|0x5000ff);event.setBorderEnd(((int)(80*tooltipFade)<<24)|0x28007f);}}
    @SubscribeEvent public static void overlay(RenderGameOverlayEvent.Post event){
        var mc=Minecraft.getMinecraft();if(event.getType()!=RenderGameOverlayEvent.ElementType.ALL||mc.player==null||mc.world==null||mc.currentScreen!=null||mc.gameSettings.hideGUI||mc.player.isSpectator()||!GogglesItem.isWearing(mc.player))return;
        var hit=mc.objectMouseOver;if(hit==null||hit.typeOfHit!=RayTraceResult.Type.BLOCK||!(mc.world.getTileEntity(hit.getBlockPos()) instanceof KineticTile tile)||!(tile.motor()||tile.getWorld().getBlockState(tile.getPos()).getBlock() instanceof MechanicalBlock m&&m.kind==MechanicalBlock.Kind.CRANK))return;
        double total=Math.abs(tile.motor()?tile.generatedRpm:tile.theoreticalRpm)*(tile.motor()?16384d:8);
        List<String> lines=List.of("     Generator Stats:","     §7Kinetic Stress Capacity:","      §b"+KineticReadout.number(total)+"su §8at current speed");
        int width=0;for(var line:lines)width=Math.max(width,mc.fontRenderer.getStringWidth(line));int height=30;
        int x=Math.min(event.getResolution().getScaledWidth()/2+20,event.getResolution().getScaledWidth()-width-20),y=Math.min(event.getResolution().getScaledHeight()/2,event.getResolution().getScaledHeight()-height-20);
        float fade=Math.max(0,Math.min(1,(hover+event.getPartialTicks())/24f));int alpha=(int)(180*fade),borderAlpha=(int)(80*fade);GlStateManager.pushMatrix();GlStateManager.translate(Math.pow(1-fade,3)*8,0,0);
        drawing=true;tooltipFade=fade;
        try{GuiUtils.drawHoveringText(new ItemStack(Create.GOGGLES),lines,x,y,event.getResolution().getScaledWidth(),event.getResolution().getScaledHeight(),-1,mc.fontRenderer);}finally{drawing=false;}
        RenderHelper.enableGUIStandardItemLighting();GlStateManager.enableDepth();mc.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Create.GOGGLES),x+10,y-16);RenderHelper.disableStandardItemLighting();GlStateManager.color(1,1,1,1);GlStateManager.popMatrix();
    }
}
