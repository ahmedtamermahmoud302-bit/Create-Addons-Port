package dev.createport.kinetics;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import java.util.*;

public class KineticTile extends TileEntity implements ITickable {
    @Override public net.minecraft.util.math.AxisAlignedBB getRenderBoundingBox(){return new net.minecraft.util.math.AxisAlignedBB(pos).grow(1);}
    @Override public double getMaxRenderDistanceSquared(){return 256*256;}
    @Override public boolean shouldRefresh(net.minecraft.world.World world,BlockPos pos,IBlockState oldState,IBlockState newState){return oldState.getBlock()!=newState.getBlock();}
    public int generatedRpm = 16,crankTicks;public float crankAngle,crankVelocity;
    public final float[] portRpm=new float[6];
    public BlockPos beltFirst,beltLast;public int beltIndex,beltLength;public boolean beltPaid;
    public float portRpm(EnumFacing side){return portRpm[side.getIndex()];}
    public float rpm,theoreticalRpm;
    public double networkCapacity,networkStress;
    public int networkMembers;
    public KineticNetwork.Status status = KineticNetwork.Status.UNPOWERED;
    public EnumFacing.Axis axis() { return world.getBlockState(pos).getValue(KineticBlock.AXIS); }
    public EnumFacing facing() { return world.getBlockState(pos).getValue(KineticBlock.FACING); }
    public boolean connects(EnumFacing side) { return KineticTopology.hasPort(world.getBlockState(pos),side); }
    public boolean motor() { return world.getBlockState(pos).getBlock() instanceof KineticBlock b && b.motor; }
    public void setGeneratedRpm(int speed) {
        if (world.isRemote || !motor()) return;
        generatedRpm = Math.max(-256, Math.min(256, speed)); markDirty();
        KineticTopology.invalidate(world);recompute(); sync();
    }
    @Override public void update() {
        if(isInvalid()||world.getTileEntity(pos)!=this||!(world.getBlockState(pos).getBlock() instanceof KineticBlock))return;
        if(world.isRemote){if(world.getBlockState(pos).getBlock() instanceof MechanicalBlock m&&m.kind==MechanicalBlock.Kind.CRANK){crankVelocity+=(rpm*10/3-crankVelocity)*.25f;crankAngle+=crankVelocity;}if(beltFirst!=null&&beltFirst.equals(pos))BeltTransport.tick(this);return;}
        if(crankTicks>0&&--crankTicks==0){KineticTopology.invalidate(world);recompute();}
        if(beltFirst!=null&&beltFirst.equals(pos))BeltTransport.tick(this);
        if(Math.floorMod(world.getTotalWorldTime()+pos.toLong(),10)==0)recompute();
    }
    public static void refreshAround(net.minecraft.world.World world,BlockPos pos) {
        if(world.isRemote)return;KineticTopology.invalidate(world);
        java.util.List<BlockPos> positions=new java.util.ArrayList<>();positions.add(pos);
        for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++)for(int z=-1;z<=1;z++)positions.add(pos.add(x,y,z));
        for(BlockPos p:positions) if(world.isBlockLoaded(p) && world.getBlockState(p).getBlock() instanceof KineticBlock
                && world.getTileEntity(p) instanceof KineticTile tile)tile.recompute();
    }
    public void recompute(){if(world==null||isInvalid()||world.getTileEntity(pos)!=this||!(world.getBlockState(pos).getBlock() instanceof KineticBlock))return;if(beltFirst!=null&&!pos.equals(beltFirst)&&!pos.equals(beltLast))return;KineticTopology.solve(this);}
    public void sync() { IBlockState state = world.getBlockState(pos); world.notifyBlockUpdate(pos, state, state, 3); }
    @Override public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);nbt.setInteger("CrankTicks",crankTicks);var ports=new net.minecraft.nbt.NBTTagList();for(float speed:portRpm)ports.appendTag(new net.minecraft.nbt.NBTTagFloat(speed));nbt.setTag("PortRPM",ports);
        if(beltFirst!=null&&beltLast!=null){nbt.setLong("BeltFirst",beltFirst.toLong());nbt.setLong("BeltLast",beltLast.toLong());nbt.setInteger("BeltIndex",beltIndex);nbt.setInteger("BeltLength",beltLength);nbt.setBoolean("BeltPaid",beltPaid);} nbt.setInteger("GeneratedRPM", generatedRpm); nbt.setFloat("RPM", rpm); nbt.setString("Status", status.name());
        nbt.setFloat("TheoreticalRPM",theoreticalRpm);nbt.setDouble("NetworkCapacity",networkCapacity);
        nbt.setDouble("NetworkStress",networkStress);nbt.setInteger("NetworkMembers",networkMembers);return nbt;
    }
    @Override public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);crankTicks=Math.max(0,Math.min(10,nbt.getInteger("CrankTicks")));var ports=nbt.getTagList("PortRPM",5);for(int i=0;i<6;i++)portRpm[i]=i<ports.tagCount()?ports.getFloatAt(i):nbt.getFloat("RPM");
        beltFirst=nbt.hasKey("BeltFirst")?BlockPos.fromLong(nbt.getLong("BeltFirst")):null;beltLast=nbt.hasKey("BeltLast")?BlockPos.fromLong(nbt.getLong("BeltLast")):null;beltIndex=nbt.getInteger("BeltIndex");beltLength=Math.max(0,Math.min(21,nbt.getInteger("BeltLength")));beltPaid=nbt.getBoolean("BeltPaid"); generatedRpm = nbt.hasKey("GeneratedRPM") ? Math.max(-256, Math.min(256, nbt.getInteger("GeneratedRPM"))) : 16;
        rpm = nbt.getFloat("RPM"); if (!Float.isFinite(rpm)) rpm = 0;
        theoreticalRpm=nbt.hasKey("TheoreticalRPM") ? nbt.getFloat("TheoreticalRPM"):rpm;
        if(!Float.isFinite(theoreticalRpm))theoreticalRpm=0;
        networkCapacity=nbt.getDouble("NetworkCapacity");if(!Double.isFinite(networkCapacity) || networkCapacity<0)networkCapacity=0;
        networkStress=nbt.getDouble("NetworkStress");if(!Double.isFinite(networkStress) || networkStress<0)networkStress=0;
        networkMembers=Math.max(0,nbt.getInteger("NetworkMembers"));
        try { status = KineticNetwork.Status.valueOf(nbt.getString("Status")); } catch (IllegalArgumentException ignored) { status = KineticNetwork.Status.UNPOWERED; }
    }
    @Override public NBTTagCompound getUpdateTag() { return writeToNBT(new NBTTagCompound()); }
    @Override public SPacketUpdateTileEntity getUpdatePacket() { return new SPacketUpdateTileEntity(pos, 0, getUpdateTag()); }
    @Override public void onDataPacket(NetworkManager manager, SPacketUpdateTileEntity packet) { readFromNBT(packet.getNbtCompound()); }
}
