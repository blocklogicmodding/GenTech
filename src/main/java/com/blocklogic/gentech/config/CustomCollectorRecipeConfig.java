package com.blocklogic.gentech.config;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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
import java.util.regex.Pattern;

public class CustomCollectorRecipeConfig {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String CONFIG_DIR = "gentech";
    private static final String RECIPES_FILE = "custom_collector_recipes.toml";
    private static final String ERROR_DIR = "config_errors";

    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    private static final Map<String, CollectorRecipe> loadedRecipes = new HashMap<>();
    private static final Map<String, List<CollectorRecipe>> recipesByFluid = new HashMap<>();

    private static final List<String> currentSessionErrors = new ArrayList<>();
    private static Path currentErrorFile = null;

    public static class CollectorRecipe {
        public final String name;
        public final Fluid targetFluid;
        public final int minimumSources;
        public final int collectionTime;
        public final int fluidPerCollection;
        public final int priority;

        public CollectorRecipe(String name, Fluid targetFluid, int minimumSources,
                               int collectionTime, int fluidPerCollection, int priority) {
            this.name = name;
            this.targetFluid = targetFluid;
            this.minimumSources = minimumSources;
            this.collectionTime = collectionTime;
            this.fluidPerCollection = fluidPerCollection;
            this.priority = priority;
        }
    }

    public static void loadRecipes() {
        LOGGER.info("Loading custom collector recipes...");

        loadedRecipes.clear();
        recipesByFluid.clear();
        currentSessionErrors.clear();

        Path configDir = FMLPaths.CONFIGDIR.get().resolve(CONFIG_DIR);
        Path recipesFile = configDir.resolve(RECIPES_FILE);

        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            if (!Files.exists(recipesFile)) {
                LOGGER.info("Collector recipes file not found, creating default: {}", recipesFile);
                createDefaultRecipesFile(recipesFile);
            }

            parseRecipesFile(recipesFile);
            indexRecipesByFluid();

        } catch (IOException e) {
            LOGGER.error("Failed to load collector recipes: {}", e.getMessage());
            generateDefaultRecipes();
        }

        if (!currentSessionErrors.isEmpty()) {
            LOGGER.warn("Collector recipe loading completed with {} errors. Check error log: {}",
                    currentSessionErrors.size(), currentErrorFile);
            finalizeErrorLog();
        } else {
            LOGGER.info("Collector recipe loading completed successfully with no errors.");
        }

