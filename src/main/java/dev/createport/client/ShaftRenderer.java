package dev.createport.client;

import dev.createport.Create;
import dev.createport.kinetics.KineticTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;

public final class ShaftRenderer extends TileEntitySpecialRenderer<KineticTile> {
    @Override public void render(KineticTile tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + .5, y + .5, z + .5);
        int parity = (tile.axis() == EnumFacing.Axis.X ? 0 : tile.getPos().getX())
                + (tile.axis() == EnumFacing.Axis.Y ? 0 : tile.getPos().getY())
                + (tile.axis() == EnumFacing.Axis.Z ? 0 : tile.getPos().getZ());
        float angle = (((tile.getWorld().getTotalWorldTime() + partialTicks) * tile.rpm * .3f)
                + (parity % 2 == 0 ? 22.5f : 0)) % 360;
        if (tile.motor()) {
            EnumFacing facing = tile.facing();
            if (facing == EnumFacing.NORTH) GlStateManager.rotate(180, 0, 1, 0);
            if (facing == EnumFacing.EAST) GlStateManager.rotate(90, 0, 1, 0);
            if (facing == EnumFacing.WEST) GlStateManager.rotate(-90, 0, 1, 0);
            if (facing == EnumFacing.UP) GlStateManager.rotate(-90, 1, 0, 0);
            if (facing == EnumFacing.DOWN) GlStateManager.rotate(90, 1, 0, 0);
            float sign = facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 1 : -1;
            GlStateManager.rotate(sign * angle, 0, 0, 1);
            Minecraft mc = Minecraft.getMinecraft();
            mc.getRenderItem().renderItem(new ItemStack(Create.SHAFT), mc.getRenderItem().getItemModelMesher().getModelManager().getModel(new ModelResourceLocation("create:shaft_half", "inventory")));
        } else {
        if (tile.axis() == EnumFacing.Axis.X) GlStateManager.rotate(-90, 0, 0, 1);
        if (tile.axis() == EnumFacing.Axis.Z) GlStateManager.rotate(90, 1, 0, 0);
        GlStateManager.rotate(angle, 0, 1, 0);
        Minecraft.getMinecraft().getRenderItem().renderItem(new ItemStack(Create.SHAFT), ItemCameraTransforms.TransformType.NONE);
        }
        GlStateManager.popMatrix();
    }
}
