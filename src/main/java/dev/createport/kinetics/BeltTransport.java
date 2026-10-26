package dev.createport.kinetics;
import java.util.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
public final class BeltTransport {
    private static final Set<World> REMOVING=Collections.newSetFromMap(new IdentityHashMap<>());
    public static BeltGeometry.Step plan(KineticTile tile){var d=tile.beltLast.subtract(tile.beltFirst);return BeltGeometry.connect(tile.axis().ordinal(),d.getX(),d.getY(),d.getZ());}
    public static void remove(World world,KineticTile tile,BlockPos broken,net.minecraft.util.EnumFacing.Axis axis){
        if(world.isRemote||tile.beltFirst==null||tile.beltLast==null||!REMOVING.add(world))return;
        try{
            var first=tile.beltFirst;var last=tile.beltLast;var delta=last.subtract(first);
            var step=BeltGeometry.connect(axis.ordinal(),delta.getX(),delta.getY(),delta.getZ());
            var line=new ArrayList<BlockPos>();
            for(int i=0;i<=step.length();i++)line.add(first.add(step.x()*i,step.y()*i,step.z()*i));
            // Disconnect the whole line before neighbor callbacks can traverse a removed pulley.
            for(var p:line)if(world.getTileEntity(p) instanceof KineticTile segment&&first.equals(segment.beltFirst)&&last.equals(segment.beltLast)){
                segment.beltFirst=null;segment.beltLast=null;segment.beltLength=segment.beltIndex=0;
            }
            KineticTopology.invalidate(world);
            var shaft=dev.createport.Create.SHAFT.getDefaultState().withProperty(KineticBlock.AXIS,axis);
            for(int i=0;i<line.size();i++){var p=line.get(i);if(!p.equals(broken)&&world.getBlockState(p).getBlock()==dev.createport.Create.BELT)world.setBlockState(p,i==0||i==step.length()?shaft:net.minecraft.init.Blocks.AIR.getDefaultState(),3);}
        }finally{REMOVING.remove(world);KineticTopology.invalidate(world);}
    }
    public static void tick(KineticTile tile){
        var world=tile.getWorld();if(tile.beltFirst==null||tile.beltLast==null||world.getBlockState(tile.getPos()).getBlock()!=dev.createport.Create.BELT||world.getTileEntity(tile.getPos())!=tile)return;var step=plan(tile);if(tile.axis()==net.minecraft.util.EnumFacing.Axis.Y||step.x()==0&&step.z()==0)return;double speed=BeltGeometry.travel(tile.rpm,step);if(Math.abs(speed)<1e-6)return;
        var area=new AxisAlignedBB(tile.beltFirst,tile.beltLast).expand(1,1,1).grow(.5);for(var entity:world.getEntitiesWithinAABB(Entity.class,area)){
            if(entity.isDead||entity.noClip||entity.isRiding()||entity instanceof EntityPlayerMP||entity.motionY>.2)continue;if(world.isRemote&&!(entity instanceof EntityPlayer p&&p.isUser()))continue;
            Vec3d a=new Vec3d(tile.beltFirst).add(.5,.8125,.5),direction=new Vec3d(step.x(),step.y(),step.z());double length=direction.lengthSquared();double t=entity.getPositionVector().subtract(a).dotProduct(direction)/length;
            if(t<-.5||t>step.length()+.5)continue;Vec3d closest=a.add(direction.scale(t));double horizontal=Math.hypot(entity.posX-closest.x,entity.posZ-closest.z);if(horizontal>.6||Math.abs(entity.getEntityBoundingBox().minY-closest.y)>.3)continue;
            entity.move(net.minecraft.entity.MoverType.SELF,step.x()*speed,step.y()*speed,step.z()*speed);entity.fallDistance=0;entity.onGround=true;
        }
    }
    private BeltTransport(){}
}
