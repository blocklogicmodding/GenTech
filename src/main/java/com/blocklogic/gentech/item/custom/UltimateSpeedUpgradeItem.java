package com.blocklogic.gentech.item.custom;

import com.blocklogic.gentech.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class UltimateSpeedUpgradeItem extends Item {
    public UltimateSpeedUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        double multiplier = Config.getUltimateSpeedUpgradeMultiplier();
        double speedIncrease = (multiplier - 1.0) * 100;
        double timeReduction = (1.0 - (1.0 / multiplier)) * 100;

        tooltipComponents.add(Component.translatable("tooltip.gentech.speed_upgrade.ultimate")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));

        tooltipComponents.add(Component.translatable("tooltip.gentech.speed_increase",
                        String.format("%.0f", speedIncrease))
                .withStyle(ChatFormatting.GOLD));

        tooltipComponents.add(Component.translatable("tooltip.gentech.time_reduction",
                        String.format("%.0f", timeReduction))
                .withStyle(ChatFormatting.AQUA));

        tooltipComponents.add(Component.empty());

        tooltipComponents.add(Component.translatable("tooltip.gentech.upgrade.stacking")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));

        tooltipComponents.add(Component.translatable("tooltip.gentech.upgrade.applies_to")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}