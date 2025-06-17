package com.blocklogic.gentech.item.custom;

import com.blocklogic.gentech.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class UltimateEfficiencyUpgradeItem extends Item {
    public UltimateEfficiencyUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        double reduction = Config.getUltimateEfficiencyUpgradeReduction() * 100;

        tooltipComponents.add(Component.translatable("tooltip.gentech.fluid_reduction",
                        String.format("%.0f", reduction))
                .withStyle(ChatFormatting.AQUA));
    }
}