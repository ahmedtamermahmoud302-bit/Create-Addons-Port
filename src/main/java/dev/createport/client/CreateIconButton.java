package dev.createport.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
/** Original Create widget atlas, including WideIconButton's second background slice. */
public final class CreateIconButton extends GuiButton {
    private final String sheet;private final int u,v,iconWidth;public final String tooltip;
    public CreateIconButton(int id,int x,int y,String sheet,int u,int v,boolean wide,String tooltip){super(id,x,y,wide?26:18,18,"");this.sheet=sheet;this.u=u;this.v=v;iconWidth=wide?24:16;this.tooltip=tooltip;}
    @Override public void drawButton(Minecraft mc,int mouseX,int mouseY,float partial){if(!visible)return;hovered=mouseX>=x&&mouseY>=y&&mouseX<x+width&&mouseY<y+height;int background=!enabled?90:hovered&&Mouse.isButtonDown(0)?36:hovered?18:0;GlStateManager.color(1,1,1,1);GlStateManager.enableBlend();mc.getTextureManager().bindTexture(new ResourceLocation("create","textures/gui/widgets.png"));drawModalRectWithCustomSizedTexture(x,y,background,0,18,18,256,256);if(width==26)drawModalRectWithCustomSizedTexture(x+9,y,background+1,0,17,18,256,256);mc.getTextureManager().bindTexture(new ResourceLocation("create","textures/gui/"+sheet+".png"));drawModalRectWithCustomSizedTexture(x+1,y+1,u,v,iconWidth,16,256,256);GlStateManager.color(1,1,1,1);}
    public boolean hovering(){return hovered&&visible;}
}
