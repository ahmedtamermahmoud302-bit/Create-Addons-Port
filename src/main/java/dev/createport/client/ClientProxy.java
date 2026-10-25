package dev.createport.client;

import dev.createport.*;
import dev.createport.kinetics.KineticTile;
import dev.createport.trains.TrainEntity;
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
    @Override public void preInit() { ClientRegistry.bindTileEntitySpecialRenderer(KineticTile.class, new ShaftRenderer());
        RenderingRegistry.registerEntityRenderingHandler(TrainEntity.class,TrainRenderer::new);
    }
    @SubscribeEvent public static void models(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(Create.SUPER_GLUE,0,new ModelResourceLocation("create:super_glue","inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Create.TRACK),0,new ModelResourceLocation("create:track","inventory"));
        ModelLoader.setCustomModelResourceLocation(Create.CARRIAGE,0,new ModelResourceLocation("create:train_carriage","inventory"));
        ModelLoader.registerItemVariants(Item.getItemFromBlock(Create.SHAFT), new ResourceLocation("create:shaft_half"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Create.SHAFT), 0, new ModelResourceLocation("create:shaft", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Create.MOTOR), 0, new ModelResourceLocation("create:creative_motor", "inventory"));
    }
}
