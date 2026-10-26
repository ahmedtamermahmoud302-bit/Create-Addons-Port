package dev.createport.client;

import dev.createport.Create;
import dev.createport.kinetics.SpeedPacket;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Mouse;
import org.lwjgl.input.Keyboard;
import java.io.IOException;

/** 1.12 renderer adapter for Create 6.0.8's two-row ValueSettingsBoard geometry. */
public final class SpeedSettingsScreen extends GuiScreen {
    private static final ResourceLocation TEXTURE = new ResourceLocation("create", "textures/gui/value_settings.png");
    private final BlockPos pos;
    private final int initialSpeed;
    private int left, top, labelWidth, barWidth,ticksOpen;
    public SpeedSettingsScreen(BlockPos pos, int speed) { this.pos = pos; this.initialSpeed = speed; }
    @Override public void initGui() {
        labelWidth = Math.max(fontRenderer.getStringWidth("§l\u27f3"),fontRenderer.getStringWidth("§l\u27f2"));
        barWidth = 258 + 9 * 4;
        left = (width - labelWidth - 14 - barWidth - 10) / 2; top = (height - 22) / 2;
        int row = initialSpeed < 0 ? 0 : 1;
        Mouse.setCursorPosition((int)(coordinate(Math.abs(initialSpeed)) * mc.displayWidth / width),
            (int)((height - (top + (row + .5f) * 11 - .5f)) * mc.displayHeight / height));
    }
    private float coordinate(int column) {
        float x = left + ((Math.max(1,column)-1)/32)*4 + column + 1.5f + labelWidth + 18;
        if (column % 32 == 0) x += 2;
        if (column > 0) x += 4;
        return x;
    }
    private int closest(int mouseX) {
        int best = 0; float distance = Float.MAX_VALUE;
        for (int col=0; col<=256; col += isShiftKeyDown() ? 32 : 1) {
            float d = Math.abs(coordinate(col)-mouseX);
            if (d <= distance) { distance=d; best=col; }
        }
        return best;
    }
    private void texture(int x,int y,int u,int v,int sw,int sh,int w,int h) {
        mc.getTextureManager().bindTexture(TEXTURE); GlStateManager.color(1,1,1,1);
        drawScaledCustomSizeModalRect(x,y,u,v,sw,sh,w,h,256,256);
    }
    private void frame(int x,int y,int w,int h) {
        texture(x,y,65,9,4,4,4,4); texture(x+w-4,y,70,9,4,4,4,4);
        texture(x,y+h-4,65,19,4,4,4,4); texture(x+w-4,y+h-4,70,19,4,4,4,4);
        texture(x,y+4,65,14,3,4,3,h-8);texture(x+w-3,y+4,71,14,3,4,3,h-8);
        for (int d=4;d<w-4;d+=256) {
            int part=Math.min(256,w-4-d);
            texture(x+d,y,0,24,part,3,part,3);texture(x+d,y+h-3,0,27,part,3,part,3);
        }
    }
    @Override public void drawScreen(int mouseX,int mouseY,float partialTicks) {
        GlStateManager.enableBlend();
        String title="Rotation Speed",tip=net.minecraft.client.resources.I18n.format("create.gui.value_settings.release_to_confirm",mc.gameSettings.keyBindUseItem.getDisplayName());
        int windowWidth=labelWidth+barWidth+24,fattest=Math.max(fontRenderer.getStringWidth(title),fontRenderer.getStringWidth(tip));
        int fatOffset=Math.max(0,fattest+10-(windowWidth+13))/2,fullWidth=Math.max(windowWidth+13,fattest+10);
        float fade=Math.min(1,(ticksOpen+partialTicks)/4f);int faded=(int)(fullWidth*fade),start=(fullWidth-faded)/2-fatOffset;
        texture(left-11+start,top-17,80,9,1,1,faded,55);texture(left-10+start,top-18,80,9,1,1,Math.max(0,faded-2),1);texture(left-10+start,top+38,80,9,1,1,Math.max(0,faded-2),1);
        if(faded>fattest){int center=left-11-fatOffset+fullWidth/2;fontRenderer.drawString(title,center-fontRenderer.getStringWidth(title)/2,top-14,0xdddddd);fontRenderer.drawString(tip,center-fontRenderer.getStringWidth(tip)/2,top+28,0xdddddd);}
        frame(left+labelWidth+14,top-3,barWidth+8,27);frame(left-7,top-3,labelWidth+14,27);
        int barX=left+labelWidth+18;
        texture(barX-1,top,75,9,1,1,barWidth+2,21);
        for (int row=0;row<2;row++) {
            int y=top+row*11;
            texture(left-4,y,0,31,labelWidth+8,11,labelWidth+8,11);
            fontRenderer.drawString(row==0 ? "§l\u27f3" : "§l\u27f2",left,y+1,0x442000);
            for (int d=0;d<barWidth;d+=248) {
                int part=Math.min(248,barWidth-d);texture(barX+d,y+1,7,0,part,8,part,8);
            }
            for(int m=0;m<9;m++) texture(barX+m*36,y+1,0,0,7,8,7,8);
        }
        int row=mouseY < top+10.5 ? 0 : 1, value=closest(mouseX);
        String text=Math.max(1,value)+(row==0 ? "§l\u27f3" : "§l\u27f2");
        int cursorWidth=fontRenderer.getStringWidth(text)/2*2+3;
        int x=(int)coordinate(value)-cursorWidth/2,y=top+(int)((row+.5f)*11-.5f)-7;
        texture(x-3,y,0,9,3,14,3,14);texture(x,y,4,9,cursorWidth,14,cursorWidth,14);
        texture(x+cursorWidth,y,61,9,3,14,3,14);fontRenderer.drawString(text,x+2,y+3,0x442000);
    }
    private void confirm(int mouseX,int mouseY) {
        int value=Math.max(1,closest(mouseX));
        Create.NETWORK.sendToServer(new SpeedPacket(pos,mouseY<top+10.5 ? -value : value));
        mc.displayGuiScreen(null);
    }
    @Override public void updateScreen() {
        ticksOpen++;
        int key=mc.gameSettings.keyBindUseItem.getKeyCode();
        boolean down=key<0 ? Mouse.isButtonDown(key+100) : key>0 && Keyboard.isKeyDown(key);
        if (!down) confirm(Mouse.getX()*width/mc.displayWidth,height-Mouse.getY()*height/mc.displayHeight-1);
    }
    @Override protected void mouseReleased(int mouseX,int mouseY,int button) {
        if (button != mc.gameSettings.keyBindUseItem.getKeyCode()+100) return;
        confirm(mouseX,mouseY);
    }
    @Override public void handleMouseInput() throws IOException {
        super.handleMouseInput();int wheel=Mouse.getEventDWheel();if(wheel==0)return;
        int mouseX=Mouse.getX()*width/mc.displayWidth;
        int value=Math.max(0,Math.min(256,closest(mouseX)+Integer.signum(wheel)*(isShiftKeyDown()?32:1)));
        Mouse.setCursorPosition((int)(coordinate(value)*mc.displayWidth/width),Mouse.getY());
    }
    @Override public boolean doesGuiPauseGame() { return false; }
}
