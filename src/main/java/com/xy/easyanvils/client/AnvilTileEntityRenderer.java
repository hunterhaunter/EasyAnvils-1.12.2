package com.xy.easyanvils.client;

import com.xy.easyanvils.config.EasyAnvilsConfig;
import com.xy.easyanvils.tileentity.TileEntityAnvil;

import net.minecraft.block.BlockAnvil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

/**
 * Renders the two stored input items lying flat on top of an anvil - slot 0 ("main") and slot 1
 * ("combine") at the two ends of the bar, separated along the axis perpendicular to the anvil's
 * facing. Toggled by the {@code renderAnvilContents} client option.
 */
public class AnvilTileEntityRenderer extends TileEntitySpecialRenderer<TileEntityAnvil> {

    private static final double TOP_Y = 1.0D;        // anvil top face height
    // Push each item toward the anvil edge (4px from centre) plus 1px = 5px = 0.3125.
    private static final double SPREAD = 0.3125D;
    private static final float ITEM_SCALE = 0.5F;

    @Override
    public void render(TileEntityAnvil te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        if (!EasyAnvilsConfig.renderAnvilContents || te.isEmpty() || te.getWorld() == null) {
            return;
        }
        IBlockState state = te.getWorld().getBlockState(te.getPos());
        if (!(state.getBlock() instanceof BlockAnvil)) {
            return;
        }
        EnumFacing facing = state.getValue(BlockAnvil.FACING);
        renderFlatItem(0, te.getStackInSlot(0), facing, x, y, z);
        renderFlatItem(1, te.getStackInSlot(1), facing, x, y, z);
    }

    private void renderFlatItem(int index, ItemStack stack, EnumFacing facing, double x, double y, double z) {
        if (stack.isEmpty()) {
            return;
        }
        // The two items sit side by side along the anvil's wide top axis (along its facing).
        EnumFacing bar = facing;
        double sign = index == 0 ? -1.0D : 1.0D; // slot 0 (sword) one side, slot 1 (book) the other
        double offX = bar.getXOffset() * SPREAD * sign;
        double offZ = bar.getZOffset() * SPREAD * sign;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D + offX, y + TOP_Y + 0.02D, z + 0.5D + offZ);
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);                 // lay flat on the top face
        // align with anvil + face the player. N/S (Z axis) need an extra 180 vs E/W (X axis).
        float yaw = -facing.getHorizontalAngle() + 90.0F;
        if (facing.getAxis() == EnumFacing.Axis.Z) {
            yaw += 180.0F;
        }
        GlStateManager.rotate(yaw, 0.0F, 0.0F, 1.0F);
        GlStateManager.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);

        RenderHelper.enableStandardItemLighting();
        Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
        RenderHelper.disableStandardItemLighting();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }
}
