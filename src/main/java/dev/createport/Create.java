package dev.createport;

import dev.createport.kinetics.*;
import dev.createport.trains.*;
import dev.createport.glue.SuperGlueItem;
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

@Mod(modid = Create.ID, name = "Create", version = "0.2.0-alpha", acceptedMinecraftVersions = "[1.12.2]")
@Mod.EventBusSubscriber(modid = Create.ID)
public class Create {
    public static final String ID = "create";
    public static Create instance;
    public static KineticBlock SHAFT;
    public static KineticBlock MOTOR;
    public static TrainTrackBlock TRACK;
    public static TrainCarriageItem CARRIAGE;
    public static SuperGlueItem SUPER_GLUE;
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
        GameRegistry.registerTileEntity(KineticTile.class, new ResourceLocation(ID, "kinetic"));
        NETWORK.registerMessage(SpeedPacket.Handler.class, SpeedPacket.class, 0, Side.SERVER);
        NETWORK.registerMessage(TrainControlPacket.Handler.class,TrainControlPacket.class,1,Side.SERVER);
        EntityRegistry.registerModEntity(new ResourceLocation(ID,"train_carriage"),TrainEntity.class,
                "train_carriage",0,this,96,1,true);
        proxy.preInit();
    }
    @SubscribeEvent public static void interact(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntityPlayer().isSneaking() || event.getEntityPlayer().isSpectator()
                || !event.getEntityPlayer().capabilities.allowEdit || event.getFace() == null) return;
        if (event.getWorld().getTileEntity(event.getPos()) instanceof KineticTile tile && tile.motor()
                && event.getFace().getAxis() != tile.axis()
                && !(tile.axis() != EnumFacing.Axis.Y && event.getFace() == EnumFacing.DOWN)) {
            event.setCanceled(true); event.setCancellationResult(EnumActionResult.SUCCESS);
        }
    }
    @SubscribeEvent public static void blocks(RegistryEvent.Register<Block> event) {
        SHAFT = new KineticBlock("shaft", false);
        MOTOR = new KineticBlock("creative_motor", true);
        TRACK = new TrainTrackBlock();
        event.getRegistry().registerAll(SHAFT, MOTOR, TRACK);
    }
    @SubscribeEvent public static void items(RegistryEvent.Register<Item> event) {
        for (Block block : new Block[] { SHAFT, MOTOR, TRACK })
            event.getRegistry().register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
        CARRIAGE = new TrainCarriageItem();
        event.getRegistry().register(CARRIAGE);
        SUPER_GLUE = new SuperGlueItem();
        event.getRegistry().register(SUPER_GLUE);
    }
}
