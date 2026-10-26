package dev.createport.client;
import dev.createport.Create;
import dev.createport.trains.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
@Mod.EventBusSubscriber(modid=Create.ID,value=Side.CLIENT)
public final class StationPlacementVisuals {
    @SubscribeEvent public static void render(RenderWorldLastEvent event){var mc=Minecraft.getMinecraft();if(mc.player==null||mc.world==null||mc.currentScreen!=null)return;var stack=mc.player.getHeldItemMainhand();if(!(stack.getItem() instanceof StationItem))return;BlockPos pos=null;EnumFacing direction=null;
        if(stack.hasTagCompound()&&stack.getTagCompound().hasKey("TrackTarget")){pos=BlockPos.fromLong(stack.getTagCompound().getLong("TrackTarget"));direction=EnumFacing.byIndex(stack.getTagCompound().getInteger("Direction"));}
        else if(mc.objectMouseOver!=null&&mc.objectMouseOver.typeOfHit==RayTraceResult.Type.BLOCK){pos=mc.objectMouseOver.getBlockPos();if(mc.world.getBlockState(pos).getBlock() instanceof TrainTrackBlock){var axis=TrackItem.axis(mc.world.getBlockState(pos));if(axis.dot(new TrackCurve.V(mc.player.getLookVec().x,mc.player.getLookVec().y,mc.player.getLookVec().z))>0)axis=axis.scale(-1);direction=EnumFacing.getFacingFromVector((float)axis.x(),0,(float)axis.z());}}
        if(pos==null||direction==null||!(mc.world.getBlockState(pos).getBlock() instanceof TrainTrackBlock))return;WorldVisuals.begin(event.getPartialTicks());try{StationRenderer.overlay(pos,direction,"station",0xffffff);}finally{WorldVisuals.end();}
    }
}
