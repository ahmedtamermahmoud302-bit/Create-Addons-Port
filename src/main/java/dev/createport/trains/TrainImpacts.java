package dev.createport.trains;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
/** Source threshold, relative-speed damage, monster multiplier and knockback. */
public final class TrainImpacts {
    public static void collide(TrainEntity train,double x,double y,double z,float yaw){
        if(train.world.isRemote||Math.abs(train.speed())<=.35)return;
        Vec3d movement=new Vec3d(train.posX-x,train.posY-y,train.posZ-z);if(movement.length()<=.35)return;
        for(var entity:train.world.getEntitiesWithinAABB(Entity.class,train.visualBounds().union(train.visualBounds().offset(x-train.posX,y-train.posY,z-train.posZ)))){
            if(entity==train||entity instanceof TrainEntity||entity instanceof EntityItem||entity.isDead||entity.getRidingEntity()==train||TrainCollisions.supported(train,entity,x,y,z,yaw)||entity instanceof EntityPlayer p&&(p.isCreative()||p.isSpectator()))continue;
            boolean struck=false;int samples=Math.max(1,(int)Math.ceil(movement.length()/.2));
            for(int i=0;i<=samples&&!struck;i++)for(var box:train.localCollisions()){double t=i/(double)samples;var clipped=PlatformGeometry.clip(TrainCollisions.box(box),x+(train.posX-x)*t,y+(train.posY-y)*t,z+(train.posZ-z)*t,(float)(yaw+MathHelper.wrapDegrees(train.rotationYaw-yaw)*t),TrainCollisions.box(entity.getEntityBoundingBox()));if(clipped!=null){struck=true;break;}}
            if(!struck)continue;var relative=movement.subtract(entity.motionX,entity.motionY,entity.motionZ);double damage=relative.length()*(entity instanceof IMob?2:1);if(relative.length()<=.35)continue;
            if(entity.attackEntityFrom(new EntityDamageSource("create.train",train),(int)(damage*16))){var push=movement.normalize().add(0,.25,0).scale(damage*4).add(relative);if(push.length()>3)push=push.normalize().scale(3);entity.addVelocity(push.x,push.y,push.z);entity.velocityChanged=true;train.world.playSound(null,entity.posX,entity.posY,entity.posZ,net.minecraft.init.SoundEvents.ENTITY_PLAYER_ATTACK_CRIT,SoundCategory.NEUTRAL,1,.75f);}
        }
    }
    private TrainImpacts(){}
}
