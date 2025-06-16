package com.blocklogic.gentech.block.entity;

import com.blocklogic.gentech.Config;
import com.blocklogic.gentech.block.custom.CopperGeneratorBlock;
import com.blocklogic.gentech.block.custom.DiamondGeneratorBlock;
import com.blocklogic.gentech.block.custom.IronGeneratorBlock;
import com.blocklogic.gentech.block.custom.NetheriteGeneratorBlock;
import com.blocklogic.gentech.item.GTItems;
import com.blocklogic.gentech.screen.custom.GeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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

public class GeneratorBlockEntity extends BlockEntity implements MenuProvider {

    private static final int OUTPUT_SLOTS = 12;
    private static final int COPPER_UPGRADE_SLOTS = 0;
    private static final int IRON_UPGRADE_SLOTS = 1;
    private static final int DIAMOND_UPGRADE_SLOTS = 2;
    private static final int NETHERITE_UPGRADE_SLOTS = 3;

    private int progress = 0;
    private int maxProgress = 0;
    private Block targetBlock = null;
    private BlockCategory targetCategory = null;
    private int lastValidatedTick = -1;
    private static final int VALIDATION_INTERVAL = 20;

    private final ItemStackHandler itemHandler;

    private final FluidTank waterTank;
    private final FluidTank lavaTank;

    private GeneratorTier tier;
    private int upgradeSlots;

    private int lastWaterAmount = -1;
    private int lastLavaAmount = -1;

