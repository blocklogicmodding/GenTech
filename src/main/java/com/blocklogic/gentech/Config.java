package com.blocklogic.gentech;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.List;
import java.util.ArrayList;

public class Config {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();

    public static ModConfigSpec COMMON_CONFIG;

    public static ModConfigSpec SPEC;

    // ========================================
    // CATEGORY CONSTANTS
    // ========================================

    public static final String CATEGORY_GENERATORS = "generators";
    public static final String CATEGORY_COLLECTORS = "collectors";
    public static final String CATEGORY_UPGRADES = "upgrades";
    public static final String CATEGORY_BLOCK_CATEGORIES = "block_categories";

    // ========================================
    // GENERATOR CONFIGURATION
    // ========================================

    // Copper Generator
    public static ModConfigSpec.IntValue COPPER_GENERATOR_SOFT_SPEED;
    public static ModConfigSpec.IntValue COPPER_GENERATOR_MEDIUM_SPEED;
    public static ModConfigSpec.IntValue COPPER_GENERATOR_HARD_SPEED;
    public static ModConfigSpec.IntValue COPPER_GENERATOR_SOFT_CONSUMPTION;
    public static ModConfigSpec.IntValue COPPER_GENERATOR_MEDIUM_CONSUMPTION;
    public static ModConfigSpec.IntValue COPPER_GENERATOR_HARD_CONSUMPTION;
    public static ModConfigSpec.IntValue COPPER_GENERATOR_FLUID_BUFFER;

    // Iron Generator
    public static ModConfigSpec.IntValue IRON_GENERATOR_SOFT_SPEED;
    public static ModConfigSpec.IntValue IRON_GENERATOR_MEDIUM_SPEED;
    public static ModConfigSpec.IntValue IRON_GENERATOR_HARD_SPEED;
    public static ModConfigSpec.IntValue IRON_GENERATOR_SOFT_CONSUMPTION;
    public static ModConfigSpec.IntValue IRON_GENERATOR_MEDIUM_CONSUMPTION;
    public static ModConfigSpec.IntValue IRON_GENERATOR_HARD_CONSUMPTION;
    public static ModConfigSpec.IntValue IRON_GENERATOR_FLUID_BUFFER;

    // Diamond Generator
    public static ModConfigSpec.IntValue DIAMOND_GENERATOR_SOFT_SPEED;
    public static ModConfigSpec.IntValue DIAMOND_GENERATOR_MEDIUM_SPEED;
    public static ModConfigSpec.IntValue DIAMOND_GENERATOR_HARD_SPEED;
    public static ModConfigSpec.IntValue DIAMOND_GENERATOR_SOFT_CONSUMPTION;
    public static ModConfigSpec.IntValue DIAMOND_GENERATOR_MEDIUM_CONSUMPTION;
    public static ModConfigSpec.IntValue DIAMOND_GENERATOR_HARD_CONSUMPTION;
    public static ModConfigSpec.IntValue DIAMOND_GENERATOR_FLUID_BUFFER;

    // Netherite Generator
    public static ModConfigSpec.IntValue NETHERITE_GENERATOR_SOFT_SPEED;
    public static ModConfigSpec.IntValue NETHERITE_GENERATOR_MEDIUM_SPEED;
    public static ModConfigSpec.IntValue NETHERITE_GENERATOR_HARD_SPEED;
    public static ModConfigSpec.IntValue NETHERITE_GENERATOR_SOFT_CONSUMPTION;
    public static ModConfigSpec.IntValue NETHERITE_GENERATOR_MEDIUM_CONSUMPTION;
    public static ModConfigSpec.IntValue NETHERITE_GENERATOR_HARD_CONSUMPTION;
    public static ModConfigSpec.IntValue NETHERITE_GENERATOR_FLUID_BUFFER;

    // ========================================
    // COLLECTOR CONFIGURATION
    // ========================================

    public static ModConfigSpec.IntValue COLLECTOR_FLUID_CAPACITY;
    public static ModConfigSpec.IntValue COLLECTOR_BASE_COLLECTION_TIME;
    public static ModConfigSpec.IntValue COLLECTOR_VALIDATION_INTERVAL;

    // ========================================
    // UPGRADE CONFIGURATION
    // ========================================

