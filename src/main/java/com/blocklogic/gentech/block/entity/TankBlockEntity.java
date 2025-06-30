package com.blocklogic.gentech.block.entity;

import com.blocklogic.gentech.block.custom.CopperTankBlock;
import com.blocklogic.gentech.block.custom.DiamondTankBlock;
import com.blocklogic.gentech.block.custom.IronTankBlock;
import com.blocklogic.gentech.block.custom.NetheriteTankBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

public class TankBlockEntity extends BlockEntity {

    private final FluidTank fluidTank;
    private TankTier tier;
    private int lastFluidAmount = -1;

    public TankBlockEntity(BlockPos pos, BlockState blockState) {
        super(GTBlockEntities.TANK_BLOCK_ENTITY.get(), pos, blockState);

        this.tier = determineTier(blockState);
        int capacity = getCapacityForTier(this.tier);

        this.fluidTank = new FluidTank(capacity) {
            @Override
            public boolean isFluidValid(FluidStack stack) {
                FluidStack existing = getFluid();
                return existing.isEmpty() || existing.getFluid() == stack.getFluid();
            }

            @Override
            protected void onContentsChanged() {
                setChanged();
                if (level != null && !level.isClientSide()) {
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                }
            }
        };
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TankBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        int currentFluid = blockEntity.fluidTank.getFluidAmount();
        if (currentFluid != blockEntity.lastFluidAmount) {
            blockEntity.lastFluidAmount = currentFluid;
            level.sendBlockUpdated(pos, state, state, 3);
            blockEntity.setChanged();
        }
    }

    private TankTier determineTier(BlockState blockState) {
        if (blockState.getBlock() instanceof CopperTankBlock) {
            return TankTier.COPPER;
        } else if (blockState.getBlock() instanceof IronTankBlock) {
            return TankTier.IRON;
        } else if (blockState.getBlock() instanceof DiamondTankBlock) {
            return TankTier.DIAMOND;
        } else if (blockState.getBlock() instanceof NetheriteTankBlock) {
            return TankTier.NETHERITE;
        }
        return TankTier.COPPER;
    }

    private int getCapacityForTier(TankTier tier) {
        return switch (tier) {
            case COPPER -> 20000;
            case IRON -> 50000;
            case DIAMOND -> 100000;
            case NETHERITE -> 250000;
        };
    }

    public boolean handleBucketInteraction(FluidStack bucketFluid, boolean simulate) {
        if (bucketFluid.isEmpty()) {
            FluidStack drained = fluidTank.drain(1000, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
            return drained.getAmount() == 1000;
        } else {
            int filled = fluidTank.fill(new FluidStack(bucketFluid.getFluid(), 1000), simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
            return filled == 1000;
        }
    }

    public FluidStack extractBucket() {
        return fluidTank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
    }

    public boolean insertBucket(FluidStack bucketFluid) {
        int filled = fluidTank.fill(new FluidStack(bucketFluid.getFluid(), 1000), IFluidHandler.FluidAction.EXECUTE);
        return filled == 1000;
    }

    public FluidTank getFluidTank() {
        return fluidTank;
    }

    public int getFluidAmount() {
        return fluidTank.getFluidAmount();
    }

    public int getFluidCapacity() {
        return fluidTank.getCapacity();
    }

    public FluidStack getFluidStack() {
        return fluidTank.getFluid();
    }

    public TankTier getTier() {
        return tier;
    }

    public float getFluidLevel() {
        int capacity = getFluidCapacity();
        if (capacity == 0) return 0.0f;
        return (float) getFluidAmount() / (float) capacity;
    }

    public @Nullable IFluidHandler getFluidHandler(Direction side) {
        return fluidTank;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("fluidTank", fluidTank.writeToNBT(registries, new CompoundTag()));
        tag.putString("tier", tier.name());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("fluidTank")) {
            fluidTank.readFromNBT(registries, tag.getCompound("fluidTank"));
        }
        if (tag.contains("tier")) {
            try {
                this.tier = TankTier.valueOf(tag.getString("tier"));
            } catch (IllegalArgumentException e) {
                this.tier = determineTier(getBlockState());
            }
        }
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, GTBlockEntities.TANK_BLOCK_ENTITY.get(),
                (blockEntity, direction) -> {
                    if (blockEntity instanceof TankBlockEntity tankBlockEntity) {
                        return tankBlockEntity.getFluidHandler(direction);
                    }
                    return null;
                });
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
            level.invalidateCapabilities(getBlockPos());
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            level.invalidateCapabilities(getBlockPos());
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public enum TankTier {
        COPPER, IRON, DIAMOND, NETHERITE
    }
}