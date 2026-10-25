package dev.createport.client;
import dev.createport.Create;
import dev.createport.trains.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import java.io.IOException;
public final class StationScreen extends GuiScreen {
    private final BlockPos pos;private GuiTextField name;
    public StationScreen(BlockPos pos){this.pos=pos;}
    @Override public void initGui(){int x=(width-200)/2,y=(height-178)/2;buttonList.clear();buttonList.add(new GuiButton(0,x+12,y+48,176,20,"Toggle Assembly Mode"));buttonList.add(new GuiButton(1,x+12,y+74,176,20,"Assemble Train"));buttonList.add(new GuiButton(2,x+12,y+100,176,20,"Disassemble Train"));name=new GuiTextField(0,fontRenderer,x+12,y+24,176,16);name.setMaxStringLength(32);if(mc.world.getTileEntity(pos) instanceof TrainStationTile station)name.setText(station.trainName);}
    @Override protected void actionPerformed(GuiButton button){Create.NETWORK.sendToServer(new StationActionPacket(pos,button.id,name.getText()));}
    @Override public void drawScreen(int mouseX,int mouseY,float partial){drawDefaultBackground();int x=(width-200)/2,y=(height-178)/2;mc.getTextureManager().bindTexture(new ResourceLocation("create","textures/gui/assemble.png"));GlStateManager.color(1,1,1,1);drawModalRectWithCustomSizedTexture(x,y,0,0,200,178,256,256);drawCenteredString(fontRenderer,"Train Station",width/2,y+7,0xffffff);name.drawTextBox();drawRect(x+8,y+122,x+192,y+174,0xee303030);if(mc.world.getTileEntity(pos) instanceof TrainStationTile station){fontRenderer.drawSplitString(station.message,x+12,y+126,176,0xeeeeee);buttonList.get(0).displayString=station.assembling?"Finish Assembly Mode":"Begin Assembly Mode";}super.drawScreen(mouseX,mouseY,partial);}
    @Override protected void keyTyped(char c,int key) throws IOException{if(!name.textboxKeyTyped(c,key))super.keyTyped(c,key);}
    @Override protected void mouseClicked(int x,int y,int button) throws IOException{super.mouseClicked(x,y,button);name.mouseClicked(x,y,button);}
    @Override public boolean doesGuiPauseGame(){return false;}
}
