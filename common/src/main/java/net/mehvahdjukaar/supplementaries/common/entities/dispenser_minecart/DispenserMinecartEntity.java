package net.mehvahdjukaar.supplementaries.common.entities.dispenser_minecart;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;

public class DispenserMinecartEntity extends Minecart implements Container, MenuProvider {

    private static final BlockState BLOCK_STATE = Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.UP);

    private final MovingDispenserBlockEntity dispenser;

    private boolean onActivator = false;
    private boolean powered = false;

    public DispenserMinecartEntity(Level level, double x, double y, double z) {
        this(ModEntities.DISPENSER_MINECART.get(), level);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    public DispenserMinecartEntity(EntityType<DispenserMinecartEntity> entityType, Level level) {
        super(entityType, level);
        this.dispenser = new MovingDispenserBlockEntity(BlockEntityType.DISPENSER, BlockPos.ZERO, BLOCK_STATE, this);
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!this.level.isClientSide && reason.shouldDestroy()) {
            Containers.dropContents(this.level, this, this);
        }

        super.remove(reason);
    }

    @Override
    protected void applyNaturalSlowdown() {
        float f = 0.98F;
        int i = 15 - AbstractContainerMenu.getRedstoneSignalFromContainer(this);
        f += i * 0.001F;

        if (this.isInWater()) {
            f *= 0.95F;
        }
        this.setDeltaMovement(this.getDeltaMovement().multiply(f, 0.0, f));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.put("Dispenser", this.dispenser.saveWithoutMetadata());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.dispenser.load(pCompound.getCompound("Dispenser"));
    }

    @Override
    public ItemStack getPickResult() {
        return ModRegistry.DISPENSER_MINECART_ITEM.get().getDefaultInstance();
    }

    @Override
    protected Item getDropItem() {
        return ModRegistry.DISPENSER_MINECART_ITEM.get();
    }

    @Override
    public Type getMinecartType() {
        return Type.CHEST;
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return BLOCK_STATE;
    }

    @Override
    public InteractionResult interact(Player pPlayer, InteractionHand pHand) {
        InteractionResult ret = InteractionResult.PASS;
        if (ret.consumesAction()) return ret;
        pPlayer.openMenu(this);
        if (!pPlayer.level.isClientSide) {
            this.gameEvent(GameEvent.CONTAINER_OPEN, pPlayer);
            PiglinAi.angerNearbyPiglins(pPlayer, true);
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.SUCCESS;
        }
    }

    //-------container stuff-------

    /**
     * Returns the number of slots in the inventory.
     */
    @Override
    public int getContainerSize() {
        return this.dispenser.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return this.dispenser.isEmpty();
    }

    @Override
    public ItemStack getItem(int pIndex) {
        return this.dispenser.getItem(pIndex);
    }

    @Override
    public ItemStack removeItem(int pIndex, int pCount) {
        return this.dispenser.removeItem(pIndex, pCount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int pIndex) {
        return this.dispenser.removeItemNoUpdate(pIndex);
    }

    @Override
    public void setItem(int pIndex, ItemStack pStack) {
        this.dispenser.setItem(pIndex, pStack);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        if (this.isRemoved()) {
            return false;
        } else {
            return pPlayer.distanceToSqr(this) <= 64.0D;
        }
    }

    @Override
    public void clearContent() {
        this.dispenser.clearContent();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        return this.dispenser.createMenu(pContainerId, pInventory, pPlayer);
    }

    //------end-container-stuff------

    @Override
    public SlotAccess getSlot(final int pSlot) {
        return pSlot >= 0 && pSlot < this.getContainerSize() ? new SlotAccess() {
            public ItemStack get() {
                return dispenser.getItem(pSlot);
            }

            public boolean set(ItemStack carried) {
                dispenser.setItem(pSlot, carried);
                return true;
            }
        } : super.getSlot(pSlot);
    }

    /**
     * Called every tick the minecart is on an activator rail.
     */
    @Override
    public void activateMinecart(int pX, int pY, int pZ, boolean pReceivingPower) {
        this.onActivator = true;
        if (!this.powered && pReceivingPower) {
            if (this.level instanceof ServerLevel serverLevel) {
                this.dispenseFrom(serverLevel, this.blockPosition());
            }
        }
        this.powered = pReceivingPower;
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick() {
        super.tick();
        this.dispenser.setLevel(this.getLevel());
        if (!this.level.isClientSide && this.isAlive() && this.powered) {
            if (!this.onActivator) this.powered = false;
            this.onActivator = false;
        }
    }

    @Override
    public void teleportTo(double pX, double pY, double pZ) {
        super.teleportTo(pX, pY, pZ);
        level.broadcastEntityEvent(this, (byte) 46);
    }

    protected void dispenseFrom(ServerLevel pLevel, BlockPos pPos) {

        ((ILevelEventRedirect) pLevel).setRedirected(true, this.position());

        int i = this.dispenser.getRandomSlot(pLevel.getRandom());

        if (i < 0) {
            //replace with client side animation
            pLevel.levelEvent(1001, pPos, 0);
            pLevel.gameEvent(this, GameEvent.DISPENSE_FAIL, pPos);
        } else {
            ItemStack itemstack = this.dispenser.getItem(i);
            try {
                DispenseItemBehavior dispenseitembehavior =
                        ((DispenserBlock) Blocks.DISPENSER).getDispenseMethod(itemstack);
                if (dispenseitembehavior != DispenseItemBehavior.NOOP) {
                    MovingBlockSource<?> blockSource = new MovingBlockSource<>(this, dispenser);
                    this.dispenser.setItem(i, dispenseitembehavior.dispense(blockSource, itemstack));
                }
            } catch (Exception e) {
                Supplementaries.LOGGER.warn("Failed to execute Dispenser Minecart behavior for item {}", itemstack.getItem());
            }
        }
        ((ILevelEventRedirect) pLevel).setRedirected(false, Vec3.ZERO);

    }

}