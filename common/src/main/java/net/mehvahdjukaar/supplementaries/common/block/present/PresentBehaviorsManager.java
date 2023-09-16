package net.mehvahdjukaar.supplementaries.common.block.present;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.TrappedPresentBlock;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSendKnockbackPacket;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Position;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import java.util.Optional;

//TODO: add eggs and snowballs
public class PresentBehaviorsManager {

    public static void registerBehaviors() {
        for (Item i : Registry.ITEM) {
            if (i instanceof BlockItem bi && bi.getBlock() instanceof TntBlock) {
                TrappedPresentBlock.registerBehavior(i, TNT_BEHAVIOR);
            }
            if (i instanceof SpawnEggItem sp) {
                TrappedPresentBlock.registerBehavior(sp, SPAWN_EGG_BEHAVIOR);
            }
        }

        TrappedPresentBlock.registerBehavior(Items.FIREWORK_ROCKET, FIREWORK_BEHAVIOR);
        TrappedPresentBlock.registerBehavior(Items.SPLASH_POTION, SPLASH_POTION_BEHAVIOR);
        TrappedPresentBlock.registerBehavior(Items.LINGERING_POTION, SPLASH_POTION_BEHAVIOR);
    }

    //projectiles, fireworks, tnt, spawn eggs

    private static final IPresentItemBehavior SPAWN_EGG_BEHAVIOR = (source, stack) -> {

        EntityType<?> type = ((SpawnEggItem) stack.getItem()).getType(stack.getTag());

        try {
            ServerLevel level = source.getLevel();

            BlockPos pos = source.getPos();
            Entity e = spawnMob(type, level, source, stack);
            if (e != null) {
                stack.shrink(1);
                level.gameEvent(null,GameEvent.ENTITY_PLACE, pos);
                return Optional.of(stack);
            }
        } catch (Exception exception) {
            Supplementaries.LOGGER.error("Error while dispensing spawn egg from trapped present at {}", source.getPos(), exception);
        }
        return Optional.empty();
    };


    private static final IPresentItemBehavior TNT_BEHAVIOR = (source, stack) -> {

        Level level = source.getLevel();
        BlockPos blockpos = source.getPos().above();
        if (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof TntBlock tnt) {
            Explosion dummyExplosion = new Explosion(level, null,
                    blockpos.getX() + 0.5, blockpos.getX() + 0.5, blockpos.getX() + 0.5, 0, false, Explosion.BlockInteraction.NONE);
            tnt.wasExploded(level, blockpos, dummyExplosion);

            var entities = level.getEntitiesOfClass(PrimedTnt.class, (new AABB(blockpos)).move(0, 0.5, 0));
            for (var e : entities) {
                Vec3 p = e.position();
                e.setPos(new Vec3(p.x, blockpos.getY() + 10 / 16f, p.z));
            }
            level.gameEvent(null, GameEvent.ENTITY_PLACE, blockpos);
            stack.shrink(1);
            return Optional.of(stack);
        }
        return Optional.empty();
    };


    private static final IPresentItemBehavior FIREWORK_BEHAVIOR = new IPresentItemBehavior() {

        @Override
        public Optional<ItemStack> performSpecialAction(BlockSource source, ItemStack stack) {

            Level level = source.getLevel();
            var p = IPresentItemBehavior.getDispensePosition(source);
            FireworkRocketEntity fireworkrocketentity = new FireworkRocketEntity(level, stack,
                    p.x(), p.y(), p.z(), true);

            fireworkrocketentity.shoot(0, 1, 0, 0.5F, 1.0F);
            level.addFreshEntity(fireworkrocketentity);
            stack.shrink(1);
            return Optional.of(stack);
        }

        @Override
        public void playAnimation(BlockSource pSource) {
            IPresentItemBehavior.super.playAnimation(pSource);
            pSource.getLevel().levelEvent(1004, pSource.getPos(), 0);
        }

    };


    @Nullable
    private static Entity spawnMob(EntityType<?> entityType, ServerLevel serverLevel, BlockSource source, @Nullable ItemStack stack) {
        BlockPos pos = source.getPos();
        CompoundTag tag = stack == null ? null : stack.getTag();
        Component component = stack != null && stack.hasCustomHoverName() ? stack.getHoverName() : null;


        Entity entity = entityType.create(serverLevel);
        if (entity != null) {

            if (component != null && entity instanceof LivingEntity) {
                entity.setCustomName(component);
            }

            EntityType.updateCustomEntityTag(serverLevel, null, entity, tag);
            var p = IPresentItemBehavior.getDispensePosition(source);
            entity.setPos(p.x(), p.y(), p.z());
            entity.moveTo(p.x(), p.y(), p.z(), Mth.wrapDegrees(serverLevel.random.nextFloat() * 360.0F), 0.0F);

            entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0.3, 0));

            if (entity instanceof Mob mob) {
                mob.yHeadRot = mob.getYRot();
                mob.yBodyRot = mob.getYRot();
                mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.DISPENSER, null, tag);
                mob.playAmbientSound();
            }
            serverLevel.addFreshEntityWithPassengers(entity);
            //update client velocity
            NetworkHandler.CHANNEL.sendToAllClientPlayersInRange( serverLevel, pos,48,
                    new ClientBoundSendKnockbackPacket(entity.getDeltaMovement(), entity.getId()));
        }
        return entity;
    }

    private static final AbstractProjectileBehavior SPLASH_POTION_BEHAVIOR = new AbstractProjectileBehavior() {

        @Override
        protected Projectile getProjectile(Level worldIn, Position position, ItemStack stackIn) {
            return Util.make(new ThrownPotion(worldIn, position.x(), position.y(), position.z()), (potion) -> {
                potion.setItem(stackIn);
            });
        }

        @Override
        protected float getPower() {
            return 0.5F;
        }

        @Override
        protected float getUncertainty() {
            return 11.0F;
        }
    };

    public abstract static class AbstractProjectileBehavior implements IPresentItemBehavior {

        @Override
        public Optional<ItemStack> performSpecialAction(BlockSource source, ItemStack stack) {
            Level level = source.getLevel();
            Position position = IPresentItemBehavior.getDispensePosition(source);
            Projectile projectile = this.getProjectile(level, position, stack);
            projectile.shoot(0, 1, 0, this.getPower(), this.getUncertainty());
            level.addFreshEntity(projectile);
            stack.shrink(1);
            return Optional.of(stack);
        }

        protected abstract Projectile getProjectile(Level pLevel, Position pPosition, ItemStack pStack);

        protected float getUncertainty() {
            return 6.0F;
        }

        protected float getPower() {
            return 0.4F;
        }

    }

}



