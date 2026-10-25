package dev.createport.trains;

import dev.createport.Create;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

/** A persistent connection between two track anchors. The mesh uses the original Create segments. */
public final class CurveTrackEntity extends Entity {
    private static final DataParameter<NBTTagCompound> DATA=EntityDataManager.createKey(CurveTrackEntity.class,DataSerializers.COMPOUND_TAG);
    private NBTTagCompound cached;private TrackCurve curve;
    public CurveTrackEntity(World world){super(world);setSize(.1f,.1f);}
    @Override protected void entityInit(){dataManager.register(DATA,new NBTTagCompound());}
    public static void vector(NBTTagCompound n,String key,TrackCurve.V v){n.setDouble(key+"X",v.x());n.setDouble(key+"Y",v.y());n.setDouble(key+"Z",v.z());}
    public static TrackCurve.V vector(NBTTagCompound n,String key){return new TrackCurve.V(n.getDouble(key+"X"),n.getDouble(key+"Y"),n.getDouble(key+"Z"));}
    public void configure(BlockPos first,BlockPos second,TrackCurve path,boolean paid){
        NBTTagCompound n=new NBTTagCompound();n.setLong("First",first.toLong());n.setLong("Second",second.toLong());
        vector(n,"A",path.a);vector(n,"B",path.b);vector(n,"DA",path.axisA);vector(n,"DB",path.axisB);n.setBoolean("Paid",paid);
        dataManager.set(DATA,n);resize();
    }
    public TrackCurve curve(){
        NBTTagCompound n=dataManager.get(DATA);if(n!=cached){cached=n;curve=new TrackCurve(vector(n,"A"),vector(n,"B"),vector(n,"DA"),vector(n,"DB"));}return curve;
    }
    public NBTTagCompound spec(){return dataManager.get(DATA).copy();}
    public void loadSpec(NBTTagCompound spec){dataManager.set(DATA,spec.copy());resize();}
    public BlockPos anchor(boolean first){return BlockPos.fromLong(dataManager.get(DATA).getLong(first?"First":"Second"));}
    private void resize(){
        if(dataManager.get(DATA).isEmpty())return;TrackCurve c=curve();TrackCurve.V mid=c.point(.5);setPosition(mid.x(),mid.y(),mid.z());
        AxisAlignedBB box=new AxisAlignedBB(c.a.x(),c.a.y(),c.a.z(),c.a.x(),c.a.y(),c.a.z());
        for(int i=1;i<=32;i++){var p=c.point(i/32d);box=box.union(new AxisAlignedBB(p.x(),p.y(),p.z(),p.x(),p.y(),p.z()));}
        setEntityBoundingBox(box.grow(1.375));
    }
    @Override public void notifyDataManagerChange(DataParameter<?> key){super.notifyDataManagerChange(key);if(DATA.equals(key))resize();}
    @Override public void onUpdate(){
        super.onUpdate();if(world.isRemote || ticksExisted%20!=0)return;
        if(world.isBlockLoaded(anchor(true)) && !(world.getBlockState(anchor(true)).getBlock() instanceof TrainTrackBlock)
                || world.isBlockLoaded(anchor(false)) && !(world.getBlockState(anchor(false)).getBlock() instanceof TrainTrackBlock)) {
            if(!isDead && dataManager.get(DATA).getBoolean("Paid") && world.getGameRules().getBoolean("doTileDrops")) {
                int count=(curve().segments()+1)/2;
                while(count>0){int stack=Math.min(64,count);entityDropItem(new ItemStack(Create.TRACK,stack),0);count-=stack;}
            }
            setDead();
        }
    }
    @Override public boolean canBeCollidedWith(){return false;}
    @Override protected void writeEntityToNBT(NBTTagCompound n){n.setTag("Connection",dataManager.get(DATA).copy());}
    @Override protected void readEntityFromNBT(NBTTagCompound n){
        try{dataManager.set(DATA,n.getCompoundTag("Connection").copy());resize();}catch(IllegalArgumentException bad){setDead();}
    }
    public static CurveTrackEntity at(World world,TrackCurve.V endpoint){return CurveGraphData.get(world).at(world,endpoint);}
}
