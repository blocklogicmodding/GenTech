package com.blocklogic.gentech.block.entity;

import com.blocklogic.gentech.GenTech;
import com.blocklogic.gentech.block.GTBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class GTBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, GenTech.MODID);

    public static final Supplier<BlockEntityType<GeneratorBlockEntity>> GENERATOR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("generator_block_entity", () -> BlockEntityType.Builder.of(
                    GeneratorBlockEntity::new,
                            GTBlocks.COPPER_GENERATOR.get(),
                            GTBlocks.IRON_GENERATOR.get(),
                            GTBlocks.DIAMOND_GENERATOR.get(),
                            GTBlocks.NETHERITE_GENERATOR.get()
                    ).build(null));

    public static final Supplier<BlockEntityType<CollectorBlockEntity>> COLLECTOR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("collector_block_entity", () -> BlockEntityType.Builder.of(
                    CollectorBlockEntity::new,
                    GTBlocks.HYDRO_COLLECTOR.get(),
                    GTBlocks.MAGMA_COLLECTOR.get()
            ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}