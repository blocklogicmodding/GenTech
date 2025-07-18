package com.blocklogic.gentech.block.custom;

import com.blocklogic.gentech.block.entity.GTBlockEntities;
import com.blocklogic.gentech.block.entity.TankBlockEntity;
import com.blocklogic.gentech.component.GTDataComponents;
import com.blocklogic.gentech.util.TankBucketInteractionHandler;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CopperTankBlock extends BaseEntityBlock {
    public static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 15, 14);
    public static final MapCodec<CopperTankBlock> CODEC = simpleCodec(CopperTankBlock::new);

    public CopperTankBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (TankBucketInteractionHandler.handleTankBucketInteraction(level, pos, state, player, hand, hit, stack)) {
            return ItemInteractionResult.CONSUME;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TankBlockEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }

        return createTickerHelper(blockEntityType, GTBlockEntities.TANK_BLOCK_ENTITY.get(),
                TankBlockEntity::tick);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);

        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof TankBlockEntity tankEntity) {
            for (ItemStack drop : drops) {
                if (drop.getItem() instanceof BlockItem blockItem &&
                        blockItem.getBlock() == this) {

                    FluidStack tankFluid = tankEntity.getFluidStack();
                    if (!tankFluid.isEmpty()) {
                        GTDataComponents.FluidData fluidData = GTDataComponents.FluidData.create(
                                tankFluid.getFluid(), tankFluid.getAmount(),
                                Fluids.EMPTY, 0
                        );
                        drop.set(GTDataComponents.FLUID_DATA.get(), fluidData);
                    }
                }
            }
        }

        return drops;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TankBlockEntity tankEntity) {
            GTDataComponents.FluidData fluidData = stack.get(GTDataComponents.FLUID_DATA.get());
            if (fluidData != null && !fluidData.isEmpty()) {
                if (fluidData.fluid1Amount() > 0) {
                    Fluid fluid = fluidData.getFluid1();
                    if (fluid != Fluids.EMPTY) {
                        FluidStack fluidStack = new FluidStack(fluid, fluidData.fluid1Amount());
                        tankEntity.getFluidTank().fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        GTDataComponents.TankMode mode = stack.getOrDefault(GTDataComponents.TANK_MODE.get(), GTDataComponents.TankMode.TANK);
        tooltipComponents.add(Component.translatable("tooltip.gentech.tank.mode." + mode.getName())
                .withStyle(mode == GTDataComponents.TankMode.BUCKET ? ChatFormatting.GOLD : ChatFormatting.AQUA));

        tooltipComponents.add(Component.translatable("tooltip.gentech.tank.toggle_mode")
                .withStyle(ChatFormatting.GRAY));

        tooltipComponents.add(Component.empty());

        tooltipComponents.add(Component.translatable("tooltip.gentech.tank.tier.copper")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        tooltipComponents.add(Component.translatable("tooltip.gentech.tank.any_fluid")
                .withStyle(ChatFormatting.GREEN));

        GTDataComponents.FluidData fluidData = stack.get(GTDataComponents.FLUID_DATA.get());
        if (fluidData != null && !fluidData.isEmpty()) {
            tooltipComponents.add(Component.translatable("tooltip.gentech.stored_fluids")
                    .withStyle(ChatFormatting.YELLOW));

            if (fluidData.fluid1Amount() > 0) {
                String fluidName = fluidData.getFluid1().getFluidType().getDescription().getString();
                tooltipComponents.add(Component.translatable("tooltip.gentech.stored_fluid",
                                fluidName, NumberFormat.getInstance(Locale.US).format(fluidData.fluid1Amount()))
                        .withStyle(ChatFormatting.BLUE));
            }
        }
    }
}