package dev.createport.kinetics;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
/** Create's MotorValueBox sided offset, inset 12.5/16 and facing correction -1/16. */
public final class MotorSettings {
    public static boolean active(KineticTile tile,EnumFacing side){return side!=null&&side.getAxis()!=tile.axis()&&(tile.axis()==EnumFacing.Axis.Y||side!=EnumFacing.DOWN);}
    public static Vec3d center(KineticTile tile,EnumFacing side){var facing=tile.facing();return new Vec3d(.5+side.getXOffset()*4.5/16-facing.getXOffset()/16d,.5+side.getYOffset()*4.5/16-facing.getYOffset()/16d,.5+side.getZOffset()*4.5/16-facing.getZOffset()/16d);}
    public static boolean hit(KineticTile tile,EnumFacing side,Vec3d local){return active(tile,side)&&local.distanceTo(center(tile,side))<.25;}
    private MotorSettings(){}
}
