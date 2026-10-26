package dev.createport.client;

import dev.createport.Create;
import dev.createport.trains.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.*;
import org.lwjgl.opengl.GL11;

/** Source TrainHUD atlas regions and physical-key control capture for standing/seated drivers. */
@Mod.EventBusSubscriber(modid=Create.ID,value=Side.CLIENT)
public final class TrainControls extends Gui {
    private static int trainId=-1;private static float throttle=1,displaySpeed,displayThrottle=1,displayPrompt;private static boolean stopping;
    private static final ResourceLocation WIDGETS=new ResourceLocation("create","textures/gui/widgets.png");
    public static TrainEntity controlled(){var mc=Minecraft.getMinecraft();if(mc.player==null||mc.world==null)return null;for(var e:mc.world.loadedEntityList)if(e instanceof TrainEntity train&&train.driverEntityId()==mc.player.getEntityId()&&!train.isDead)return train;return null;}
    private static boolean down(KeyBinding key){int code=key.getKeyCode();return code<0?Mouse.isButtonDown(code+100):code>0&&Keyboard.isKeyDown(code);}
    @SubscribeEvent public static void input(InputUpdateEvent event){var mc=Minecraft.getMinecraft();if(event.getEntityPlayer()!=mc.player||controlled()==null)return;var input=event.getMovementInput();input.moveForward=0;input.moveStrafe=0;input.jump=false;if(!down(mc.gameSettings.keyBindSneak))input.sneak=false;}
    @SubscribeEvent(priority=EventPriority.HIGH) public static void wheel(MouseEvent event){if(event.getDwheel()==0||controlled()==null)return;throttle=Math.max(1/18f,Math.min(1,throttle+Math.signum(event.getDwheel())/18f));event.setCanceled(true);}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getMinecraft();var train=controlled();if(train==null){trainId=-1;stopping=false;return;}
        if(train.getEntityId()!=trainId){trainId=train.getEntityId();throttle=train.throttle();stopping=false;}
        if(mc.player.ticksExisted%2==0&&!stopping){boolean stop=mc.currentScreen!=null||Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)||down(mc.gameSettings.keyBindSneak);int input=(down(mc.gameSettings.keyBindForward)?1:0)-(down(mc.gameSettings.keyBindBack)?1:0),steer=(down(mc.gameSettings.keyBindLeft)?1:0)-(down(mc.gameSettings.keyBindRight)?1:0);Create.NETWORK.sendToServer(new TrainControlPacket(trainId,input,down(mc.gameSettings.keyBindJump),steer,throttle,stop));stopping=stop;}
        float target=(int)(Math.min(1,Math.abs(train.speed())/TrainMotion.MAX_SPEED+.05)*18)/18f;displaySpeed+=(target-displaySpeed)*.5;displayThrottle+=(throttle-displayThrottle)*.75;
        String prompt=prompt(train);float width=prompt.isEmpty()?0:mc.fontRenderer.getStringWidth(prompt)+17;displayPrompt+=(width-displayPrompt)*.5;
    }
    private static String prompt(TrainEntity train){var mc=Minecraft.getMinecraft();return switch(train.prompt()){
        case 1->I18n.format("create.contraption.controls.approach_station",mc.gameSettings.keyBindJump.getDisplayName(),train.stationName());
        case 2->{double fraction=train.approachTotal()<=0?0:train.stationDistance()/train.approachTotal();int remaining=(int)(Math.max(0,Math.min(1,1-(1-fraction)*(1-fraction)))*30);yield "§2"+"|".repeat(30-remaining)+"§6"+"|".repeat(remaining);}
        case 3->I18n.format("create.train.arrived_at",train.stationName());case 4->I18n.format("create.train.departing_from",train.stationName());default->"";};}
    private static void sprite(int x,int y,int u,int v,int w,int h){Minecraft.getMinecraft().getTextureManager().bindTexture(WIDGETS);GlStateManager.color(1,1,1,1);drawModalRectWithCustomSizedTexture(x,y,u,v,w,h,256,256);}
    @SubscribeEvent public static void hud(RenderGameOverlayEvent.Post event){
        if(event.getType()!=RenderGameOverlayEvent.ElementType.ALL)return;var mc=Minecraft.getMinecraft();var train=controlled();if(train==null||mc.gameSettings.hideGUI||mc.player.isSpectator())return;
        int left=event.getResolution().getScaledWidth()/2-91,top=event.getResolution().getScaledHeight()-29;
        GlStateManager.enableBlend();GlStateManager.disableDepth();sprite(left-2,top+1,0,200,186,7);sprite(left,top,0,190,182,5);sprite(left,top,0,185,(int)(182*displaySpeed),5);
        int width=(int)(182*(1-displayThrottle)),inv=182-width;sprite(left+inv,top,inv,195,width,5);sprite(left+Math.max(1,inv)-3,top-2,0,209,6,9);sprite(left+77,top-20,77,165,28,20);
        int size=Math.min(250,(int)displayPrompt);if(size>1){int x=left+91-size/2,y=top-27;sprite(x-3,y,8,209,3,16);sprite(x+size,y,11,209,3,16);sprite(x,y,128-size/2,230,size,16);String prompt=prompt(train);if(mc.fontRenderer.getStringWidth(prompt)<size-10)mc.fontRenderer.drawString(prompt,left+91-mc.fontRenderer.getStringWidth(prompt)/2,y+4,0x544d45);}
        mc.getTextureManager().bindTexture(new ResourceLocation("create","textures/gui/placement_indicator.png"));int frame=Math.floorMod((int)Math.round(MathHelperAngle(train,mc)/22.5),16);double v=frame/16d,V=(frame+1)/16d,r=9*.925;var b=Tessellator.getInstance().getBuffer();b.begin(GL11.GL_QUADS,DefaultVertexFormats.POSITION_TEX);b.pos(left+91-r,top-9-r,50).tex(0,v).endVertex();b.pos(left+91-r,top-9+r,50).tex(0,V).endVertex();b.pos(left+91+r,top-9+r,50).tex(1,V).endVertex();b.pos(left+91+r,top-9-r,50).tex(1,v).endVertex();Tessellator.getInstance().draw();GlStateManager.enableDepth();GlStateManager.color(1,1,1,1);
    }
    private static double MathHelperAngle(TrainEntity train,Minecraft mc){double diff=net.minecraft.util.math.MathHelper.wrapDegrees(mc.player.rotationYaw-train.rotationYaw);if(Math.abs(diff)<60)diff=0;return 22.5*Math.round((diff+(down(mc.gameSettings.keyBindLeft)?-45:0)+(down(mc.gameSettings.keyBindRight)?45:0)+(down(mc.gameSettings.keyBindBack)?180:0))/22.5);}
}
