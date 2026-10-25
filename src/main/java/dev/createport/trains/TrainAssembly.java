package dev.createport.trains;

import dev.createport.glue.*;
import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.Item;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/** Limited glue-to-carriage assembly, with validation before removing source blocks. */
public final class TrainAssembly {
    public static boolean assemble(World world,BlockPos track,EntityPlayer player,TrainEntity train) {
        GlueWorldData glue=GlueWorldData.get(world);BlockPos anchor=track.up();
        if (!glue.graph().connected(GlueWorldData.pos(anchor))) return false;
        if (world.getBlockState(track).getValue(TrainTrackBlock.SHAPE).getMetadata()>1) return fail(player,"Assemble glued carriages on straight track.");
        Set<GlueGraph.Pos> component;
        try { component=glue.graph().component(GlueWorldData.pos(anchor),64); }
        catch (IllegalArgumentException tooBig) { return fail(player,"This carriage supports up to 64 glued blocks."); }
        Map<BlockPos,IBlockState> source=new LinkedHashMap<>();List<TrainStructure.Part> parts=new ArrayList<>();
        double yaw=Math.toRadians(train.rotationYaw),sin=Math.sin(yaw),cos=Math.cos(yaw);
        Rotation rotation=Rotation.values()[Math.floorMod(Math.round(-train.rotationYaw/90f),4)];
        for (GlueGraph.Pos point:component) {
            BlockPos p=GlueWorldData.block(point);
            if (!world.isBlockLoaded(p) || !world.isBlockModifiable(player,p)
                    || !player.canPlayerEdit(p,net.minecraft.util.EnumFacing.UP,player.getHeldItemMainhand())) return fail(player,"Cannot move a protected or unloaded block.");
            IBlockState state=world.getBlockState(p);
            if (!state.isFullCube() || !state.getMaterial().isSolid() || state.getBlock().hasTileEntity(state)
                    || state.getBlockHardness(world,p)<0 || Item.getItemFromBlock(state.getBlock())==Items.AIR)
                return fail(player,"This first carriage can move ordinary solid blocks only; inventories and machines stay in place.");
            int dx=p.getX()-track.getX(),dz=p.getZ()-track.getZ(),y=p.getY()-anchor.getY();
            int x=(int)Math.round(cos*dx+sin*dz),z=(int)Math.round(-sin*dx+cos*dz);
            if (Math.abs(x)>4 || Math.abs(z)>6 || y<0 || y>4) return fail(player,"Keep the glued body within 9 blocks wide, 13 long and 5 high above this track.");
            source.put(p,state);parts.add(new TrainStructure.Part(x,y,z,state.withRotation(rotation)));
        }
        train.setStructure(parts);train.place(track,forward(world,track,train.rotationYaw));
        // Source blocks are expected to overlap the new carriage before transfer. Other obstacles are not.
        AxisAlignedBB bounds=train.getEntityBoundingBox();
        for (BlockPos p:BlockPos.getAllInBox(new BlockPos(bounds.minX,bounds.minY,bounds.minZ),new BlockPos(bounds.maxX,bounds.maxY,bounds.maxZ))) {
            if (source.containsKey(p)) continue;
            IBlockState state=world.getBlockState(p);AxisAlignedBB collision=state.getCollisionBoundingBox(world,p);
            if (collision!=null && collision.offset(p).intersects(bounds))return fail(player,"Clear the space around and above the carriage first.");
        }
        if (!world.getEntitiesWithinAABB(TrainEntity.class,bounds).isEmpty())return fail(player,"A carriage is already in this space.");
        if (!world.spawnEntity(train))return false;
        for (var entry:source.entrySet()) if (!world.getBlockState(entry.getKey()).equals(entry.getValue())) { train.setDead();return false; }
        List<BlockPos> removed=new ArrayList<>();
        for (var entry:source.entrySet()) {
            if (!world.setBlockState(entry.getKey(),Blocks.AIR.getDefaultState(),2)) {
                for (BlockPos p:removed)world.setBlockState(p,source.get(p),2);
                train.setDead();return fail(player,"Assembly was interrupted; the source blocks were restored.");
            }
            removed.add(entry.getKey());
        }
        for (BlockPos p:source.keySet()) { glue.remove(p);world.notifyNeighborsOfStateChange(p,source.get(p).getBlock(),false); }
        player.sendStatusMessage(new TextComponentString("Glued carriage assembled: "+parts.size()+" blocks. Right-click it to ride."),true);
        return true;
    }
    private static int forward(World world,BlockPos track,float heading) {
        RailPath.Shape shape=RailPath.Shape.valueOf(world.getBlockState(track).getValue(TrainTrackBlock.SHAPE).name());
        RailPath.Point tangent=RailPath.of(shape).tangent(.5);double angle=Math.toRadians(heading);
        return -Math.sin(angle)*tangent.x()+Math.cos(angle)*tangent.z()<0 ? -1:1;
    }
    private static boolean fail(EntityPlayer player,String text) { player.sendStatusMessage(new TextComponentString(text),true);return false; }
    private TrainAssembly() {}
}
