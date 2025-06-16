package com.blocklogic.gentech.block.entity;

import com.blocklogic.gentech.Config;
import com.blocklogic.gentech.block.custom.CopperGeneratorBlock;
import com.blocklogic.gentech.block.custom.DiamondGeneratorBlock;
import com.blocklogic.gentech.block.custom.IronGeneratorBlock;
import com.blocklogic.gentech.block.custom.NetheriteGeneratorBlock;
import com.blocklogic.gentech.screen.custom.GeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class GeneratorBlockEntity extends BlockEntity implements MenuProvider {

    // Constants for slot counts
    private static final int OUTPUT_SLOTS = 12;
    private static final int COPPER_UPGRADE_SLOTS = 0;
    private static final int IRON_UPGRADE_SLOTS = 1;
    private static final int DIAMOND_UPGRADE_SLOTS = 2;
    private static final int NETHERITE_UPGRADE_SLOTS = 3;

    // Inventory handler
    private final ItemStackHandler itemHandler;

    // Fluid tanks
    private final FluidTank waterTank;
    private final FluidTank lavaTank;

    // Cached tier info
    private GeneratorTier tier;
    private int upgradeSlots;

    // Client sync tracking
    private int lastWaterAmount = -1;
    private int lastLavaAmount = -1;

    public GeneratorBlockEntity(BlockPos pos, BlockState blockState) {
        super(GTBlockEntities.GENERATOR_BLOCK_ENTITY.get(), pos, blockState);

        // Determine tier and upgrade slots based on block type
        this.tier = determineTier(blockState);
        this.upgradeSlots = getUpgradeSlotsForTier(this.tier);

        // Get fluid buffer capacity from config based on tier
        int fluidCapacity = getFluidCapacityForTier(this.tier);

        // Create fluid tanks with tier-specific capacities
        this.waterTank = new FluidTank(fluidCapacity) {
            @Override
            public boolean isFluidValid(FluidStack stack) {
                return stack.getFluid() == Fluids.WATER;
            }

            @Override
            protected void onContentsChanged() {
                setChanged();
                if (level != null && !level.isClientSide()) {
                    // Force block update to sync to client
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                }
            }
        };

        this.lavaTank = new FluidTank(fluidCapacity) {
            @Override
            public boolean isFluidValid(FluidStack stack) {
                return stack.getFluid() == Fluids.LAVA;
            }

            @Override
            protected void onContentsChanged() {
                setChanged();
                if (level != null && !level.isClientSide()) {
                    // Force block update to sync to client
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                }
            }
        };

        // Create inventory handler with output slots + upgrade slots
        this.itemHandler = new ItemStackHandler(OUTPUT_SLOTS + upgradeSlots) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                // Output slots (0-7) - no input allowed
                if (slot < OUTPUT_SLOTS) {
                    return false;
                }
                // Upgrade slots - only accept upgrade items (will implement tag check later)
                return true; // TODO: Add upgrade item tag validation
            }
        };
    }

    // Static tick method for the block entity
    public static void tick(Level level, BlockPos pos, BlockState state, GeneratorBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return; // Only tick on server
        }

        // Check if fluid amounts have changed and force sync if needed
        int currentWater = blockEntity.waterTank.getFluidAmount();
        int currentLava = blockEntity.lavaTank.getFluidAmount();

        if (currentWater != blockEntity.lastWaterAmount || currentLava != blockEntity.lastLavaAmount) {
            blockEntity.lastWaterAmount = currentWater;
            blockEntity.lastLavaAmount = currentLava;

            // Force block update to sync fluid data to clients
            level.sendBlockUpdated(pos, state, state, 3);
            blockEntity.setChanged();
        }

        // TODO: Add generation logic here
        // This is where you'll implement the actual block generation
    }

    // Determine generator tier from block state
    private GeneratorTier determineTier(BlockState blockState) {
        if (blockState.getBlock() instanceof CopperGeneratorBlock) {
            return GeneratorTier.COPPER;
        } else if (blockState.getBlock() instanceof IronGeneratorBlock) {
            return GeneratorTier.IRON;
        } else if (blockState.getBlock() instanceof DiamondGeneratorBlock) {
            return GeneratorTier.DIAMOND;
        } else if (blockState.getBlock() instanceof NetheriteGeneratorBlock) {
            return GeneratorTier.NETHERITE;
        }
        return GeneratorTier.COPPER; // Fallback
    }

    // Get upgrade slot count for tier
    private int getUpgradeSlotsForTier(GeneratorTier tier) {
        return switch (tier) {
            case COPPER -> COPPER_UPGRADE_SLOTS;
            case IRON -> IRON_UPGRADE_SLOTS;
            case DIAMOND -> DIAMOND_UPGRADE_SLOTS;
            case NETHERITE -> NETHERITE_UPGRADE_SLOTS;
        };
    }

    // Get fluid capacity from config based on tier
    private int getFluidCapacityForTier(GeneratorTier tier) {
        return switch (tier) {
            case COPPER -> Config.getCopperGeneratorFluidBuffer();
            case IRON -> Config.getIronGeneratorFluidBuffer();
            case DIAMOND -> Config.getDiamondGeneratorFluidBuffer();
            case NETHERITE -> Config.getNetheriteGeneratorFluidBuffer();
        };
    }

    // Create side-specific fluid handler for capability system
    private IFluidHandler createSideFluidHandler(Direction side) {
        // Get the actual facing direction of the block
        Direction facing = getBlockState().getValue(CopperGeneratorBlock.FACING);

        // Convert relative side to absolute direction
        Direction absoluteSide = getAbsoluteSide(facing, side);

        return switch (absoluteSide) {
            case WEST -> waterTank;
            case EAST -> lavaTank;
            case NORTH, SOUTH -> new NoInputFluidHandler();
            default -> new NoAccessFluidHandler();
        };
    }

    // Convert relative side to absolute direction based on block facing
    private Direction getAbsoluteSide(Direction facing, Direction side) {
        if (side == Direction.UP || side == Direction.DOWN) {
            return side; // Top/bottom are always absolute
        }

        // Rotate the side based on the block's facing direction
        return switch (facing) {
            case NORTH -> side; // No rotation needed
            case SOUTH -> side.getOpposite();
            case EAST -> side.getClockWise();
            case WEST -> side.getCounterClockWise();
            default -> side;
        };
    }

    // Getters
    public GeneratorTier getTier() {
        return tier;
    }

    public int getUpgradeSlots() {
        return upgradeSlots;
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public FluidTank getWaterTank() {
        return waterTank;
    }

    public FluidTank getLavaTank() {
        return lavaTank;
    }

    // Get water amount for GUI syncing
    public int getWaterAmount() {
        return waterTank.getFluidAmount();
    }

    // Get water capacity for GUI syncing
    public int getWaterCapacity() {
        return waterTank.getCapacity();
    }

    // Get lava amount for GUI syncing
    public int getLavaAmount() {
        return lavaTank.getFluidAmount();
    }

    // Get lava capacity for GUI syncing
    public int getLavaCapacity() {
        return lavaTank.getCapacity();
    }

    // Get water level as percentage (0.0 to 1.0) for GUI
    public float getWaterLevel() {
        if (waterTank.getCapacity() == 0) return 0.0f;
        return (float) waterTank.getFluidAmount() / (float) waterTank.getCapacity();
    }

    // Get lava level as percentage (0.0 to 1.0) for GUI
    public float getLavaLevel() {
        if (lavaTank.getCapacity() == 0) return 0.0f;
        return (float) lavaTank.getFluidAmount() / (float) lavaTank.getCapacity();
    }

    // Test method to add fluids for testing (remove in production)
    public void addTestFluids() {
        waterTank.fill(new FluidStack(Fluids.WATER, 5000), IFluidHandler.FluidAction.EXECUTE);
        lavaTank.fill(new FluidStack(Fluids.LAVA, 7500), IFluidHandler.FluidAction.EXECUTE);
        setChanged();
    }

    // MenuProvider implementation
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.gentech." + tier.name().toLowerCase() + "_generator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new GeneratorMenu(containerId, playerInventory, this);
    }

    // NBT Save/Load
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.put("waterTank", waterTank.writeToNBT(registries, new CompoundTag()));
        tag.put("lavaTank", lavaTank.writeToNBT(registries, new CompoundTag()));
        tag.putString("tier", tier.name());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        if (tag.contains("waterTank")) {
            waterTank.readFromNBT(registries, tag.getCompound("waterTank"));
        }
        if (tag.contains("lavaTank")) {
            lavaTank.readFromNBT(registries, tag.getCompound("lavaTank"));
        }
        if (tag.contains("tier")) {
            try {
                this.tier = GeneratorTier.valueOf(tag.getString("tier"));
                this.upgradeSlots = getUpgradeSlotsForTier(this.tier);
            } catch (IllegalArgumentException e) {
                // Fallback to block-based detection if NBT is invalid
                this.tier = determineTier(getBlockState());
                this.upgradeSlots = getUpgradeSlotsForTier(this.tier);
            }
        }
    }

    // Capability provider method - to be called from block registration
    public @Nullable IFluidHandler getFluidHandler(Direction side) {
        // Handle null direction (usually means internal access)
        if (side == null) {
            // Return a combined handler for internal access
            return new CombinedFluidHandler(waterTank, lavaTank);
        }
        return createSideFluidHandler(side);
    }

    // Static capability registration method
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, GTBlockEntities.GENERATOR_BLOCK_ENTITY.get(),
                (blockEntity, direction) -> {
                    if (blockEntity instanceof GeneratorBlockEntity generatorBlockEntity) {
                        IFluidHandler handler = generatorBlockEntity.getFluidHandler(direction);
                        return handler;
                    }
                    return null;
                });
    }

    // Helper classes for restricted fluid access
    private static class CombinedFluidHandler implements IFluidHandler {
        private final FluidTank waterTank;
        private final FluidTank lavaTank;

        public CombinedFluidHandler(FluidTank waterTank, FluidTank lavaTank) {
            this.waterTank = waterTank;
            this.lavaTank = lavaTank;
        }

        @Override
        public int getTanks() {
            return 2;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return switch (tank) {
                case 0 -> waterTank.getFluid();
                case 1 -> lavaTank.getFluid();
                default -> FluidStack.EMPTY;
            };
        }

        @Override
        public int getTankCapacity(int tank) {
            return switch (tank) {
                case 0 -> waterTank.getCapacity();
                case 1 -> lavaTank.getCapacity();
                default -> 0;
            };
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return switch (tank) {
                case 0 -> waterTank.isFluidValid(stack);
                case 1 -> lavaTank.isFluidValid(stack);
                default -> false;
            };
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.getFluid() == Fluids.WATER) {
                return waterTank.fill(resource, action);
            } else if (resource.getFluid() == Fluids.LAVA) {
                return lavaTank.fill(resource, action);
            }
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.getFluid() == Fluids.WATER) {
                return waterTank.drain(resource, action);
            } else if (resource.getFluid() == Fluids.LAVA) {
                return lavaTank.drain(resource, action);
            }
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            // Try water first, then lava
            FluidStack drained = waterTank.drain(maxDrain, action);
            if (drained.isEmpty()) {
                drained = lavaTank.drain(maxDrain, action);
            }
            return drained;
        }
    }

    private static class NoInputFluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 0;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return 0;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0; // No input allowed
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY; // No output on these sides for now
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY; // No output on these sides for now
        }
    }

    private static class NoAccessFluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 0;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return 0;
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
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
            level.invalidateCapabilities(getBlockPos());
            // Force sync when data changes
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

    // Enum for generator tiers
    public enum GeneratorTier {
        COPPER, IRON, DIAMOND, NETHERITE
    }
}