    // Speed Upgrades
    public static ModConfigSpec.DoubleValue BASIC_SPEED_UPGRADE_MULTIPLIER;
    public static ModConfigSpec.DoubleValue ADVANCED_SPEED_UPGRADE_MULTIPLIER;
    public static ModConfigSpec.DoubleValue ULTIMATE_SPEED_UPGRADE_MULTIPLIER;

    // Efficiency Upgrades
    public static ModConfigSpec.DoubleValue BASIC_EFFICIENCY_UPGRADE_REDUCTION;
    public static ModConfigSpec.DoubleValue ADVANCED_EFFICIENCY_UPGRADE_REDUCTION;
    public static ModConfigSpec.DoubleValue ULTIMATE_EFFICIENCY_UPGRADE_REDUCTION;

    // ========================================
    // BLOCK CATEGORIES
    // ========================================

    public static ModConfigSpec.ConfigValue<List<? extends String>> SOFT_GENERATABLE_BLOCKS;
    public static ModConfigSpec.ConfigValue<List<? extends String>> MEDIUM_GENERATABLE_BLOCKS;
    public static ModConfigSpec.ConfigValue<List<? extends String>> HARD_GENERATABLE_BLOCKS;

    public static void register(ModContainer container) {
        registerCommonConfigs(container);
    }

