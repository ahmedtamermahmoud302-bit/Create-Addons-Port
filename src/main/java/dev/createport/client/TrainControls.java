package dev.createport.client;

import dev.createport.Create;
import dev.createport.trains.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid=Create.ID,value=Side.CLIENT)
public final class TrainControls {
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase!=TickEvent.Phase.END) return;
        Minecraft mc=Minecraft.getMinecraft();
        if (mc.player==null || !(mc.player.getRidingEntity() instanceof TrainEntity train) || mc.player.ticksExisted%2!=0) return;
        int input=0;boolean brake=mc.currentScreen!=null;
        if (!brake) {
            input=(mc.gameSettings.keyBindForward.isKeyDown() ? 1:0)-(mc.gameSettings.keyBindBack.isKeyDown() ? 1:0);
            brake=mc.gameSettings.keyBindJump.isKeyDown();
        }
        Create.NETWORK.sendToServer(new TrainControlPacket(train.getEntityId(),input,brake));
    }
    @SubscribeEvent public static void hud(RenderGameOverlayEvent.Post event) {
        if (event.getType()!=RenderGameOverlayEvent.ElementType.ALL) return;
        Minecraft mc=Minecraft.getMinecraft();
        if (mc.player==null || !(mc.player.getRidingEntity() instanceof TrainEntity train)) return;
        if(!train.controlsActive())return;
        int y=event.getResolution().getScaledHeight()-65;
        String speed=String.format(java.util.Locale.ROOT,"Train  %.1f km/h",Math.abs(train.speed())*72);
        mc.fontRenderer.drawStringWithShadow(speed,10,y,0xffdd88);
        mc.fontRenderer.drawStringWithShadow("Forward / Back: drive   Jump: brake   Sneak: dismount",10,y+12,0xffffff);
    }
}
