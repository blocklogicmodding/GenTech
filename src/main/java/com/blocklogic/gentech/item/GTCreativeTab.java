package com.blocklogic.gentech.item;

import com.blocklogic.gentech.GenTech;
import com.blocklogic.gentech.block.GTBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class GTCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GenTech.MODID);

    public static final Supplier<CreativeModeTab> GENTECH = CREATIVE_MODE_TAB.register("gentech",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(GTBlocks.DIAMOND_GENERATOR.get()))
                    .title(Component.translatable("creativetab.gentech.gentech"))
                    .displayItems((ItemDisplayParameters, output) -> {
                        output.accept(GTBlocks.COPPER_GENERATOR);
                        output.accept(GTBlocks.IRON_GENERATOR);
                        output.accept(GTBlocks.DIAMOND_GENERATOR);
                        output.accept(GTBlocks.NETHERITE_GENERATOR);

                        output.accept(GTBlocks.COPPER_TANK);
                        output.accept(GTBlocks.IRON_TANK);
                        output.accept(GTBlocks.DIAMOND_TANK);
                        output.accept(GTBlocks.NETHERITE_TANK);

                        output.accept(GTBlocks.HYDRO_COLLECTOR);
                        output.accept(GTBlocks.MAGMA_COLLECTOR);

                        output.accept(GTItems.BASIC_EFFICIENCY_UPGRADE);
                        output.accept(GTItems.ADVANCED_EFFICIENCY_UPGRADE);
                        output.accept(GTItems.ULTIMATE_EFFICIENCY_UPGRADE);
                        output.accept(GTItems.BASIC_SPEED_UPGRADE);
                        output.accept(GTItems.ADVANCED_SPEED_UPGRADE);
                        output.accept(GTItems.ULTIMATE_SPEED_UPGRADE);
                        output.accept(GTItems.COPPER_TO_IRON_GENERATOR_UPGRADE);
                        output.accept(GTItems.IRON_TO_DIAMOND_GENERATOR_UPGRADE);
                        output.accept(GTItems.DIAMOND_TO_NETHERITE_GENERATOR_UPGRADE);
                    }).build());

    public static void register (IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
