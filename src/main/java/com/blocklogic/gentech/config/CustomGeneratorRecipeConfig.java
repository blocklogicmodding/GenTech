package com.blocklogic.gentech.config;

import com.blocklogic.gentech.Config;
import com.blocklogic.gentech.block.entity.GeneratorBlockEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class CustomGeneratorRecipeConfig {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String CONFIG_DIR = "gentech";
    private static final String RECIPES_FILE = "custom_generator_recipes.toml";
    private static final String ERROR_DIR = "config_errors";

    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    private static Map<String, CustomGeneratorRecipe> loadedRecipes = new LinkedHashMap<>();
    private static Map<String, List<CustomGeneratorRecipe>> recipesByBlock = new HashMap<>();

    // Error tracking - only used when errors occur
    private static final AtomicInteger errorCounter = new AtomicInteger(0);
    private static Path currentErrorFile = null;
    private static final List<String> currentSessionErrors = new ArrayList<>();

    public static class CustomGeneratorRecipe {
        public final String name;
        public final Block catalyst;
        public final Fluid fluid1;
        public final Fluid fluid2;
        public final GeneratorBlockEntity.BlockCategory category;

        public CustomGeneratorRecipe(String name, Block catalyst, Fluid fluid1, Fluid fluid2, GeneratorBlockEntity.BlockCategory category) {
            this.name = name;
            this.catalyst = catalyst;
            this.fluid1 = fluid1;
            this.fluid2 = fluid2;
            this.category = category;
        }
    }

    public static void loadRecipes() {
        // Reset error tracking for this session
        currentSessionErrors.clear();
        errorCounter.set(0);
        currentErrorFile = null;

        loadedRecipes.clear();
        recipesByBlock.clear();

        Path configDir = FMLPaths.CONFIGDIR.get().resolve(CONFIG_DIR);
        Path recipesFile = configDir.resolve(RECIPES_FILE);

        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            if (!Files.exists(recipesFile)) {
                createDefaultRecipesFile(recipesFile);
                return;
            }

            parseRecipesFile(recipesFile);
            generateDefaultRecipes();

        } catch (Exception e) {
            LOGGER.error("Failed to load custom generator recipes: {}", e.getMessage(), e);
            logError("Critical error loading recipes file", e.getMessage(), 0);
        }

        // Only show error summary and create log if there were actual errors
        if (!currentSessionErrors.isEmpty()) {
            LOGGER.warn("Recipe loading completed with {} errors. Check error log: {}",
                    currentSessionErrors.size(), currentErrorFile);
            finalizeErrorLog();
        } else {
            LOGGER.info("Recipe loading completed successfully with no errors.");
        }

        LOGGER.info("Loaded {} custom generator recipes", loadedRecipes.size());
    }

    private static void generateDefaultRecipes() {
        addDefaultRecipesForCategory("soft", Config.getValidatedSoftGeneratableBlocks(), GeneratorBlockEntity.BlockCategory.SOFT);
        addDefaultRecipesForCategory("medium", Config.getValidatedMediumGeneratableBlocks(), GeneratorBlockEntity.BlockCategory.MEDIUM);
        addDefaultRecipesForCategory("hard", Config.getValidatedHardGeneratableBlocks(), GeneratorBlockEntity.BlockCategory.HARD);

        LOGGER.info("Generated {} default recipes", loadedRecipes.size());
    }

    private static void addDefaultRecipesForCategory(String categoryName, List<String> blockList, GeneratorBlockEntity.BlockCategory category) {
        for (String blockId : blockList) {
            Block block = parseBlock(blockId);
            if (block != null) {
                String recipeName = categoryName + "_" + blockId.replace(":", "_");
                CustomGeneratorRecipe recipe = new CustomGeneratorRecipe(
                        recipeName,
                        block,
                        Fluids.WATER,
                        Fluids.LAVA,
                        category
                );
                loadedRecipes.put(recipeName, recipe);

                String blockKey = BuiltInRegistries.BLOCK.getKey(block).toString();
                recipesByBlock.computeIfAbsent(blockKey, k -> new ArrayList<>()).add(recipe);
            } else {
                logError("Default recipe generation", "Failed to parse block: " + blockId, 0);
            }
        }
    }

    private static void createDefaultRecipesFile(Path recipesFile) throws IOException {
        String defaultContent = """
                # GenTech Custom Generator Recipes
                # Define what blocks can be generated with which fluid combinations
                # 
                # Recipe format:
                # [[recipes]]
                # name = "unique_recipe_name"           # Must be unique and alphanumeric with underscores
                # catalyst = "modid:block_name"         # The block that will be generated. It is also the block that must be placed under the generator.
                # fluid1 = "modid:fluid_name"           # First required fluid
                # fluid2 = "modid:fluid_name"           # Second required fluid
                # category = "soft"                     # Category: soft, medium, or hard (required) - determines generation speed and fluid consumption from main config
                #
                # Categories determine generation speed and fluid consumption (see main config)
                # Multiple recipes can use the same catalyst with different fluids
                # Conflicts resolved alphabetically by recipe name (each recipe name must be unique)
                
                # Example custom recipe (commented out):
                # [[recipes]]
                # name = "custom_obsidian_recipe"
                # catalyst = "minecraft:obsidian"
                # fluid1 = "minecraft:water"
                # fluid2 = "minecraft:lava"
                # category = "hard"
                """;

        Files.writeString(recipesFile, defaultContent);
        LOGGER.info("Created default custom generator recipes file at: {}", recipesFile);
    }

    private static void parseRecipesFile(Path recipesFile) throws IOException {
        List<String> lines = Files.readAllLines(recipesFile);
        List<Map<String, String>> recipes = parseTomlRecipes(lines);

        for (Map<String, String> recipeData : recipes) {
            try {
                CustomGeneratorRecipe recipe = parseRecipe(recipeData);
                if (recipe != null) {
                    loadedRecipes.put(recipe.name, recipe);

                    String blockKey = BuiltInRegistries.BLOCK.getKey(recipe.catalyst).toString();
                    recipesByBlock.computeIfAbsent(blockKey, k -> new ArrayList<>()).add(recipe);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to parse recipe: {}", e.getMessage());
                logError("Recipe parsing error", e.getMessage(), 0);
            }
        }

        recipesByBlock.values().forEach(list -> list.sort(Comparator.comparing(r -> r.name)));
    }

    private static List<Map<String, String>> parseTomlRecipes(List<String> lines) {
        List<Map<String, String>> recipes = new ArrayList<>();
        Map<String, String> currentRecipe = null;
        int lineNumber = 0;

        for (String line : lines) {
            lineNumber++;
            String originalLine = line;
            line = line.trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            if (line.equals("[[recipes]]")) {
                if (currentRecipe != null) {
                    recipes.add(currentRecipe);
                }
                currentRecipe = new HashMap<>();
                continue;
            }

            if (currentRecipe != null && line.contains("=")) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();

                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }

                    currentRecipe.put(key, value);
                } else {
                    logError("TOML parsing error", "Invalid line format: " + originalLine, lineNumber);
                }
            } else if (currentRecipe != null) {
                logError("TOML parsing error", "Unexpected line outside recipe section: " + originalLine, lineNumber);
            }
        }

        if (currentRecipe != null) {
            recipes.add(currentRecipe);
        }

        return recipes;
    }

    private static CustomGeneratorRecipe parseRecipe(Map<String, String> recipeData) {
        String name = recipeData.get("name");
        String catalystId = recipeData.get("catalyst");
        String fluid1Id = recipeData.get("fluid1");
        String fluid2Id = recipeData.get("fluid2");
        String categoryStr = recipeData.get("category");

        if (name == null || name.isEmpty()) {
            logError("Recipe validation error", "Missing or empty 'name' field", 0);
            return null;
        }

        if (!VALID_NAME_PATTERN.matcher(name).matches()) {
            logError("Recipe validation error", "Invalid name '" + name + "' - use only letters, numbers, and underscores", 0);
            return null;
        }

        if (loadedRecipes.containsKey(name)) {
            logError("Recipe validation error", "Duplicate recipe name: " + name, 0);
            return null;
        }

        Block catalyst = parseBlock(catalystId);
        if (catalyst == null) {
            logError("Recipe validation error", "Invalid or missing catalyst: " + catalystId, 0);
            return null;
        }

        Fluid fluid1 = parseFluid(fluid1Id);
        if (fluid1 == null) {
            logError("Recipe validation error", "Invalid or missing fluid1: " + fluid1Id, 0);
            return null;
        }

        Fluid fluid2 = parseFluid(fluid2Id);
        if (fluid2 == null) {
            logError("Recipe validation error", "Invalid or missing fluid2: " + fluid2Id, 0);
            return null;
        }

        GeneratorBlockEntity.BlockCategory category;
        try {
            category = GeneratorBlockEntity.BlockCategory.valueOf(categoryStr.toUpperCase());
        } catch (Exception e) {
            logError("Recipe validation error", "Invalid category '" + categoryStr + "' - must be: soft, medium, or hard", 0);
            return null;
        }

        return new CustomGeneratorRecipe(name, catalyst, fluid1, fluid2, category);
    }

    private static Block parseBlock(String blockId) {
        if (blockId == null || blockId.isEmpty()) {
            return null;
        }

        try {
            ResourceLocation location = ResourceLocation.parse(blockId);
            Block block = BuiltInRegistries.BLOCK.get(location);

            if (block == null || block == net.minecraft.world.level.block.Blocks.AIR) {
                return null;
            }

            return block;
        } catch (Exception e) {
            return null;
        }
    }

    private static Fluid parseFluid(String fluidId) {
        if (fluidId == null || fluidId.isEmpty()) {
            return null;
        }

        try {
            ResourceLocation location = ResourceLocation.parse(fluidId);
            Fluid fluid = BuiltInRegistries.FLUID.get(location);

            if (fluid == null || fluid == net.minecraft.world.level.material.Fluids.EMPTY) {
                return null;
            }

            return fluid;
        } catch (Exception e) {
            return null;
        }
    }

    private static void logError(String errorType, String message, int lineNumber) {
        int errorNum = errorCounter.incrementAndGet();
        String fullMessage = String.format("[Error #%d] %s: %s%s",
                errorNum, errorType, message, lineNumber > 0 ? " (Line: " + lineNumber + ")" : "");

        LOGGER.error("[GenTech Recipe Error] {}", fullMessage);
        currentSessionErrors.add(fullMessage);

        if (currentErrorFile == null) {
            initializeErrorLogging();
        }

        if (currentErrorFile != null) {
            try {
                String logEntry = String.format("[%s] %s%n",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                        fullMessage);

                Files.writeString(currentErrorFile, logEntry,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            } catch (IOException e) {
                LOGGER.error("Failed to write to error log file: {}", e.getMessage());
            }
        }
    }

    private static void initializeErrorLogging() {
        try {
            Path configDir = FMLPaths.CONFIGDIR.get().resolve(CONFIG_DIR);
            Path errorDir = configDir.resolve(ERROR_DIR);

            if (!Files.exists(errorDir)) {
                Files.createDirectories(errorDir);
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            currentErrorFile = errorDir.resolve(timestamp + "_recipe_errors.log");

            String header = String.format("=== GenTech Recipe Error Log ===%n" +
                            "Session started: %s%n" +
                            "Config file: %s%n" +
                            "========================================%n%n",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    FMLPaths.CONFIGDIR.get().resolve(CONFIG_DIR).resolve(RECIPES_FILE));

            Files.writeString(currentErrorFile, header, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        } catch (Exception e) {
            LOGGER.error("Failed to initialize error logging: {}", e.getMessage(), e);
            currentErrorFile = null;
        }
    }

    private static void finalizeErrorLog() {
        if (currentErrorFile != null) {
            try {
                String footer = String.format("%n========================================%n" +
                                "Session completed: %s%n" +
                                "Total errors: %d%n" +
                                "Total recipes loaded: %d%n" +
                                "========================================%n",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        currentSessionErrors.size(),
                        loadedRecipes.size());

                Files.writeString(currentErrorFile, footer,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            } catch (IOException e) {
                LOGGER.error("Failed to finalize error log: {}", e.getMessage());
            }
        }
    }

    public static List<CustomGeneratorRecipe> getRecipesForBlock(Block block) {
        String blockKey = BuiltInRegistries.BLOCK.getKey(block).toString();
        return recipesByBlock.getOrDefault(blockKey, Collections.emptyList());
    }

    public static Collection<CustomGeneratorRecipe> getAllRecipes() {
        return loadedRecipes.values();
    }

    public static boolean hasRecipes() {
        return !loadedRecipes.isEmpty();
    }

    public static int getErrorCount() {
        return currentSessionErrors.size();
    }

    public static List<String> getCurrentSessionErrors() {
        return new ArrayList<>(currentSessionErrors);
    }

    public static Path getCurrentErrorLogFile() {
        return currentErrorFile;
    }
}