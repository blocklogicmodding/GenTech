package com.blocklogic.gentech.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class IronToDiamondUpgradeItem extends Item {
    public IronToDiamondUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        tooltipComponents.add(Component.empty());

        tooltipComponents.add(Component.translatable("tooltip.gentech.tier_upgrade.benefits")
                .withStyle(ChatFormatting.YELLOW));

        tooltipComponents.add(Component.translatable("tooltip.gentech.tier_upgrade.speed_improvement")
                .withStyle(ChatFormatting.GREEN));

        tooltipComponents.add(Component.translatable("tooltip.gentech.tier_upgrade.efficiency_improvement")
                .withStyle(ChatFormatting.AQUA));

        tooltipComponents.add(Component.translatable("tooltip.gentech.tier_upgrade.buffer_increase")
                .withStyle(ChatFormatting.BLUE));

        tooltipComponents.add(Component.translatable("tooltip.gentech.tier_upgrade.upgrade_slot", "2")
                .withStyle(ChatFormatting.LIGHT_PURPLE));

        tooltipComponents.add(Component.empty());

        tooltipComponents.add(Component.translatable("tooltip.gentech.tier_upgrade.usage")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}