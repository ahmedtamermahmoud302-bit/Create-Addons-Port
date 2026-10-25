package dev.createport.trains;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
public final class BogeyTile extends TileEntity {
    @Override public AxisAlignedBB getRenderBoundingBox(){return new AxisAlignedBB(pos).grow(2);}
}
