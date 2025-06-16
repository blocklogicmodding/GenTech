package com.blocklogic.gentech.block.custom;

import com.blocklogic.gentech.Config;
import com.blocklogic.gentech.block.entity.GTBlockEntities;
import com.blocklogic.gentech.block.entity.GeneratorBlockEntity;
import com.blocklogic.gentech.component.GTDataComponents;
import com.blocklogic.gentech.util.GeneratorUpgradeHandler;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class DiamondGeneratorBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);
    public static final MapCodec<DiamondGeneratorBlock> CODEC = simpleCodec(DiamondGeneratorBlock::new);

    public DiamondGeneratorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (GeneratorUpgradeHandler.isGeneratorUpgradeItem(stack)) {
            if (GeneratorUpgradeHandler.tryUpgradeGenerator(level, pos, player, stack)) {
                return ItemInteractionResult.SUCCESS;
            } else {
                return ItemInteractionResult.FAIL;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof GeneratorBlockEntity) {
                player.openMenu((GeneratorBlockEntity) entity, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);

        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof GeneratorBlockEntity blockEntity) {
            for (ItemStack drop : drops) {
                if (drop.getItem() instanceof BlockItem blockItem &&
                        blockItem.getBlock() == this) {

                    int waterAmount = blockEntity.getWaterAmount();
                    int lavaAmount = blockEntity.getLavaAmount();

                    if (waterAmount > 0 || lavaAmount > 0) {
                        drop.set(GTDataComponents.FLUID_DATA.get(),
                                new GTDataComponents.FluidData(waterAmount, lavaAmount));
                    }
                }
            }
        }

        return drops;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof GeneratorBlockEntity blockEntity) {
            GTDataComponents.FluidData fluidData = stack.get(GTDataComponents.FLUID_DATA.get());
            if (fluidData != null && !fluidData.isEmpty()) {
                blockEntity.restoreFluidData(fluidData);
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new GeneratorBlockEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }

        return createTickerHelper(blockEntityType, GTBlockEntities.GENERATOR_BLOCK_ENTITY.get(),
                GeneratorBlockEntity::tick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof GeneratorBlockEntity composterBlockEntity) {
                composterBlockEntity.drops();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        GTDataComponents.FluidData fluidData = stack.get(GTDataComponents.FLUID_DATA.get());

        tooltipComponents.add(Component.translatable("tooltip.gentech.generator.tier.diamond")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        tooltipComponents.add(Component.empty());

        tooltipComponents.add(Component.translatable("tooltip.gentech.generator.specs")
                .withStyle(ChatFormatting.YELLOW));

        tooltipComponents.add(Component.translatable("tooltip.gentech.generator.upgrade_slots", "2")
                .withStyle(ChatFormatting.LIGHT_PURPLE));

        tooltipComponents.add(Component.translatable("tooltip.gentech.generator.buffer_capacity",
                        formatter.format(Config.getDiamondGeneratorFluidBuffer()))
                .withStyle(ChatFormatting.BLUE));

        tooltipComponents.add(Component.empty());

        tooltipComponents.add(Component.translatable("tooltip.gentech.generator.generation_speeds")
                .withStyle(ChatFormatting.GREEN));

        tooltipComponents.add(Component.translatable("tooltip.gentech.generator.soft_blocks",
                        String.format("%.1f", Config.getDiamondGeneratorSoftSpeed() / 20.0))
                .withStyle(ChatFormatting.GREEN));

        tooltipComponents.add(Component.translatable("tooltip.gentech.generator.medium_blocks",
                        String.format("%.1f", Config.getDiamondGeneratorMediumSpeed() / 20.0))
                .withStyle(ChatFormatting.YELLOW));

        tooltipComponents.add(Component.translatable("tooltip.gentech.generator.hard_blocks",
                        String.format("%.1f", Config.getDiamondGeneratorHardSpeed() / 20.0))
                .withStyle(ChatFormatting.RED));

        tooltipComponents.add(Component.empty());

        tooltipComponents.add(Component.translatable("tooltip.gentech.generator.usage")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));

        if (fluidData != null && !fluidData.isEmpty()) {

            tooltipComponents.add(Component.empty());
            tooltipComponents.add(Component.translatable("tooltip.gentech.stored_fluids")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));

            if (fluidData.waterAmount() > 0) {
                tooltipComponents.add(Component.translatable("tooltip.gentech.stored_water",
                                formatter.format(fluidData.waterAmount()))
                        .withStyle(ChatFormatting.BLUE));
            }

            if (fluidData.lavaAmount() > 0) {
                tooltipComponents.add(Component.translatable("tooltip.gentech.stored_lava",
                                formatter.format(fluidData.lavaAmount()))
                        .withStyle(ChatFormatting.RED));
            }
        }
    }
}