package net.mehvahdjukaar.supplementaries.common.utils.forge;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.supplementaries.common.block.IKeyLockable;
import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SafeBlockTile;
import net.mehvahdjukaar.supplementaries.common.capabilities.CapabilityHandler;
import net.mehvahdjukaar.supplementaries.common.items.SackItem;
import net.mehvahdjukaar.supplementaries.common.utils.ItemsUtil;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CuriosCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public class ItemsUtilImpl {

    public static boolean extractFromContainerItemIntoSlot(Player player, ItemStack containerStack, Slot slot) {
        if (slot.mayPickup(player) && containerStack.getCount() == 1) {

            var handlerAndTe = getItemHandler(containerStack, player);
            if (handlerAndTe != null) {
                IItemHandler handler = handlerAndTe.getFirst();
                for (int s = 0; s < handler.getSlots(); s++) {
                    ItemStack selected = handler.getStackInSlot(s);
                    if (!selected.isEmpty()) {
                        ItemStack dropped = handler.extractItem(s, 1, false);

                        if (slot.mayPlace(dropped)) {
                            slot.set(dropped);
                            CompoundTag newTag = new CompoundTag();
                            newTag.put("BlockEntityTag", handlerAndTe.getSecond().saveWithoutMetadata());
                            containerStack.setTag(newTag);
                            return true;
                        }
                        return false;
                    }
                }
            }
        }
        return false;
    }

    public static boolean addToContainerItem(Player player, ItemStack containerStack, ItemStack stack, Slot slot, boolean simulate, boolean inSlot) {
        if (slot.mayPickup(player) && containerStack.getCount() == 1) {

            var handlerAndTe = getItemHandler(containerStack, player);
            if (handlerAndTe != null) {
                IItemHandler handler = handlerAndTe.getFirst();
                ItemStack result = ItemHandlerHelper.insertItem(handler, stack.copy(), simulate);
                boolean success = result.isEmpty() || result.getCount() != stack.getCount();
                if (success) {
                    if (simulate) {
                        return true;
                    } else {
                        //this is a mess and probably not even correct
                        CompoundTag tileTag = handlerAndTe.getSecond().saveWithoutMetadata();
                        if (inSlot) {
                            stack.setCount(result.getCount());
                            ItemStack newStack = containerStack.copy();
                            newStack.getOrCreateTag().put("BlockEntityTag", tileTag);
                            if (slot.mayPlace(newStack)) {
                                slot.set(newStack);
                                return true;
                            }
                        } else {
                            int i = stack.getCount() - result.getCount();
                            slot.safeTake(i, i, player);
                            containerStack.getOrCreateTag().put("BlockEntityTag", tileTag);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    public static Pair<IItemHandler, BlockEntity> getItemHandler(ItemStack containerStack, Player player) {
        CompoundTag tag = containerStack.getTag();
        if(tag != null) {
            CompoundTag cmp = tag.getCompound("BlockEntityTag");
            if (!cmp.contains("LootTable")) {
                BlockEntity te = ItemsUtil.loadBlockEntityFromItem(cmp.copy(), containerStack.getItem());

                if (te != null) {
                    if (te instanceof SafeBlockTile safe && !safe.canPlayerOpen(player, false)) return null;
                    LazyOptional<IItemHandler> handlerHolder = te.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
                    if (handlerHolder.isPresent()) {
                        return Pair.of(handlerHolder.orElseGet(EmptyHandler::new), te);
                    }
                }
            }
        }
        return null;
    }

    public static float getEncumbermentFromInventory(ItemStack stack, ServerPlayer player) {
        float amount = 0;
        AtomicReference<IItemHandler> reference = new AtomicReference<>();
        player.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(reference::set);
        if (reference.get() != null) {
            for (int idx = 0; idx < reference.get().getSlots(); idx++) {
                ItemStack slotItem = reference.get().getStackInSlot(idx);
                amount += SackItem.getEncumber(slotItem);
            }
        }
        return amount;
    }

    public static KeyLockableTile.KeyStatus hasKeyInInventory(Player player, String key) {
        if (key == null) return KeyLockableTile.KeyStatus.CORRECT_KEY;
        KeyLockableTile.KeyStatus found = KeyLockableTile.KeyStatus.NO_KEY;
        if (CompatHandler.CURIOS) {
            found = CuriosCompat.isKeyInCurio(player, key);
            if (found == KeyLockableTile.KeyStatus.CORRECT_KEY) return found;
        }

        AtomicReference<IItemHandler> itemHandler = new AtomicReference<>();
        player.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(itemHandler::set);
        if (itemHandler.get() != null) {
            for (int idx = 0; idx < itemHandler.get().getSlots(); idx++) {
                ItemStack stack = itemHandler.get().getStackInSlot(idx);
                KeyLockableTile.KeyStatus status = IKeyLockable.getKeyStatus(stack, key);
                if (status == KeyLockableTile.KeyStatus.CORRECT_KEY) {
                    return status;
                } else if (status == KeyLockableTile.KeyStatus.INCORRECT_KEY) {
                    found = status;
                }
            }
        }
        return found;
    }

    public static ItemStack removeFirstStackFromInventory(Level level, BlockPos pos, Direction dir, BlockEntity tile) {
        IItemHandler itemHandler = CapabilityHandler.get(tile, ForgeCapabilities.ITEM_HANDLER, dir);
        if (itemHandler != null) {
            for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                ItemStack itemstack = itemHandler.getStackInSlot(slot);
                if (!itemstack.isEmpty()) {
                    ItemStack extracted = itemHandler.extractItem(slot, 1, false);
                    //empty stack means it can't extract from inventory
                    if (!extracted.isEmpty()) {
                        tile.setChanged();
                        return extracted.copy();
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

}
