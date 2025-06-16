package com.blocklogic.gentech;

import com.blocklogic.gentech.block.GTBlocks;
import com.blocklogic.gentech.block.custom.CopperGeneratorBlock;
import com.blocklogic.gentech.block.custom.DiamondGeneratorBlock;
import com.blocklogic.gentech.block.custom.IronGeneratorBlock;
import com.blocklogic.gentech.block.custom.NetheriteGeneratorBlock;
import com.blocklogic.gentech.block.entity.GTBlockEntities;
import com.blocklogic.gentech.block.entity.GeneratorBlockEntity;
import com.blocklogic.gentech.item.GTCreativeTab;
import com.blocklogic.gentech.item.GTItems;
import com.blocklogic.gentech.screen.GTMenuTypes;
import com.blocklogic.gentech.screen.custom.GeneratorScreen;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(GenTech.MODID)
public class GenTech {
    public static final String MODID = "gentech";

    private static final Logger LOGGER = LogUtils.getLogger();

    public GenTech(IEventBus modEventBus, ModContainer modContainer) {

        modEventBus.addListener(this::commonSetup);


        NeoForge.EVENT_BUS.register(this);

        GTBlocks.register(modEventBus);
        GTItems.register(modEventBus);
        GTCreativeTab.register(modEventBus);
        GTBlockEntities.register(modEventBus);
        GTMenuTypes.register(modEventBus);

        modEventBus.addListener(this::addCreative);

        modEventBus.addListener(GeneratorBlockEntity::registerCapabilities);

        Config.register(modContainer);
        modEventBus.register(Config.class);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        GeneratorBlockEntity.registerCapabilities(event);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(GTMenuTypes.GENERATOR_MENU.get(), GeneratorScreen::new);
        }
    }
}
