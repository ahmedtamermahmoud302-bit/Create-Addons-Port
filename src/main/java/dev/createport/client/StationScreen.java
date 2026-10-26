package dev.createport.client;
import dev.createport.Create;
import dev.createport.trains.*;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
/** Source StationScreen / AssemblyScreen layouts, atlas regions and icon-button coordinates. */
public final class StationScreen extends GuiScreen {
    private final BlockPos pos;private GuiTextField stationName,trainName;private boolean assembling;private int left,top,panelHeight;
    public StationScreen(BlockPos pos){this.pos=pos;}
    private TrainStationTile station(){return mc.world!=null&&mc.world.getTileEntity(pos) instanceof TrainStationTile station?station:null;}
    private String text(String key,Object... args){return I18n.format("create.station."+key,args);}
    private void sprite(String name,int x,int y,int u,int v,int w,int h){mc.getTextureManager().bindTexture(new ResourceLocation("create","textures/gui/"+name+".png"));GlStateManager.color(1,1,1,1);drawModalRectWithCustomSizedTexture(x,y,u,v,w,h,256,256);}
    @Override public void initGui(){
        var station=station();assembling=station!=null&&station.assembling;panelHeight=assembling?178:127;left=(width-200)/2;top=(height-panelHeight)/2;buttonList.clear();
        buttonList.add(new CreateIconButton(3,left+167,top+panelHeight-24,"icons",assembling?64:0,assembling?0:16,false,text(assembling?"close":"close")));
        if(assembling){buttonList.add(new CreateIconButton(1,left+94,top+154,"schedule_2",64,239,true,text("assemble_train")));buttonList.add(new CreateIconButton(0,left+73,top+154,"icons",128,160,false,text("cancel")));}
        else{buttonList.add(new CreateIconButton(0,left+84,top+65,"schedule_2",14,239,true,text("create_train")));buttonList.add(new CreateIconButton(2,left+94,top+65,"schedule_2",39,239,true,text("disassemble_train")));buttonList.add(new CreateIconButton(4,left+73,top+65,"icons",192,48,false,""));}
        stationName=new GuiTextField(0,fontRenderer,left+23,top+4,180,10);stationName.setMaxStringLength(25);stationName.setEnableBackgroundDrawing(false);stationName.setTextColor(0x592424);stationName.setText(station==null?"Track Station":station.stationName);
        trainName=new GuiTextField(1,fontRenderer,left+23,top+47,125,10);trainName.setMaxStringLength(35);trainName.setEnableBackgroundDrawing(false);trainName.setTextColor(0xc6c6c6);trainName.setText(station==null?"Unnamed Train":station.trainName);centerFields();updateButtons();
    }
    private void centerFields(){stationName.x=left+100-(Math.min(fontRenderer.getStringWidth(stationName.getText()),180)+10)/2;trainName.x=left+100-(Math.min(fontRenderer.getStringWidth(trainName.getText()),125)+10)/2;}
    private void updateButtons(){var s=station();if(s==null)return;for(var b:buttonList){if(b.id==1)b.enabled=s.bogeyCount>0;if(b.id==4){b.enabled=false;b.visible=s.trainPresent;}if(!assembling&&b.id==0)b.visible=!s.trainPresent;if(!assembling&&b.id==2){b.visible=s.trainPresent;b.enabled=s.canDisassemble;}}}
    private void names(){if(stationName!=null)Create.NETWORK.sendToServer(new StationActionPacket(pos,5,stationName.getText()));if(trainName!=null&&!assembling)Create.NETWORK.sendToServer(new StationActionPacket(pos,6,trainName.getText()));}
    @Override public void onGuiClosed(){names();}
    @Override protected void actionPerformed(GuiButton button){if(button.id==3){mc.displayGuiScreen(null);return;}names();Create.NETWORK.sendToServer(new StationActionPacket(pos,button.id,""));}
    @Override public void updateScreen(){stationName.updateCursorCounter();trainName.updateCursorCounter();var s=station();if(s==null){mc.displayGuiScreen(null);return;}if(s.assembling!=assembling)initGui();updateButtons();}
    @Override public void drawScreen(int mouseX,int mouseY,float partial){
        GlStateManager.disableDepth();drawDefaultBackground();var s=station();if(s==null)return;
        sprite(assembling?"assemble":"schedule_2",left,top,0,assembling?0:111,200,panelHeight);
        if(assembling){drawCentered(text("assembly_title"),left+100,top+4,0x0E2233);
            if(s.lastError){drawCentered(text("failed"),left+97,top+47,0x775B5B);fontRenderer.drawSplitString(s.message,left+30,top+67,134,0x775B5B);int lines=fontRenderer.listFormattedStringToWidth(s.message,134).size();fontRenderer.drawSplitString(text("retry"),left+30,top+72+lines*9,134,0x7A7A7A);}
            else{drawCentered(text(s.bogeyCount==0?"no_bogeys":s.bogeyCount==1?"one_bogey":"more_bogeys",s.bogeyCount),left+97,top+47,0x7A7A7A);fontRenderer.drawSplitString(text("how_to"),left+28,top+62,134,0x7A7A7A);fontRenderer.drawSplitString(text("how_to_1"),left+28,top+94,134,0x7A7A7A);fontRenderer.drawSplitString(text("how_to_2"),left+28,top+117,138,0x7A7A7A);}
        }else{
            centerFields();if(stationName.isFocused())stationName.drawTextBox();else fontRenderer.drawString(fontRenderer.trimStringToWidth(stationName.getText(),180),stationName.x,top+4,0x592424);if(!stationName.isFocused())sprite("schedule_2",stationName.x+fontRenderer.getStringWidth(stationName.getText())+5,top+1,0,239,13,13);
            if(!s.trainPresent)drawCentered(text("idle"),left+97,top+47,0x7A7A7A);
            else{sprite("assemble",left+90,top+20,64,205,19,10);sprite("assemble",left+21,top+42,1,179,150,18);for(int i=0;i<26;i++)sprite("assemble",left+21,top+60+i,1,198,150,1);sprite("assemble",left+21,top+86,1,200,150,4);sprite("assemble",left+96,top+38,152,179,8,6);trainName.drawTextBox();if(!trainName.isFocused())sprite("schedule_2",Math.min(trainName.x+fontRenderer.getStringWidth(trainName.getText())+5,left+156),top+44,89,239,13,13);}
            mc.getTextureManager().bindTexture(new ResourceLocation("create","textures/item/train_door.png"));GlStateManager.color(1,1,1,1);drawModalRectWithCustomSizedTexture(left+14,top+103,0,0,16,16,16,16);
            String[] doors={"all","north","east","south","west","none"};fontRenderer.drawStringWithShadow(I18n.format("create.contraption.door_control."+doors[Math.floorMod(s.doorMode,6)]+".short"),left+39,top+108,0xffffff);
        }
        renderStation(s,partial);GlStateManager.disableDepth();super.drawScreen(mouseX,mouseY,partial);
        for(var button:buttonList)if(button instanceof CreateIconButton icon&&icon.hovering()&&!icon.tooltip.isEmpty())drawHoveringText(List.of(icon.tooltip),mouseX,mouseY);
    }
    private void drawCentered(String text,int x,int y,int color){fontRenderer.drawString(text,x-fontRenderer.getStringWidth(text)/2,y,color);}
    private void renderStation(TrainStationTile s,float partial){
        GlStateManager.enableDepth();GlStateManager.pushMatrix();GlStateManager.translate(left+204,top+panelHeight+4,100);GlStateManager.scale(40,-40,40);GlStateManager.rotate(22,1,0,0);GlStateManager.rotate(63,0,1,0);RenderHelper.enableGUIStandardItemLighting();mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);GlStateManager.color(1,1,1,1);
        var state=mc.world.getBlockState(pos);mc.getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(mc.getBlockRendererDispatcher().getModelForState(state),1,1,1,1);StationRenderer.flag(s,partial,true);RenderHelper.disableStandardItemLighting();GlStateManager.popMatrix();GlStateManager.color(1,1,1,1);
    }
    @Override protected void keyTyped(char c,int key)throws IOException{if(key==28||key==156){names();stationName.setFocused(false);trainName.setFocused(false);return;}if(!stationName.textboxKeyTyped(c,key)&&!trainName.textboxKeyTyped(c,key))super.keyTyped(c,key);centerFields();}
    @Override protected void mouseClicked(int x,int y,int button)throws IOException{super.mouseClicked(x,y,button);if(!assembling){stationName.mouseClicked(x,y,button);if(station().trainPresent)trainName.mouseClicked(x,y,button);if(y>top&&y<top+14&&x>left&&x<left+200)stationName.setFocused(true);}}
    @Override public void handleMouseInput()throws IOException{
        super.handleMouseInput();int wheel=org.lwjgl.input.Mouse.getEventDWheel();if(wheel==0||assembling)return;int x=org.lwjgl.input.Mouse.getEventX()*width/mc.displayWidth,y=height-org.lwjgl.input.Mouse.getEventY()*height/mc.displayHeight-1;
        if(x>=left+35&&x<left+88&&y>=top+102&&y<top+118){var s=station();Create.NETWORK.sendToServer(new StationActionPacket(pos,7+Math.max(0,Math.min(5,s.doorMode+Integer.signum(wheel))),""));}
    }
    @Override public boolean doesGuiPauseGame(){return false;}
}