    private static void registerCommonConfigs(ModContainer container) {
        generatorConfig();
        collectorConfig();
        upgradeConfig();
        blockCategoryConfig();
        COMMON_CONFIG = COMMON_BUILDER.build();
        SPEC = COMMON_CONFIG; // Legacy compatibility
        container.registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG);
    }

    // ========================================
    // CONFIGURATION CATEGORY METHODS
    // ========================================

    private static void generatorConfig() {
        COMMON_BUILDER.comment("Generator Settings - speeds in ticks, consumption in millibuckets").push(CATEGORY_GENERATORS);

        // Copper Generator
        COMMON_BUILDER.comment("Copper Generator Configuration").push("copper_generator");
        COPPER_GENERATOR_SOFT_SPEED = COMMON_BUILDER.comment("Generation speed for soft blocks (ticks)")
                .defineInRange("soft_speed", 80, 1, 72000);
        COPPER_GENERATOR_MEDIUM_SPEED = COMMON_BUILDER.comment("Generation speed for medium blocks (ticks)")
                .defineInRange("medium_speed", 160, 1, 72000);
        COPPER_GENERATOR_HARD_SPEED = COMMON_BUILDER.comment("Generation speed for hard blocks (ticks)")
                .defineInRange("hard_speed", 320, 1, 72000);
        COPPER_GENERATOR_SOFT_CONSUMPTION = COMMON_BUILDER.comment("Fluid consumption for soft blocks (mb per operation)")
                .defineInRange("soft_consumption", 100, 1, 10000);
        COPPER_GENERATOR_MEDIUM_CONSUMPTION = COMMON_BUILDER.comment("Fluid consumption for medium blocks (mb per operation)")
                .defineInRange("medium_consumption", 80, 1, 10000);
        COPPER_GENERATOR_HARD_CONSUMPTION = COMMON_BUILDER.comment("Fluid consumption for hard blocks (mb per operation)")
                .defineInRange("hard_consumption", 50, 1, 10000);
        COPPER_GENERATOR_FLUID_BUFFER = COMMON_BUILDER.comment("Fluid buffer capacity (mb)")
                .defineInRange("fluid_buffer", 10000, 1000, 1000000);
        COMMON_BUILDER.pop();

        // Iron Generator
        COMMON_BUILDER.comment("Iron Generator Configuration").push("iron_generator");
        IRON_GENERATOR_SOFT_SPEED = COMMON_BUILDER.comment("Generation speed for soft blocks (ticks)")
                .defineInRange("soft_speed", 40, 1, 72000);
        IRON_GENERATOR_MEDIUM_SPEED = COMMON_BUILDER.comment("Generation speed for medium blocks (ticks)")
                .defineInRange("medium_speed", 80, 1, 72000);
        IRON_GENERATOR_HARD_SPEED = COMMON_BUILDER.comment("Generation speed for hard blocks (ticks)")
                .defineInRange("hard_speed", 160, 1, 72000);
        IRON_GENERATOR_SOFT_CONSUMPTION = COMMON_BUILDER.comment("Fluid consumption for soft blocks (mb per operation)")
                .defineInRange("soft_consumption", 80, 1, 10000);
        IRON_GENERATOR_MEDIUM_CONSUMPTION = COMMON_BUILDER.comment("Fluid consumption for medium blocks (mb per operation)")
                .defineInRange("medium_consumption", 60, 1, 10000);
        IRON_GENERATOR_HARD_CONSUMPTION = COMMON_BUILDER.comment("Fluid consumption for hard blocks (mb per operation)")
                .defineInRange("hard_consumption", 40, 1, 10000);
        IRON_GENERATOR_FLUID_BUFFER = COMMON_BUILDER.comment("Fluid buffer capacity (mb)")
                .defineInRange("fluid_buffer", 20000, 1000, 1000000);
        COMMON_BUILDER.pop();

        // Diamond Generator
        COMMON_BUILDER.comment("Diamond Generator Configuration").push("diamond_generator");
        DIAMOND_GENERATOR_SOFT_SPEED = COMMON_BUILDER.comment("Generation speed for soft blocks (ticks)")
                .defineInRange("soft_speed", 12, 1, 72000);
        DIAMOND_GENERATOR_MEDIUM_SPEED = COMMON_BUILDER.comment("Generation speed for medium blocks (ticks)")
                .defineInRange("medium_speed", 24, 1, 72000);
        DIAMOND_GENERATOR_HARD_SPEED = COMMON_BUILDER.comment("Generation speed for hard blocks (ticks)")
                .defineInRange("hard_speed", 48, 1, 72000);
        DIAMOND_GENERATOR_SOFT_CONSUMPTION = COMMON_BUILDER.comment("Fluid consumption for soft blocks (mb per operation)")
                .defineInRange("soft_consumption", 60, 1, 10000);
        DIAMOND_GENERATOR_MEDIUM_CONSUMPTION = COMMON_BUILDER.comment("Fluid consumption for medium blocks (mb per operation)")
                .defineInRange("medium_consumption", 40, 1, 10000);
        DIAMOND_GENERATOR_HARD_CONSUMPTION = COMMON_BUILDER.comment("Fluid consumption for hard blocks (mb per operation)")
                .defineInRange("hard_consumption", 30, 1, 10000);
        DIAMOND_GENERATOR_FLUID_BUFFER = COMMON_BUILDER.comment("Fluid buffer capacity (mb)")
                .defineInRange("fluid_buffer", 50000, 1000, 1000000);
        COMMON_BUILDER.pop();

        // Netherite Generator
        COMMON_BUILDER.comment("Netherite Generator Configuration").push("netherite_generator");
        NETHERITE_GENERATOR_SOFT_SPEED = COMMON_BUILDER.comment("Generation speed for soft blocks (ticks)")
                .defineInRange("soft_speed", 4, 1, 72000);
        NETHERITE_GENERATOR_MEDIUM_SPEED = COMMON_BUILDER.comment("Generation speed for medium blocks (ticks)")
                .defineInRange("medium_speed", 8, 1, 72000);
        NETHERITE_GENERATOR_HARD_SPEED = COMMON_BUILDER.comment("Generation speed for hard blocks (ticks)")
                .defineInRange("hard_speed", 16, 1, 72000);
        NETHERITE_GENERATOR_SOFT_CONSUMPTION = COMMON_BUILDER.comment("Fluid consumption for soft blocks (mb per operation)")
                .defineInRange("soft_consumption", 40, 1, 10000);
        NETHERITE_GENERATOR_MEDIUM_CONSUMPTION = COMMON_BUILDER.comment("Fluid consumption for medium blocks (mb per operation)")
                .defineInRange("medium_consumption", 20, 1, 10000);
        NETHERITE_GENERATOR_HARD_CONSUMPTION = COMMON_BUILDER.comment("Fluid consumption for hard blocks (mb per operation)")
                .defineInRange("hard_consumption", 20, 1, 10000);
        NETHERITE_GENERATOR_FLUID_BUFFER = COMMON_BUILDER.comment("Fluid buffer capacity (mb)")
                .defineInRange("fluid_buffer", 100000, 1000, 1000000);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.pop();
    }

    private static void collectorConfig() {
        COMMON_BUILDER.comment("Collector Settings - Configure default collection behavior").push(CATEGORY_COLLECTORS);

        COLLECTOR_FLUID_CAPACITY = COMMON_BUILDER.comment("Default fluid tank capacity for collectors (mb) - Note: individual recipes can override collection amounts")
                .defineInRange("fluid_capacity", 10000, 1000, 1000000);

        COLLECTOR_BASE_COLLECTION_TIME = COMMON_BUILDER.comment("Default base collection time in ticks when no recipe is found (20 ticks = 1 second)")
                .defineInRange("base_collection_time", 600, 1, 72000);

        COLLECTOR_VALIDATION_INTERVAL = COMMON_BUILDER.comment("How often to check for adjacent fluid sources (ticks)")
                .defineInRange("validation_interval", 20, 1, 600);

        COMMON_BUILDER.pop();
    }

    private static void upgradeConfig() {
        COMMON_BUILDER.comment("Upgrade Module Settings").push(CATEGORY_UPGRADES);

        // Speed Upgrades
        COMMON_BUILDER.comment("Speed Upgrade Configuration").push("speed_upgrades");
        BASIC_SPEED_UPGRADE_MULTIPLIER = COMMON_BUILDER.comment("Speed multiplier for Basic Speed Upgrade (reduces tick time by 20%)")
                .defineInRange("basic_speed_multiplier", 1.25, 0.1, 10.0);
        ADVANCED_SPEED_UPGRADE_MULTIPLIER = COMMON_BUILDER.comment("Speed multiplier for Advanced Speed Upgrade (reduces tick time by 33%)")
                .defineInRange("advanced_speed_multiplier", 1.5, 0.1, 10.0);
        ULTIMATE_SPEED_UPGRADE_MULTIPLIER = COMMON_BUILDER.comment("Speed multiplier for Ultimate Speed Upgrade (reduces tick time by 50%)")
                .defineInRange("ultimate_speed_multiplier", 2.0, 0.1, 10.0);
        COMMON_BUILDER.pop();

        // Efficiency Upgrades
        COMMON_BUILDER.comment("Efficiency Upgrade Configuration").push("efficiency_upgrades");
        BASIC_EFFICIENCY_UPGRADE_REDUCTION = COMMON_BUILDER.comment("Fluid consumption reduction for Basic Efficiency Upgrade (15%)")
                .defineInRange("basic_efficiency_reduction", 0.15, 0.0, 1.0);
        ADVANCED_EFFICIENCY_UPGRADE_REDUCTION = COMMON_BUILDER.comment("Fluid consumption reduction for Advanced Efficiency Upgrade (25%)")
                .defineInRange("advanced_efficiency_reduction", 0.25, 0.0, 1.0);
        ULTIMATE_EFFICIENCY_UPGRADE_REDUCTION = COMMON_BUILDER.comment("Fluid consumption reduction for Ultimate Efficiency Upgrade (40%)")
                .defineInRange("ultimate_efficiency_reduction", 0.40, 0.0, 1.0);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.pop();
    }

    private static void blockCategoryConfig() {
        COMMON_BUILDER.comment("Block Category Configuration - Add blocks using format 'modid:blockname'").push(CATEGORY_BLOCK_CATEGORIES);

        SOFT_GENERATABLE_BLOCKS = COMMON_BUILDER.comment("Soft generatable blocks (fastest speed, highest fluid cost)")
                .defineListAllowEmpty("soft_blocks", List.of(
                        "minecraft:cobblestone",
                        "minecraft:stone",
                        "minecraft:cobbled_deepslate",
                        "minecraft:tuff",
                        "minecraft:calcite",
                        "minecraft:netherrack"
                ), Config::validateBlockName);

        MEDIUM_GENERATABLE_BLOCKS = COMMON_BUILDER.comment("Medium generatable blocks (medium speed and fluid cost)")
                .defineListAllowEmpty("medium_blocks", List.of(
                        "minecraft:deepslate",
                        "minecraft:andesite",
                        "minecraft:diorite",
                        "minecraft:granite",
                        "minecraft:basalt",
                        "minecraft:end_stone"
                ), Config::validateBlockName);

        HARD_GENERATABLE_BLOCKS = COMMON_BUILDER.comment("Hard generatable blocks (slowest speed, lowest fluid cost)")
                .defineListAllowEmpty("hard_blocks", List.of(
                        "minecraft:smooth_basalt",
                        "minecraft:obsidian",
                        "minecraft:crying_obsidian",
                        "minecraft:dripstone_block",
                        "minecraft:blackstone",
                        "minecraft:smooth_stone"
                ), Config::validateBlockName);

        COMMON_BUILDER.pop();
    }

    // ========================================
    // GETTER METHODS FOR GENERATOR SETTINGS
    // ========================================

    // Copper Generator Getters
    public static int getCopperGeneratorSoftSpeed() {
        return COPPER_GENERATOR_SOFT_SPEED.get();
    }

    public static int getCopperGeneratorMediumSpeed() {
        return COPPER_GENERATOR_MEDIUM_SPEED.get();
    }

    public static int getCopperGeneratorHardSpeed() {
        return COPPER_GENERATOR_HARD_SPEED.get();
    }

    public static int getCopperGeneratorSoftConsumption() {
        return COPPER_GENERATOR_SOFT_CONSUMPTION.get();
    }

    public static int getCopperGeneratorMediumConsumption() {
        return COPPER_GENERATOR_MEDIUM_CONSUMPTION.get();
    }

    public static int getCopperGeneratorHardConsumption() {
        return COPPER_GENERATOR_HARD_CONSUMPTION.get();
    }

    public static int getCopperGeneratorFluidBuffer() {
        return COPPER_GENERATOR_FLUID_BUFFER.get();
    }

    // Iron Generator Getters
    public static int getIronGeneratorSoftSpeed() {
        return IRON_GENERATOR_SOFT_SPEED.get();
    }

    public static int getIronGeneratorMediumSpeed() {
        return IRON_GENERATOR_MEDIUM_SPEED.get();
    }

    public static int getIronGeneratorHardSpeed() {
        return IRON_GENERATOR_HARD_SPEED.get();
    }

    public static int getIronGeneratorSoftConsumption() {
        return IRON_GENERATOR_SOFT_CONSUMPTION.get();
    }

    public static int getIronGeneratorMediumConsumption() {
        return IRON_GENERATOR_MEDIUM_CONSUMPTION.get();
    }

    public static int getIronGeneratorHardConsumption() {
        return IRON_GENERATOR_HARD_CONSUMPTION.get();
    }

    public static int getIronGeneratorFluidBuffer() {
        return IRON_GENERATOR_FLUID_BUFFER.get();
    }

    // Diamond Generator Getters
    public static int getDiamondGeneratorSoftSpeed() {
        return DIAMOND_GENERATOR_SOFT_SPEED.get();
    }

    public static int getDiamondGeneratorMediumSpeed() {
        return DIAMOND_GENERATOR_MEDIUM_SPEED.get();
    }

    public static int getDiamondGeneratorHardSpeed() {
        return DIAMOND_GENERATOR_HARD_SPEED.get();
    }

    public static int getDiamondGeneratorSoftConsumption() {
        return DIAMOND_GENERATOR_SOFT_CONSUMPTION.get();
    }

    public static int getDiamondGeneratorMediumConsumption() {
        return DIAMOND_GENERATOR_MEDIUM_CONSUMPTION.get();
    }

    public static int getDiamondGeneratorHardConsumption() {
        return DIAMOND_GENERATOR_HARD_CONSUMPTION.get();
    }

    public static int getDiamondGeneratorFluidBuffer() {
        return DIAMOND_GENERATOR_FLUID_BUFFER.get();
    }

    // Netherite Generator Getters
    public static int getNetheriteGeneratorSoftSpeed() {
        return NETHERITE_GENERATOR_SOFT_SPEED.get();
    }

    public static int getNetheriteGeneratorMediumSpeed() {
        return NETHERITE_GENERATOR_MEDIUM_SPEED.get();
    }

    public static int getNetheriteGeneratorHardSpeed() {
        return NETHERITE_GENERATOR_HARD_SPEED.get();
    }

    public static int getNetheriteGeneratorSoftConsumption() {
        return NETHERITE_GENERATOR_SOFT_CONSUMPTION.get();
    }

    public static int getNetheriteGeneratorMediumConsumption() {
        return NETHERITE_GENERATOR_MEDIUM_CONSUMPTION.get();
    }

    public static int getNetheriteGeneratorHardConsumption() {
        return NETHERITE_GENERATOR_HARD_CONSUMPTION.get();
    }

    public static int getNetheriteGeneratorFluidBuffer() {
        return NETHERITE_GENERATOR_FLUID_BUFFER.get();
    }

    // ========================================
    // GETTER METHODS FOR COLLECTOR SETTINGS
    // ========================================

    public static int getCollectorFluidCapacity() {
        return COLLECTOR_FLUID_CAPACITY.get();
    }

    public static int getCollectorBaseCollectionTime() {
        return COLLECTOR_BASE_COLLECTION_TIME.get();
    }

    public static int getCollectorValidationInterval() {
        return COLLECTOR_VALIDATION_INTERVAL.get();
    }

    // ========================================
    // GETTER METHODS FOR UPGRADE SETTINGS
    // ========================================

    // Speed Upgrade Getters
    public static double getBasicSpeedUpgradeMultiplier() {
        return BASIC_SPEED_UPGRADE_MULTIPLIER.get();
    }

    public static double getAdvancedSpeedUpgradeMultiplier() {
        return ADVANCED_SPEED_UPGRADE_MULTIPLIER.get();
    }

    public static double getUltimateSpeedUpgradeMultiplier() {
        return ULTIMATE_SPEED_UPGRADE_MULTIPLIER.get();
    }

    // Efficiency Upgrade Getters
    public static double getBasicEfficiencyUpgradeReduction() {
        return BASIC_EFFICIENCY_UPGRADE_REDUCTION.get();
    }

    public static double getAdvancedEfficiencyUpgradeReduction() {
        return ADVANCED_EFFICIENCY_UPGRADE_REDUCTION.get();
    }

    public static double getUltimateEfficiencyUpgradeReduction() {
        return ULTIMATE_EFFICIENCY_UPGRADE_REDUCTION.get();
    }

    // ========================================
    // BLOCK CATEGORY GETTERS
    // ========================================

    @SuppressWarnings("unchecked")
    public static List<String> getSoftGeneratableBlocks() {
        return (List<String>) SOFT_GENERATABLE_BLOCKS.get();
    }

    @SuppressWarnings("unchecked")
    public static List<String> getMediumGeneratableBlocks() {
        return (List<String>) MEDIUM_GENERATABLE_BLOCKS.get();
    }

    @SuppressWarnings("unchecked")
    public static List<String> getHardGeneratableBlocks() {
        return (List<String>) HARD_GENERATABLE_BLOCKS.get();
    }

    public static List<String> getValidatedSoftGeneratableBlocks() {
        return validateBlockNames(getSoftGeneratableBlocks(), "soft");
    }

    public static List<String> getValidatedMediumGeneratableBlocks() {
        return validateBlockNames(getMediumGeneratableBlocks(), "medium");
    }

    public static List<String> getValidatedHardGeneratableBlocks() {
        return validateBlockNames(getHardGeneratableBlocks(), "hard");
    }

    // ========================================
    // VALIDATION METHODS
    // ========================================

    private static boolean validateBlockName(Object obj) {
        if (!(obj instanceof String blockName)) {
            return false;
        }

        if (blockName.isEmpty()) {
            return false;
        }

        try {
            ResourceLocation location = ResourceLocation.parse(blockName);
            return BuiltInRegistries.BLOCK.containsKey(location);
        } catch (Exception e) {
            return false;
        }
    }

    private static List<String> validateBlockNames(List<? extends String> blockList, String categoryName) {
        List<String> validBlocks = new ArrayList<>();
        int invalidCount = 0;

        for (String blockName : blockList) {
            if (validateBlockName(blockName)) {
                validBlocks.add(blockName);
            } else {
                invalidCount++;
            }
        }

        if (invalidCount > 0) {
            LOGGER.warn("Filtered out {} invalid block entries from {} generatable blocks category", invalidCount, categoryName);
        }

        return validBlocks;
    }

    public static void loadConfig() {
        LOGGER.info("GenTech configs reloaded");

        int totalSoft = getSoftGeneratableBlocks().size();
        int validSoft = getValidatedSoftGeneratableBlocks().size();
        if (totalSoft != validSoft) {
            LOGGER.warn("Soft blocks: {}/{} entries are valid", validSoft, totalSoft);
        }

        int totalMedium = getMediumGeneratableBlocks().size();
        int validMedium = getValidatedMediumGeneratableBlocks().size();
        if (totalMedium != validMedium) {
            LOGGER.warn("Medium blocks: {}/{} entries are valid", validMedium, totalMedium);
        }

        int totalHard = getHardGeneratableBlocks().size();
        int validHard = getValidatedHardGeneratableBlocks().size();
        if (totalHard != validHard) {
            LOGGER.warn("Hard blocks: {}/{} entries are valid", validHard, totalHard);
        }
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        LOGGER.info("GenTech configuration loaded");
        logConfigValues();
    }

    private static void logConfigValues() {
        LOGGER.info("Generator Configuration:");
        LOGGER.info("  Copper Generator - Soft: {}t/{}mb, Medium: {}t/{}mb, Hard: {}t/{}mb, Buffer: {}mb",
                getCopperGeneratorSoftSpeed(), getCopperGeneratorSoftConsumption(),
                getCopperGeneratorMediumSpeed(), getCopperGeneratorMediumConsumption(),
                getCopperGeneratorHardSpeed(), getCopperGeneratorHardConsumption(),
                getCopperGeneratorFluidBuffer());
        LOGGER.info("  Iron Generator - Soft: {}t/{}mb, Medium: {}t/{}mb, Hard: {}t/{}mb, Buffer: {}mb",
                getIronGeneratorSoftSpeed(), getIronGeneratorSoftConsumption(),
                getIronGeneratorMediumSpeed(), getIronGeneratorMediumConsumption(),
                getIronGeneratorHardSpeed(), getIronGeneratorHardConsumption(),
                getIronGeneratorFluidBuffer());
        LOGGER.info("  Diamond Generator - Soft: {}t/{}mb, Medium: {}t/{}mb, Hard: {}t/{}mb, Buffer: {}mb",
                getDiamondGeneratorSoftSpeed(), getDiamondGeneratorSoftConsumption(),
                getDiamondGeneratorMediumSpeed(), getDiamondGeneratorMediumConsumption(),
                getDiamondGeneratorHardSpeed(), getDiamondGeneratorHardConsumption(),
                getDiamondGeneratorFluidBuffer());
        LOGGER.info("  Netherite Generator - Soft: {}t/{}mb, Medium: {}t/{}mb, Hard: {}t/{}mb, Buffer: {}mb",
                getNetheriteGeneratorSoftSpeed(), getNetheriteGeneratorSoftConsumption(),
                getNetheriteGeneratorMediumSpeed(), getNetheriteGeneratorMediumConsumption(),
                getNetheriteGeneratorHardSpeed(), getNetheriteGeneratorHardConsumption(),
                getNetheriteGeneratorFluidBuffer());

        LOGGER.info("Collector Configuration:");
        LOGGER.info("  Fluid Capacity: {}mb, Base Collection Time: {}t, Validation Interval: {}t",
                getCollectorFluidCapacity(), getCollectorBaseCollectionTime(), getCollectorValidationInterval());

        LOGGER.info("Upgrade Configuration:");
        LOGGER.info("  Speed Upgrades - Basic: {}x, Advanced: {}x, Ultimate: {}x",
                getBasicSpeedUpgradeMultiplier(), getAdvancedSpeedUpgradeMultiplier(), getUltimateSpeedUpgradeMultiplier());
        LOGGER.info("  Efficiency Upgrades - Basic: {}%, Advanced: {}%, Ultimate: {}%",
                getBasicEfficiencyUpgradeReduction() * 100, getAdvancedEfficiencyUpgradeReduction() * 100, getUltimateEfficiencyUpgradeReduction() * 100);

        LOGGER.info("Block Categories:");
        LOGGER.info("  Soft blocks: {} configured", getValidatedSoftGeneratableBlocks().size());
        LOGGER.info("  Medium blocks: {} configured", getValidatedMediumGeneratableBlocks().size());
        LOGGER.info("  Hard blocks: {} configured", getValidatedHardGeneratableBlocks().size());
    }
}