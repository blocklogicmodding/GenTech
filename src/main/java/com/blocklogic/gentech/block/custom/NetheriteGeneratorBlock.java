package com.blocklogic.gentech.block.custom;

import com.blocklogic.gentech.Config;
import com.blocklogic.gentech.block.entity.GTBlockEntities;
import com.blocklogic.gentech.block.entity.GeneratorBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class NetheriteGeneratorBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);
    public static final MapCodec<NetheriteGeneratorBlock> CODEC = simpleCodec(NetheriteGeneratorBlock::new);

    public NetheriteGeneratorBlock(Properties properties) {
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        NumberFormat formatter = NumberFormat.getInstance(Locale.US);

        tooltipComponents.add(Component.translatable("tooltip.gentech.generator.tier.netherite")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));

        tooltipComponents.add(Component.empty());

        tooltipComponents.add(Component.translatable("tooltip.gentech.generator.specs")
                .withStyle(ChatFormatting.YELLOW));

        tooltipComponents.add(Component.translatable("tooltip.gentech.generator.upgrade_slots", "3")
                .withStyle(ChatFormatting.LIGHT_PURPLE));

        tooltipComponents.add(Component.translatable("tooltip.gentech.generator.buffer_capacity",
                        formatter.format(Config.getNetheriteGeneratorFluidBuffer()))
                .withStyle(ChatFormatting.BLUE));

        tooltipComponents.add(Component.empty());

        tooltipComponents.add(Component.translatable("tooltip.gentech.generator.generation_speeds")
                .withStyle(ChatFormatting.GREEN));

        tooltipComponents.add(Component.translatable("tooltip.gentech.generator.soft_blocks",
                        String.format("%.1f", Config.getNetheriteGeneratorSoftSpeed() / 20.0))
                .withStyle(ChatFormatting.GREEN));

        tooltipComponents.add(Component.translatable("tooltip.gentech.generator.medium_blocks",
                        String.format("%.1f", Config.getNetheriteGeneratorMediumSpeed() / 20.0))
                .withStyle(ChatFormatting.YELLOW));

        tooltipComponents.add(Component.translatable("tooltip.gentech.generator.hard_blocks",
                        String.format("%.1f", Config.getNetheriteGeneratorHardSpeed() / 20.0))
                .withStyle(ChatFormatting.RED));

        tooltipComponents.add(Component.empty());

        tooltipComponents.add(Component.translatable("tooltip.gentech.generator.usage")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
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
}