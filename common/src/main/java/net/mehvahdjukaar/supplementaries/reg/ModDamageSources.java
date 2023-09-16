package net.mehvahdjukaar.supplementaries.reg;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ModDamageSources extends DamageSource {

    protected ModDamageSources(String string) {
        super(string);
    }

    public static final DamageSource BOTTLING_DAMAGE = new ModDamageSources("supplementaries.xp_extracting");
}
