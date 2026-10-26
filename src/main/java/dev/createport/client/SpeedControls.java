package dev.createport.client;

import dev.createport.Create;
import dev.createport.equipment.*;
import dev.createport.kinetics.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = Create.ID, value = Side.CLIENT)
public final class SpeedControls {
    private static KineticTile target() {
        Minecraft mc = Minecraft.getMinecraft();
        RayTraceResult hit = mc.objectMouseOver;
        if (mc.world == null || mc.player == null || hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) return null;
        return mc.world.getTileEntity(hit.getBlockPos()) instanceof KineticTile tile && tile.motor() ? tile : null;
    }
    private static int heldTicks;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft(); KineticTile tile = target();
        if (tile == null || !MotorSettings.hit(tile,mc.objectMouseOver.sideHit,mc.objectMouseOver.hitVec.subtract(new net.minecraft.util.math.Vec3d(tile.getPos()))) || WrenchItem.isHolding(mc.player) || mc.currentScreen != null || mc.player.isSneaking() || mc.player.isSpectator()
                || !mc.player.capabilities.allowEdit || !mc.gameSettings.keyBindUseItem.isKeyDown()
                || mc.objectMouseOver.sideHit.getAxis() == tile.axis()
                || (tile.axis() != net.minecraft.util.EnumFacing.Axis.Y && mc.objectMouseOver.sideHit == net.minecraft.util.EnumFacing.DOWN)) { heldTicks=0; return; }
        if (heldTicks++ == 5) { mc.displayGuiScreen(new SpeedSettingsScreen(tile.getPos(),tile.generatedRpm)); heldTicks=0; }
    }
}