    public GeneratorBlockEntity(BlockPos pos, BlockState blockState) {
        super(GTBlockEntities.GENERATOR_BLOCK_ENTITY.get(), pos, blockState);

        this.tier = determineTier(blockState);
        this.upgradeSlots = getUpgradeSlotsForTier(this.tier);

        int fluidCapacity = getFluidCapacityForTier(this.tier);

        this.waterTank = new FluidTank(fluidCapacity) {
            @Override
            public boolean isFluidValid(FluidStack stack) {
                return stack.getFluid() == Fluids.WATER;
            }

            @Override
            protected void onContentsChanged() {
                setChanged();
                if (level != null && !level.isClientSide()) {
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
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                }
            }
        };

        this.itemHandler = new ItemStackHandler(OUTPUT_SLOTS + upgradeSlots) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                if (slot < OUTPUT_SLOTS) {
                    return false;
                }
                return isValidUpgradeItem(stack);
            }
        };
    }

    public @Nullable IItemHandler getItemHandler(Direction side) {
        if (side == null) {
            return itemHandler;
        }

        Direction facing = getBlockState().getValue(CopperGeneratorBlock.FACING);
        Direction absoluteSide = getAbsoluteSide(facing, side);

        if (absoluteSide == Direction.NORTH || absoluteSide == Direction.SOUTH) {
            return new OutputOnlyItemHandler(itemHandler);
        }

        return null;
    }

    private static class OutputOnlyItemHandler implements IItemHandler {
        private final ItemStackHandler internal;

        public OutputOnlyItemHandler(ItemStackHandler internal) {
            this.internal = internal;
        }

        @Override
        public int getSlots() {
            return OUTPUT_SLOTS;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot >= OUTPUT_SLOTS) return ItemStack.EMPTY;
            return internal.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot >= OUTPUT_SLOTS) return ItemStack.EMPTY;
            return internal.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot >= OUTPUT_SLOTS) return 0;
            return internal.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GeneratorBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        int currentWater = blockEntity.waterTank.getFluidAmount();
        int currentLava = blockEntity.lavaTank.getFluidAmount();

        if (currentWater != blockEntity.lastWaterAmount || currentLava != blockEntity.lastLavaAmount) {
            blockEntity.lastWaterAmount = currentWater;
            blockEntity.lastLavaAmount = currentLava;
            level.sendBlockUpdated(pos, state, state, 3);
            blockEntity.setChanged();
        }

        if (level.getGameTime() % VALIDATION_INTERVAL == 0 || blockEntity.lastValidatedTick == -1) {
            blockEntity.validateTargetBlock();
            blockEntity.lastValidatedTick = (int) level.getGameTime();
        }

        if (blockEntity.targetBlock == null || blockEntity.targetCategory == null) {
            if (blockEntity.progress > 0) {
                blockEntity.progress = 0;
                blockEntity.maxProgress = 0;
                blockEntity.setChanged();
            }
            return;
        }

        if (!blockEntity.canGenerate()) {
            return;
        }

        if (blockEntity.maxProgress == 0) {
            blockEntity.startGeneration();
        }

        blockEntity.progress++;

        if (blockEntity.progress >= blockEntity.maxProgress) {
            blockEntity.completeGeneration();
        }

        blockEntity.setChanged();

        if (level.getGameTime() % 10 == 0) {
            blockEntity.tryPushItemsToChestAbove();
        }
    }

    private void validateTargetBlock() {
        if (level == null) return;

        BlockPos belowPos = worldPosition.below();
        BlockState belowState = level.getBlockState(belowPos);
        Block belowBlock = belowState.getBlock();

        ResourceLocation blockLocation = BuiltInRegistries.BLOCK.getKey(belowBlock);
        String blockName = blockLocation.toString();

        List<String> softBlocks = Config.getValidatedSoftGeneratableBlocks();
        List<String> mediumBlocks = Config.getValidatedMediumGeneratableBlocks();
        List<String> hardBlocks = Config.getValidatedHardGeneratableBlocks();

        if (softBlocks.contains(blockName)) {
            this.targetBlock = belowBlock;
            this.targetCategory = BlockCategory.SOFT;
        } else if (mediumBlocks.contains(blockName)) {
            this.targetBlock = belowBlock;
            this.targetCategory = BlockCategory.MEDIUM;
        } else if (hardBlocks.contains(blockName)) {
            this.targetBlock = belowBlock;
            this.targetCategory = BlockCategory.HARD;
        } else {
            this.targetBlock = null;
            this.targetCategory = null;
        }
    }

    private boolean canGenerate() {
        if (targetBlock == null || targetCategory == null) {
            return false;
        }

        if (!hasOutputSpace()) {
            return false;
        }

        int requiredFluidAmount = getRequiredFluidConsumption();
        return waterTank.getFluidAmount() >= requiredFluidAmount &&
                lavaTank.getFluidAmount() >= requiredFluidAmount;
    }

    private boolean hasOutputSpace() {
        ItemStack targetItem = new ItemStack(targetBlock.asItem());

        for (int i = 0; i < OUTPUT_SLOTS; i++) {
            ItemStack slotStack = itemHandler.getStackInSlot(i);

            if (slotStack.isEmpty()) {
                return true;
            }

            if (ItemStack.isSameItemSameComponents(slotStack, targetItem) &&
                    slotStack.getCount() < slotStack.getMaxStackSize()) {
                return true;
            }
        }

        return false;
    }

    private void startGeneration() {
        if (targetCategory == null) return;

        int baseTime = getBaseGenerationTime();

        double speedMultiplier = getSpeedMultiplier();

        this.maxProgress = Math.max(1, (int) (baseTime / speedMultiplier));
        this.progress = 0;
    }

    private void completeGeneration() {
        if (targetBlock == null) return;

        int fluidConsumption = getRequiredFluidConsumption();
        waterTank.drain(fluidConsumption, IFluidHandler.FluidAction.EXECUTE);
        lavaTank.drain(fluidConsumption, IFluidHandler.FluidAction.EXECUTE);

        ItemStack generatedItem = new ItemStack(targetBlock.asItem());
        addItemToOutput(generatedItem);

        this.progress = 0;
        this.maxProgress = 0;
    }

    private void addItemToOutput(ItemStack item) {
        for (int i = 0; i < OUTPUT_SLOTS; i++) {
            ItemStack slotStack = itemHandler.getStackInSlot(i);

            if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(slotStack, item)) {
                int canAdd = Math.min(item.getCount(), slotStack.getMaxStackSize() - slotStack.getCount());
                if (canAdd > 0) {
                    slotStack.grow(canAdd);
                    item.shrink(canAdd);
                    if (item.isEmpty()) {
                        return;
                    }
                }
            }
        }

        for (int i = 0; i < OUTPUT_SLOTS; i++) {
            ItemStack slotStack = itemHandler.getStackInSlot(i);
            if (slotStack.isEmpty()) {
                itemHandler.setStackInSlot(i, item.copy());
                return;
            }
        }
    }

    private int getBaseGenerationTime() {
        if (targetCategory == null) return 100;

        return switch (tier) {
            case COPPER -> switch (targetCategory) {
                case SOFT -> Config.getCopperGeneratorSoftSpeed();
                case MEDIUM -> Config.getCopperGeneratorMediumSpeed();
                case HARD -> Config.getCopperGeneratorHardSpeed();
            };
            case IRON -> switch (targetCategory) {
                case SOFT -> Config.getIronGeneratorSoftSpeed();
                case MEDIUM -> Config.getIronGeneratorMediumSpeed();
                case HARD -> Config.getIronGeneratorHardSpeed();
            };
            case DIAMOND -> switch (targetCategory) {
                case SOFT -> Config.getDiamondGeneratorSoftSpeed();
                case MEDIUM -> Config.getDiamondGeneratorMediumSpeed();
                case HARD -> Config.getDiamondGeneratorHardSpeed();
            };
            case NETHERITE -> switch (targetCategory) {
                case SOFT -> Config.getNetheriteGeneratorSoftSpeed();
                case MEDIUM -> Config.getNetheriteGeneratorMediumSpeed();
                case HARD -> Config.getNetheriteGeneratorHardSpeed();
            };
        };
    }

    private int getRequiredFluidConsumption() {
        if (targetCategory == null) return 100;

        int baseConsumption = switch (tier) {
            case COPPER -> switch (targetCategory) {
                case SOFT -> Config.getCopperGeneratorSoftConsumption();
                case MEDIUM -> Config.getCopperGeneratorMediumConsumption();
                case HARD -> Config.getCopperGeneratorHardConsumption();
            };
            case IRON -> switch (targetCategory) {
                case SOFT -> Config.getIronGeneratorSoftConsumption();
                case MEDIUM -> Config.getIronGeneratorMediumConsumption();
                case HARD -> Config.getIronGeneratorHardConsumption();
            };
            case DIAMOND -> switch (targetCategory) {
                case SOFT -> Config.getDiamondGeneratorSoftConsumption();
                case MEDIUM -> Config.getDiamondGeneratorMediumConsumption();
                case HARD -> Config.getDiamondGeneratorHardConsumption();
            };
            case NETHERITE -> switch (targetCategory) {
                case SOFT -> Config.getNetheriteGeneratorSoftConsumption();
                case MEDIUM -> Config.getNetheriteGeneratorMediumConsumption();
                case HARD -> Config.getNetheriteGeneratorHardConsumption();
            };
        };

        double efficiencyReduction = getEfficiencyReduction();
        return Math.max(1, (int) (baseConsumption * (1.0 - efficiencyReduction)));
    }

    private double getSpeedMultiplier() {
        double multiplier = 1.0;

        for (int i = OUTPUT_SLOTS; i < OUTPUT_SLOTS + upgradeSlots; i++) {
            ItemStack upgrade = itemHandler.getStackInSlot(i);
            if (!upgrade.isEmpty()) {
                Item upgradeItem = upgrade.getItem();

                if (upgradeItem == GTItems.BASIC_SPEED_UPGRADE.get()) {
                    multiplier *= Config.getBasicSpeedUpgradeMultiplier();
                } else if (upgradeItem == GTItems.ADVANCED_SPEED_UPGRADE.get()) {
                    multiplier *= Config.getAdvancedSpeedUpgradeMultiplier();
                } else if (upgradeItem == GTItems.ULTIMATE_SPEED_UPGRADE.get()) {
                    multiplier *= Config.getUltimateSpeedUpgradeMultiplier();
                }
            }
        }

        return multiplier;
    }

    private double getEfficiencyReduction() {
        double reduction = 0.0;

        for (int i = OUTPUT_SLOTS; i < OUTPUT_SLOTS + upgradeSlots; i++) {
            ItemStack upgrade = itemHandler.getStackInSlot(i);
            if (!upgrade.isEmpty()) {
                Item upgradeItem = upgrade.getItem();

                if (upgradeItem == GTItems.BASIC_EFFICIENCY_UPGRADE.get()) {
                    reduction += Config.getBasicEfficiencyUpgradeReduction();
                } else if (upgradeItem == GTItems.ADVANCED_EFFICIENCY_UPGRADE.get()) {
                    reduction += Config.getAdvancedEfficiencyUpgradeReduction();
                } else if (upgradeItem == GTItems.ULTIMATE_EFFICIENCY_UPGRADE.get()) {
                    reduction += Config.getUltimateEfficiencyUpgradeReduction();
                }
            }
        }

        return Math.min(0.95, reduction);
    }

    private boolean isValidUpgradeItem(ItemStack stack) {
        if (stack.isEmpty()) return false;

        Item item = stack.getItem();
        return item == GTItems.BASIC_SPEED_UPGRADE.get() ||
                item == GTItems.ADVANCED_SPEED_UPGRADE.get() ||
                item == GTItems.ULTIMATE_SPEED_UPGRADE.get() ||
                item == GTItems.BASIC_EFFICIENCY_UPGRADE.get() ||
                item == GTItems.ADVANCED_EFFICIENCY_UPGRADE.get() ||
                item == GTItems.ULTIMATE_EFFICIENCY_UPGRADE.get();
    }

    public float getProgressLevel() {
        if (maxProgress == 0) return 0.0f;
        return (float) progress / (float) maxProgress;
    }

    public Block getTargetBlock() {
        return targetBlock;
    }

    public BlockCategory getTargetCategory() {
        return targetCategory;
    }

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
        return GeneratorTier.COPPER;
    }

    private int getUpgradeSlotsForTier(GeneratorTier tier) {
        return switch (tier) {
            case COPPER -> COPPER_UPGRADE_SLOTS;
            case IRON -> IRON_UPGRADE_SLOTS;
            case DIAMOND -> DIAMOND_UPGRADE_SLOTS;
            case NETHERITE -> NETHERITE_UPGRADE_SLOTS;
        };
    }

    private int getFluidCapacityForTier(GeneratorTier tier) {
        return switch (tier) {
            case COPPER -> Config.getCopperGeneratorFluidBuffer();
            case IRON -> Config.getIronGeneratorFluidBuffer();
            case DIAMOND -> Config.getDiamondGeneratorFluidBuffer();
            case NETHERITE -> Config.getNetheriteGeneratorFluidBuffer();
        };
    }

    private IFluidHandler createSideFluidHandler(Direction side) {
        Direction facing = getBlockState().getValue(CopperGeneratorBlock.FACING);

        Direction absoluteSide = getAbsoluteSide(facing, side);

        return switch (absoluteSide) {
            case WEST -> waterTank;
            case EAST -> lavaTank;
            case NORTH, SOUTH -> new NoInputFluidHandler();
            default -> new NoAccessFluidHandler();
        };
    }

    private Direction getAbsoluteSide(Direction facing, Direction side) {
        if (side == Direction.UP || side == Direction.DOWN) {
            return side;
        }

        return switch (facing) {
            case NORTH -> side;
            case SOUTH -> side.getOpposite();
            case EAST -> side.getClockWise();
            case WEST -> side.getCounterClockWise();
            default -> side;
        };
    }

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

    public int getWaterAmount() {
        return waterTank.getFluidAmount();
    }

    public int getWaterCapacity() {
        return waterTank.getCapacity();
    }

    public int getLavaAmount() {
        return lavaTank.getFluidAmount();
    }

    public int getLavaCapacity() {
        return lavaTank.getCapacity();
    }

    public float getWaterLevel() {
        if (waterTank.getCapacity() == 0) return 0.0f;
        return (float) waterTank.getFluidAmount() / (float) waterTank.getCapacity();
    }

    public float getLavaLevel() {
        if (lavaTank.getCapacity() == 0) return 0.0f;
        return (float) lavaTank.getFluidAmount() / (float) lavaTank.getCapacity();
    }

    public void addTestFluids() {
        waterTank.fill(new FluidStack(Fluids.WATER, 5000), IFluidHandler.FluidAction.EXECUTE);
        lavaTank.fill(new FluidStack(Fluids.LAVA, 7500), IFluidHandler.FluidAction.EXECUTE);
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.gentech." + tier.name().toLowerCase() + "_generator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new GeneratorMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.put("waterTank", waterTank.writeToNBT(registries, new CompoundTag()));
        tag.put("lavaTank", lavaTank.writeToNBT(registries, new CompoundTag()));
        tag.putString("tier", tier.name());
        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);

        if (targetBlock != null) {
            ResourceLocation blockLocation = BuiltInRegistries.BLOCK.getKey(targetBlock);
            tag.putString("targetBlock", blockLocation.toString());
        }

        if (targetCategory != null) {
            tag.putString("targetCategory", targetCategory.name());
        }
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
                this.tier = determineTier(getBlockState());
                this.upgradeSlots = getUpgradeSlotsForTier(this.tier);
            }
        }

        this.progress = tag.getInt("progress");
        this.maxProgress = tag.getInt("maxProgress");

        if (tag.contains("targetBlock")) {
            try {
                ResourceLocation blockLocation = ResourceLocation.parse(tag.getString("targetBlock"));
                this.targetBlock = BuiltInRegistries.BLOCK.get(blockLocation);
            } catch (Exception e) {
                this.targetBlock = null;
            }
        }

        if (tag.contains("targetCategory")) {
            try {
                this.targetCategory = BlockCategory.valueOf(tag.getString("targetCategory"));
            } catch (IllegalArgumentException e) {
                this.targetCategory = null;
            }
        }
    }

    public @Nullable IFluidHandler getFluidHandler(Direction side) {
        if (side == null) {
            return new CombinedFluidHandler(waterTank, lavaTank);
        }
        return createSideFluidHandler(side);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, GTBlockEntities.GENERATOR_BLOCK_ENTITY.get(),
                (blockEntity, direction) -> {
                    if (blockEntity instanceof GeneratorBlockEntity generatorBlockEntity) {
                        return generatorBlockEntity.getFluidHandler(direction);
                    }
                    return null;
                });

        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, GTBlockEntities.GENERATOR_BLOCK_ENTITY.get(),
                (blockEntity, direction) -> {
                    if (blockEntity instanceof GeneratorBlockEntity generatorBlockEntity) {
                        return generatorBlockEntity.getItemHandler(direction);
                    }
                    return null;
                });
    }

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

    private void tryPushItemsToChestAbove() {
        if (level == null || level.isClientSide()) return;

        BlockPos abovePos = worldPosition.above();

        IItemHandler chestHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, abovePos, Direction.DOWN);

        if (chestHandler != null) {
            for (int i = 0; i < OUTPUT_SLOTS; i++) {
                ItemStack outputStack = itemHandler.getStackInSlot(i);
                if (!outputStack.isEmpty()) {
                    ItemStack remaining = insertItemIntoHandler(chestHandler, outputStack, false);

                    if (remaining.getCount() != outputStack.getCount()) {
                        itemHandler.setStackInSlot(i, remaining);
                        setChanged();
                    }
                }
            }
        }
    }

    private ItemStack insertItemIntoHandler(IItemHandler handler, ItemStack stack, boolean simulate) {
        ItemStack remaining = stack.copy();

        for (int slot = 0; slot < handler.getSlots(); slot++) {
            remaining = handler.insertItem(slot, remaining, simulate);
            if (remaining.isEmpty()) {
                break;
            }
        }

        return remaining;
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

    public enum GeneratorTier {
        COPPER, IRON, DIAMOND, NETHERITE
    }

    public enum BlockCategory {
        SOFT, MEDIUM, HARD
    }
}