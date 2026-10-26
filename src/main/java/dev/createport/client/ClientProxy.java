package dev.createport.client;

import dev.createport.*;
import dev.createport.kinetics.KineticTile;
import dev.createport.trains.*;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Create.ID, value = Side.CLIENT)
public final class ClientProxy extends CommonProxy {
    @Override public void preInit() {
        RenderingRegistry.registerEntityRenderingHandler(SeatEntity.class,manager->new net.minecraft.client.renderer.entity.Render<SeatEntity>(manager){protected ResourceLocation getEntityTexture(SeatEntity e){return net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE;}}); ClientRegistry.bindTileEntitySpecialRenderer(KineticTile.class, new ShaftRenderer());
        RenderingRegistry.registerEntityRenderingHandler(TrainEntity.class,TrainRenderer::new);
        ClientRegistry.bindTileEntitySpecialRenderer(TrackRenderTile.class,new AnchorTrackRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(BogeyTile.class,new WorldBogeyRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TrainStationTile.class,new StationRenderer());
    }
    @Override public void acceptGlueData(net.minecraft.nbt.NBTTagCompound data){GlueVisuals.accept(data);}
    @Override public void openStation(net.minecraft.util.math.BlockPos pos){net.minecraft.client.Minecraft.getMinecraft().displayGuiScreen(new StationScreen(pos));}
    @SubscribeEvent public static void stitch(net.minecraftforge.client.event.TextureStitchEvent.Pre event){
        event.getMap().registerSprite(new ResourceLocation("create:block/railway_casing_connected"));event.getMap().registerSprite(new ResourceLocation("create:block/railway_casing_side_connected"));
    }
    @SubscribeEvent public static void bake(net.minecraftforge.client.event.ModelBakeEvent event){
        var atlas=net.minecraft.client.Minecraft.getMinecraft().getTextureMapBlocks();
        for(var modelKey:event.getModelRegistry().getKeys())if(modelKey.getNamespace().equals("create")&&modelKey.getPath().equals("railway_casing")){
            var original=event.getModelRegistry().getObject(modelKey);event.getModelRegistry().putObject(modelKey,new CasingModel(original,atlas.getAtlasSprite("create:block/railway_casing_connected"),atlas.getAtlasSprite("create:block/railway_casing_side_connected")));
        }
        var key=new ModelResourceLocation("create:goggles","inventory");var worn=new ModelResourceLocation("create:goggles_worn","inventory");
        event.getModelRegistry().putObject(key,new GogglesModel(event.getModelRegistry().getObject(key),event.getModelRegistry().getObject(worn)));
    }
    @SubscribeEvent public static void models(ModelRegistryEvent event) {
        for(String part:new String[]{"track_station_flag_on","track_station_flag_off","track_station_flag_assemble","track_overlay_station","track_overlay_assembling"})ModelLoader.registerItemVariants(Item.getItemFromBlock(Create.TRACK_STATION),new ResourceLocation("create:partial_"+part));
        for(var seat:Create.SEATS)ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(seat),0,new ModelResourceLocation(seat.getRegistryName(),"inventory"));
        for(var block:new net.minecraft.block.Block[]{Create.RAILWAY_CASING,Create.TRAIN_CONTROLS,Create.TRACK_STATION})
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block),0,new ModelResourceLocation(block.getRegistryName(),"inventory"));
        ModelLoader.setCustomModelResourceLocation(Create.WRENCH,0,new ModelResourceLocation("create:wrench","inventory"));
        ModelLoader.setCustomModelResourceLocation(Create.GOGGLES,0,new ModelResourceLocation("create:goggles","inventory"));
        ModelLoader.registerItemVariants(Create.GOGGLES,new ResourceLocation("create:goggles_worn"));
        ModelLoader.setCustomModelResourceLocation(Create.SUPER_GLUE,0,new ModelResourceLocation("create:super_glue","inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Create.TRACK),0,new ModelResourceLocation("create:track","inventory"));
        ModelLoader.registerItemVariants(Item.getItemFromBlock(Create.SHAFT), new ResourceLocation("create:shaft_half"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Create.SHAFT), 0, new ModelResourceLocation("create:shaft", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Create.MOTOR), 0, new ModelResourceLocation("create:creative_motor", "inventory"));
    }
}
