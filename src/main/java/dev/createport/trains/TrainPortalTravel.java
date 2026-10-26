package dev.createport.trains;

import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.ITeleporter;

/** Whole-carriage transfer preserves the block palette, glue, riders and standing players. */
public final class TrainPortalTravel {
    private static final class Probe extends Entity {
        Probe(World world){super(world);setSize(.1f,.1f);}
        protected void entityInit(){}protected void readEntityFromNBT(NBTTagCompound n){}protected void writeEntityToNBT(NBTTagCompound n){}
    }
    private static PortalTrackLinks.Endpoint resolve(TrainEntity train,BlockPos rail,EnumFacing into){
        var source=(WorldServer)train.world;var links=PortalTrackLinks.get(source);var entry=new PortalTrackLinks.Endpoint(source.provider.getDimension(),rail,into);var linked=links.exit(entry);if(linked!=null)return linked;
        BlockPos portal=rail.offset(into);if(source.getBlockState(portal).getBlock()!=Blocks.PORTAL)return null;
        int current=source.provider.getDimension();if(current!=0&&current!=-1)return null;int targetId=current==-1?0:-1;var target=PortalTrackCommand.dimension(source.getMinecraftServer(),targetId);if(target==null||!source.getMinecraftServer().getAllowNether())return null;
        Probe probe=new Probe(source);probe.setPositionAndRotation(portal.getX()+.5,portal.getY(),portal.getZ()+.5,into.getHorizontalAngle(),0);probe.setPortal(portal);
        double scale=source.provider.getMovementFactor()/target.provider.getMovementFactor();probe.setWorld(target);probe.dimension=targetId;probe.setPosition(probe.posX*scale,probe.posY,probe.posZ*scale);
        if(!target.getDefaultTeleporter().placeInExistingPortal(probe,probe.rotationYaw))return null;
        PortalTrackLinks.Endpoint best=null;double bestDistance=Double.POSITIVE_INFINITY;BlockPos center=new BlockPos(probe);
        for(int x=-3;x<=3;x++)for(int y=-4;y<=4;y++)for(int z=-3;z<=3;z++){
            BlockPos p=center.add(x,y,z);var state=target.getBlockState(p);if(!(state.getBlock() instanceof TrainTrackBlock))continue;
            for(EnumFacing out:EnumFacing.Plane.HORIZONTAL){if(target.getBlockState(p.offset(out.getOpposite())).getBlock()!=Blocks.PORTAL)continue;var shape=TrainTrackBlock.pathShape(state);if(shape!=(out.getAxis()==EnumFacing.Axis.X?RailPath.Shape.EAST_WEST:RailPath.Shape.NORTH_SOUTH))continue;
                double distance=p.distanceSq(center);if(distance<bestDistance){bestDistance=distance;best=new PortalTrackLinks.Endpoint(targetId,p,out);}
            }
        }
        if(best!=null)links.connect(entry,best);return best;
    }
    private record Traveller(Entity entity,Vec3d local,boolean riding,boolean driver){}
    public static boolean transfer(TrainEntity train,BlockPos rail,EnumFacing into){
        if(train.portalDelay>0||train.world.isRemote)return false;var exit=resolve(train,rail,into);if(exit==null)return false;var destination=PortalTrackCommand.dimension(train.world.getMinecraftServer(),exit.dimension());if(destination==null)return false;
        var state=destination.getBlockState(exit.track());if(!(state.getBlock() instanceof TrainTrackBlock))return false;
        int sign=train.speed()<0?-1:1;int spacing=0;for(var part:train.structure())if(Bogeys.is(part.state()))spacing=Math.max(spacing,part.z());
        BlockPos root=exit.track().offset(exit.direction(),sign<0?spacing:0);int forward=exit.direction().getAxisDirection().getOffset()*sign;
        // Match the carriage's bounded chunk window before validating its far bogey/body.
        // The destination may have no player keeping those existing rails loaded.
        for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)destination.getChunk(root.add(x*16,0,z*16));
        var preview=new TrainEntity(destination);if(train.worldBuilt())preview.setStationAssembly(train.stationAssembly(),train.structure());preview.place(root,forward);if(!preview.routeFits())return false;
        List<Traveller> travellers=new ArrayList<>();for(var e:train.world.getEntitiesWithinAABB(Entity.class,train.visualBounds().grow(2)))if(e!=train&&!(e instanceof TrainEntity)&&!e.isDead&&(e.getRidingEntity()==train||TrainCollisions.supported(train,e,train.posX,train.posY,train.posZ,train.rotationYaw))){if(!ForgeHooks.onTravelToDimension(e,exit.dimension()))return false;travellers.add(new Traveller(e,train.toLocal(e.getPositionVector()),e.getRidingEntity()==train,e==train.driver()));}
        int control=train.controlIndex();float oldYaw=train.rotationYaw;ITeleporter place=(world,entity,yaw)->{entity.setWorld(destination);if(entity instanceof TrainEntity carriage){carriage.place(root,forward);carriage.portalDelay=80;carriage.clearDock();}};
        for(var t:travellers)if(t.riding)t.entity.dismountRidingEntity();
        var changed=train.changeDimension(exit.dimension(),place);if(!(changed instanceof TrainEntity moved)){for(var t:travellers)if(t.riding)t.entity.startRiding(train,true);return false;}
        moved.place(root,forward);moved.portalDelay=80;moved.clearDock();
        for(var traveller:travellers){var old=traveller.entity;Vec3d point=moved.toWorld(traveller.local);float facing=old.rotationYaw+MathHelper.wrapDegrees(moved.rotationYaw-oldYaw);
            ITeleporter arrival=(world,entity,yaw)->{entity.setWorld(destination);entity.setLocationAndAngles(point.x,point.y,point.z,facing,old.rotationPitch);};Entity arrived;
            if(old instanceof EntityPlayerMP player){player.getServer().getPlayerList().transferPlayerToDimension(player,exit.dimension(),arrival);player.connection.setPlayerLocation(point.x,point.y,point.z,facing,player.rotationPitch);arrived=player;}else arrived=old.changeDimension(exit.dimension(),arrival);
            if(arrived==null)continue;arrived.fallDistance=0;arrived.timeUntilPortal=80;if(traveller.riding)arrived.startRiding(moved,true);if(traveller.driver&&arrived instanceof EntityPlayerMP player)moved.startControlling(player,control);
        }
        return true;
    }
    private TrainPortalTravel(){}
}
