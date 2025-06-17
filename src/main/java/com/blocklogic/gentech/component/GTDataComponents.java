package com.blocklogic.gentech.component;

import com.blocklogic.gentech.GenTech;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
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

    public record FluidData(String fluid1Type, int fluid1Amount, String fluid2Type, int fluid2Amount) {
        public static final Codec<FluidData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("fluid1_type").forGetter(FluidData::fluid1Type),
                        Codec.INT.fieldOf("fluid1_amount").forGetter(FluidData::fluid1Amount),
                        Codec.STRING.fieldOf("fluid2_type").forGetter(FluidData::fluid2Type),
                        Codec.INT.fieldOf("fluid2_amount").forGetter(FluidData::fluid2Amount)
                ).apply(instance, FluidData::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, FluidData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, FluidData::fluid1Type,
                ByteBufCodecs.INT, FluidData::fluid1Amount,
                ByteBufCodecs.STRING_UTF8, FluidData::fluid2Type,
                ByteBufCodecs.INT, FluidData::fluid2Amount,
                FluidData::new);

        public static FluidData empty() {
            return new FluidData("", 0, "", 0);
        }

        public boolean isEmpty() {
            return fluid1Amount == 0 && fluid2Amount == 0;
        }

        public int waterAmount() {
            if ("minecraft:water".equals(fluid1Type)) {
                return fluid1Amount;
            } else if ("minecraft:water".equals(fluid2Type)) {
                return fluid2Amount;
            }
            return 0;
        }

        public int lavaAmount() {
            if ("minecraft:lava".equals(fluid1Type)) {
                return fluid1Amount;
            } else if ("minecraft:lava".equals(fluid2Type)) {
                return fluid2Amount;
            }
            return 0;
        }

        public Fluid getFluid1() {
            if (fluid1Type.isEmpty()) return Fluids.EMPTY;
            try {
                ResourceLocation location = ResourceLocation.parse(fluid1Type);
                return BuiltInRegistries.FLUID.get(location);
            } catch (Exception e) {
                return Fluids.EMPTY;
            }
        }

        public Fluid getFluid2() {
            if (fluid2Type.isEmpty()) return Fluids.EMPTY;
            try {
                ResourceLocation location = ResourceLocation.parse(fluid2Type);
                return BuiltInRegistries.FLUID.get(location);
            } catch (Exception e) {
                return Fluids.EMPTY;
            }
        }

        public static FluidData create(Fluid fluid1, int amount1, Fluid fluid2, int amount2) {
            String fluid1Id = fluid1 == Fluids.EMPTY ? "" : BuiltInRegistries.FLUID.getKey(fluid1).toString();
            String fluid2Id = fluid2 == Fluids.EMPTY ? "" : BuiltInRegistries.FLUID.getKey(fluid2).toString();
            return new FluidData(fluid1Id, amount1, fluid2Id, amount2);
        }
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}