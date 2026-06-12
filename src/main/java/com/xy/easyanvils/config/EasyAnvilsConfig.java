package com.xy.easyanvils.config;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * 1-to-1 port of Easy Anvils' ServerConfig + ClientConfig (puzzleslib) onto the
 * Forge 1.12.2 {@link Configuration} system. Every value the original exposes is a
 * config option here - nothing is hardcoded.
 */
public class EasyAnvilsConfig {

    // ---- prior_work_penalty ----
    public static PriorWorkPenalty priorWorkPenalty = PriorWorkPenalty.FIXED;
    public static int priorWorkPenaltyConstant = 4;
    public static RenameAndRepairCost renameAndRepairCosts = RenameAndRepairCost.FIXED;
    public static boolean penaltyFreeRenamesAndRepairs = true;

    // ---- costs ----
    public static int tooExpensiveLimit = -1;
    public static FreeRenames freeRenames = FreeRenames.ALL_ITEMS;
    public static int commonEnchantmentMultiplier = 1;
    public static int uncommonEnchantmentMultiplier = 2;
    public static int rareEnchantmentMultiplier = 4;
    public static int veryRareEnchantmentMultiplier = 8;
    public static boolean halvedBookCosts = true;
    public static int repairWithMaterialUnitCost = 1;
    public static int repairWithOtherItemCost = 2;

    // ---- miscellaneous ----
    public static boolean anvilRepairing = true;
    public static boolean editNameTagsNoAnvil = true;
    public static double anvilBreakChance = 0.05;
    public static boolean riskFreeAnvilRenaming = true;
    public static boolean renamingSupportsFormatting = true;
    public static boolean allowIncompatibleEnchantments = false;

    // ---- client ----
    public static boolean renderAnvilContents = true;

    private static final String CAT_PENALTY = "prior_work_penalty";
    private static final String CAT_COSTS = "costs";
    private static final String CAT_MISC = "miscellaneous";
    private static final String CAT_CLIENT = "client";

    private static Configuration config;

    public static Configuration getConfig() {
        return config;
    }

    public static void init(File configFile) {
        config = new Configuration(configFile);
        config.load();
        syncConfig();
    }

