package dev.createport.trains;

import dev.createport.Create;
import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.util.math.*;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Physical carried blocks and rail surfaces, on both sides of the connection. */
@Mod.EventBusSubscriber(modid=Create.ID)
public final class TrainCollisions {
    public static PlatformGeometry.Box box(AxisAlignedBB b){return new PlatformGeometry.Box(b.minX,b.minY,b.minZ,b.maxX,b.maxY,b.maxZ);}
    public static AxisAlignedBB box(PlatformGeometry.Box b){return new AxisAlignedBB(b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ());}
    @SubscribeEvent public static void boxes(GetCollisionBoxesEvent event){
        var entity=event.getEntity();if(entity instanceof TrainEntity||entity!=null&&(entity.noClip||entity.isRiding()))return;
        var query=event.getAabb();var result=event.getCollisionBoxesList();
        for(var train:event.getWorld().getEntitiesWithinAABB(TrainEntity.class,query.grow(32))){
            if(!train.worldBuilt()||train.isDead||!train.visualBounds().intersects(query))continue;
            for(var local:train.localCollisions()){
                var clipped=PlatformGeometry.clip(box(local),train.posX,train.posY,train.posZ,train.rotationYaw,box(query));if(clipped!=null)result.add(box(clipped));
            }
        }
        for(var curve:TrackSurfaces.curves(event.getWorld()))TrackSurfaces.collisions(curve,query,result);
    }
    public static boolean supported(TrainEntity train,Entity entity,double x,double y,double z,float yaw){return Double.isFinite(supportHeight(train,entity,x,y,z,yaw));}
    private static double supportHeight(TrainEntity train,Entity entity,double x,double y,double z,float yaw){
        if(entity.isRiding()||entity.noClip||entity.motionY>.12)return Double.NaN;
        var feet=entity.getEntityBoundingBox();var query=new AxisAlignedBB(feet.minX+.02,feet.minY-.18,feet.minZ+.02,feet.maxX-.02,feet.minY+.03,feet.maxZ-.02);
        for(var local:train.localCollisions()){
            double top=y+local.maxY;if(Math.abs(top-feet.minY)>.18)continue;
            if(PlatformGeometry.clip(box(local),x,y,z,yaw,box(query))!=null)return top;
        }return Double.NaN;
    }
    public static void carry(TrainEntity train,double x,double y,double z,float yaw){
        double turn=net.minecraft.util.math.MathHelper.wrapDegrees(train.rotationYaw-yaw);
        for(var entity:train.world.getEntitiesWithinAABB(Entity.class,train.visualBounds().grow(2))){
            if(entity==train||entity instanceof TrainEntity||entity.isDead||entity.isRiding()||entity instanceof EntityPlayerMP||!supported(train,entity,x,y,z,yaw))continue;
            // Server owns mobs; the client owns its walking player's position, as in Create.
            if(train.world.isRemote&&!(entity instanceof EntityPlayer player&&player.isUser()))continue;
            var local=PlatformGeometry.local(entity.posX,entity.posZ,x,z,yaw);var next=PlatformGeometry.world(local.x(),local.z(),train.posX,train.posZ,train.rotationYaw);
            double dx=next.x()-entity.posX,dy=train.posY-y+supportHeight(train,entity,x,y,z,yaw)-entity.getEntityBoundingBox().minY+1e-7,dz=next.z()-entity.posZ;
            entity.move(MoverType.SELF,dx,dy,dz);entity.onGround=true;entity.fallDistance=0;entity.motionY=0;
            entity.rotationYaw+=turn;entity.prevRotationYaw+=turn;if(entity instanceof EntityLivingBase living){living.rotationYawHead+=turn;living.prevRotationYawHead+=turn;}
        }
    }
    private static final class FlightFields {
        static final java.lang.reflect.Field COUNT=net.minecraftforge.fml.relauncher.ReflectionHelper.findField(net.minecraft.network.NetHandlerPlayServer.class,"floatingTickCount","field_147365_f");
        static final java.lang.reflect.Field FLAG=net.minecraftforge.fml.relauncher.ReflectionHelper.findField(net.minecraft.network.NetHandlerPlayServer.class,"floating","field_184344_B");
    }
    @SubscribeEvent public static void playerTick(net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent event){
        if(event.phase!=net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END||!(event.player instanceof EntityPlayerMP player))return;
        for(var train:player.world.getEntitiesWithinAABB(TrainEntity.class,player.getEntityBoundingBox().grow(32)))if(train.worldBuilt()&&supported(train,player,train.posX,train.posY,train.posZ,train.rotationYaw)){
            player.onGround=true;player.fallDistance=0;player.motionY=0;
            try{FlightFields.COUNT.setInt(player.connection,0);FlightFields.FLAG.setBoolean(player.connection,false);}catch(IllegalAccessException error){throw new IllegalStateException("Cannot report a train-supported player to the server",error);}return;
        }
    }
    private TrainCollisions(){}
}
