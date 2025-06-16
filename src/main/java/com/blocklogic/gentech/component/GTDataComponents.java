package com.blocklogic.gentech.component;

import com.blocklogic.gentech.GenTech;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GTDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, GenTech.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FluidData>> FLUID_DATA =
            DATA_COMPONENTS.register("fluid_data", () -> DataComponentType.<FluidData>builder()
                    .persistent(FluidData.CODEC)
                    .networkSynchronized(FluidData.STREAM_CODEC)
                    .build());

    public record FluidData(int waterAmount, int lavaAmount) {
        public static final Codec<FluidData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT.fieldOf("water_amount").forGetter(FluidData::waterAmount),
                        Codec.INT.fieldOf("lava_amount").forGetter(FluidData::lavaAmount)
                ).apply(instance, FluidData::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, FluidData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, FluidData::waterAmount,
                ByteBufCodecs.INT, FluidData::lavaAmount,
                FluidData::new);

        public static FluidData empty() {
            return new FluidData(0, 0);
        }

        public boolean isEmpty() {
            return waterAmount == 0 && lavaAmount == 0;
        }
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}