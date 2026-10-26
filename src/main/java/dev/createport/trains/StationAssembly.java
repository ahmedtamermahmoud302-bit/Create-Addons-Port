package dev.createport.trains;

import dev.createport.Create;
import dev.createport.glue.*;
import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;

/** Validates a world-built glued carriage before transfer; restoration refuses occupied positions. */
public final class StationAssembly {
    public static BlockPos local(BlockPos p,BlockPos origin,float yaw){
        double a=Math.toRadians(yaw),dx=p.getX()-origin.getX(),dz=p.getZ()-origin.getZ();
        return new BlockPos(Math.round(Math.cos(a)*dx+Math.sin(a)*dz),p.getY()-origin.getY(),Math.round(-Math.sin(a)*dx+Math.cos(a)*dz));
    }
    public static BlockPos world(BlockPos p,BlockPos origin,float yaw){
        double a=Math.toRadians(yaw);return origin.add(Math.round(Math.cos(a)*p.getX()-Math.sin(a)*p.getZ()),p.getY(),Math.round(Math.sin(a)*p.getX()+Math.cos(a)*p.getZ()));
    }
    private static Rotation rotation(float yaw,boolean inverse){return Rotation.values()[Math.floorMod(Math.round((inverse?-yaw:yaw)/90f),4)];}
    public static void assemble(TrainStationTile station,EntityPlayer player){
        var world=station.getWorld();var target=station.target;
        if(station.train()!=null){station.error(player,"This station already has an assembled train.");return;}
        var shape=TrainTrackBlock.pathShape(world.getBlockState(target));
        if(shape!=RailPath.Shape.EAST_WEST && shape!=RailPath.Shape.NORTH_SOUTH){station.error(player,"Assembly requires straight track.");return;}
        List<BlockPos> bogeys=new ArrayList<>();
        station.refresh();for(int i=1;i<=station.assemblyLength;i++){BlockPos p=target.offset(station.direction,i).up();if(Bogeys.is(world.getBlockState(p)))bogeys.add(p);}
        if(bogeys.isEmpty() || bogeys.size()>2){station.error(player,"Place one or two bogeys for this carriage using Train Casing on the track.");return;}
        if(!bogeys.get(0).equals(target.offset(station.direction).up())){station.error(player,"The frontmost bogey must be directly at the station marker.");return;}
        bogeys.sort(Comparator.comparingInt(p->station.offset(p.down())));
        BlockPos first=bogeys.get(0),track=first.down();var glue=GlueWorldData.get(world);
        Set<BlockPos> selected=new LinkedHashSet<>();ArrayDeque<BlockPos> pending=new ArrayDeque<>();pending.add(first);
        while(!pending.isEmpty()){
            BlockPos p=pending.remove();if(!selected.add(p))continue;
            if(selected.size()>256){station.error(player,"This carriage exceeds the 256-block assembly limit.");return;}
            if(!world.isBlockLoaded(p) || !world.isBlockModifiable(player,p) || !player.canPlayerEdit(p,EnumFacing.UP,player.getHeldItemMainhand())){station.error(player,"A block cannot be moved from this position.");return;}
            var state=world.getBlockState(p);boolean bogey=Bogeys.is(state);
            if(!state.getMaterial().isSolid() || state.getBlockHardness(world,p)<0 || state.getBlock().hasTileEntity(state) && !bogey){station.error(player,"This carriage cannot capture inventories, machines, or unbreakable blocks yet.");return;}
            if(state.getBlock()==Create.TRACK_STATION){station.error(player,"The station must remain outside the glued carriage.");return;}
            for(var connected:glue.neighbours(world,p))if(!GlueWorldData.block(connected).equals(p))pending.add(GlueWorldData.block(connected));
            if(bogey && !world.isAirBlock(p.up()))pending.add(p.up());
            if(Bogeys.is(world.getBlockState(p.down())))pending.add(p.down());
        }
        if(!selected.containsAll(bogeys)){station.error(player,"Glue the bodies above the bogeys into one connected carriage.");return;}
        if(selected.stream().noneMatch(p->world.getBlockState(p).getBlock()==Create.TRAIN_CONTROLS)){station.error(player,"Add Train Controls to the glued carriage.");return;}
        for(BlockPos p:bogeys){
            var below=world.getBlockState(p.down());
            if(!(below.getBlock() instanceof TrainTrackBlock) || TrainTrackBlock.pathShape(below)!=shape){station.error(player,"Each bogey needs matching straight track underneath it.");return;}
        }
        if(selected.stream().filter(p->world.getBlockState(p).getBlock()==Create.TRAIN_CONTROLS).anyMatch(p->world.getBlockState(p).getValue(RailwayBlock.FACING).getAxis()!=(shape==RailPath.Shape.EAST_WEST?EnumFacing.Axis.X:EnumFacing.Axis.Z))){station.error(player,"Rotate the Train Controls to face along the track.");return;}
        TrainEntity train=new TrainEntity(world);train.place(track,station.direction.getAxisDirection()==EnumFacing.AxisDirection.POSITIVE?1:-1);float yaw=train.rotationYaw;
        Map<BlockPos,IBlockState> source=new LinkedHashMap<>();List<TrainStructure.Part> parts=new ArrayList<>();
        for(BlockPos p:selected){BlockPos offset=local(p,first,yaw);if(Math.abs(offset.getX())>8 || offset.getY()<0 || offset.getY()>12 || Math.abs(offset.getZ())>24){station.error(player,"Carriage body is outside the supported 17 x 49 x 13 envelope.");return;}
            var state=world.getBlockState(p);source.put(p,state);parts.add(new TrainStructure.Part(offset.getX(),offset.getY(),offset.getZ(),state.withRotation(rotation(yaw,true))));}
        NBTTagCompound info=new NBTTagCompound();info.setLong("Station",station.getPos().toLong());info.setInteger("Dimension",world.provider.getDimension());info.setString("Name",station.trainName);NBTTagList bonds=new NBTTagList();
        for(var bond:glue.graph().bonds()){
            BlockPos a=GlueWorldData.block(bond.a()),b=GlueWorldData.block(bond.b());if(!selected.contains(a)||!selected.contains(b))continue;
            var n=new NBTTagCompound();n.setLong("A",local(a,first,yaw).toLong());n.setLong("B",local(b,first,yaw).toLong());bonds.appendTag(n);
        }info.setTag("Bonds",bonds);
        NBTTagList areas=new NBTTagList();for(var area:glue.areas())if(selected.contains(area.first())&&selected.contains(area.second())){var n=new NBTTagCompound();n.setLong("A",local(area.first(),first,yaw).toLong());n.setLong("B",local(area.second(),first,yaw).toLong());areas.appendTag(n);}info.setTag("GlueAreas",areas);
        train.setStationAssembly(info,parts);train.place(track,station.direction.getAxisDirection()==EnumFacing.AxisDirection.POSITIVE?1:-1);
        if(!world.getEntitiesWithinAABB(TrainEntity.class,train.getEntityBoundingBox()).isEmpty()){station.error(player,"Another train occupies the assembly area.");return;}
        if(!world.spawnEntity(train)){station.error(player,"The world rejected train assembly.");return;}
        for(var entry:source.entrySet())if(!world.getBlockState(entry.getKey()).equals(entry.getValue())){train.setDead();station.error(player,"The build changed during assembly; no blocks were moved.");return;}
        List<BlockPos> removed=new ArrayList<>();
        for(var entry:source.entrySet())if(world.setBlockState(entry.getKey(),Blocks.AIR.getDefaultState(),2))removed.add(entry.getKey());else{
            for(BlockPos p:removed)world.setBlockState(p,source.get(p),2);train.setDead();station.error(player,"Assembly was interrupted; source blocks restored.");return;}
        glue.captured(source.keySet());train.dock(station);
        station.assembling=false;station.status(player,"Assembled "+parts.size()+" blocks. Use the controls on the moving carriage to drive.");
    }
    public static void disassemble(TrainStationTile station,TrainEntity train,EntityPlayer player){
        var world=station.getWorld();if(Math.abs(train.speed())>.0001 || train.onCurve() || train.getPosition().distanceSq(station.target)>32*32){station.error(player,"Stop the train on straight track in this station's assembly area.");return;}
        BlockPos track=new BlockPos(train.posX,train.posY-.15,train.posZ);if(!(world.getBlockState(track).getBlock() instanceof TrainTrackBlock)){station.error(player,"There is no track below the stopped carriage.");return;}
        var shape=TrainTrackBlock.pathShape(world.getBlockState(track));if(shape!=RailPath.Shape.EAST_WEST && shape!=RailPath.Shape.NORTH_SOUTH){station.error(player,"Disassembly requires straight track.");return;}
        if(track.getY()!=station.target.getY() || station.direction.getAxis()==EnumFacing.Axis.X&&track.getZ()!=station.target.getZ() || station.direction.getAxis()==EnumFacing.Axis.Z&&track.getX()!=station.target.getX() || Math.abs(train.rotationPitch)>.001 || Math.abs(Math.sin(Math.toRadians(train.rotationYaw*2)))>.001){station.error(player,"Align the whole carriage with the station's straight track before disassembly.");return;}
        float yaw=(float)Math.toDegrees(Math.atan2(-station.direction.getXOffset(),station.direction.getZOffset()));BlockPos origin=station.target.offset(station.direction).up();Map<BlockPos,IBlockState> restore=new LinkedHashMap<>();
        for(var part:train.structure()){
            BlockPos p=world(new BlockPos(part.x(),part.y(),part.z()),origin,yaw);
            if(!world.isBlockLoaded(p) || !world.isBlockModifiable(player,p) || !player.canPlayerEdit(p,EnumFacing.UP,player.getHeldItemMainhand()) || !world.getBlockState(p).getBlock().isReplaceable(world,p)){station.error(player,"Clear the carriage footprint before disassembly; no blocks were replaced.");return;}
            restore.put(p,part.state().withRotation(rotation(yaw,false)));
        }
        Map<BlockPos,IBlockState> old=new LinkedHashMap<>();List<BlockPos> placed=new ArrayList<>();
        for(var entry:restore.entrySet()){
            old.put(entry.getKey(),world.getBlockState(entry.getKey()));
            if(!world.setBlockState(entry.getKey(),entry.getValue(),2)){for(BlockPos p:placed)world.setBlockState(p,old.get(p),2);station.error(player,"Disassembly interrupted; the train was retained.");return;}placed.add(entry.getKey());
        }
        var bonds=train.stationAssembly().getTagList("Bonds",10);var glue=GlueWorldData.get(world);
        for(int i=0;i<bonds.tagCount();i++){var n=bonds.getCompoundTagAt(i);glue.connect(world(BlockPos.fromLong(n.getLong("A")),origin,yaw),world(BlockPos.fromLong(n.getLong("B")),origin,yaw));}
        var areas=train.stationAssembly().getTagList("GlueAreas",10);for(int i=0;i<areas.tagCount();i++){var n=areas.getCompoundTagAt(i);glue.addArea(world(BlockPos.fromLong(n.getLong("A")),origin,yaw),world(BlockPos.fromLong(n.getLong("B")),origin,yaw));}
        train.removePassengers();train.setDead();station.assembling=true;station.status(player,"Disassembled into "+restore.size()+" world blocks. Glue bonds restored.");
    }
    private StationAssembly(){}
}
