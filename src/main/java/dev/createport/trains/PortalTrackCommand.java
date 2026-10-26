package dev.createport.trains;
import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
/** Operator binding for custom portal systems; default Nether portals resolve automatically. */
public final class PortalTrackCommand extends CommandBase {
    @Override public String getName(){return "createportal";}
    @Override public int getRequiredPermissionLevel(){return 2;}
    @Override public String getUsage(ICommandSender s){return "/createportal link <fromDim> <x> <y> <z> <entryFacing> <toDim> <x> <y> <z> <exitFacing> | unlink <dim> <x> <y> <z> <entryFacing>";}
    public static WorldServer dimension(MinecraftServer server,int id){if(!DimensionManager.isDimensionRegistered(id))return null;var world=server.getWorld(id);if(world==null){DimensionManager.initDimension(id);world=server.getWorld(id);}return world;}
    private static EnumFacing facing(String value)throws CommandException{var f=EnumFacing.byName(value);if(f==null||f.getAxis()==EnumFacing.Axis.Y)throw new CommandException("Use north, south, east or west");return f;}
    private static PortalTrackLinks.Endpoint endpoint(String[] a,int i)throws CommandException{return new PortalTrackLinks.Endpoint(parseInt(a[i]),new BlockPos(parseInt(a[i+1]),parseInt(a[i+2],0,255),parseInt(a[i+3])),facing(a[i+4]));}
    private static void validate(MinecraftServer server,PortalTrackLinks.Endpoint e)throws CommandException{var world=dimension(server,e.dimension());if(world==null)throw new CommandException("Dimension is not registered");var state=world.getBlockState(e.track());if(!(state.getBlock() instanceof TrainTrackBlock))throw new CommandException("Endpoint must be Railway Track");var shape=TrainTrackBlock.pathShape(state);if(shape!=(e.direction().getAxis()==EnumFacing.Axis.X?RailPath.Shape.EAST_WEST:RailPath.Shape.NORTH_SOUTH))throw new CommandException("Endpoint track must be straight and face along the portal link");}
    @Override public void execute(MinecraftServer server,ICommandSender sender,String[] a)throws CommandException{
        if(a.length==11&&a[0].equals("link")){var entry=endpoint(a,1);var exit=endpoint(a,6);validate(server,entry);validate(server,exit);if(entry.dimension()==exit.dimension())throw new CommandException("A dimension portal must lead to another dimension");PortalTrackLinks.get(sender.getEntityWorld()).connect(entry,exit);sender.sendMessage(new TextComponentString("Portal tracks linked in both directions."));return;}
        if(a.length==6&&a[0].equals("unlink")){var entry=endpoint(a,1);boolean removed=PortalTrackLinks.get(sender.getEntityWorld()).remove(entry);sender.sendMessage(new TextComponentString(removed?"Portal track link removed in both directions.":"No portal track link at that endpoint."));return;}
        throw new WrongUsageException(getUsage(sender));
    }
}
