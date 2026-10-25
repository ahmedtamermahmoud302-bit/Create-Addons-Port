package dev.createport.client;

import dev.createport.Create;
import dev.createport.trains.TrainEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.relauncher.Side;

/** Pick carried blocks individually, leaving empty space around a carriage usable. */
@Mod.EventBusSubscriber(modid=Create.ID,value=Side.CLIENT)
public final class TrainPicking {
    @SubscribeEvent public static void render(RenderWorldLastEvent event){pick(event.getPartialTicks());}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void mouse(MouseEvent event){
        if(event.getButton()<0)return;pick(1);var mc=Minecraft.getMinecraft();
        if(event.getButton()==1 && event.isButtonstate() && mc.currentScreen==null && mc.player!=null && !mc.player.isSpectator()
                && mc.objectMouseOver!=null && mc.objectMouseOver.entityHit instanceof TrainEntity train && train.worldBuilt()){
            Create.NETWORK.sendToServer(new dev.createport.trains.TrainInteractPacket(train.getEntityId()));event.setCanceled(true);
        }
    }
    private static void pick(float partial){
        var mc=Minecraft.getMinecraft();var viewer=mc.getRenderViewEntity();
        if(mc.world==null || viewer==null || mc.currentScreen!=null)return;
        double reach=mc.playerController.getBlockReachDistance();Vec3d eye=viewer.getPositionEyes(partial),end=eye.add(viewer.getLook(partial).scale(reach));
        RayTraceResult best=viewer.rayTrace(reach,partial);double distance=best==null?reach:eye.distanceTo(best.hitVec);
        // Preserve vanilla entities when they are closer than a carried block.
        if(mc.objectMouseOver!=null && mc.objectMouseOver.typeOfHit==RayTraceResult.Type.ENTITY && (!(mc.objectMouseOver.entityHit instanceof TrainEntity t) || !t.worldBuilt())){
            double d=eye.distanceTo(mc.objectMouseOver.hitVec);if(d<distance){best=mc.objectMouseOver;distance=d;}
        }
        for(var train:mc.world.getEntitiesWithinAABB(TrainEntity.class,new AxisAlignedBB(eye,end).grow(32))){
            if(!train.worldBuilt() || train.isDead || viewer.getRidingEntity()==train)continue;
            double angle=Math.toRadians(train.rotationYaw),cos=Math.cos(angle),sin=Math.sin(angle);
            Vec3d delta=eye.subtract(train.posX,train.posY,train.posZ),deltaEnd=end.subtract(train.posX,train.posY,train.posZ);
            Vec3d from=new Vec3d(cos*delta.x+sin*delta.z,delta.y,-sin*delta.x+cos*delta.z);
            Vec3d to=new Vec3d(cos*deltaEnd.x+sin*deltaEnd.z,deltaEnd.y,-sin*deltaEnd.x+cos*deltaEnd.z);
            for(var part:train.structure()){
                double bottom=.85+part.y();if(part.state().getBlock()==Create.SMALL_BOGEY)bottom+=.4375;
                var box=new AxisAlignedBB(part.x()-.5,bottom,part.z()-.5,part.x()+.5,1.85+part.y(),part.z()+.5);
                var hit=box.calculateIntercept(from,to);if(hit==null)continue;
                double d=from.distanceTo(hit.hitVec);if(d>=distance)continue;
                Vec3d p=hit.hitVec;best=new RayTraceResult(train,new Vec3d(train.posX+cos*p.x-sin*p.z,train.posY+p.y,train.posZ+sin*p.x+cos*p.z));distance=d;
            }
        }
        mc.objectMouseOver=best;mc.pointedEntity=best!=null && best.typeOfHit==RayTraceResult.Type.ENTITY?best.entityHit:null;
    }
}
