package com.blocklogic.gentech.item;

import com.blocklogic.gentech.Config;
import com.blocklogic.gentech.GenTech;
import com.blocklogic.gentech.item.custom.*;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GTItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GenTech.MODID);

    public static final DeferredItem<Item> BASIC_SPEED_UPGRADE = ITEMS.register("basic_speed_upgrade",
            () -> new BasicSpeedUpgradeItem(new Item.Properties()));

    public static final DeferredItem<Item> ADVANCED_SPEED_UPGRADE = ITEMS.register("advanced_speed_upgrade",
            () -> new AdvancedSpeedUpgradeItem(new Item.Properties()));

    public static final DeferredItem<Item> ULTIMATE_SPEED_UPGRADE = ITEMS.register("ultimate_speed_upgrade",
            () -> new UltimateSpeedUpgradeItem(new Item.Properties()));

    public static final DeferredItem<Item> BASIC_EFFICIENCY_UPGRADE = ITEMS.register("basic_efficiency_upgrade",
            () -> new BasicEfficiencyUpgradeItem(new Item.Properties()));

    public static final DeferredItem<Item> ADVANCED_EFFICIENCY_UPGRADE = ITEMS.register("advanced_efficiency_upgrade",
            () -> new AdvancedEfficiencyUpgradeItem(new Item.Properties()));

    public static final DeferredItem<Item> ULTIMATE_EFFICIENCY_UPGRADE = ITEMS.register("ultimate_efficiency_upgrade",
            () -> new UltimateEfficiencyUpgradeItem(new Item.Properties()));

    public static final DeferredItem<Item> COPPER_TO_IRON_GENERATOR_UPGRADE = ITEMS.register("copper_to_iron_generator_upgrade",
            () -> new CopperToIronUpgradeItem(new Item.Properties()));

    public static final DeferredItem<Item> IRON_TO_DIAMOND_GENERATOR_UPGRADE = ITEMS.register("iron_to_diamond_generator_upgrade",
            () -> new IronToDiamondUpgradeItem(new Item.Properties()));

    public static final DeferredItem<Item> DIAMOND_TO_NETHERITE_GENERATOR_UPGRADE = ITEMS.register("diamond_to_netherite_generator_upgrade",
            () -> new DiamondToNetheriteUpgradeItem(new Item.Properties()));

    public static void register (IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}