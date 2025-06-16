package com.blocklogic.gentech.screen.custom;

import com.blocklogic.gentech.block.entity.GeneratorBlockEntity;
import com.blocklogic.gentech.screen.GTMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class GeneratorMenu extends AbstractContainerMenu {
    private final GeneratorBlockEntity blockEntity;
    private final int upgradeSlots;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    // Slot positions based on your layout
    private static final int OUTPUT_START_X = 71;
    private static final int OUTPUT_START_Y = 17;
    private static final int UPGRADE_START_X = 188;
    private static final int UPGRADE_START_Y = 17;
    private static final int UPGRADE_SLOT_SPACING = 18;

    private static final int PLAYER_INVENTORY_X = 26;
    private static final int PLAYER_INVENTORY_Y = 86;
    private static final int PLAYER_HOTBAR_X = 26;
    private static final int PLAYER_HOTBAR_Y = 145;

    // Slot indices
    private static final int OUTPUT_SLOTS = 12;
    private static final int UPGRADE_SLOT_START = OUTPUT_SLOTS;

    // Constructor for IContainerFactory (used by registration)
    public GeneratorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, (GeneratorBlockEntity) playerInventory.player.level()
                .getBlockEntity(extraData.readBlockPos()));
    }

    // Main constructor
    public GeneratorMenu(int containerId, Inventory playerInventory, GeneratorBlockEntity blockEntity) {
        super(GTMenuTypes.GENERATOR_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.upgradeSlots = blockEntity.getUpgradeSlots();
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        // Create a DIRECT reference ContainerData that gets live values
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> blockEntity.getWaterAmount();
                    case 1 -> blockEntity.getWaterCapacity();
                    case 2 -> blockEntity.getLavaAmount();
                    case 3 -> blockEntity.getLavaCapacity();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                // Server -> Client sync only, no setting from client
            }

            @Override
            public int getCount() {
                return 4;
            }
        };

        // Add the data slots for syncing
        this.addDataSlots(data);

        checkContainerSize(playerInventory, OUTPUT_SLOTS + upgradeSlots);

        // Add output slots (4x2 grid to make 8 slots)
        addOutputSlots();

        // Add upgrade slots (dynamic based on tier)
        addUpgradeSlots();

        // Add player inventory
        addPlayerInventory(playerInventory);

        // Add player hotbar
        addPlayerHotbar(playerInventory);
    }

    private void addOutputSlots() {
        // 4x3 grid for 12 output slots
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                int slotIndex = row * 4 + col;
                int x = OUTPUT_START_X + col * 18;
                int y = OUTPUT_START_Y + row * 18;

                addSlot(new SlotItemHandler(blockEntity.getItemHandler(), slotIndex, x, y) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false; // Output slots - no input allowed
                    }
                });
            }
        }
    }

    private void addUpgradeSlots() {
        // Add upgrade slots based on tier (0-3 slots)
        for (int i = 0; i < upgradeSlots; i++) {
            int slotIndex = UPGRADE_SLOT_START + i;
            int x = UPGRADE_START_X;
            int y = UPGRADE_START_Y + (i * UPGRADE_SLOT_SPACING);

            addSlot(new SlotItemHandler(blockEntity.getItemHandler(), slotIndex, x, y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    // TODO: Check if stack is a valid upgrade item using tags
                    return true; // Placeholder - will implement tag checking later
                }
            });
        }
    }

    private void addPlayerInventory(Inventory playerInventory) {
        // Player inventory (3x9 grid)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9 + 9; // +9 to skip hotbar
                int x = PLAYER_INVENTORY_X + col * 18;
                int y = PLAYER_INVENTORY_Y + row * 18;

                addSlot(new Slot(playerInventory, slotIndex, x, y));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        // Player hotbar (1x9 grid)
        for (int col = 0; col < 9; col++) {
            int x = PLAYER_HOTBAR_X + col * 18;
            int y = PLAYER_HOTBAR_Y;

            addSlot(new Slot(playerInventory, col, x, y));
        }
    }

    // Getter methods for accessing synced data - THESE NOW GET LIVE DATA
    public int getWaterAmount() {
        return this.data.get(0);
    }

    public int getWaterCapacity() {
        return this.data.get(1);
    }

    public int getLavaAmount() {
        return this.data.get(2);
    }

    public int getLavaCapacity() {
        return this.data.get(3);
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

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            // If item is in generator slots (output or upgrade)
            if (index < OUTPUT_SLOTS + upgradeSlots) {
                // Try to move to player inventory
                if (!this.moveItemStackTo(stackInSlot, OUTPUT_SLOTS + upgradeSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Item is in player inventory
                // Try to move to upgrade slots first (if it's a valid upgrade)
                if (index >= OUTPUT_SLOTS + upgradeSlots) {
                    // TODO: Add logic to identify upgrade items and move them to upgrade slots
                    // For now, just try to move to any available slot
                    if (!this.moveItemStackTo(stackInSlot, OUTPUT_SLOTS, OUTPUT_SLOTS + upgradeSlots, false)) {
                        // If that fails, do standard player inventory shuffling
                        if (index < OUTPUT_SLOTS + upgradeSlots + 27) {
                            // From main inventory to hotbar
                            if (!this.moveItemStackTo(stackInSlot, OUTPUT_SLOTS + upgradeSlots + 27, OUTPUT_SLOTS + upgradeSlots + 36, false)) {
                                return ItemStack.EMPTY;
                            }
                        } else {
                            // From hotbar to main inventory
                            if (!this.moveItemStackTo(stackInSlot, OUTPUT_SLOTS + upgradeSlots, OUTPUT_SLOTS + upgradeSlots + 27, false)) {
                                return ItemStack.EMPTY;
                            }
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