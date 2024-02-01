package net.mehvahdjukaar.supplementaries.client;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.WallLanternBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.GlobeBlockTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class WallLanternTexturesManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final Map<Block, ResourceLocation> SPECIAL_TEXTURES = new IdentityHashMap<>();
    protected static final Set<Block> POSSIBLE_LANTERNS = new HashSet<>();
    private static boolean initialized = false;

    public static final WallLanternTexturesManager RELOAD_INSTANCE = new WallLanternTexturesManager();

    private WallLanternTexturesManager() {
        super(GSON, "textures/blocks/wall_lanterns");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager manager, ProfilerFiller pProfiler) {
        reloadTextures(manager);
    }

    public static void reloadTextures(ResourceManager manager) {
        if (!initialized) {
            initialize();
            initialized = true;
        }
        SPECIAL_TEXTURES.clear();
        for (Block i : POSSIBLE_LANTERNS) {

            ResourceLocation reg = Utils.getID(i);
            String namespace = (reg.getNamespace().equals("minecraft") || reg.getNamespace().equals("supplementaries")) ? "" : reg.getNamespace() + "/";
            String s = "textures/blocks/wall_lanterns/" + namespace + reg.getPath() + ".json";
            ResourceLocation fullPath = Supplementaries.res(s);
            var resource = manager.getResource(fullPath);
            if (resource.isPresent()) {
                try (var stream = resource.get().open()) {
                    JsonElement bsElement = RPUtils.deserializeJson(stream);

                    String texture = RPUtils.findFirstResourceInJsonRecursive(bsElement);
                    if (!texture.isEmpty()) SPECIAL_TEXTURES.put(i, new ResourceLocation(texture));

                } catch (Exception ignored) {
                }
            }
        }

        //globe stuff
        GlobeBlockTile.GlobeType.recomputeCache();
    }

    private static void initialize() {
        ImmutableSet.Builder<Block> builder = ImmutableSet.builder();
        for (Block i : Registry.BLOCK) {
            if (WallLanternBlock.isValidBlock(i)) builder.add(i);
        }
        POSSIBLE_LANTERNS.clear();
        POSSIBLE_LANTERNS.addAll(builder.build());
    }

    @Nullable
    public static TextureAtlasSprite getTextureForLantern(Block block) {
        var res = SPECIAL_TEXTURES.get(block);
        if (res == null) return null;
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(res);
    }

}