        LOGGER.info("Loaded {} custom collector recipes", loadedRecipes.size());
    }

    private static void generateDefaultRecipes() {
        CollectorRecipe waterRecipe = new CollectorRecipe(
                "default_water_collection",
                Fluids.WATER,
                2,
                600,
                1000,
                1
        );
        loadedRecipes.put(waterRecipe.name, waterRecipe);

        CollectorRecipe lavaRecipe = new CollectorRecipe(
                "default_lava_collection",
                Fluids.LAVA,
                2,
                1200,
                1000,
                1
        );
        loadedRecipes.put(lavaRecipe.name, lavaRecipe);

        LOGGER.info("Generated {} default collector recipes", loadedRecipes.size());
    }

    private static void indexRecipesByFluid() {
        recipesByFluid.clear();

        for (CollectorRecipe recipe : loadedRecipes.values()) {
            String fluidKey = BuiltInRegistries.FLUID.getKey(recipe.targetFluid).toString();
            recipesByFluid.computeIfAbsent(fluidKey, k -> new ArrayList<>()).add(recipe);
        }

        for (List<CollectorRecipe> recipes : recipesByFluid.values()) {
            recipes.sort((a, b) -> Integer.compare(b.priority, a.priority));
        }
    }

    private static void createDefaultRecipesFile(Path recipesFile) throws IOException {
        String defaultContent = """
                # GenTech Custom Collector Recipes
                # Define which fluids can be collected and under what conditions
                # 
                # Recipe format:
                # [[recipes]]
                # name = "unique_recipe_name"           # Must be unique and alphanumeric with underscores
                # target_fluid = "modid:fluid_name"     # The fluid to collect from adjacent source blocks
                # minimum_sources = 2                   # Minimum number of adjacent source blocks required
                # collection_time = 600                 # Time in ticks (20 ticks = 1 second)
                # fluid_per_collection = 1000           # Amount of fluid collected per cycle (1000 = 1 bucket)
                # priority = 1                          # Priority if multiple recipes for same fluid (higher = preferred)
                #
                # Multiple recipes can target the same fluid with different requirements
                # Higher priority recipes are preferred when multiple match
                
                # Default water collection
                [[recipes]]
                name = "water_collection"
                target_fluid = "minecraft:water"
                minimum_sources = 2
                collection_time = 600
                fluid_per_collection = 1000
                priority = 1
                
                # Default lava collection (slower)
                [[recipes]]
                name = "lava_collection"
                target_fluid = "minecraft:lava"
                minimum_sources = 2
                collection_time = 1200
                fluid_per_collection = 1000
                priority = 1
                
                # Example fast water collection (requires more sources)
                # [[recipes]]
                # name = "fast_water_collection"
                # target_fluid = "minecraft:water"
                # minimum_sources = 4
                # collection_time = 300
                # fluid_per_collection = 1000
                # priority = 2
                """;

        Files.writeString(recipesFile, defaultContent);
        LOGGER.info("Created default collector recipes file at: {}", recipesFile);
    }

    private static void parseRecipesFile(Path recipesFile) throws IOException {
        List<String> lines = Files.readAllLines(recipesFile);
        List<Map<String, String>> recipes = parseTomlRecipes(lines);

        for (Map<String, String> recipeData : recipes) {
            try {
                CollectorRecipe recipe = parseRecipe(recipeData);
                if (recipe != null) {
                    loadedRecipes.put(recipe.name, recipe);
                }
            } catch (Exception e) {
                logError("Recipe parsing error", "Failed to parse recipe: " + e.getMessage(), 0);
            }
        }
    }

    private static List<Map<String, String>> parseTomlRecipes(List<String> lines) {
        List<Map<String, String>> recipes = new ArrayList<>();
        Map<String, String> currentRecipe = null;
        int lineNumber = 0;

        for (String line : lines) {
            lineNumber++;
            String trimmedLine = line.trim();

            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                continue;
            }

            if (trimmedLine.equals("[[recipes]]")) {
                if (currentRecipe != null) {
                    recipes.add(currentRecipe);
                }
                currentRecipe = new HashMap<>();
                continue;
            }

            if (currentRecipe != null && trimmedLine.contains("=")) {
                String[] parts = trimmedLine.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    currentRecipe.put(key, value);
                }
            } else if (!trimmedLine.isEmpty() && !trimmedLine.startsWith("#")) {
                logError("TOML parsing error", "Unexpected line outside recipe section: " + trimmedLine, lineNumber);
            }
        }

        if (currentRecipe != null) {
            recipes.add(currentRecipe);
        }

        return recipes;
    }

    private static CollectorRecipe parseRecipe(Map<String, String> recipeData) {
        String name = recipeData.get("name");
        String fluidId = recipeData.get("target_fluid");
        String minimumSourcesStr = recipeData.get("minimum_sources");
        String collectionTimeStr = recipeData.get("collection_time");
        String fluidPerCollectionStr = recipeData.get("fluid_per_collection");
        String priorityStr = recipeData.get("priority");

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

        Fluid targetFluid = parseFluid(fluidId);
        if (targetFluid == null) {
            logError("Recipe validation error", "Invalid or missing target_fluid: " + fluidId, 0);
            return null;
        }

        int minimumSources;
        try {
            minimumSources = Integer.parseInt(minimumSourcesStr);
            if (minimumSources < 1 || minimumSources > 4) {
                logError("Recipe validation error", "minimum_sources must be between 1 and 4: " + minimumSourcesStr, 0);
                return null;
            }
        } catch (NumberFormatException e) {
            logError("Recipe validation error", "Invalid minimum_sources: " + minimumSourcesStr, 0);
            return null;
        }

        int collectionTime;
        try {
            collectionTime = Integer.parseInt(collectionTimeStr);
            if (collectionTime < 1) {
                logError("Recipe validation error", "collection_time must be positive: " + collectionTimeStr, 0);
                return null;
            }
        } catch (NumberFormatException e) {
            logError("Recipe validation error", "Invalid collection_time: " + collectionTimeStr, 0);
            return null;
        }

        int fluidPerCollection;
        try {
            fluidPerCollection = Integer.parseInt(fluidPerCollectionStr);
            if (fluidPerCollection < 1) {
                logError("Recipe validation error", "fluid_per_collection must be positive: " + fluidPerCollectionStr, 0);
                return null;
            }
        } catch (NumberFormatException e) {
            logError("Recipe validation error", "Invalid fluid_per_collection: " + fluidPerCollectionStr, 0);
            return null;
        }

        int priority = 1;
        if (priorityStr != null) {
            try {
                priority = Integer.parseInt(priorityStr);
            } catch (NumberFormatException e) {
                logError("Recipe validation error", "Invalid priority: " + priorityStr, 0);
                return null;
            }
        }

        return new CollectorRecipe(name, targetFluid, minimumSources, collectionTime, fluidPerCollection, priority);
    }

    private static Fluid parseFluid(String fluidId) {
        if (fluidId == null || fluidId.isEmpty()) {
            return null;
        }

        try {
            ResourceLocation location = ResourceLocation.parse(fluidId);
            Fluid fluid = BuiltInRegistries.FLUID.get(location);
            return fluid != Fluids.EMPTY ? fluid : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static void logError(String context, String message, int lineNumber) {
        String errorMessage = String.format("[%s] %s", context, message);
        if (lineNumber > 0) {
            errorMessage += " (line " + lineNumber + ")";
        }

        currentSessionErrors.add(errorMessage);
        LOGGER.error(errorMessage);

        if (currentErrorFile == null) {
            initializeErrorLog();
        }

        try {
            Files.writeString(currentErrorFile, errorMessage + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            LOGGER.error("Failed to write to error log: {}", e.getMessage());
        }
    }

    private static void initializeErrorLog() {
        try {
            Path configDir = FMLPaths.CONFIGDIR.get().resolve(CONFIG_DIR);
            Path errorDir = configDir.resolve(ERROR_DIR);

            if (!Files.exists(errorDir)) {
                Files.createDirectories(errorDir);
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            currentErrorFile = errorDir.resolve(timestamp + "_collector_recipe_errors.log");

            String header = String.format("=== GenTech Collector Recipe Error Log ===%n" +
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
                Files.writeString(currentErrorFile, footer, StandardOpenOption.APPEND);
            } catch (IOException e) {
                LOGGER.error("Failed to finalize error log: {}", e.getMessage());
            }
        }
    }

    public static List<CollectorRecipe> getRecipesForFluid(Fluid fluid) {
        String fluidKey = BuiltInRegistries.FLUID.getKey(fluid).toString();
        return recipesByFluid.getOrDefault(fluidKey, Collections.emptyList());
    }

    public static Collection<CollectorRecipe> getAllRecipes() {
        return Collections.unmodifiableCollection(loadedRecipes.values());
    }

    public static CollectorRecipe getRecipeByName(String name) {
        return loadedRecipes.get(name);
    }

    public static boolean hasRecipes() {
        return !loadedRecipes.isEmpty();
    }

    public static void reloadRecipes() {
        LOGGER.info("Reloading collector recipes...");
        loadRecipes();
    }
}