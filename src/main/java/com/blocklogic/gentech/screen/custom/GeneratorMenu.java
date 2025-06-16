package com.blocklogic.gentech.screen.custom;

import com.blocklogic.gentech.block.entity.GeneratorBlockEntity;
import com.blocklogic.gentech.screen.GTMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.SlotItemHandler;

public class GeneratorMenu extends AbstractContainerMenu {
    private final GeneratorBlockEntity blockEntity;
    private final int upgradeSlots;
    private final ContainerLevelAccess access;
    private final Level level;

    private int lastWaterAmount = 0;
    private int lastWaterCapacity = 0;
    private int lastLavaAmount = 0;
    private int lastLavaCapacity = 0;
    private float lastProgressLevel = 0.0f;
    private GeneratorBlockEntity.BlockCategory lastTargetCategory = null;

    private static final int OUTPUT_START_X = 71;
    private static final int OUTPUT_START_Y = 17;
    private static final int UPGRADE_START_X = 188;
    private static final int UPGRADE_START_Y = 17;
    private static final int UPGRADE_SLOT_SPACING = 18;

    private static final int PLAYER_INVENTORY_X = 26;
    private static final int PLAYER_INVENTORY_Y = 86;
    private static final int PLAYER_HOTBAR_X = 26;
    private static final int PLAYER_HOTBAR_Y = 145;

    private static final int OUTPUT_SLOTS = 12;
    private static final int UPGRADE_SLOT_START = OUTPUT_SLOTS;

    public GeneratorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, (GeneratorBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public GeneratorMenu(int containerId, Inventory playerInventory, GeneratorBlockEntity blockEntity) {
        super(GTMenuTypes.GENERATOR_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.upgradeSlots = blockEntity.getUpgradeSlots();
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        this.level = playerInventory.player.level();

        checkContainerSize(playerInventory, OUTPUT_SLOTS + upgradeSlots);

        addOutputSlots();

        addUpgradeSlots();

        addPlayerInventory(playerInventory);

        addPlayerHotbar(playerInventory);

        addDataSlots();
    }

    private void addDataSlots() {
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getWaterAmount();
            }

            @Override
            public void set(int value) {
                lastWaterAmount = value;
            }
        });

        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getWaterCapacity();
            }

            @Override
            public void set(int value) {
                lastWaterCapacity = value;
            }
        });

        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getLavaAmount();
            }

            @Override
            public void set(int value) {
                lastLavaAmount = value;
            }
        });

        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getLavaCapacity();
            }

            @Override
            public void set(int value) {
                lastLavaCapacity = value;
            }
        });

        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (int) (blockEntity.getProgressLevel() * 1000);
            }

            @Override
            public void set(int value) {
                lastProgressLevel = value / 1000.0f;
            }
        });

        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                GeneratorBlockEntity.BlockCategory category = blockEntity.getTargetCategory();
                return category != null ? category.ordinal() : -1;
            }

            @Override
            public void set(int value) {
                if (value >= 0 && value < GeneratorBlockEntity.BlockCategory.values().length) {
                    lastTargetCategory = GeneratorBlockEntity.BlockCategory.values()[value];
                } else {
                    lastTargetCategory = null;
                }
            }
        });
    }

    private void addOutputSlots() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                int slotIndex = row * 4 + col;
                int x = OUTPUT_START_X + col * 18;
                int y = OUTPUT_START_Y + row * 18;

                addSlot(new SlotItemHandler(blockEntity.getItemHandler(), slotIndex, x, y) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                });
            }
        }
    }

    private void addUpgradeSlots() {
        for (int i = 0; i < upgradeSlots; i++) {
            int slotIndex = UPGRADE_SLOT_START + i;
            int x = UPGRADE_START_X;
            int y = UPGRADE_START_Y + (i * UPGRADE_SLOT_SPACING);

            addSlot(new SlotItemHandler(blockEntity.getItemHandler(), slotIndex, x, y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return blockEntity.getItemHandler().isItemValid(slotIndex, stack);
                }
            });
        }
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9 + 9;
                int x = PLAYER_INVENTORY_X + col * 18;
                int y = PLAYER_INVENTORY_Y + row * 18;

                addSlot(new Slot(playerInventory, slotIndex, x, y));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            int x = PLAYER_HOTBAR_X + col * 18;
            int y = PLAYER_HOTBAR_Y;

            addSlot(new Slot(playerInventory, col, x, y));
        }
    }

    public int getWaterAmount() {
        return level.isClientSide ? lastWaterAmount : blockEntity.getWaterAmount();
    }

    public int getWaterCapacity() {
        return level.isClientSide ? lastWaterCapacity : blockEntity.getWaterCapacity();
    }

    public int getLavaAmount() {
        return level.isClientSide ? lastLavaAmount : blockEntity.getLavaAmount();
    }

    public int getLavaCapacity() {
        return level.isClientSide ? lastLavaCapacity : blockEntity.getLavaCapacity();
    }

    public float getWaterLevel() {
        int capacity = getWaterCapacity();
        if (capacity == 0) return 0.0f;
        return (float) getWaterAmount() / (float) capacity;
    }

    public float getLavaLevel() {
        int capacity = getLavaCapacity();
        if (capacity == 0) return 0.0f;
        return (float) getLavaAmount() / (float) capacity;
    }

    public float getProgressLevel() {
        return level.isClientSide ? lastProgressLevel : blockEntity.getProgressLevel();
    }

    public GeneratorBlockEntity.BlockCategory getTargetCategory() {
        return level.isClientSide ? lastTargetCategory : blockEntity.getTargetCategory();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            if (index < OUTPUT_SLOTS + upgradeSlots) {
                if (!this.moveItemStackTo(stackInSlot, OUTPUT_SLOTS + upgradeSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                boolean movedToUpgrade = false;

                for (int i = OUTPUT_SLOTS; i < OUTPUT_SLOTS + upgradeSlots; i++) {
                    Slot upgradeSlot = this.slots.get(i);
                    if (upgradeSlot.mayPlace(stackInSlot)) {
                        if (this.moveItemStackTo(stackInSlot, i, i + 1, false)) {
                            movedToUpgrade = true;
                            break;
                        }
                    }
                }

                if (!movedToUpgrade) {
                    if (index < OUTPUT_SLOTS + upgradeSlots + 27) {
                        if (!this.moveItemStackTo(stackInSlot, OUTPUT_SLOTS + upgradeSlots + 27, OUTPUT_SLOTS + upgradeSlots + 36, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!this.moveItemStackTo(stackInSlot, OUTPUT_SLOTS + upgradeSlots, OUTPUT_SLOTS + upgradeSlots + 27, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, this.blockEntity.getBlockState().getBlock());
    }

    public GeneratorBlockEntity getBlockEntity() {
        return blockEntity;
    }
}