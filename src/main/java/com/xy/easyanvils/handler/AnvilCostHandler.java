package com.xy.easyanvils.handler;

import com.xy.easyanvils.config.EasyAnvilsConfig;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Forge 1.12.2 {@link AnvilUpdateEvent} handler that reproduces Easy Anvils'
 * config-driven anvil cost and result math — originally implemented by
 * {@code ModAnvilMenu.createResult} on 1.20.1.
 *
 * <p>Registered via {@code MinecraftForge.EVENT_BUS.register(new AnvilCostHandler())}.
 * Runs at {@link EventPriority#LOWEST} so higher-priority handlers can pre-empt.
 */
public class AnvilCostHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onAnvilUpdate(AnvilUpdateEvent event) {
        // Another handler already produced a result — don't override.
        if (!event.getOutput().isEmpty()) return;

        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        String itemName = event.getName();

        // Left slot empty → no result. Cancel so vanilla does not recompute its own result
        // (an empty, non-canceled AnvilUpdateEvent lets vanilla fall through to its native calc).
        if (left.isEmpty()) {
            return;
        }

        ItemStack output = left.copy();
        Map<Enchantment, Integer> outputEnchantments = EnchantmentHelper.getEnchantments(output);

        // === Prior-work penalty (config-driven) ===
        int baseRepairCost = left.getRepairCost() + (right.isEmpty() ? 0 : right.getRepairCost());
        baseRepairCost = EasyAnvilsConfig.priorWorkPenalty.apply(baseRepairCost);

        int materialCost = 0;
        boolean isBook = false;
        int repairOperationCost = 0;
        int enchantOperationCost = 0;
        int renameOperationCost = 0;

        if (!right.isEmpty()) {
            isBook = right.getItem() == Items.ENCHANTED_BOOK
                    && ItemEnchantedBook.getEnchantments(right).tagCount() > 0;

            if (output.isItemStackDamageable() && output.getItem().getIsRepairable(left, right)) {
                // --- Repair with material ---
                int damageToRepair = Math.min(output.getItemDamage(), output.getMaxDamage() / 4);
                if (damageToRepair <= 0) {
                    event.setCanceled(true);
                    return;
                }
                int repairCount;
                for (repairCount = 0; damageToRepair > 0 && repairCount < right.getCount(); ++repairCount) {
                    int newDamage = output.getItemDamage() - damageToRepair;
                    output.setItemDamage(newDamage);
                    repairOperationCost += EasyAnvilsConfig.repairWithMaterialUnitCost;
                    damageToRepair = Math.min(output.getItemDamage(), output.getMaxDamage() / 4);
                }
                materialCost = repairCount;
            } else {
                // --- Not a material repair — must be book or same-item combine ---
                if (!(isBook || (output.getItem() == right.getItem() && output.isItemStackDamageable()))) {
                    event.setCanceled(true);
                    return;
                }

                // --- Repair by combining two damageable items ---
                if (output.isItemStackDamageable() && !isBook) {
                    int leftDur = left.getMaxDamage() - left.getItemDamage();
                    int rightDur = right.getMaxDamage() - right.getItemDamage();
                    int combinedDur = leftDur + rightDur + output.getMaxDamage() * 12 / 100;
                    int newDamage = output.getMaxDamage() - combinedDur;
                    if (newDamage < 0) newDamage = 0;
                    if (newDamage < output.getItemDamage()) {
                        output.setItemDamage(newDamage);
                        repairOperationCost += EasyAnvilsConfig.repairWithOtherItemCost;
                    }
                }

                // --- Enchantment combining ---
                Map<Enchantment, Integer> rightEnchantments = EnchantmentHelper.getEnchantments(right);
                boolean compatFound = false;
                boolean incompatFound = false;

                for (Map.Entry<Enchantment, Integer> entry : rightEnchantments.entrySet()) {
                    Enchantment enchantment = entry.getKey();
                    if (enchantment == null) continue;

                    int currentLevel = outputEnchantments.getOrDefault(enchantment, 0);
                    int rightLevel = entry.getValue();
                    int newLevel = (currentLevel == rightLevel)
                            ? rightLevel + 1
                            : Math.max(rightLevel, currentLevel);

                    boolean canApply = enchantment.canApply(left);
                    // Book is always compatible (no creative on AnvilUpdateEvent).
                    if (left.getItem() == Items.ENCHANTED_BOOK) canApply = true;

                    // Mutually-exclusive enchantments (sharpness/smite/bane, protections, ...) normally
                    // block each other; the config option lets them coexist on one item.
                    if (!EasyAnvilsConfig.allowIncompatibleEnchantments) {
                        for (Enchantment existing : outputEnchantments.keySet()) {
                            if (existing == enchantment || enchantment.isCompatibleWith(existing)) continue;
                            canApply = false;
                            ++enchantOperationCost;
                        }
                    }

                    if (!canApply) {
                        if (repairOperationCost > 0) continue; // repair takes priority
                        incompatFound = true;
                        continue;
                    }
                    compatFound = true;

                    if (newLevel > enchantment.getMaxLevel()) newLevel = enchantment.getMaxLevel();

                    // Ensure level is never below the max already on either item.
                    int maxLevel = Math.max(
                            outputEnchantments.getOrDefault(enchantment, 0),
                            rightEnchantments.get(enchantment));
                    maxLevel = Math.max(maxLevel, newLevel);
                    if (maxLevel != newLevel) newLevel = maxLevel;

                    // --- Config-driven enchantment rarity multiplier ---
                    int rarityCostMultiplier;
                    switch (enchantment.getRarity()) {
                        case COMMON:
                            rarityCostMultiplier = EasyAnvilsConfig.commonEnchantmentMultiplier;
                            break;
                        case UNCOMMON:
                            rarityCostMultiplier = EasyAnvilsConfig.uncommonEnchantmentMultiplier;
                            break;
                        case RARE:
                            rarityCostMultiplier = EasyAnvilsConfig.rareEnchantmentMultiplier;
                            break;
                        case VERY_RARE:
                            rarityCostMultiplier = EasyAnvilsConfig.veryRareEnchantmentMultiplier;
                            break;
                        default:
                            rarityCostMultiplier = 1;
                    }

                    if (isBook && EasyAnvilsConfig.halvedBookCosts) {
                        rarityCostMultiplier = Math.max(1, rarityCostMultiplier / 2);
                    }

                    Integer oldLevel = outputEnchantments.put(enchantment, newLevel);
                    if (oldLevel == null || oldLevel != newLevel) {
                        enchantOperationCost += rarityCostMultiplier * newLevel;
                    }

                    // Multi-stack enchant → no result (no creative — AnvilUpdateEvent has no player).
                    if (left.getCount() > 1) {
                        event.setCanceled(true);
                        return;
                    }
                }

                if (incompatFound && !compatFound) {
                    event.setCanceled(true);
                    return;
                }
            }
        }

        // === Rename handling ===
        // 1.12.2 item names are plain §-formatted strings, so formatting support is just an
        // ampersand->section-sign conversion here (replaces the original ComponentDecomposer).
        if (itemName != null && EasyAnvilsConfig.renamingSupportsFormatting) {
            itemName = com.xy.easyanvils.util.FormattingHelper.toFormattingCodes(itemName);
        }
        boolean hasRenamedItem = false;
        if (StringUtils.isBlank(itemName)) {
            if (left.hasDisplayName()) {
                renameOperationCost = EasyAnvilsConfig.freeRenames.test(left) ? 0 : 1;
                hasRenamedItem = true;
                output.clearCustomName();
            }
        } else if (!itemName.equals(left.getDisplayName())) {
            renameOperationCost = EasyAnvilsConfig.freeRenames.test(left) ? 0 : 1;
            hasRenamedItem = true;
            output.setStackDisplayName(itemName);
        }

        // === Book-enchantability guard ===
        if (isBook && !output.getItem().isBookEnchantable(output, right)) {
            output = ItemStack.EMPTY;
        }

        // === Final cost computation (renameAndRepairCosts mode) ===
        int allOperationsCost = enchantOperationCost + repairOperationCost + renameOperationCost;
        int cost;
        if (allOperationsCost == 0) {
            cost = 0;
            if (!hasRenamedItem) output = ItemStack.EMPTY;
        } else if (enchantOperationCost == 0
                && EasyAnvilsConfig.renameAndRepairCosts == EasyAnvilsConfig.RenameAndRepairCost.FIXED) {
            // FIXED: ignore prior-work base penalty when only renaming/repairing.
            cost = allOperationsCost;
        } else {
            cost = baseRepairCost + allOperationsCost;
        }

        // === Too-expensive limit ===
        int maxCost = EasyAnvilsConfig.tooExpensiveLimit;
        boolean unlimited = (maxCost == -1);
        if (unlimited) maxCost = 40;
        if (cost >= maxCost) {
            if (enchantOperationCost == 0
                    && EasyAnvilsConfig.renameAndRepairCosts == EasyAnvilsConfig.RenameAndRepairCost.LIMITED) {
                cost = maxCost - 1;
            } else if (!unlimited) {
                output = ItemStack.EMPTY;
            }
        }

        // === No usable result (book not enchantable, too expensive, nothing to do) ===
        // Cancel rather than emit an empty output, otherwise vanilla recomputes its own result.
        if (output.isEmpty()) {
            event.setCanceled(true);
            return;
        }

        // === Finalise output ===
        if (!output.isEmpty()) {
            int outputRepairCost = output.getRepairCost();
            if (!right.isEmpty() && outputRepairCost < right.getRepairCost()) {
                outputRepairCost = right.getRepairCost();
            }
            // Penalty-free renames/repairs: skip the *2+1 prior-work bump.
            if (!(allOperationsCost <= 0
                    || (enchantOperationCost <= 0 && EasyAnvilsConfig.penaltyFreeRenamesAndRepairs))) {
                outputRepairCost = outputRepairCost * 2 + 1;
            }
            if (outputRepairCost > 0) {
                output.setRepairCost(outputRepairCost);
            }
            EnchantmentHelper.setEnchantments(outputEnchantments, output);
        }

        event.setOutput(output);
        event.setCost(cost);
        if (materialCost > 0) {
            event.setMaterialCost(materialCost);
        }
    }
}
