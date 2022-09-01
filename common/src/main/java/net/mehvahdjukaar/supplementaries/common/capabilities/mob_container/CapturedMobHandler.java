package net.mehvahdjukaar.supplementaries.common.capabilities.mob_container;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.mehvahdjukaar.supplementaries.common.items.AbstractMobContainerItem;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;

public class CapturedMobHandler extends SimpleJsonResourceReloadListener {

    private static final Map<EntityType<?>, DataDefinedCatchableMob> CUSTOM_MOB_PROPERTIES = new IdentityHashMap<>();
    private static DataDefinedCatchableMob MODDED_FISH_PROPERTIES;

    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final CapturedMobHandler RELOAD_INSTANCE = new CapturedMobHandler();

    private CapturedMobHandler() {
        super(GSON, "catchable_mobs_properties");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {
        CUSTOM_MOB_PROPERTIES.clear();
        var list = new ArrayList<DataDefinedCatchableMob>();
        jsons.forEach((key, json) -> {
            var v = DataDefinedCatchableMob.CODEC.parse(JsonOps.INSTANCE, json);
            var data = v.getOrThrow(false, e -> Supplementaries.LOGGER.error("failed to parse structure map trade: {}", e));
            if (key.getPath().equals("generic_fish")) {
                MODDED_FISH_PROPERTIES = data;
            } else {
                list.add(data);
            }
        });
        for (var c : list) {
            for (var o : c.getOwners()) {
                Registry.ENTITY_TYPE.getOptional(o).ifPresent(e -> CUSTOM_MOB_PROPERTIES.put(e, c));
            }
        }
        StuffToRemove.generateStuff();
    }

    public static ICatchableMob getDataCap(EntityType<?> type, boolean isFish) {
        var c = CUSTOM_MOB_PROPERTIES.get(type);
        if (c == null && isFish) return MODDED_FISH_PROPERTIES;
        return c;
    }

    public static ICatchableMob getCatchableMobCapOrDefault(Entity entity) {
        if (entity instanceof ICatchableMob cap) return cap;
        var forgeCap = getForgeCap(entity);
        if (forgeCap != null) return forgeCap;
        var prop = getDataCap(entity.getType(), BucketHelper.isModdedFish(entity));
        if (prop != null) return prop;
        return ICatchableMob.DEFAULT;
    }

    @ExpectPlatform
    private static ICatchableMob getForgeCap(Entity entity) {
        throw new AssertionError();
    }
}