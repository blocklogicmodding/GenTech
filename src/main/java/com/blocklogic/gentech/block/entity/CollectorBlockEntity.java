package com.blocklogic.gentech.block.entity;

import com.blocklogic.gentech.Config;
import com.blocklogic.gentech.config.CustomCollectorRecipeConfig;
import com.blocklogic.gentech.item.GTItems;
import com.blocklogic.gentech.screen.custom.CollectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CollectorBlockEntity extends BlockEntity implements MenuProvider {

    private static final int UPGRADE_SLOT = 0;

    private int progress = 0;
    private int maxProgress;
    private boolean hasValidSources = false;
    private int lastValidatedTick = -1;

    private final ItemStackHandler itemHandler;
    private final FluidTank fluidTank;

    private Fluid currentTargetFluid = Fluids.EMPTY;
    private CustomCollectorRecipeConfig.CollectorRecipe currentRecipe = null;

    private int lastFluidAmount = -1;

    public CollectorBlockEntity(BlockPos pos, BlockState blockState) {
        super(GTBlockEntities.COLLECTOR_BLOCK_ENTITY.get(), pos, blockState);

        this.maxProgress = Config.getCollectorBaseCollectionTime();

        this.itemHandler = new ItemStackHandler(1) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return isValidSpeedUpgrade(stack);
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };

        this.fluidTank = new FluidTank(Config.getCollectorFluidCapacity()) {
            @Override
            public boolean isFluidValid(FluidStack stack) {
                if (currentTargetFluid == Fluids.EMPTY) return true;
                FluidStack existing = getFluid();
                return existing.isEmpty() || existing.getFluid() == currentTargetFluid;
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

    public static void tick(Level level, BlockPos pos, BlockState state, CollectorBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        int validationInterval = Config.getCollectorValidationInterval();
        if (level.getGameTime() % validationInterval == 0 ||
                blockEntity.lastValidatedTick == -1 ||
                level.getGameTime() - blockEntity.lastValidatedTick >= validationInterval) {
            blockEntity.validateSources();
            blockEntity.lastValidatedTick = (int) level.getGameTime();
        }

        if (blockEntity.hasValidSources && blockEntity.canCollect()) {
            blockEntity.progress++;

            if (blockEntity.progress >= blockEntity.maxProgress) {
                blockEntity.completeCollection();
            }
        } else {
            blockEntity.progress = 0;
        }

        blockEntity.setChanged();
    }

    private void validateSources() {
        if (level == null) return;

        hasValidSources = false;
        currentTargetFluid = Fluids.EMPTY;
        currentRecipe = null;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacentPos = worldPosition.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacentPos);

            if (adjacentState.getFluidState().isSource()) {
                Fluid detectedFluid = adjacentState.getFluidState().getType();

                if (detectedFluid == Fluids.EMPTY) continue;

                List<CustomCollectorRecipeConfig.CollectorRecipe> recipes =
                        CustomCollectorRecipeConfig.getRecipesForFluid(detectedFluid);

                if (!recipes.isEmpty()) {
                    int sourceCount = countAdjacentSources(detectedFluid);

                    CustomCollectorRecipeConfig.CollectorRecipe recipe = recipes.get(0);

                    if (sourceCount >= recipe.minimumSources) {
                        currentTargetFluid = detectedFluid;
                        currentRecipe = recipe;
                        hasValidSources = true;
                        break;
                    }
                }
            }
        }
    }

    private int countAdjacentSources(Fluid fluid) {
        if (level == null) return 0;

        int count = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacentPos = worldPosition.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacentPos);

            if (adjacentState.getFluidState().getType() == fluid &&
                    adjacentState.getFluidState().isSource()) {
                count++;
            }
        }
        return count;
    }

    private boolean canCollect() {
        if (currentRecipe == null) return false;
        return fluidTank.getSpace() >= currentRecipe.fluidPerCollection;
    }

    private void completeCollection() {
        if (currentRecipe == null || currentTargetFluid == Fluids.EMPTY) return;

        FluidStack fluidToAdd = new FluidStack(currentTargetFluid, currentRecipe.fluidPerCollection);
        fluidTank.fill(fluidToAdd, IFluidHandler.FluidAction.EXECUTE);

        this.progress = 0;
        this.maxProgress = getActualCollectionTime();
    }

    private int getActualCollectionTime() {
        if (currentRecipe == null) {
            return Config.getCollectorBaseCollectionTime();
        }

        double speedMultiplier = getSpeedMultiplier();
        return Math.max(1, (int) (currentRecipe.collectionTime / speedMultiplier));
    }

    private double getSpeedMultiplier() {
        ItemStack upgrade = itemHandler.getStackInSlot(UPGRADE_SLOT);
        if (upgrade.isEmpty()) return 1.0;

        Item upgradeItem = upgrade.getItem();
        if (upgradeItem == GTItems.BASIC_SPEED_UPGRADE.get()) {
            return Config.getBasicSpeedUpgradeMultiplier();
        } else if (upgradeItem == GTItems.ADVANCED_SPEED_UPGRADE.get()) {
            return Config.getAdvancedSpeedUpgradeMultiplier();
        } else if (upgradeItem == GTItems.ULTIMATE_SPEED_UPGRADE.get()) {
            return Config.getUltimateSpeedUpgradeMultiplier();
        }

        return 1.0;
    }

    private boolean isValidSpeedUpgrade(ItemStack stack) {
        if (stack.isEmpty()) return false;

        Item item = stack.getItem();
        return item == GTItems.BASIC_SPEED_UPGRADE.get() ||
                item == GTItems.ADVANCED_SPEED_UPGRADE.get() ||
                item == GTItems.ULTIMATE_SPEED_UPGRADE.get();
    }

    public float getProgressLevel() {
        if (maxProgress == 0) return 0.0f;
        return (float) progress / (float) maxProgress;
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

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public boolean hasValidSources() {
        return hasValidSources;
    }

    public Fluid getCurrentTargetFluid() {
        return currentTargetFluid;
    }

    public CustomCollectorRecipeConfig.CollectorRecipe getCurrentRecipe() {
        return currentRecipe;
    }

    public @Nullable IFluidHandler getFluidHandler(Direction side) {
        if (side == Direction.UP || side == Direction.DOWN) {
            return new OutputOnlyFluidHandler(fluidTank);
        }
        return null;
    }

    public @Nullable IItemHandler getItemHandler(Direction side) {
        return null;
    }

    private static class OutputOnlyFluidHandler implements IFluidHandler {
        private final FluidTank internal;

        public OutputOnlyFluidHandler(FluidTank internal) {
            this.internal = internal;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return internal.getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return internal.getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return internal.drain(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return internal.drain(maxDrain, action);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.gentech.fluid_collector");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CollectorMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.put("fluid_tank", fluidTank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("progress", progress);
        tag.putInt("max_progress", maxProgress);
        tag.putBoolean("has_valid_sources", hasValidSources);
        tag.putInt("last_validated_tick", lastValidatedTick);

        if (currentTargetFluid != Fluids.EMPTY) {
            tag.putString("current_target_fluid", currentTargetFluid.toString());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        fluidTank.readFromNBT(registries, tag.getCompound("fluid_tank"));
        progress = tag.getInt("progress");
        maxProgress = tag.getInt("max_progress");
        hasValidSources = tag.getBoolean("has_valid_sources");
        lastValidatedTick = tag.getInt("last_validated_tick");

        if (tag.contains("current_target_fluid")) {
            try {
                String fluidName = tag.getString("current_target_fluid");
                currentTargetFluid = Fluids.EMPTY;
            } catch (Exception e) {
                currentTargetFluid = Fluids.EMPTY;
            }
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

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, GTBlockEntities.COLLECTOR_BLOCK_ENTITY.get(),
                (blockEntity, direction) -> {
                    if (blockEntity instanceof CollectorBlockEntity collectorBlockEntity) {
                        return collectorBlockEntity.getFluidHandler(direction);
                    }
                    return null;
                });

        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, GTBlockEntities.COLLECTOR_BLOCK_ENTITY.get(),
                (blockEntity, direction) -> {
                    if (blockEntity instanceof CollectorBlockEntity collectorBlockEntity) {
                        return collectorBlockEntity.getItemHandler(direction);
                    }
                    return null;
                });
    }

    public void drops() {
        if (level == null || level.isClientSide()) return;

        ItemStack upgrade = itemHandler.getStackInSlot(UPGRADE_SLOT);
        if (!upgrade.isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), upgrade);
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
            level.invalidateCapabilities(getBlockPos());
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }
}