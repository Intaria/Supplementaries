package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.moonlight.api.entity.ImprovedFallingBlockEntity;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FallingLanternEntity extends ImprovedFallingBlockEntity {

    public FallingLanternEntity(EntityType<FallingLanternEntity> type, Level level) {
        super(type, level);
    }

    public FallingLanternEntity(Level level) {
        super(ModEntities.FALLING_LANTERN.get(), level);
    }

    public FallingLanternEntity(Level level, BlockPos pos, BlockState blockState, double yOffset) {
        super(ModEntities.FALLING_LANTERN.get(), level, pos, blockState, false);
        this.yo = pos.getY() + yOffset;
    }

    public static FallingBlockEntity fall(Level level, BlockPos pos, BlockState state, double yOffset) {
        FallingLanternEntity entity = new FallingLanternEntity(level, pos, state, yOffset);
        level.setBlock(pos, state.getFluidState().createLegacyBlock(), 3);
        level.addFreshEntity(entity);
        return entity;
    }

    @Override
    public boolean causeFallDamage(float height, float amount, DamageSource source) {
        boolean r = super.causeFallDamage(height, amount, source);
        if (CommonConfigs.Tweaks.FALLING_LANTERNS.get().hasFire() && this.getDeltaMovement().lengthSqr() > 0.4 * 0.4) {
            BlockState state = this.getBlockState();

            BlockPos pos = new BlockPos(this.getX(), this.getY() + 0.25, this.getZ());
            //break event
            level.levelEvent(null, 2001, pos, Block.getId(state));
            if (state.getLightEmission() != 0) {

                //GunpowderBlock.createMiniExplosion(level, pos, true);
            } else {
                this.spawnAtLocation(state.getBlock());
            }
            this.setCancelDrop(true);
            this.discard();
        }
        return r;
    }


}
