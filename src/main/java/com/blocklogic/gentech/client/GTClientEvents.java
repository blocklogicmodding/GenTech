package com.blocklogic.gentech.client;

import com.blocklogic.gentech.GenTech;
import com.blocklogic.gentech.network.GTNetworking;
import com.blocklogic.gentech.network.ToggleTankModePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = GenTech.MODID, value = Dist.CLIENT)
public class GTClientEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (GTKeybinds.TOGGLE_TANK_MODE.consumeClick()) {
            ItemStack mainHand = mc.player.getMainHandItem();
            ItemStack offHand = mc.player.getOffhandItem();

            if (isTankItem(mainHand) || isTankItem(offHand)) {
                GTNetworking.sendToServer(new ToggleTankModePacket());
            }
        }
    }

    private static boolean isTankItem(ItemStack stack) {
        return stack.getItem().toString().contains("tank");
    }
}