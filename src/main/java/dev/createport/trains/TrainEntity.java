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

/** Initial self-propelled carriage. Train assembly/contraption capture is a separate porting stage. */
public final class TrainEntity extends Entity {
    private static final DataParameter<NBTTagCompound> STRUCTURE=EntityDataManager.createKey(TrainEntity.class,DataSerializers.COMPOUND_TAG);
    private NBTTagCompound structureTag;
    private java.util.List<TrainStructure.Part> parts=java.util.List.of();
    private static final DataParameter<Float> SPEED=EntityDataManager.createKey(TrainEntity.class,DataSerializers.FLOAT);
    private static final DataParameter<Float> WHEELS=EntityDataManager.createKey(TrainEntity.class,DataSerializers.FLOAT);
    private ForgeChunkManager.Ticket ticket;
    private Cursor cursor;
    private double speed,cruise;
    private int input,lastInput=-100;
    private boolean brake;
    private double targetX,targetY,targetZ;
    private float targetYaw,targetPitch;
    private int interpolation;
    public TrainEntity(World world) { super(world);setSize(1.8f,1.7f);noClip=false; }
    @Override protected void entityInit() { dataManager.register(SPEED,0f);dataManager.register(WHEELS,0f);dataManager.register(STRUCTURE,new NBTTagCompound()); }
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
        for(int x=-1;x<=1;x++) for(int z=-1;z<=1;z++) wanted.add(new ChunkPos(chunkX+x,chunkZ+z));
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
            height=Math.max(height,2.3f+part.y());
        }
        setSize(width,height);
    }
    public float speed() { return dataManager.get(SPEED); }
    public float wheels() { return dataManager.get(WHEELS); }
    private RailPath.Shape track(Cell cell) {
        BlockPos p=new BlockPos(cell.x(),cell.y(),cell.z());
        if (!world.isBlockLoaded(p) || !(world.getBlockState(p).getBlock() instanceof TrainTrackBlock)) return null;
        return RailPath.Shape.valueOf(world.getBlockState(p).getValue(TrainTrackBlock.SHAPE).name());
    }
    public void place(BlockPos pos,int forward) {
        cursor=new Cursor(new Cell(pos.getX(),pos.getY(),pos.getZ()),.5,forward);align();
    }
    public void control(int input,boolean brake) { this.input=input;this.brake=brake;lastInput=ticksExisted;cruise=0; }
    public void cruise(double value) { cruise=Math.max(-1,Math.min(1,value));input=0;lastInput=-100; }
    private void align() {
        RailPath.Point p=RailNavigator.position(cursor,this::track);
        RailPath.Point tangent=RailPath.of(track(cursor.cell())).tangent(cursor.progress());
        setPosition(p.x(),p.y()+.15,p.z());
        rotationYaw=(float)Math.toDegrees(Math.atan2(-tangent.x()*cursor.forward(),tangent.z()*cursor.forward()));
        rotationPitch=(float)-Math.toDegrees(Math.atan2(tangent.y()*cursor.forward(),Math.hypot(tangent.x(),tangent.z())));
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
        if (track(cursor.cell())==null) { stop();return; }
        boolean driven=getControllingPassenger() instanceof EntityPlayer;
        boolean recent=driven && ticksExisted-lastInput<=10;
        speed=TrainMotion.approach(speed,recent ? input:cruise,recent ? brake:driven);
        Cursor old=cursor;
        Advance next=RailNavigator.advance(cursor,speed,this::track);cursor=next.cursor();
        RailPath.Point p=RailNavigator.position(cursor,this::track);
        AxisAlignedBB future=getEntityBoundingBox().offset(p.x()-posX,p.y()+.15-posY,p.z()-posZ);
        boolean occupied=!world.getEntitiesWithinAABB(TrainEntity.class,future.grow(.05),e -> e!=this && !e.isDead).isEmpty();
        if (occupied || !world.getCollisionBoxes(this,future).isEmpty()) { cursor=old;stop();return; }
        align();updateChunks();
        dataManager.set(WHEELS,(float)((wheels()+TrainMotion.wheelAngle(next.distance(),TrainMotion.WHEEL_RADIUS))%360));
        if (next.blocked()) { speed=0;cruise=0; }
        dataManager.set(SPEED,(float)speed);
        motionX=posX-prevPosX;motionY=posY-prevPosY;motionZ=posZ-prevPosZ;
    }
    private void stop() { speed=0;cruise=0;dataManager.set(SPEED,0f);motionX=motionY=motionZ=0; }
    @Override public void setPositionAndRotationDirect(double x,double y,double z,float yaw,float pitch,int steps,boolean teleport) {
        targetX=x;targetY=y;targetZ=z;targetYaw=yaw;targetPitch=pitch;interpolation=Math.max(1,steps);
    }
    @Override public boolean processInitialInteract(EntityPlayer player,EnumHand hand) {
        if (player.isSneaking()) return false;
        if (!world.isRemote && getPassengers().isEmpty()) player.startRiding(this);
        return true;
    }
    @Override public Entity getControllingPassenger() { return getPassengers().isEmpty() ? null:getPassengers().get(0); }
    // Only the server moves this entity; vanilla vehicle prediction would overwrite its rail cursor.
    @Override public boolean canPassengerSteer() { return false; }
    @Override protected boolean canFitPassenger(Entity passenger) { return getPassengers().isEmpty(); }
    @Override public double getMountedYOffset() {
        double seat=1.3;for (TrainStructure.Part part:structure())seat=Math.max(seat,2.3+part.y());return seat;
    }
    @Override public boolean canBeCollidedWith() { return !isDead; }
    @Override public boolean canBePushed() { return false; }
    @Override public boolean attackEntityFrom(DamageSource source,float amount) {
        if (isDead || isEntityInvulnerable(source)) return false;
        if (!world.isRemote && source.getTrueSource() instanceof EntityPlayer player) {
            removePassengers();setDead();if (!player.capabilities.isCreativeMode) {
                entityDropItem(new net.minecraft.item.ItemStack(Create.CARRIAGE),0);
                for (TrainStructure.Part part:structure()) entityDropItem(new net.minecraft.item.ItemStack(part.state().getBlock(),1,part.state().getBlock().damageDropped(part.state())),0);
            }
        }
        return true;
    }
    @Override protected void writeEntityToNBT(NBTTagCompound nbt) {
        if (cursor!=null) {
            nbt.setInteger("TrackX",cursor.cell().x());nbt.setInteger("TrackY",cursor.cell().y());nbt.setInteger("TrackZ",cursor.cell().z());
            nbt.setDouble("Progress",cursor.progress());nbt.setInteger("Forward",cursor.forward());
        }
        nbt.setTag("Structure",dataManager.get(STRUCTURE).copy());
        nbt.setDouble("TrainSpeed",speed);nbt.setDouble("Cruise",cruise);nbt.setFloat("WheelAngle",wheels());
    }
    @Override protected void readEntityFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("TrackX")) cursor=new Cursor(new Cell(nbt.getInteger("TrackX"),nbt.getInteger("TrackY"),nbt.getInteger("TrackZ")),
                finiteClamp(nbt.getDouble("Progress"),0,1),nbt.getInteger("Forward")==-1 ? -1:1);
        setStructure(TrainStructure.read(nbt.getCompoundTag("Structure")));
        speed=finiteClamp(nbt.getDouble("TrainSpeed"),-TrainMotion.MAX_SPEED,TrainMotion.MAX_SPEED);
        cruise=finiteClamp(nbt.getDouble("Cruise"),-1,1);dataManager.set(SPEED,(float)speed);
        dataManager.set(WHEELS,(float)finiteClamp(nbt.getFloat("WheelAngle"),-360,360));
    }
    private static double finiteClamp(double value,double min,double max) { return Double.isFinite(value) ? Math.max(min,Math.min(max,value)):0; }
}
