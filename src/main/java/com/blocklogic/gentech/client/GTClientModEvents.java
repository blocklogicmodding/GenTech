package com.blocklogic.gentech.client;

import com.blocklogic.gentech.GenTech;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = GenTech.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class GTClientModEvents {

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(GTKeybinds.TOGGLE_TANK_MODE);
    }
}