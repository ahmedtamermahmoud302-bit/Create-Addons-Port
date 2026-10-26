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
    private static final DataParameter<Integer> SEAT_INDEX=EntityDataManager.createKey(TrainEntity.class,DataSerializers.VARINT);
    private static final DataParameter<Integer> CONTROL_INDEX=EntityDataManager.createKey(TrainEntity.class,DataSerializers.VARINT);
    private static final DataParameter<Boolean> WORLD_BUILT=EntityDataManager.createKey(TrainEntity.class,DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> DRIVER=EntityDataManager.createKey(TrainEntity.class,DataSerializers.VARINT);
    private static final DataParameter<Float> THROTTLE=EntityDataManager.createKey(TrainEntity.class,DataSerializers.FLOAT);
    private static final DataParameter<Integer> PROMPT=EntityDataManager.createKey(TrainEntity.class,DataSerializers.VARINT);
    private static final DataParameter<String> STATION_NAME=EntityDataManager.createKey(TrainEntity.class,DataSerializers.STRING);
    private static final DataParameter<Float> DISTANCE=EntityDataManager.createKey(TrainEntity.class,DataSerializers.FLOAT);
    private static final DataParameter<Float> APPROACH_TOTAL=EntityDataManager.createKey(TrainEntity.class,DataSerializers.FLOAT);
    public int portalDelay;
    private java.util.UUID driverId;private BlockPos dockedStation;private TrainStationTile approachStation;private int approachSign=1,steering,promptTicks;private double approachTotal;
    private Cursor cursor;
    private java.util.UUID curveId;private double curveDistance;private int curveForward=1;
    private double speed,cruise;
    private int input,lastInput=-100;
    private boolean brake;
    private double targetX,targetY,targetZ;
    private float targetYaw,targetPitch;
    private int interpolation;
    public TrainEntity(World world) { super(world);setSize(1.8f,1.7f);noClip=false; }
    @Override protected void entityInit() { dataManager.register(DRIVER,-1);dataManager.register(THROTTLE,1f);dataManager.register(PROMPT,0);dataManager.register(STATION_NAME,"");dataManager.register(DISTANCE,0f);dataManager.register(APPROACH_TOTAL,0f);dataManager.register(SEAT_INDEX,-1);dataManager.register(CONTROL_INDEX,-1);dataManager.register(FRONT_YAW,0f);dataManager.register(REAR_YAW,0f);dataManager.register(SPEED,0f);dataManager.register(WHEELS,0f);dataManager.register(STRUCTURE,new NBTTagCompound());dataManager.register(WORLD_BUILT,false); }
    @Override public synchronized void onAddedToWorld() {
        super.onAddedToWorld();
        if (!world.isRemote && ticket==null) {
            ForgeChunkManager.Ticket requested=ForgeChunkManager.requestTicket(Create.instance,world,ForgeChunkManager.Type.ENTITY);
            if (requested!=null) { requested.bindEntity(this);acceptTicket(requested); }
        }
    }
    public synchronized void acceptTicket(ForgeChunkManager.Ticket restored) {
        if (ticket!=null && ticket!=restored) ForgeChunkManager.releaseTicket(ticket);
        ticket=restored;updateChunks();
    }
    private synchronized void updateChunks() {
        if (ticket==null) return;
        int chunkX=MathHelper.floor(posX)>>4,chunkZ=MathHelper.floor(posZ)>>4;
        Set<ChunkPos> wanted=new HashSet<>();
        for(int x=-2;x<=2;x++) for(int z=-2;z<=2;z++) wanted.add(new ChunkPos(chunkX+x,chunkZ+z));
        for (ChunkPos chunk:new HashSet<>(ticket.getChunkList())) if (!wanted.contains(chunk)) ForgeChunkManager.unforceChunk(ticket,chunk);
        for (ChunkPos chunk:wanted) if (!ticket.getChunkList().contains(chunk)) ForgeChunkManager.forceChunk(ticket,chunk);
    }
    @Override public synchronized void setDead() {
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
    private java.util.List<TrainStructure.Part> collisionParts;private java.util.List<AxisAlignedBB> collisionCache=java.util.List.of();
    public java.util.List<AxisAlignedBB> localCollisions(){
        var parts=structure();if(parts!=collisionParts){collisionParts=parts;var access=new CarriageBlockAccess(parts);var boxes=new java.util.ArrayList<AxisAlignedBB>();
            for(var p:parts){var local=new BlockPos(p.x(),p.y(),p.z());var box=p.state().getCollisionBoundingBox(access,local);if(box!=null)boxes.add(box.offset(p.x()-.5,.85+p.y(),p.z()-.5));}collisionCache=java.util.List.copyOf(boxes);
        }return collisionCache;
    }
    public AxisAlignedBB visualBounds(){
        AxisAlignedBB result=null;for(var local:localCollisions()){var b=TrainCollisions.box(PlatformGeometry.bounds(TrainCollisions.box(local),posX,posY,posZ,rotationYaw));result=result==null?b:result.union(b);}
        return (result==null?getEntityBoundingBox():result).grow(2);
    }
    public boolean belongsTo(BlockPos station){return stationData.hasKey("Station")&&stationData.getInteger("Dimension")==world.provider.getDimension()&&stationData.getLong("Station")==station.toLong();}
    public boolean worldBuilt(){return dataManager.get(WORLD_BUILT);}
    public NBTTagCompound stationAssembly(){return stationData.copy();}
    public boolean onCurve(){return curveId!=null;}
    public void setStationAssembly(NBTTagCompound info,java.util.List<TrainStructure.Part> parts){stationData=info.copy();dataManager.set(WORLD_BUILT,true);setStructure(parts);setCustomNameTag(info.getString("Name"));}
    public double bodyOffset(){return worldBuilt()?1.15:1.6;}
    private TrainStructure.Part controller(){
        var parts=structure();int controlIndex=dataManager.get(CONTROL_INDEX);if(controlIndex>=0 && controlIndex<parts.size() && parts.get(controlIndex).state().getBlock()==Create.TRAIN_CONTROLS)return parts.get(controlIndex);
        if(dataManager.get(SEAT_INDEX)>=0)return null;
        for(var p:parts)if(p.state().getBlock()==Create.TRAIN_CONTROLS)return p;return null;
    }
    public boolean controlsActive(){return dataManager.get(DRIVER)>=0;}
    public int driverEntityId(){return dataManager.get(DRIVER);}
    public EntityPlayer driver(){return driverId==null?null:world.getPlayerEntityByUUID(driverId);}
    public float throttle(){return dataManager.get(THROTTLE);}
    public int prompt(){return dataManager.get(PROMPT);}
    public String stationName(){return dataManager.get(STATION_NAME);}
    public float stationDistance(){return dataManager.get(DISTANCE);}
    public float approachTotal(){return dataManager.get(APPROACH_TOTAL);}
    public boolean dockedAt(BlockPos station){return dockedStation!=null&&dockedStation.equals(station);}
    public void dock(TrainStationTile station){dockedStation=station.getPos();dataManager.set(STATION_NAME,station.stationName);dataManager.set(PROMPT,3);}
    public int controlIndex(){return dataManager.get(CONTROL_INDEX);}
    public void clearDock(){dockedStation=null;approachStation=null;dataManager.set(PROMPT,0);}
    public boolean routeFits(){var p=pose();return p.supported&&!bodyObstructed(p);}
    public boolean driverInReach(EntityPlayer player){var part=controller();if(part==null)return !worldBuilt()&&player.getRidingEntity()==this;var p=toWorld(new Vec3d(part.x(),1.35+part.y(),part.z()));return p.squareDistanceTo(player.getPositionVector())<=64;}
    public void stopControlling(){driverId=null;dataManager.set(DRIVER,-1);dataManager.set(CONTROL_INDEX,-1);input=0;brake=false;approachStation=null;dataManager.set(PROMPT,0);}
    public void startControlling(EntityPlayer player,int index){driverId=player.getUniqueID();dataManager.set(DRIVER,player.getEntityId());dataManager.set(CONTROL_INDEX,index);lastInput=ticksExisted;}
    public Vec3d toLocal(Vec3d p){var q=PlatformGeometry.local(p.x,p.z,posX,posZ,rotationYaw);return new Vec3d(q.x(),p.y-posY,q.z());}
    public Vec3d toWorld(Vec3d p){var q=PlatformGeometry.world(p.x,p.z,posX,posZ,rotationYaw);return new Vec3d(q.x(),posY+p.y,q.z());}
    public float speed() { return world.isRemote?dataManager.get(SPEED):(float)speed; }
    public float wheels() { return dataManager.get(WHEELS); }
    private RailPath.Shape track(Cell cell) {
        BlockPos p=new BlockPos(cell.x(),cell.y(),cell.z());
        if (!world.isBlockLoaded(p) || !(world.getBlockState(p).getBlock() instanceof TrainTrackBlock)) return null;
        return TrainTrackBlock.pathShape(world.getBlockState(p));
    }
    public void place(BlockPos pos,int forward) {
        curveId=null;curveDistance=0;curveForward=1;cursor=new Cursor(new Cell(pos.getX(),pos.getY(),pos.getZ()),.5,forward);align();
    }
    public void control(int input,boolean brake,int steering,float throttle){this.steering=steering;dataManager.set(THROTTLE,Math.max(1/18f,Math.min(1,throttle)));control(input,brake);}
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
        if(worldBuilt())for(var part:structure())if(Bogeys.is(part.state()))spacing=Math.max(spacing,part.z());
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
            if(Bogeys.is(part.state()))bottom+=.4375;
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
        if(isDead)return;
        super.onUpdate();prevPosX=posX;prevPosY=posY;prevPosZ=posZ;prevRotationYaw=rotationYaw;prevRotationPitch=rotationPitch;
        if (world.isRemote) {
            if (interpolation>0) {
                setPosition(posX+(targetX-posX)/interpolation,posY+(targetY-posY)/interpolation,posZ+(targetZ-posZ)/interpolation);
                rotationYaw+=MathHelper.wrapDegrees(targetYaw-rotationYaw)/interpolation;
                rotationPitch+=(targetPitch-rotationPitch)/interpolation;interpolation--;
            }
            TrainCollisions.carry(this,prevPosX,prevPosY,prevPosZ,prevRotationYaw);return;
        }
        if(portalDelay>0)portalDelay--;
        if (cursor==null) {
            BlockPos p=new BlockPos(posX,posY,posZ);
            if (!(world.getBlockState(p).getBlock() instanceof TrainTrackBlock)) p=p.down();
            if (!(world.getBlockState(p).getBlock() instanceof TrainTrackBlock)) { stop();return; }
            place(p,1);
        }
        if (curveId==null && track(cursor.cell())==null || curveId!=null && CurveGraphData.get(world).node(world,curveId)==null) { stop();return; }
        var driver=driver();if(driverId!=null&&(driver==null||driver.isSpectator()||!driverInReach(driver)||ticksExisted-lastInput>40))stopControlling();
        boolean driven=driver()!=null||!worldBuilt()&&getControllingPassenger() instanceof EntityPlayer;
        boolean recent=driven&&ticksExisted-lastInput<=10;
        boolean approaching=updateStationApproach(recent);
        if(!approaching)speed=TrainMotion.approach(speed,recent?input*throttle():cruise,recent&&brake,onCurve()?TrainMotion.TURN_SPEED:TrainMotion.MAX_SPEED);
        var old=routeState();RouteMove next=travel(speed);var pose=pose();
        AxisAlignedBB future=getEntityBoundingBox().offset(pose.position.x()-posX,pose.position.y()+.15-posY,pose.position.z()-posZ);
        if((!pose.supported||next.blocked)&&Math.abs(speed)>1e-6&&tryPortal())return;
        boolean occupied=!world.getEntitiesWithinAABB(TrainEntity.class,future.grow(.05),e -> e!=this && !e.isDead).isEmpty();
        if(!pose.supported || occupied || bodyObstructed(pose)){restoreRoute(old);stop();return;}
        align(pose);updateChunks();finishStationApproach();TrainImpacts.collide(this,prevPosX,prevPosY,prevPosZ,prevRotationYaw);TrainCollisions.carry(this,prevPosX,prevPosY,prevPosZ,prevRotationYaw);
        dataManager.set(WHEELS,(float)((wheels()+TrainMotion.wheelAngle(next.distance(),TrainMotion.WHEEL_RADIUS))%9000));
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
        var parts=structure();int selected=-1;RayTraceResult selectedHit=null;double nearest=from.distanceTo(to)+1e-6;
        var worldHit=world.rayTraceBlocks(player.getPositionEyes(1),player.getPositionEyes(1).add(look),false,true,false);
        if(worldHit!=null)nearest=Math.min(nearest,player.getPositionEyes(1).distanceTo(worldHit.hitVec));
        for(int i=0;i<parts.size();i++){
            var part=parts.get(i);double bottom=.85+part.y();if(Bogeys.is(part.state()))bottom+=.4375;
            var box=new AxisAlignedBB(part.x()-.5,bottom,part.z()-.5,part.x()+.5,(part.state().getBlock() instanceof SeatBlock?1.35:1.85)+part.y(),part.z()+.5);
            var intersect=box.calculateIntercept(from,to);double distance=box.contains(from)?0:intersect==null?Double.POSITIVE_INFINITY:from.distanceTo(intersect.hitVec);
            if(distance<nearest){nearest=distance;selected=i;selectedHit=intersect;}
        }
        if(selected>=0 && (parts.get(selected).state().getBlock()==Create.TRAIN_CONTROLS || parts.get(selected).state().getBlock() instanceof SeatBlock)){
            if(!world.isRemote&&!player.isSpectator()&&!player.isSneaking()){
                boolean seat=parts.get(selected).state().getBlock() instanceof SeatBlock;
                if(!seat){if(driver()!=null&&driver()!=player)return EnumActionResult.FAIL;if(driver()==player)stopControlling();else startControlling(player,selected);return EnumActionResult.SUCCESS;}
                if(getPassengers().isEmpty()){dataManager.set(SEAT_INDEX,selected);player.startRiding(this);player.sendStatusMessage(new net.minecraft.util.text.TextComponentString("Use nearby Train Controls to start driving."),true);}
            }return EnumActionResult.SUCCESS;
        }
        if(selected>=0&&selectedHit!=null)return TrainBuilding.place(this,player,parts.get(selected),selectedHit.sideHit,selectedHit.hitVec);
        return EnumActionResult.PASS;
    }
    public void linkCarriedBlocks(BlockPos a,BlockPos b){var bonds=stationData.getTagList("Bonds",10);var n=new NBTTagCompound();n.setLong("A",a.toLong());n.setLong("B",b.toLong());bonds.appendTag(n);stationData.setTag("Bonds",bonds);}
    private boolean tryPortal(){
        var old=routeState();BlockPos end=null;EnumFacing direction=null;int sign=speed<0?-1:1;
        try{double spacing=0;if(sign>0)for(var p:structure())if(Bogeys.is(p.state()))spacing=Math.max(spacing,p.z());var move=travel(sign*(spacing+.1));
            if(move.blocked&&curveId==null){var tangent=routeTangent().scale(sign);end=new BlockPos(cursor.cell().x(),cursor.cell().y(),cursor.cell().z());direction=EnumFacing.getFacingFromVector((float)tangent.x(),0,(float)tangent.z());}
        }finally{restoreRoute(old);}
        return end!=null&&TrainPortalTravel.transfer(this,end,direction);
    }
    private record Stop(TrainStationTile station,double distance){}
    private Stop findStation(int sign,TrainStationTile only,double maximum){
        var stations=new java.util.ArrayList<TrainStationTile>();for(var tile:world.loadedTileEntityList)if(tile instanceof TrainStationTile station&&station.target!=null&&!station.assembling&&(only==null||only==station))stations.add(station);
        var old=routeState();double distance=0;try{
            while(distance<maximum){var a=routePosition();var move=travel(sign*Math.min(1,maximum-distance));var b=routePosition();var delta=b.sub(a);double square=delta.dot(delta);
                for(var station:stations){var target=station.target.offset(station.direction);var point=new TrackCurve.V(target.getX()+.5,target.getY(),target.getZ()+.5);double t=square<1e-10?0:Math.max(0,Math.min(1,point.sub(a).dot(delta)/square));
                    if(a.add(delta.scale(t)).sub(point).length()>.035)continue;double d=distance+Math.abs(move.distance)*t;
                    if(only==null&&d<StationApproach.minimumSearchDistance(speed)+.05)continue;
                    var occupying=station.train();if(occupying!=null&&occupying!=this)continue;return new Stop(station,d);
                }
                distance+=Math.abs(move.distance);if(move.blocked||Math.abs(move.distance)<1e-6)break;
            }
        }finally{restoreRoute(old);}return null;
    }
    private boolean updateStationApproach(boolean recent){
        if(!recent){approachStation=null;if(promptTicks--<=0)dataManager.set(PROMPT,0);return false;}
        if(dockedStation!=null){if(brake){speed=0;dataManager.set(PROMPT,3);return true;}if(input!=0){dockedStation=null;dataManager.set(PROMPT,4);promptTicks=40;}}
        if(!brake)approachStation=null;
        if(approachStation!=null){var stop=findStation(approachSign,approachStation,192);if(stop==null){approachStation=null;dataManager.set(PROMPT,0);return false;}
            dataManager.set(DISTANCE,(float)stop.distance);speed=approachSign*StationApproach.next(Math.max(0,speed*approachSign),stop.distance,Math.min(onCurve()?TrainMotion.TURN_SPEED:TrainMotion.MAX_SPEED,TrainMotion.MAX_SPEED*throttle()),TrainMotion.ACCELERATION);dataManager.set(PROMPT,2);return true;
        }
        if(ticksExisted%5==0||brake){int sign=input!=0?Integer.signum(input):speed<0?-1:1;var stop=findStation(sign,null,StationApproach.maximumSearchDistance(speed));
            if(stop!=null){dataManager.set(STATION_NAME,stop.station.stationName);dataManager.set(DISTANCE,(float)stop.distance);dataManager.set(PROMPT,1);if(brake){approachStation=stop.station;approachSign=sign;approachTotal=stop.distance;dataManager.set(APPROACH_TOTAL,(float)approachTotal);return updateStationApproach(true);}}
            else if(promptTicks--<=0)dataManager.set(PROMPT,0);
        }return false;
    }
    private void finishStationApproach(){if(approachStation==null)return;var stop=findStation(approachSign,approachStation,192);if(stop!=null&&stop.distance<1e-5){speed=0;dock(approachStation);approachStation.sync();approachStation=null;}}
    @Override public boolean processInitialInteract(EntityPlayer player,EnumHand hand) {
        if(worldBuilt() || player.isSneaking())return false;
        if(!world.isRemote && getPassengers().isEmpty())player.startRiding(this);return true;
    }
    @Override public void updatePassenger(Entity passenger){
        if(!isPassenger(passenger))return;
        int seat=dataManager.get(SEAT_INDEX);
        if(seat>=0&&seat<structure().size()&&structure().get(seat).state().getBlock() instanceof SeatBlock){
            var p=structure().get(seat);double a=Math.toRadians(rotationYaw);
            passenger.setPosition(posX+Math.cos(a)*p.x()-Math.sin(a)*p.z(),posY+.85+p.y()+.5+passenger.getYOffset(),posZ+Math.sin(a)*p.x()+Math.cos(a)*p.z());return;
        }
        var control=controller();
        if(!worldBuilt() || control==null){super.updatePassenger(passenger);return;}
        var face=control.state().getValue(RailwayBlock.FACING);double x=control.x()+face.getXOffset()*.85,z=control.z()+face.getZOffset()*.85,a=Math.toRadians(rotationYaw);
        passenger.setPosition(posX+Math.cos(a)*x-Math.sin(a)*z,posY+.85+control.y()+passenger.getYOffset(),posZ+Math.sin(a)*x+Math.cos(a)*z);
    }
    @Override public Entity getControllingPassenger() { return getPassengers().isEmpty() || worldBuilt()&&dataManager.get(SEAT_INDEX)>=0&&controller()==null ? null:getPassengers().get(0); }
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
        nbt.setInteger("SeatIndex",dataManager.get(SEAT_INDEX));nbt.setInteger("ControlIndex",dataManager.get(CONTROL_INDEX));
        nbt.setInteger("PortalDelay",portalDelay);
        if(dockedStation!=null)nbt.setLong("DockedStation",dockedStation.toLong());nbt.setFloat("Throttle",throttle());
        nbt.setTag("StationAssembly",stationData.copy());nbt.setBoolean("WorldBuilt",worldBuilt());
        nbt.setTag("Structure",dataManager.get(STRUCTURE).copy());
        nbt.setDouble("TrainSpeed",speed);nbt.setDouble("Cruise",cruise);nbt.setFloat("WheelAngle",wheels());
    }
    @Override protected void readEntityFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("TrackX")) cursor=new Cursor(new Cell(nbt.getInteger("TrackX"),nbt.getInteger("TrackY"),nbt.getInteger("TrackZ")),
                finiteClamp(nbt.getDouble("Progress"),0,1),nbt.getInteger("Forward")==-1 ? -1:1);
        curveId=nbt.hasUniqueId("CurveId")?nbt.getUniqueId("CurveId"):null;curveDistance=Math.max(0,nbt.getDouble("CurveDistance"));curveForward=nbt.getInteger("CurveForward")==-1?-1:1;
        dataManager.set(SEAT_INDEX,nbt.hasKey("SeatIndex")?nbt.getInteger("SeatIndex"):-1);dataManager.set(CONTROL_INDEX,nbt.hasKey("ControlIndex")?nbt.getInteger("ControlIndex"):-1);
        portalDelay=Math.max(0,nbt.getInteger("PortalDelay"));
        dockedStation=nbt.hasKey("DockedStation")?BlockPos.fromLong(nbt.getLong("DockedStation")):null;dataManager.set(THROTTLE,nbt.hasKey("Throttle")?(float)finiteClamp(nbt.getFloat("Throttle"),1/18d,1):1f);driverId=null;dataManager.set(DRIVER,-1);
        stationData=nbt.getCompoundTag("StationAssembly").copy();dataManager.set(WORLD_BUILT,nbt.getBoolean("WorldBuilt"));
        setStructure(TrainStructure.read(nbt.getCompoundTag("Structure")));
        speed=finiteClamp(nbt.getDouble("TrainSpeed"),-TrainMotion.MAX_SPEED,TrainMotion.MAX_SPEED);
        cruise=finiteClamp(nbt.getDouble("Cruise"),-1,1);dataManager.set(SPEED,(float)speed);
        dataManager.set(WHEELS,(float)finiteClamp(nbt.getFloat("WheelAngle"),-9000,9000));
    }
    private static double finiteClamp(double value,double min,double max) { return Double.isFinite(value) ? Math.max(min,Math.min(max,value)):0; }
}
