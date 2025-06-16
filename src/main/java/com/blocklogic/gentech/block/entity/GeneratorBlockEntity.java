package com.blocklogic.gentech.block.entity;

import com.blocklogic.gentech.block.custom.CopperGeneratorBlock;
import com.blocklogic.gentech.block.custom.DiamondGeneratorBlock;
import com.blocklogic.gentech.block.custom.IronGeneratorBlock;
import com.blocklogic.gentech.block.custom.NetheriteGeneratorBlock;
import com.blocklogic.gentech.screen.custom.GeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class GeneratorBlockEntity extends BlockEntity implements MenuProvider {

    // Constants for slot counts
    private static final int OUTPUT_SLOTS = 8;
    private static final int COPPER_UPGRADE_SLOTS = 0;
    private static final int IRON_UPGRADE_SLOTS = 1;
    private static final int DIAMOND_UPGRADE_SLOTS = 2;
    private static final int NETHERITE_UPGRADE_SLOTS = 3;

    // Inventory handler
    private final ItemStackHandler itemHandler;

    // Cached tier info
    private GeneratorTier tier;
    private int upgradeSlots;

    public GeneratorBlockEntity(BlockPos pos, BlockState blockState) {
        super(GTBlockEntities.GENERATOR_BLOCK_ENTITY.get(), pos, blockState);

        // Determine tier and upgrade slots based on block type
        this.tier = determineTier(blockState);
        this.upgradeSlots = getUpgradeSlotsForTier(this.tier);

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
        tag.putString("tier", tier.name());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
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

    // Enum for generator tiers
    public enum GeneratorTier {
        COPPER, IRON, DIAMOND, NETHERITE
    }
}