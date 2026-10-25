package dev.createport.kinetics;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import java.util.*;

public final class KineticTile extends TileEntity implements ITickable {
    public int generatedRpm = 16;
    public float rpm;
    public KineticNetwork.Status status = KineticNetwork.Status.UNPOWERED;
    public EnumFacing.Axis axis() { return world.getBlockState(pos).getValue(KineticBlock.AXIS); }
    public EnumFacing facing() { return world.getBlockState(pos).getValue(KineticBlock.FACING); }
    public boolean connects(EnumFacing side) { return motor() ? side == facing() : side.getAxis() == axis(); }
    public boolean motor() { return world.getBlockState(pos).getBlock() instanceof KineticBlock b && b.motor; }
    public void setGeneratedRpm(int speed) {
        if (world.isRemote || !motor()) return;
        generatedRpm = Math.max(-256, Math.min(256, speed)); markDirty();
        recompute(); sync();
    }
    @Override public void update() {
        if (!world.isRemote && Math.floorMod(world.getTotalWorldTime() + pos.toLong(), 10) == 0) recompute();
    }
    private void recompute() {
        List<KineticTile> members = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>(); ArrayDeque<BlockPos> queue = new ArrayDeque<>(); queue.add(pos);
        while (!queue.isEmpty()) {
            BlockPos p = queue.remove(); if (!visited.add(p) || !world.isBlockLoaded(p)) continue;
            if (!(world.getTileEntity(p) instanceof KineticTile tile) || tile.axis() != axis()) continue;
            members.add(tile);
            for (EnumFacing direction : EnumFacing.values()) {
                BlockPos adjacent = p.offset(direction);
                if (tile.connects(direction) && world.isBlockLoaded(adjacent)
                        && world.getTileEntity(adjacent) instanceof KineticTile other && other.connects(direction.getOpposite())) queue.add(adjacent);
            }
        }
        List<KineticNetwork.Node> nodes = new ArrayList<>(); List<KineticNetwork.Edge> edges = new ArrayList<>();
        Map<BlockPos, Integer> ids = new HashMap<>();
        for (int i = 0; i < members.size(); i++) {
            KineticTile tile = members.get(i); ids.put(tile.pos, i);
            nodes.add(new KineticNetwork.Node(i, tile.motor() ? tile.generatedRpm * (tile.facing().getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 1 : -1) : 0, tile.motor() ? 16384 : 0, 0));
        }
        for (int i = 0; i < members.size(); i++) for (EnumFacing direction : EnumFacing.values()) {
            if (direction.getAxis() != axis()) continue;
            Integer j = ids.get(members.get(i).pos.offset(direction));
            if (j != null && i < j && members.get(i).connects(direction) && members.get(j).connects(direction.getOpposite())) edges.add(new KineticNetwork.Edge(i, j, 1));
        }
        KineticNetwork.Result result = KineticNetwork.solve(nodes, edges, 256);
        for (int i = 0; i < members.size(); i++) {
            KineticTile tile = members.get(i); float speed = (float) result.speed(i);
            if (tile.rpm != speed || tile.status != result.status()) {
                tile.rpm = speed; tile.status = result.status(); tile.markDirty(); tile.sync();
            }
        }
    }
    private void sync() { IBlockState state = world.getBlockState(pos); world.notifyBlockUpdate(pos, state, state, 3); }
    @Override public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt); nbt.setInteger("GeneratedRPM", generatedRpm); nbt.setFloat("RPM", rpm); nbt.setString("Status", status.name()); return nbt;
    }
    @Override public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt); generatedRpm = nbt.hasKey("GeneratedRPM") ? Math.max(-256, Math.min(256, nbt.getInteger("GeneratedRPM"))) : 16;
        rpm = nbt.getFloat("RPM"); if (!Float.isFinite(rpm)) rpm = 0;
        try { status = KineticNetwork.Status.valueOf(nbt.getString("Status")); } catch (IllegalArgumentException ignored) { status = KineticNetwork.Status.UNPOWERED; }
    }
    @Override public NBTTagCompound getUpdateTag() { return writeToNBT(new NBTTagCompound()); }
    @Override public SPacketUpdateTileEntity getUpdatePacket() { return new SPacketUpdateTileEntity(pos, 0, getUpdateTag()); }
    @Override public void onDataPacket(NetworkManager manager, SPacketUpdateTileEntity packet) { readFromNBT(packet.getNbtCompound()); }
}
