package dev.createport.kinetics;
import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
/** Each exposed shaft face is a node, so a powered clutch can retain a turning input half. */
public final class KineticTopology {
    private record Port(BlockPos pos,EnumFacing side){}
    private record Link(Port target,double ratio){}
    private record Frame(long tick,Set<Port> solved){}
    private static final Map<World,Frame> FRAMES=new WeakHashMap<>();
    public static void invalidate(World world){FRAMES.remove(world);}
    private static MechanicalBlock machine(IBlockState s){return s.getBlock() instanceof MechanicalBlock m?m:null;}
    public static boolean hasPort(IBlockState state,EnumFacing side){
        if(!(state.getBlock() instanceof KineticBlock block))return false;var m=machine(state);if(block.motor)return state.getValue(KineticBlock.FACING)==side;
        if(m!=null&&m.kind==MechanicalBlock.Kind.GEARBOX)return side.getAxis()!=state.getValue(KineticBlock.AXIS);
        if(m!=null&&m.kind==MechanicalBlock.Kind.CRANK)return state.getValue(KineticBlock.FACING)==side.getOpposite();
        return side.getAxis()==state.getValue(KineticBlock.AXIS);
    }
    private static boolean port(World world,BlockPos p,EnumFacing side){var s=world.getBlockState(p);if(!hasPort(s,side))return false;if(machine(s)!=null&&machine(s).kind==MechanicalBlock.Kind.BELT&&world.getTileEntity(p) instanceof KineticTile tile)return p.equals(tile.beltFirst)||p.equals(tile.beltLast);return true;}
    private static EnumFacing canonical(IBlockState state){for(var side:EnumFacing.values())if(side.getAxisDirection()==EnumFacing.AxisDirection.POSITIVE&&hasPort(state,side))return side;for(var side:EnumFacing.values())if(hasPort(state,side))return side;return EnumFacing.UP;}
    public static EnumFacing.Axis chainAxis(IBlockAccess world,BlockPos pos,IBlockState state){
        for(var axis:EnumFacing.Axis.values())if(axis!=state.getValue(KineticBlock.AXIS))for(var side:EnumFacing.values())if(side.getAxis()==axis){var other=world.getBlockState(pos.offset(side));if(machine(other)!=null&&machine(other).chain()&&other.getValue(KineticBlock.AXIS)!=axis)return axis;}
        return state.getValue(KineticBlock.AXIS)==EnumFacing.Axis.X?EnumFacing.Axis.Z:EnumFacing.Axis.X;
    }
    private static List<Link> neighbours(World world,Port from){
        var state=world.getBlockState(from.pos);var machine=machine(state);List<Link> links=new ArrayList<>();
        for(var side:EnumFacing.values())if(side!=from.side&&hasPort(state,side)){
            if(machine!=null&&machine.kind==MechanicalBlock.Kind.CLUTCH&&state.getValue(MechanicalBlock.POWERED))continue;
            double ratio=machine!=null&&machine.kind==MechanicalBlock.Kind.GEARBOX?TransmissionRules.gearbox(from.side.getIndex(),side.getIndex()):machine!=null&&machine.kind==MechanicalBlock.Kind.GEARSHIFT&&state.getValue(MechanicalBlock.POWERED)?-1:1;
            links.add(new Link(new Port(from.pos,side),ratio));
        }
        var adjacent=from.pos.offset(from.side);if(world.isBlockLoaded(adjacent)&&port(world,adjacent,from.side.getOpposite()))links.add(new Link(new Port(adjacent,from.side.getOpposite()),1));
        if(from.side!=canonical(state))return links;
        if(machine!=null&&machine.cog())for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++)for(int dz=-1;dz<=1;dz++){
            if(dx==0&&dy==0&&dz==0)continue;var p=from.pos.add(dx,dy,dz);if(!world.isBlockLoaded(p))continue;var other=world.getBlockState(p);var target=machine(other);if(target==null||!target.cog())continue;
            double ratio=TransmissionRules.cogs(machine.kind==MechanicalBlock.Kind.LARGE_COG,target.kind==MechanicalBlock.Kind.LARGE_COG,state.getValue(KineticBlock.AXIS).ordinal(),other.getValue(KineticBlock.AXIS).ordinal(),dx,dy,dz);if(ratio!=0)links.add(new Link(new Port(p,canonical(other)),ratio));
        }
        if(machine!=null&&machine.chain())for(var side:EnumFacing.values())if(side.getAxis()==chainAxis(world,from.pos,state)){
            var p=from.pos.offset(side);if(!world.isBlockLoaded(p))continue;var other=world.getBlockState(p);var target=machine(other);if(target==null||!target.chain()||chainAxis(world,p,other)!=side.getAxis())continue;
            double a=machine.kind==MechanicalBlock.Kind.ADJUSTABLE_CHAIN?TransmissionRules.chain(world.getRedstonePowerFromNeighbors(from.pos)):1,b=target.kind==MechanicalBlock.Kind.ADJUSTABLE_CHAIN?TransmissionRules.chain(world.getRedstonePowerFromNeighbors(p)):1;links.add(new Link(new Port(p,canonical(other)),a/b));
        }
        if(machine!=null&&machine.kind==MechanicalBlock.Kind.BELT&&world.getTileEntity(from.pos) instanceof KineticTile tile&&tile.beltFirst!=null){var p=from.pos.equals(tile.beltFirst)?tile.beltLast:from.pos.equals(tile.beltLast)?tile.beltFirst:null;if(p!=null&&world.isBlockLoaded(p)&&world.getBlockState(p).getBlock()==dev.createport.Create.BELT)links.add(new Link(new Port(p,canonical(world.getBlockState(p))),1));}
        return links;
    }
    public static void solve(KineticTile origin){
        World world=origin.getWorld();if(world==null||world.isRemote)return;Frame frame=FRAMES.get(world);if(frame==null||frame.tick!=world.getTotalWorldTime()){frame=new Frame(world.getTotalWorldTime(),new HashSet<>());FRAMES.put(world,frame);}
        for(var side:EnumFacing.values())if(hasPort(world.getBlockState(origin.getPos()),side)){
            Port seed=new Port(origin.getPos(),side);if(frame.solved.contains(seed))continue;
            Map<Port,Integer> ids=new LinkedHashMap<>();List<KineticNetwork.Edge> edges=new ArrayList<>();ArrayDeque<Port> pending=new ArrayDeque<>();pending.add(seed);
            while(!pending.isEmpty()&&ids.size()<4096){var port=pending.remove();if(ids.containsKey(port)||!world.isBlockLoaded(port.pos)||!(world.getBlockState(port.pos).getBlock() instanceof KineticBlock)||!port(world,port.pos,port.side)||!(world.getTileEntity(port.pos) instanceof KineticTile))continue;ids.put(port,ids.size());for(var link:neighbours(world,port))if(!ids.containsKey(link.target))pending.add(link.target);}
            List<KineticNetwork.Node> nodes=new ArrayList<>();Set<BlockPos> members=new HashSet<>();
            for(var entry:ids.entrySet()){
                var port=entry.getKey();var state=world.getBlockState(port.pos);var tile=(KineticTile)world.getTileEntity(port.pos);members.add(port.pos);boolean source=port.side==canonical(state)&&(tile.motor()||machine(state)!=null&&machine(state).kind==MechanicalBlock.Kind.CRANK&&tile.crankTicks>0);
                double rpm=source?tile.generatedRpm*(tile.facing().getAxisDirection()==EnumFacing.AxisDirection.POSITIVE?1:-1):0;nodes.add(new KineticNetwork.Node(entry.getValue(),rpm,source?(tile.motor()?16384:8):0,0));
                for(var link:neighbours(world,port)){Integer target=ids.get(link.target);if(target!=null&&entry.getValue()<target)edges.add(new KineticNetwork.Edge(entry.getValue(),target,link.ratio));}
            }
            var result=KineticNetwork.solve(nodes,edges,256);for(var entry:ids.entrySet()){var tile=(KineticTile)world.getTileEntity(entry.getKey().pos);int face=entry.getKey().side.getIndex();tile.portRpm[face]=(float)result.speed(entry.getValue());tile.theoreticalRpm=result.theoreticalRpm().getOrDefault(entry.getValue(),0d).floatValue();tile.networkCapacity=result.capacity();tile.networkStress=result.stress();tile.networkMembers=members.size();tile.status=result.status();frame.solved.add(entry.getKey());}
            for(var p:members){var tile=(KineticTile)world.getTileEntity(p);var state=world.getBlockState(p);tile.rpm=tile.portRpm[canonical(state).getIndex()];tile.markDirty();tile.sync();
                if(tile.beltFirst!=null&&p.equals(tile.beltFirst)){var plan=BeltTransport.plan(tile);for(int i=1;i<plan.length();i++){var b=p.add(plan.x()*i,plan.y()*i,plan.z()*i);if(world.getTileEntity(b) instanceof KineticTile segment){segment.rpm=tile.rpm;segment.theoreticalRpm=tile.theoreticalRpm;segment.status=tile.status;segment.networkCapacity=tile.networkCapacity;segment.networkStress=tile.networkStress;segment.networkMembers=tile.networkMembers;segment.sync();}}}
            }
        }
    }
    private KineticTopology(){}
}