    /** Re-reads every value from the config object. Called on load and after in-game config edits. */
    public static void syncConfig() {
        if (config == null) {
            return;
        }

        priorWorkPenalty = PriorWorkPenalty.valueOf(config.getString(
                "priorWorkPenalty", CAT_PENALTY, PriorWorkPenalty.FIXED.name(),
                "Controls how working an item in the anvil multiple times affects the cost of future operations.\n"
                        + "FIXED: A constant value is added every time the item is worked.\n"
                        + "VANILLA: Penalty doubles every time an item is worked.\n"
                        + "DISABLED: Penalty stays at 0 and does not increase.",
                names(PriorWorkPenalty.values())));
        priorWorkPenaltyConstant = config.getInt(
                "priorWorkPenaltyConstant", CAT_PENALTY, 4, 1, Integer.MAX_VALUE,
                "Constant to use when \"priorWorkPenalty\" is set to \"FIXED\". Every subsequent operation will increase by this value in levels.");
        renameAndRepairCosts = RenameAndRepairCost.valueOf(config.getString(
                "renameAndRepairCosts", CAT_PENALTY, RenameAndRepairCost.FIXED.name(),
                "FIXED: When renaming / repairing, ignore any prior work penalty on the item. Makes prior work penalty only relevant when new enchantments are added.\n"
                        + "LIMITED: When renaming / repairing cost exceeds max anvil repair cost, limit cost just below max cost.\n"
                        + "VANILLA: Renaming / repairing increase with prior work penalty and will no longer be possible when max cost is exceeded.",
                names(RenameAndRepairCost.values())));
        penaltyFreeRenamesAndRepairs = config.getBoolean(
                "penaltyFreeRenamesAndRepairs", CAT_PENALTY, true,
                "Prevents the prior work penalty from increasing when the item has only been renamed or repaired.");

        tooExpensiveLimit = config.getInt(
                "tooExpensiveLimit", CAT_COSTS, -1, -1, Integer.MAX_VALUE,
                "Max cost of enchantment level allowed to be spent in an anvil. Every operation exceeding the limit will show as 'Too Expensive!' and will be disallowed.\n"
                        + "If set to '-1' the limit is disabled.\n"
                        + "Set to '40' enchantment levels in vanilla.");
        freeRenames = FreeRenames.valueOf(config.getString(
                "freeRenames", CAT_COSTS, FreeRenames.ALL_ITEMS.name(),
                "Renaming any item in an anvil no longer costs any enchantment levels at all. Can be restricted to only name tags.",
                names(FreeRenames.values())));
        commonEnchantmentMultiplier = config.getInt(
                "commonEnchantmentMultiplier", CAT_COSTS, 1, 0, Integer.MAX_VALUE,
                "Multiplier for each level of a common enchantment being applied.");
        uncommonEnchantmentMultiplier = config.getInt(
                "uncommonEnchantmentMultiplier", CAT_COSTS, 2, 1, Integer.MAX_VALUE,
                "Multiplier for each level of a uncommon enchantment being applied.");
        rareEnchantmentMultiplier = config.getInt(
                "rareEnchantmentMultiplier", CAT_COSTS, 4, 1, Integer.MAX_VALUE,
                "Multiplier for each level of a rare enchantment being applied.");
        veryRareEnchantmentMultiplier = config.getInt(
                "veryRareEnchantmentMultiplier", CAT_COSTS, 8, 1, Integer.MAX_VALUE,
                "Multiplier for each level of a very rare enchantment being applied.");
        halvedBookCosts = config.getBoolean(
                "halvedBookCosts", CAT_COSTS, true,
                "Costs for applying enchantments from enchanted books are halved.");
        repairWithMaterialUnitCost = config.getInt(
                "repairWithMaterialUnitCost", CAT_COSTS, 1, 0, Integer.MAX_VALUE,
                "The additional cost in levels for each valid repair material an item is repaired with in an anvil.");
        repairWithOtherItemCost = config.getInt(
                "repairWithOtherItemCost", CAT_COSTS, 2, 0, Integer.MAX_VALUE,
                "The additional cost in levels for combining an item with another item of the same kind when the first item is not fully repaired.");

        anvilRepairing = config.getBoolean(
                "anvilRepairing", CAT_MISC, true,
                "Allow using iron blocks to repair an anvil by one damage stage. Can be automated using dispensers.");
        editNameTagsNoAnvil = config.getBoolean(
                "editNameTagsNoAnvil", CAT_MISC, true,
                "Edit name tags without cost nor anvil, simply by sneak + right-clicking.");
        anvilBreakChance = config.get(CAT_MISC, "anvilBreakChance", 0.05,
                "Chance the anvil will break into chipped or damaged variant, or break completely after using. Value is set to 0.12 in vanilla.",
                0.0, 1.0).getDouble();
        riskFreeAnvilRenaming = config.getBoolean(
                "riskFreeAnvilRenaming", CAT_MISC, true,
                "Solely renaming items in an anvil will never cause the anvil to break.");
        renamingSupportsFormatting = config.getBoolean(
                "renamingSupportsFormatting", CAT_MISC, true,
                "The naming field in anvils and the name tag gui will support formatting codes for setting custom text colors and styles.\n"
                        + "Use the section sign or the ampersand (&) followed by a formatting code.");
        allowIncompatibleEnchantments = config.getBoolean(
                "allowIncompatibleEnchantments", CAT_MISC, false,
                "Allow normally mutually-exclusive enchantments to coexist on the same item via the anvil.\n"
                        + "e.g. Sharpness + Smite + Bane of Arthropods on a weapon, or every Protection on one piece of armor.");

        renderAnvilContents = config.getBoolean(
                "renderAnvilContents", CAT_CLIENT, true,
                "Render inventory contents of an anvil.");

        if (config.hasChanged()) {
            config.save();
        }
    }

    private static String[] names(Enum<?>[] values) {
        String[] out = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            out[i] = values[i].name();
        }
        return out;
    }

    // ---- behaviour helpers (ported from the original enums) ----

    /** Mirrors ModAnvilMenu.repairCostToRepairs - number of prior anvil operations encoded in a repair cost. */
    public static int repairCostToRepairs(int repairCost) {
        ++repairCost;
        int repairs = 0;
        while (repairCost >= 2) {
            repairCost /= 2;
            ++repairs;
        }
        return repairs;
    }

    public enum PriorWorkPenalty {
        DISABLED,
        VANILLA,
        FIXED;

        /** Transforms a base (summed) prior-work repair cost per the selected mode. */
        public int apply(int repairCost) {
            switch (this) {
                case DISABLED:
                    return 0;
                case VANILLA:
                    return repairCost;
                case FIXED:
                default:
                    return repairCostToRepairs(repairCost) * priorWorkPenaltyConstant;
            }
        }
    }

    public enum FreeRenames {
        OFF,
        ALL_ITEMS,
        NAME_TAGS_ONLY;

        public boolean test(ItemStack stack) {
            switch (this) {
                case ALL_ITEMS:
                    return true;
                case NAME_TAGS_ONLY:
                    return stack.getItem() == Items.NAME_TAG;
                case OFF:
                default:
                    return false;
            }
        }
    }

    public enum RenameAndRepairCost {
        VANILLA,
        FIXED,
        LIMITED
    }
}
