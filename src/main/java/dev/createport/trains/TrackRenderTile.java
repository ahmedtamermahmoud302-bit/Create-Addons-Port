package dev.createport.trains;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.nbt.*;
import net.minecraft.network.*;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
public final class TrackRenderTile extends TileEntity {
    public NBTTagList connections=new NBTTagList();
    public java.util.List<TrackCurve> renderedCurves=java.util.List.of();
    @Override public AxisAlignedBB getRenderBoundingBox(){return new AxisAlignedBB(pos).grow(connections.tagCount()>0?130:3);}
    @Override public NBTTagCompound getUpdateTag(){var n=super.getUpdateTag();n.setTag("Connections",CurveGraphData.get(world).connections(pos));return n;}
    @Override public SPacketUpdateTileEntity getUpdatePacket(){return new SPacketUpdateTileEntity(pos,0,getUpdateTag());}
    @Override public void onDataPacket(NetworkManager manager,SPacketUpdateTileEntity packet){readFromNBT(packet.getNbtCompound());}
    @Override public void readFromNBT(NBTTagCompound n){super.readFromNBT(n);connections=n.getTagList("Connections",10);
        var decoded=new java.util.ArrayList<TrackCurve>();
        for(int i=0;i<connections.tagCount();i++){var spec=connections.getCompoundTagAt(i);try{decoded.add(new TrackCurve(CurveTrackEntity.vector(spec,"A"),CurveTrackEntity.vector(spec,"B"),CurveTrackEntity.vector(spec,"DA"),CurveTrackEntity.vector(spec,"DB")));}catch(IllegalArgumentException invalid){decoded.add(null);}}
        renderedCurves=java.util.Collections.unmodifiableList(decoded);}
}
