package dev.createport;

import dev.createport.kinetics.*;
import dev.createport.trains.*;
import dev.createport.glue.SuperGlueItem;
import dev.createport.equipment.*;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = Create.ID, name = "Create", version = "0.4.0-alpha", acceptedMinecraftVersions = "[1.12.2]")
@Mod.EventBusSubscriber(modid = Create.ID)
public class Create {
    public static final String ID = "create";
    public static Create instance;
    public static KineticBlock SHAFT;
    public static KineticBlock MOTOR;
    public static TrainTrackBlock TRACK;
    public static final java.util.List<SeatBlock> SEATS=new java.util.ArrayList<>();
    public static RailwayBlock RAILWAY_CASING,SMALL_BOGEY,TRAIN_CONTROLS,TRACK_STATION;
    public static SuperGlueItem SUPER_GLUE;
    public static WrenchItem WRENCH;
    public static GogglesItem GOGGLES;
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(ID);
    @SidedProxy(clientSide = "dev.createport.client.ClientProxy", serverSide = "dev.createport.CommonProxy")
    public static CommonProxy proxy;
    @Mod.EventHandler public void preInit(FMLPreInitializationEvent event) {
        instance=this;
        ForgeChunkManager.setForcedChunkLoadingCallback(this,(tickets,world) -> {
            for (ForgeChunkManager.Ticket ticket:tickets) {
                if (ticket.getEntity() instanceof TrainEntity train) train.acceptTicket(ticket);
                else ForgeChunkManager.releaseTicket(ticket);
            }
        });
        GameRegistry.registerTileEntity(TrainStationTile.class,new ResourceLocation(ID,"track_station"));
        GameRegistry.registerTileEntity(BogeyTile.class,new ResourceLocation(ID,"small_bogey"));
        GameRegistry.registerTileEntity(TrackRenderTile.class,new ResourceLocation(ID,"track_render"));
        GameRegistry.registerTileEntity(KineticTile.class, new ResourceLocation(ID, "kinetic"));
        NETWORK.registerMessage(SpeedPacket.Handler.class, SpeedPacket.class, 0, Side.SERVER);
        NETWORK.registerMessage(TrainControlPacket.Handler.class,TrainControlPacket.class,1,Side.SERVER);
        EntityRegistry.registerModEntity(new ResourceLocation(ID,"train_carriage"),TrainEntity.class,
                "train_carriage",0,this,96,1,true);
        NETWORK.registerMessage(StationActionPacket.Handler.class,StationActionPacket.class,2,Side.SERVER);
        NETWORK.registerMessage(TrainInteractPacket.Handler.class,TrainInteractPacket.class,3,Side.SERVER);
        EntityRegistry.registerModEntity(new ResourceLocation(ID,"seat"),SeatEntity.class,"seat",1,this,64,1,false);
        NETWORK.registerMessage(dev.createport.glue.GlueDataPacket.Server.class,dev.createport.glue.GlueDataPacket.class,4,Side.SERVER);
        NETWORK.registerMessage(dev.createport.glue.GlueDataPacket.Client.class,dev.createport.glue.GlueDataPacket.class,4,Side.CLIENT);
        NETWORK.registerMessage(TrackToolPacket.Handler.class,TrackToolPacket.class,5,Side.SERVER);
        proxy.preInit();
    }
    @Mod.EventHandler public void init(FMLInitializationEvent event) { proxy.init(); }
    @SubscribeEvent public static void interact(PlayerInteractEvent.RightClickBlock event) {
        if (WrenchItem.isHolding(event.getEntityPlayer()) || event.getEntityPlayer().isSneaking() || event.getEntityPlayer().isSpectator()
                || !event.getEntityPlayer().capabilities.allowEdit || event.getFace() == null) return;
        if (event.getWorld().getTileEntity(event.getPos()) instanceof KineticTile tile && tile.motor()
                && (MotorSettings.hit(tile,event.getFace(),event.getHitVec())||MotorSettings.hit(tile,event.getFace(),event.getHitVec().subtract(new net.minecraft.util.math.Vec3d(event.getPos()))))
                && event.getFace().getAxis() != tile.axis()
                && !(tile.axis() != EnumFacing.Axis.Y && event.getFace() == EnumFacing.DOWN)) {
            event.setCanceled(true); event.setCancellationResult(EnumActionResult.SUCCESS);
        }
    }
    @SubscribeEvent public static void blocks(RegistryEvent.Register<Block> event) {
        SHAFT = new KineticBlock("shaft", false);
        MOTOR = new KineticBlock("creative_motor", true);
        TRACK = new TrainTrackBlock();
        RAILWAY_CASING=new TrainCasingBlock();SMALL_BOGEY=new RailwayBlock("small_bogey",RailwayBlock.Kind.BOGEY);
        TRAIN_CONTROLS=new ControlsBlock();TRACK_STATION=new RailwayBlock("track_station",RailwayBlock.Kind.STATION);
        event.getRegistry().registerAll(SHAFT,MOTOR,TRACK,RAILWAY_CASING,SMALL_BOGEY,TRAIN_CONTROLS,TRACK_STATION);
        for(var color:net.minecraft.item.EnumDyeColor.values()){var seat=new SeatBlock(color==net.minecraft.item.EnumDyeColor.SILVER?"light_gray":color.getName());SEATS.add(seat);event.getRegistry().register(seat);}
    }
    @SubscribeEvent public static void migrateItems(RegistryEvent.MissingMappings<Item> event){
        for(var mapping:event.getMappings())if(mapping.key.toString().equals("create:train_carriage"))mapping.remap(Item.getItemFromBlock(RAILWAY_CASING));
    }
    @SubscribeEvent public static void items(RegistryEvent.Register<Item> event) {
        for (Block block : new Block[] { SHAFT, MOTOR })
            event.getRegistry().register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
        event.getRegistry().registerAll(new TrackItem(),new TrainCasingItem(),new StationItem(),new ItemBlock(TRAIN_CONTROLS).setRegistryName(TRAIN_CONTROLS.getRegistryName()));
        for(var seat:SEATS)event.getRegistry().register(new ItemBlock(seat).setRegistryName(seat.getRegistryName()));
        SUPER_GLUE = new SuperGlueItem();
        event.getRegistry().register(SUPER_GLUE);
        WRENCH=new WrenchItem();GOGGLES=new GogglesItem();event.getRegistry().registerAll(WRENCH,GOGGLES);
    }
}
