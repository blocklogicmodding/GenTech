package com.blocklogic.gentech.screen.custom;

import com.blocklogic.gentech.block.entity.CollectorBlockEntity;
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

public class CollectorMenu extends AbstractContainerMenu {
    private final CollectorBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private final Level level;

    private int lastFluidAmount = 0;
    private int lastFluidCapacity = 0;
    private float lastProgressLevel = 0.0f;
    private boolean lastHasValidSources = false;

    private static final int UPGRADE_SLOT_X = 152;
    private static final int UPGRADE_SLOT_Y = 17;
    private static final int PLAYER_INVENTORY_X = 8;
    private static final int PLAYER_INVENTORY_Y = 86;
    private static final int PLAYER_HOTBAR_X = 8;
    private static final int PLAYER_HOTBAR_Y = 145;

    public CollectorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, (CollectorBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public CollectorMenu(int containerId, Inventory playerInventory, CollectorBlockEntity blockEntity) {
        super(GTMenuTypes.COLLECTOR_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        this.level = playerInventory.player.level();

        checkContainerSize(playerInventory, 1);

        addUpgradeSlot();
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots();
    }

    private void addDataSlots() {
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getFluidAmount();
            }

            @Override
            public void set(int value) {
                lastFluidAmount = value;
            }
        });

        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getFluidCapacity();
            }

            @Override
            public void set(int value) {
                lastFluidCapacity = value;
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
                return blockEntity.hasValidSources() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                lastHasValidSources = value == 1;
            }
        });
    }

    private void addUpgradeSlot() {
        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 0, UPGRADE_SLOT_X, UPGRADE_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return blockEntity.getItemHandler().isItemValid(0, stack);
            }
        });
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

    public int getFluidAmount() {
        return level.isClientSide ? lastFluidAmount : blockEntity.getFluidAmount();
    }

    public int getFluidCapacity() {
        return level.isClientSide ? lastFluidCapacity : blockEntity.getFluidCapacity();
    }

    public float getFluidLevel() {
        int capacity = getFluidCapacity();
        if (capacity == 0) return 0.0f;
        return (float) getFluidAmount() / (float) capacity;
    }

    public float getProgressLevel() {
        return level.isClientSide ? lastProgressLevel : blockEntity.getProgressLevel();
    }

    public boolean hasValidSources() {
        return level.isClientSide ? lastHasValidSources : blockEntity.hasValidSources();
    }

    public CollectorBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            if (index == 0) {
                if (!this.moveItemStackTo(stackInSlot, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (this.slots.get(0).mayPlace(stackInSlot)) {
                    if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (index < 28) {
                        if (!this.moveItemStackTo(stackInSlot, 28, 37, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!this.moveItemStackTo(stackInSlot, 1, 28, false)) {
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
}