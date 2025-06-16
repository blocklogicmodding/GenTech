package com.blocklogic.gentech.block;

import com.blocklogic.gentech.GenTech;
import com.blocklogic.gentech.block.custom.CopperGeneratorBlock;
import com.blocklogic.gentech.block.custom.DiamondGeneratorBlock;
import com.blocklogic.gentech.block.custom.IronGeneratorBlock;
import com.blocklogic.gentech.block.custom.NetheriteGeneratorBlock;
import com.blocklogic.gentech.item.GTItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class GTBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(GenTech.MODID);

    public static final DeferredBlock<Block> COPPER_GENERATOR = registerBlock("copper_generator",
            () -> new CopperGeneratorBlock(BlockBehaviour.Properties.of()
                    .strength(3.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)
                    .noOcclusion()
            ));

    public static final DeferredBlock<Block> IRON_GENERATOR = registerBlock("iron_generator",
            () -> new IronGeneratorBlock(BlockBehaviour.Properties.of()
                    .strength(3.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)
                    .noOcclusion()
            ));

    public static final DeferredBlock<Block> DIAMOND_GENERATOR = registerBlock("diamond_generator",
            () -> new DiamondGeneratorBlock(BlockBehaviour.Properties.of()
                    .strength(3.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)
                    .noOcclusion()
            ));

    public static final DeferredBlock<Block> NETHERITE_GENERATOR = registerBlock("netherite_generator",
            () -> new NetheriteGeneratorBlock(BlockBehaviour.Properties.of()
                    .strength(3.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)
                    .noOcclusion()
            ));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        GTItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
