package dev.createport.client;

import dev.createport.Create;
import dev.createport.equipment.*;
import dev.createport.kinetics.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid=Create.ID,value=Side.CLIENT)
public final class GogglesOverlay {
    @SubscribeEvent public static void overlay(RenderGameOverlayEvent.Post event) {
        Minecraft mc=Minecraft.getMinecraft();
        if(event.getType()!=RenderGameOverlayEvent.ElementType.ALL || mc.player==null || mc.world==null
                || mc.currentScreen!=null || mc.gameSettings.hideGUI || mc.player.isSpectator() || !GogglesItem.isWearing(mc.player))return;
        RayTraceResult hit=mc.objectMouseOver;
        if(hit==null || hit.typeOfHit!=RayTraceResult.Type.BLOCK || !(mc.world.getTileEntity(hit.getBlockPos()) instanceof KineticTile tile))return;
        KineticReadout readout=new KineticReadout(tile.rpm,tile.theoreticalRpm,tile.networkCapacity,tile.networkStress,tile.networkMembers,tile.status.name());
        List<String> lines=new ArrayList<>();lines.add("Kinetic Stats");
        lines.add("Speed: "+KineticReadout.number(Math.abs(readout.rpm()))+" RPM");
        lines.add("Capacity: "+KineticReadout.number(readout.capacity())+" SU");
        lines.add("Stress: "+KineticReadout.number(readout.stress())+" SU ("+readout.load()+")");
        lines.add("Available: "+KineticReadout.number(readout.remaining())+" SU");
        if(mc.player.isSneaking()) {
            lines.add("Theoretical: "+KineticReadout.number(readout.theoreticalRpm())+" RPM");
            lines.add("Network: "+readout.members()+" blocks");lines.add("State: "+readout.status());
            lines.add("Axis: "+tile.axis().name()+(tile.motor() ? " / "+tile.facing().name():""));
        } else lines.add("Hold Sneak for network details");
        if(WrenchItem.isHolding(mc.player))lines.add("Wrench: use to rotate; Sneak-use to pick up");
        else if(tile.motor())lines.add("Hold use to adjust motor speed");
        int w=0;for(String line:lines)w=Math.max(w,mc.fontRenderer.getStringWidth(line));
        int h=lines.size()*11+8;
        int x=Math.max(6,Math.min(event.getResolution().getScaledWidth()/2+12,event.getResolution().getScaledWidth()-w-10));
        int y=Math.max(6,Math.min(event.getResolution().getScaledHeight()/2+12,event.getResolution().getScaledHeight()-h-8));
        Gui.drawRect(x-5,y-5,x+w+5,y+h,0xd0202020);Gui.drawRect(x-5,y-5,x-3,y+h,0xffc59a47);
        int row=0;for(String line:lines) {
            int color=row==0 ? 0xffd67c:row==1 && tile.status!=KineticNetwork.Status.RUNNING ? 0xff7777:0xdddddd;
            mc.fontRenderer.drawStringWithShadow(line,x,y+row++*11,color);
        }
    }
}
