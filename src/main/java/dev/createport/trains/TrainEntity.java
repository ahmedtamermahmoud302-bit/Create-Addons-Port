package dev.createport.trains;

import dev.createport.Create;
import dev.createport.trains.RailNavigator.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import java.util.HashSet;
import java.util.Set;

/** Server-owned carriage: saved rail cursor, movable block palette and station restoration. */
public final class TrainEntity extends Entity {
    private static final DataParameter<NBTTagCompound> STRUCTURE=EntityDataManager.createKey(TrainEntity.class,DataSerializers.COMPOUND_TAG);
    private NBTTagCompound structureTag;
    private java.util.List<TrainStructure.Part> parts=java.util.List.of();
    private static final DataParameter<Float> SPEED=EntityDataManager.createKey(TrainEntity.class,DataSerializers.FLOAT);
    private static final DataParameter<Float> WHEELS=EntityDataManager.createKey(TrainEntity.class,DataSerializers.FLOAT);
    private ForgeChunkManager.Ticket ticket;
    private static final DataParameter<Float> FRONT_YAW=EntityDataManager.createKey(TrainEntity.class,DataSerializers.FLOAT);
    private static final DataParameter<Float> REAR_YAW=EntityDataManager.createKey(TrainEntity.class,DataSerializers.FLOAT);
    private NBTTagCompound stationData=new NBTTagCompound();
    private static final DataParameter<Integer> CONTROL_INDEX=EntityDataManager.createKey(TrainEntity.class,DataSerializers.VARINT);
    private static final DataParameter<Boolean> WORLD_BUILT=EntityDataManager.createKey(TrainEntity.class,DataSerializers.BOOLEAN);
    private Cursor cursor;
    private java.util.UUID curveId;private double curveDistance;private int curveForward=1;
    private double speed,cruise;
    private int input,lastInput=-100;
    private boolean brake;
    private double targetX,targetY,targetZ;
    private float targetYaw,targetPitch;
    private int interpolation;
    public TrainEntity(World world) { super(world);setSize(1.8f,1.7f);noClip=false; }
    @Override protected void entityInit() { dataManager.register(CONTROL_INDEX,-1);dataManager.register(FRONT_YAW,0f);dataManager.register(REAR_YAW,0f);dataManager.register(SPEED,0f);dataManager.register(WHEELS,0f);dataManager.register(STRUCTURE,new NBTTagCompound());dataManager.register(WORLD_BUILT,false); }
    @Override public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!world.isRemote && ticket==null) {
            ForgeChunkManager.Ticket requested=ForgeChunkManager.requestTicket(Create.instance,world,ForgeChunkManager.Type.ENTITY);
            if (requested!=null) { requested.bindEntity(this);acceptTicket(requested); }
        }
    }
    public void acceptTicket(ForgeChunkManager.Ticket restored) {
        if (ticket!=null && ticket!=restored) ForgeChunkManager.releaseTicket(ticket);
        ticket=restored;updateChunks();
    }
    private void updateChunks() {
        if (ticket==null) return;
        int chunkX=MathHelper.floor(posX)>>4,chunkZ=MathHelper.floor(posZ)>>4;
        Set<ChunkPos> wanted=new HashSet<>();
        for(int x=-2;x<=2;x++) for(int z=-2;z<=2;z++) wanted.add(new ChunkPos(chunkX+x,chunkZ+z));
        for (ChunkPos chunk:new HashSet<>(ticket.getChunkList())) if (!wanted.contains(chunk)) ForgeChunkManager.unforceChunk(ticket,chunk);
        for (ChunkPos chunk:wanted) if (!ticket.getChunkList().contains(chunk)) ForgeChunkManager.forceChunk(ticket,chunk);
    }
    @Override public void setDead() {
        if (ticket!=null) { ForgeChunkManager.releaseTicket(ticket);ticket=null; }
        super.setDead();
    }
    public java.util.List<TrainStructure.Part> structure() {
        NBTTagCompound tag=dataManager.get(STRUCTURE);
        if (tag!=structureTag) { structureTag=tag;parts=TrainStructure.read(tag); }
        return parts;
    }
    public void setStructure(java.util.List<TrainStructure.Part> value) { dataManager.set(STRUCTURE,TrainStructure.write(value));resizeStructure(); }
    @Override public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);if (STRUCTURE.equals(key))resizeStructure();
    }
    private void resizeStructure() {
        float width=1.8f,height=1.7f;
        for (TrainStructure.Part part:structure()) {
            width=Math.max(width,2*Math.max(Math.abs(part.x())+.5f,Math.abs(part.z())+.5f));
            height=Math.max(height,(worldBuilt()?1.85f:2.3f)+part.y());
        }
        setSize(width,height);
    }
    public boolean belongsTo(BlockPos station){return stationData.hasKey("Station") && stationData.getLong("Station")==station.toLong();}
    public boolean worldBuilt(){return dataManager.get(WORLD_BUILT);}
    public NBTTagCompound stationAssembly(){return stationData.copy();}
    public boolean onCurve(){return curveId!=null;}
    public void setStationAssembly(NBTTagCompound info,java.util.List<TrainStructure.Part> parts){stationData=info.copy();dataManager.set(WORLD_BUILT,true);setStructure(parts);setCustomNameTag(info.getString("Name"));}
    public double bodyOffset(){return worldBuilt()?1.15:1.6;}
    private TrainStructure.Part controller(){
        var parts=structure();int controlIndex=dataManager.get(CONTROL_INDEX);if(controlIndex>=0 && controlIndex<parts.size() && parts.get(controlIndex).state().getBlock()==Create.TRAIN_CONTROLS)return parts.get(controlIndex);
        for(var p:parts)if(p.state().getBlock()==Create.TRAIN_CONTROLS)return p;return null;
    }
    public float speed() { return dataManager.get(SPEED); }
    public float wheels() { return dataManager.get(WHEELS); }
    private RailPath.Shape track(Cell cell) {
        BlockPos p=new BlockPos(cell.x(),cell.y(),cell.z());
        if (!world.isBlockLoaded(p) || !(world.getBlockState(p).getBlock() instanceof TrainTrackBlock)) return null;
        return TrainTrackBlock.pathShape(world.getBlockState(p));
    }
    public void place(BlockPos pos,int forward) {
        cursor=new Cursor(new Cell(pos.getX(),pos.getY(),pos.getZ()),.5,forward);align();
    }
    public void control(int input,boolean brake) { var controls=controller();if(worldBuilt() && controls!=null){var face=controls.state().getValue(RailwayBlock.FACING);if(face.getAxis()==EnumFacing.Axis.Z)input*=-face.getZOffset();} this.input=input;this.brake=brake;lastInput=ticksExisted;cruise=0; }
    public void cruise(double value) { cruise=Math.max(-1,Math.min(1,value));input=0;lastInput=-100; }
    private TrackCurve.V routePosition(){
        if(curveId!=null){var node=CurveGraphData.get(world).node(world,curveId);if(node!=null)return node.curve().point(node.curve().parameterAtDistance(curveDistance));}
        var p=RailNavigator.position(cursor,this::track);return new TrackCurve.V(p.x(),p.y(),p.z());
    }
    private TrackCurve.V routeTangent(){
        if(curveId!=null){var c=CurveGraphData.get(world).node(world,curveId).curve();return c.tangent(c.parameterAtDistance(curveDistance)).scale(curveForward);}
        var t=RailPath.of(track(cursor.cell())).tangent(cursor.progress());return new TrackCurve.V(t.x(),t.y(),t.z()).scale(cursor.forward());
    }
    private record RouteState(Cursor cursor,java.util.UUID curve,double distance,int forward){}
    private RouteState routeState(){return new RouteState(cursor,curveId,curveDistance,curveForward);}
    private void restoreRoute(RouteState state){cursor=state.cursor;curveId=state.curve;curveDistance=state.distance;curveForward=state.forward;}
    private record Pose(TrackCurve.V position,TrackCurve.V tangent,TrackCurve.V frontTangent,boolean supported){}
    private Pose pose(){
        var p=routePosition();var tangent=routeTangent();double spacing=0;
        if(worldBuilt())for(var part:structure())if(part.state().getBlock()==Create.SMALL_BOGEY)spacing=Math.max(spacing,part.z());
        if(spacing<.01)return new Pose(p,tangent,tangent,true);
        var old=routeState();double lo=spacing,hi=Math.min(64,spacing*1.8);TrackCurve.V front=tangent;boolean supported=false;
        // The body's bogey spacing is a chord, while the rail cursor advances by arc length.
        // Solve the chord constraint so the second bogey stays on the rail through a turn.
        try{
            for(int i=0;i<15;i++){
                double distance=i==0?lo:(lo+hi)/2;restoreRoute(old);var result=travel(distance);var q=routePosition();double chord=q.sub(p).length();
                if(result.blocked() && chord<spacing-1e-3){hi=distance;continue;}
                if(Math.abs(chord-spacing)<1e-4){tangent=q.sub(p).unit();front=routeTangent();supported=true;break;}
                if(chord<spacing)lo=distance;else hi=distance;
                if(i==14 && Math.abs(chord-spacing)<.005){tangent=q.sub(p).unit();front=routeTangent();supported=true;}
            }
        }finally{restoreRoute(old);}
        return new Pose(p,tangent,front,supported);
    }
    private static float yaw(TrackCurve.V tangent){return (float)Math.toDegrees(Math.atan2(-tangent.x(),tangent.z()));}
    public float bogeyYaw(boolean front){return dataManager.get(front?FRONT_YAW:REAR_YAW);}
    private void align(){align(pose());}
    private void align(Pose pose){
        var p=pose.position;var tangent=pose.tangent;
        setPosition(p.x(),p.y()+.15,p.z());rotationYaw=yaw(tangent);
        rotationPitch=(float)-Math.toDegrees(Math.atan2(tangent.y(),Math.hypot(tangent.x(),tangent.z())));
        dataManager.set(REAR_YAW,MathHelper.wrapDegrees(yaw(routeTangent())-rotationYaw));
        dataManager.set(FRONT_YAW,MathHelper.wrapDegrees(yaw(pose.frontTangent)-rotationYaw));
    }
    private boolean bodyObstructed(Pose pose){
        if(!worldBuilt())return !world.getCollisionBoxes(this,getEntityBoundingBox().offset(pose.position.x()-posX,pose.position.y()+.15-posY,pose.position.z()-posZ)).isEmpty();
        double a=Math.toRadians(yaw(pose.tangent)),cos=Math.cos(a),sin=Math.sin(a);
        for(var part:structure()){
            double x=pose.position.x()+cos*part.x()-sin*part.z(),z=pose.position.z()+sin*part.x()+cos*part.z();
            double bottom=pose.position.y()+1+part.y();
            if(part.state().getBlock()==Create.SMALL_BOGEY)bottom+=.4375;
            // Use each carried block's actual world footprint, not the carriage's large selection box.
            double half=(Math.abs(cos)+Math.abs(sin))*.48;
            if(!world.getCollisionBoxes(this,new AxisAlignedBB(x-half,bottom+.01,z-half,x+half,pose.position.y()+2+part.y()-.01,z+half)).isEmpty())return true;
        }return false;
    }
    private record RouteMove(boolean blocked,double distance){}
    private RouteMove travel(double distance){
        int sign=distance<0?-1:1;double remaining=Math.abs(distance),moved=0;
        for(int transitions=0;transitions<64;transitions++){
            if(curveId!=null){
                var node=CurveGraphData.get(world).node(world,curveId);if(node==null)return new RouteMove(true,moved*sign);
                var c=node.curve();int direction=curveForward*sign;double available=direction>0?c.distanceLength()-curveDistance:curveDistance;
                double take=Math.min(remaining,Math.max(0,available));curveDistance+=direction*take;remaining-=take;moved+=take;
                if(remaining<1e-8)return new RouteMove(false,moved*sign);
                boolean first=direction<0;var anchor=node.anchor(first);var shape=track(new Cell(anchor.getX(),anchor.getY(),anchor.getZ()));
                if(shape==null)return new RouteMove(true,moved*sign);
                var endpoint=first?c.a:c.b;var path=RailPath.of(shape);int entry=0;double best=Double.POSITIVE_INFINITY;
                for(int i=0;i<2;i++){var p=path.sample(i);double d=new TrackCurve.V(p.x()+anchor.getX(),p.y()+anchor.getY(),p.z()+anchor.getZ()).sub(endpoint).length();if(d<best){best=d;entry=i;}}
                if(best>1e-4)return new RouteMove(true,moved*sign);
                cursor=new Cursor(new Cell(anchor.getX(),anchor.getY(),anchor.getZ()),entry,(entry==0?1:-1)*sign);curveId=null;
            }else{
                var next=RailNavigator.advance(cursor,sign*remaining,this::track);cursor=next.cursor();moved+=Math.abs(next.distance());remaining-=Math.abs(next.distance());
                if(!next.blocked())return new RouteMove(false,moved*sign);
                var p=RailNavigator.position(cursor,this::track);var point=new TrackCurve.V(p.x(),p.y(),p.z());var node=CurveTrackEntity.at(world,point);
                if(node==null)return new RouteMove(true,moved*sign);
                boolean first=node.curve().a.sub(point).length()<1e-5;curveId=node.getUniqueID();curveDistance=first?0:node.curve().distanceLength();curveForward=(first?1:-1)*sign;
            }
        }
        return new RouteMove(true,moved*sign);
    }
    @Override public void onUpdate() {
        super.onUpdate();prevPosX=posX;prevPosY=posY;prevPosZ=posZ;prevRotationYaw=rotationYaw;prevRotationPitch=rotationPitch;
        if (world.isRemote) {
            if (interpolation>0) {
                setPosition(posX+(targetX-posX)/interpolation,posY+(targetY-posY)/interpolation,posZ+(targetZ-posZ)/interpolation);
                rotationYaw+=MathHelper.wrapDegrees(targetYaw-rotationYaw)/interpolation;
                rotationPitch+=(targetPitch-rotationPitch)/interpolation;interpolation--;
            }
            return;
        }
        if (cursor==null) {
            BlockPos p=new BlockPos(posX,posY,posZ);
            if (!(world.getBlockState(p).getBlock() instanceof TrainTrackBlock)) p=p.down();
            if (!(world.getBlockState(p).getBlock() instanceof TrainTrackBlock)) { stop();return; }
            place(p,1);
        }
        if (curveId==null && track(cursor.cell())==null || curveId!=null && CurveGraphData.get(world).node(world,curveId)==null) { stop();return; }
        boolean driven=getControllingPassenger() instanceof EntityPlayer;
        boolean recent=driven && ticksExisted-lastInput<=10;
        speed=TrainMotion.approach(speed,recent ? input:cruise,recent ? brake:driven);
        var old=routeState();RouteMove next=travel(speed);var pose=pose();
        AxisAlignedBB future=getEntityBoundingBox().offset(pose.position.x()-posX,pose.position.y()+.15-posY,pose.position.z()-posZ);
        boolean occupied=!world.getEntitiesWithinAABB(TrainEntity.class,future.grow(.05),e -> e!=this && !e.isDead).isEmpty();
        if(!pose.supported || occupied || bodyObstructed(pose)){restoreRoute(old);stop();return;}
        align(pose);updateChunks();
        dataManager.set(WHEELS,(float)((wheels()+TrainMotion.wheelAngle(next.distance(),TrainMotion.WHEEL_RADIUS))%360));
        if (next.blocked()) { speed=0;cruise=0; }
        dataManager.set(SPEED,(float)speed);
        motionX=posX-prevPosX;motionY=posY-prevPosY;motionZ=posZ-prevPosZ;
    }
    private void stop() { speed=0;cruise=0;dataManager.set(SPEED,0f);motionX=motionY=motionZ=0; }
    @Override public void setPositionAndRotationDirect(double x,double y,double z,float yaw,float pitch,int steps,boolean teleport) {
        targetX=x;targetY=y;targetZ=z;targetYaw=yaw;targetPitch=pitch;interpolation=Math.max(1,steps);
    }
    @Override public EnumActionResult applyPlayerInteraction(EntityPlayer player,Vec3d hit,EnumHand hand){
        if(!worldBuilt())return EnumActionResult.PASS;
        double yaw=Math.toRadians(rotationYaw),cos=Math.cos(yaw),sin=Math.sin(yaw);
        Vec3d eye=player.getPositionEyes(1).subtract(posX,posY,posZ),look=player.getLookVec().scale(6);
        Vec3d from=new Vec3d(cos*eye.x+sin*eye.z,eye.y,-sin*eye.x+cos*eye.z);
        Vec3d to=from.add(cos*look.x+sin*look.z,look.y,-sin*look.x+cos*look.z);
        var parts=structure();int selected=-1;double nearest=from.distanceTo(to)+1e-6;
        var worldHit=world.rayTraceBlocks(player.getPositionEyes(1),player.getPositionEyes(1).add(look),false,true,false);
        if(worldHit!=null)nearest=Math.min(nearest,player.getPositionEyes(1).distanceTo(worldHit.hitVec));
        for(int i=0;i<parts.size();i++){
            var part=parts.get(i);double bottom=.85+part.y();if(part.state().getBlock()==Create.SMALL_BOGEY)bottom+=.4375;
            var box=new AxisAlignedBB(part.x()-.5,bottom,part.z()-.5,part.x()+.5,1.85+part.y(),part.z()+.5);
            var intersect=box.calculateIntercept(from,to);double distance=box.contains(from)?0:intersect==null?Double.POSITIVE_INFINITY:from.distanceTo(intersect.hitVec);
            if(distance<nearest){nearest=distance;selected=i;}
        }
        if(selected>=0 && parts.get(selected).state().getBlock()==Create.TRAIN_CONTROLS){
            if(!world.isRemote && !player.isSpectator() && !player.isSneaking() && getPassengers().isEmpty()){
                dataManager.set(CONTROL_INDEX,selected);player.startRiding(this);
            }return EnumActionResult.SUCCESS;
        }return EnumActionResult.PASS;
    }
    @Override public boolean processInitialInteract(EntityPlayer player,EnumHand hand) {
        if(worldBuilt() || player.isSneaking())return false;
        if(!world.isRemote && getPassengers().isEmpty())player.startRiding(this);return true;
    }
    @Override public void updatePassenger(Entity passenger){
        if(!isPassenger(passenger))return;var control=controller();
        if(!worldBuilt() || control==null){super.updatePassenger(passenger);return;}
        var face=control.state().getValue(RailwayBlock.FACING);double x=control.x()+face.getXOffset()*.85,z=control.z()+face.getZOffset()*.85,a=Math.toRadians(rotationYaw);
        passenger.setPosition(posX+Math.cos(a)*x-Math.sin(a)*z,posY+.85+control.y()+passenger.getYOffset(),posZ+Math.sin(a)*x+Math.cos(a)*z);
    }
    @Override public Entity getControllingPassenger() { return getPassengers().isEmpty() ? null:getPassengers().get(0); }
    // Only the server moves this entity; vanilla vehicle prediction would overwrite its rail cursor.
    @Override public boolean canPassengerSteer() { return false; }
    @Override protected boolean canFitPassenger(Entity passenger) { return getPassengers().isEmpty(); }
    @Override public double getMountedYOffset() {
        double seat=1.3;for (TrainStructure.Part part:structure())seat=Math.max(seat,2.3+part.y());return seat;
    }
    @Override public boolean canBeCollidedWith() { return !isDead && !worldBuilt(); }
    @Override public boolean canBePushed() { return false; }
    @Override public boolean attackEntityFrom(DamageSource source,float amount) { return false; }
    @Override protected void writeEntityToNBT(NBTTagCompound nbt) {
        if (cursor!=null) {
            nbt.setInteger("TrackX",cursor.cell().x());nbt.setInteger("TrackY",cursor.cell().y());nbt.setInteger("TrackZ",cursor.cell().z());
            nbt.setDouble("Progress",cursor.progress());nbt.setInteger("Forward",cursor.forward());
        }
        if(curveId!=null){nbt.setUniqueId("CurveId",curveId);nbt.setDouble("CurveDistance",curveDistance);nbt.setInteger("CurveForward",curveForward);}
        nbt.setTag("StationAssembly",stationData.copy());nbt.setBoolean("WorldBuilt",worldBuilt());
        nbt.setTag("Structure",dataManager.get(STRUCTURE).copy());
        nbt.setDouble("TrainSpeed",speed);nbt.setDouble("Cruise",cruise);nbt.setFloat("WheelAngle",wheels());
    }
    @Override protected void readEntityFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("TrackX")) cursor=new Cursor(new Cell(nbt.getInteger("TrackX"),nbt.getInteger("TrackY"),nbt.getInteger("TrackZ")),
                finiteClamp(nbt.getDouble("Progress"),0,1),nbt.getInteger("Forward")==-1 ? -1:1);
        curveId=nbt.hasUniqueId("CurveId")?nbt.getUniqueId("CurveId"):null;curveDistance=Math.max(0,nbt.getDouble("CurveDistance"));curveForward=nbt.getInteger("CurveForward")==-1?-1:1;
        stationData=nbt.getCompoundTag("StationAssembly").copy();dataManager.set(WORLD_BUILT,nbt.getBoolean("WorldBuilt"));
        setStructure(TrainStructure.read(nbt.getCompoundTag("Structure")));
        speed=finiteClamp(nbt.getDouble("TrainSpeed"),-TrainMotion.MAX_SPEED,TrainMotion.MAX_SPEED);
        cruise=finiteClamp(nbt.getDouble("Cruise"),-1,1);dataManager.set(SPEED,(float)speed);
        dataManager.set(WHEELS,(float)finiteClamp(nbt.getFloat("WheelAngle"),-360,360));
    }
    private static double finiteClamp(double value,double min,double max) { return Double.isFinite(value) ? Math.max(min,Math.min(max,value)):0; }
}
