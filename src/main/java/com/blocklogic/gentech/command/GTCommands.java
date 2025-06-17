package com.blocklogic.gentech.command;

import com.blocklogic.gentech.Config;
import com.blocklogic.gentech.GenTech;
import com.blocklogic.gentech.config.CustomGeneratorRecipeConfig;
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
                        .requires(source -> source.hasPermission(2)) // OP level 2 required
                        .then(Commands.literal("reload")
                                .then(Commands.literal("config")
                                        .executes(GTCommands::reloadMainConfig))
                                .then(Commands.literal("recipes")
                                        .executes(GTCommands::reloadRecipeConfig))
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

    private static int reloadRecipeConfig(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            CustomGeneratorRecipeConfig.loadRecipes();

            int recipeCount = CustomGeneratorRecipeConfig.getAllRecipes().size();

            source.sendSuccess(() -> Component.literal("Custom generator recipes reloaded successfully! ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal("(" + recipeCount + " recipes loaded)")
                            .withStyle(ChatFormatting.YELLOW)), true);

            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to reload custom generator recipes: " + e.getMessage())
                    .withStyle(ChatFormatting.RED));

            LOGGER.error("Failed to reload recipe config via command", e);
            return 0;
        }
    }

    private static int reloadAllConfigs(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        boolean mainConfigSuccess = false;
        boolean recipeConfigSuccess = false;

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
            recipeConfigSuccess = true;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to reload custom generator recipes: " + e.getMessage())
                    .withStyle(ChatFormatting.RED));
            LOGGER.error("Failed to reload recipe config via command", e);
        }

        if (mainConfigSuccess && recipeConfigSuccess) {
            int recipeCount = CustomGeneratorRecipeConfig.getAllRecipes().size();

            source.sendSuccess(() -> Component.literal("All GenTech configurations reloaded successfully! ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal("(" + recipeCount + " recipes loaded)")
                            .withStyle(ChatFormatting.YELLOW)), true);

            return 1;
        } else if (mainConfigSuccess) {
            source.sendSuccess(() -> Component.literal("Main configuration reloaded successfully, but recipe reload failed!")
                    .withStyle(ChatFormatting.YELLOW), true);

            return 1;
        } else if (recipeConfigSuccess) {
            int recipeCount = CustomGeneratorRecipeConfig.getAllRecipes().size();

            source.sendSuccess(() -> Component.literal("Custom generator recipes reloaded successfully ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("(" + recipeCount + " recipes loaded), ")
                            .withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal("but main config reload failed!")
                            .withStyle(ChatFormatting.YELLOW)), true);

            return 1;
        } else {
            source.sendFailure(Component.literal("Both configuration reloads failed!")
                    .withStyle(ChatFormatting.RED));

            return 0;
        }
    }
}