package com.blocklogic.gentech.network;

import com.blocklogic.gentech.GenTech;
import com.blocklogic.gentech.component.GTDataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleTankModePacket() implements CustomPacketPayload {
    public static final Type<ToggleTankModePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(GenTech.MODID, "toggle_tank_mode"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleTankModePacket> STREAM_CODEC =
            StreamCodec.unit(new ToggleTankModePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleTankModePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ItemStack heldItem = serverPlayer.getItemInHand(InteractionHand.MAIN_HAND);
                if (isTankItem(heldItem)) {
                    toggleTankMode(heldItem, serverPlayer);
                    return;
                }

                heldItem = serverPlayer.getItemInHand(InteractionHand.OFF_HAND);
                if (isTankItem(heldItem)) {
                    toggleTankMode(heldItem, serverPlayer);
                }
            }
        });
    }

    private static boolean isTankItem(ItemStack stack) {
        return stack.getItem().toString().contains("tank");
    }

    private static void toggleTankMode(ItemStack stack, ServerPlayer player) {
        GTDataComponents.TankMode currentMode = stack.getOrDefault(GTDataComponents.TANK_MODE.get(), GTDataComponents.TankMode.TANK);
        GTDataComponents.TankMode newMode = currentMode == GTDataComponents.TankMode.TANK ? GTDataComponents.TankMode.BUCKET : GTDataComponents.TankMode.TANK;

        stack.set(GTDataComponents.TANK_MODE.get(), newMode);

        String messageKey = newMode == GTDataComponents.TankMode.BUCKET ?
                "message.gentech.tank.mode.bucket" : "message.gentech.tank.mode.tank";
        player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable(messageKey),
                true
        );
    }
}