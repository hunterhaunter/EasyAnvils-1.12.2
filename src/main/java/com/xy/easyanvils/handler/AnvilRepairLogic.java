package com.xy.easyanvils.handler;

import net.minecraft.block.BlockAnvil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Shared "repair an anvil one damage stage with an iron block" logic, used by both the
 * right-click handler (player) and the dispenser behaviour (automation). 1.12.2 anvils are
 * a single block with a DAMAGE meta property (0 intact, 1 chipped, 2 damaged) - repairing
 * decrements DAMAGE, mirroring the original's chipped/damaged -> lower-tier transition.
 */
public final class AnvilRepairLogic {

    private AnvilRepairLogic() {
    }

    @Nullable
    public static IBlockState getRepairedState(IBlockState state) {
        if (!(state.getBlock() instanceof BlockAnvil)) {
            return null;
        }
        int damage = state.getValue(BlockAnvil.DAMAGE);
        if (damage <= 0) {
            return null;
        }
        return state.withProperty(BlockAnvil.DAMAGE, damage - 1);
    }

    /** Returns true if the anvil was repaired by one stage. Plays the vanilla "anvil used" effect. */
    public static boolean tryRepairAnvil(World world, BlockPos pos, IBlockState state) {
        IBlockState repaired = getRepairedState(state);
        if (repaired == null) {
            return false;
        }
        if (!world.isRemote) {
            world.setBlockState(pos, repaired, 2);
            world.playEvent(1030, pos, 0);
        }
        return true;
    }
}
