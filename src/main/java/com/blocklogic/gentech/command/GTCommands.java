package com.blocklogic.gentech.command;

import com.blocklogic.gentech.Config;
import com.blocklogic.gentech.GenTech;
import com.blocklogic.gentech.config.CustomGeneratorRecipeConfig;
import com.blocklogic.gentech.config.CustomCollectorRecipeConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

@EventBusSubscriber(modid = GenTech.MODID, bus = EventBusSubscriber.Bus.GAME)
public class GTCommands {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("gentech")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("reload")
                                .then(Commands.literal("config")
                                        .executes(GTCommands::reloadMainConfig))
                                .then(Commands.literal("recipes")
                                        .then(Commands.literal("generator")
                                                .executes(GTCommands::reloadGeneratorRecipes))
                                        .then(Commands.literal("collector")
                                                .executes(GTCommands::reloadCollectorRecipes))
                                        .then(Commands.literal("all")
                                                .executes(GTCommands::reloadAllRecipes)))
                                .then(Commands.literal("all")
                                        .executes(GTCommands::reloadAllConfigs)))
        );
    }

    private static int reloadMainConfig(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            Config.loadConfig();

            source.sendSuccess(() -> Component.literal("GenTech main configuration reloaded successfully!")
                    .withStyle(ChatFormatting.GREEN), true);

            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to reload main configuration: " + e.getMessage())
                    .withStyle(ChatFormatting.RED));

            LOGGER.error("Failed to reload main config via command", e);
            return 0;
        }
    }

    private static int reloadGeneratorRecipes(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            CustomGeneratorRecipeConfig.loadRecipes();

            int recipeCount = CustomGeneratorRecipeConfig.getAllRecipes().size();

            source.sendSuccess(() -> Component.literal("Generator recipes reloaded successfully! ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal("(" + recipeCount + " recipes loaded)")
                            .withStyle(ChatFormatting.YELLOW)), true);

            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to reload generator recipes: " + e.getMessage())
                    .withStyle(ChatFormatting.RED));

            LOGGER.error("Failed to reload generator recipe config via command", e);
            return 0;
        }
    }

    private static int reloadCollectorRecipes(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            CustomCollectorRecipeConfig.loadRecipes();

            int recipeCount = CustomCollectorRecipeConfig.getAllRecipes().size();

            source.sendSuccess(() -> Component.literal("Collector recipes reloaded successfully! ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal("(" + recipeCount + " recipes loaded)")
                            .withStyle(ChatFormatting.YELLOW)), true);

            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to reload collector recipes: " + e.getMessage())
                    .withStyle(ChatFormatting.RED));

            LOGGER.error("Failed to reload collector recipe config via command", e);
            return 0;
        }
    }

    private static int reloadAllRecipes(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        boolean generatorSuccess = false;
        boolean collectorSuccess = false;

        try {
            CustomGeneratorRecipeConfig.loadRecipes();
            generatorSuccess = true;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to reload generator recipes: " + e.getMessage())
                    .withStyle(ChatFormatting.RED));
            LOGGER.error("Failed to reload generator recipe config via command", e);
        }

        try {
            CustomCollectorRecipeConfig.loadRecipes();
            collectorSuccess = true;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to reload collector recipes: " + e.getMessage())
                    .withStyle(ChatFormatting.RED));
            LOGGER.error("Failed to reload collector recipe config via command", e);
        }

        if (generatorSuccess && collectorSuccess) {
            int generatorCount = CustomGeneratorRecipeConfig.getAllRecipes().size();
            int collectorCount = CustomCollectorRecipeConfig.getAllRecipes().size();

            source.sendSuccess(() -> Component.literal("All recipes reloaded successfully! ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal("(Generator: " + generatorCount + ", Collector: " + collectorCount + ")")
                            .withStyle(ChatFormatting.YELLOW)), true);
            return 1;
        } else if (generatorSuccess) {
            int generatorCount = CustomGeneratorRecipeConfig.getAllRecipes().size();
            source.sendSuccess(() -> Component.literal("Generator recipes reloaded successfully ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("(" + generatorCount + " recipes), ")
                            .withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal("but collector recipe reload failed!")
                            .withStyle(ChatFormatting.RED)), true);
            return 1;
        } else if (collectorSuccess) {
            int collectorCount = CustomCollectorRecipeConfig.getAllRecipes().size();
            source.sendSuccess(() -> Component.literal("Collector recipes reloaded successfully ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("(" + collectorCount + " recipes), ")
                            .withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal("but generator recipe reload failed!")
                            .withStyle(ChatFormatting.RED)), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("Both recipe reloads failed!")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int reloadAllConfigs(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        boolean mainConfigSuccess = false;
        boolean generatorRecipeSuccess = false;
        boolean collectorRecipeSuccess = false;

        try {
            Config.loadConfig();
            mainConfigSuccess = true;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to reload main configuration: " + e.getMessage())
                    .withStyle(ChatFormatting.RED));
            LOGGER.error("Failed to reload main config via command", e);
        }

        try {
            CustomGeneratorRecipeConfig.loadRecipes();
            generatorRecipeSuccess = true;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to reload generator recipes: " + e.getMessage())
                    .withStyle(ChatFormatting.RED));
            LOGGER.error("Failed to reload generator recipe config via command", e);
        }

        try {
            CustomCollectorRecipeConfig.loadRecipes();
            collectorRecipeSuccess = true;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to reload collector recipes: " + e.getMessage())
                    .withStyle(ChatFormatting.RED));
            LOGGER.error("Failed to reload collector recipe config via command", e);
        }

        if (mainConfigSuccess && generatorRecipeSuccess && collectorRecipeSuccess) {
            int generatorCount = CustomGeneratorRecipeConfig.getAllRecipes().size();
            int collectorCount = CustomCollectorRecipeConfig.getAllRecipes().size();

            source.sendSuccess(() -> Component.literal("All GenTech configurations reloaded successfully! ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal("(Generator: " + generatorCount + ", Collector: " + collectorCount + " recipes)")
                            .withStyle(ChatFormatting.YELLOW)), true);

            return 1;
        } else {
            StringBuilder message = new StringBuilder();
            if (mainConfigSuccess) message.append("Main config reloaded. ");
            if (generatorRecipeSuccess) {
                int count = CustomGeneratorRecipeConfig.getAllRecipes().size();
                message.append("Generator recipes (").append(count).append(") reloaded. ");
            }
            if (collectorRecipeSuccess) {
                int count = CustomCollectorRecipeConfig.getAllRecipes().size();
                message.append("Collector recipes (").append(count).append(") reloaded. ");
            }

            if (message.length() > 0) {
                source.sendSuccess(() -> Component.literal(message.toString())
                        .withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal("Some reloads failed!")
                                .withStyle(ChatFormatting.RED)), true);
                return 1;
            } else {
                source.sendFailure(Component.literal("All configuration reloads failed!")
                        .withStyle(ChatFormatting.RED));
                return 0;
            }
        }
    }
}