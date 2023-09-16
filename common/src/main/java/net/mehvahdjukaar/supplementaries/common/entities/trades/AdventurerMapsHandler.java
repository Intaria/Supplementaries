package net.mehvahdjukaar.supplementaries.common.entities.trades;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.map.MapHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ModMapMarkers;
import net.mehvahdjukaar.supplementaries.common.worldgen.StructureLocator;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

public class AdventurerMapsHandler extends SimpleJsonResourceReloadListener {

    //cursed
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
            .registerTypeAdapter(AdventurerMapTrade.class, (JsonDeserializer<AdventurerMapTrade>)
                    (json, typeOfT, context) -> AdventurerMapTrade.CODEC.parse(JsonOps.INSTANCE, json)
                            .getOrThrow(false, e ->
                                    Supplementaries.LOGGER.error("failed to parse structure map trade: {}", e))
            ).create();

    public static final PreparableReloadListener RELOAD_INSTANCE = new AdventurerMapsHandler();

    public AdventurerMapsHandler() {
        super(GSON, "structure_maps");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {

        CUSTOM_MAPS_TRADES.clear();

        jsons.forEach((key, json) -> {
            //CUSTOM_MAPS_TRADES.add(GSON.fromJson(json, AdventurerMapTrade.class));
            var v = AdventurerMapTrade.CODEC.parse(JsonOps.INSTANCE, json);
            var data = v.getOrThrow(false, e -> Supplementaries.LOGGER.error("failed to parse structure map trade: {}", e));
            CUSTOM_MAPS_TRADES.add(data);
        });
        if (CUSTOM_MAPS_TRADES.size() != 0)
            Supplementaries.LOGGER.info("Loaded  " + CUSTOM_MAPS_TRADES.size() + " structure maps trades");
    }


    public static final int SEARCH_RADIUS = 150;
    private static final List<AdventurerMapTrade> CUSTOM_MAPS_TRADES = new ArrayList<>();

    private static final Map<TagKey<Structure>,
            Pair<ResourceLocation, Integer>> DEFAULT_STRUCTURE_MARKERS = new HashMap<>();


    private static void associateStructureMarker(TagKey<Structure> tag, ResourceLocation res, int color) {
        DEFAULT_STRUCTURE_MARKERS.put(tag, Pair.of(res, color));
    }

    static {
        associateStructureMarker(StructureTags.SHIPWRECK, ModMapMarkers.SHIPWRECK_TYPE, 0x34200f);
        associateStructureMarker(ModTags.ANCIENT_CITY, ModMapMarkers.ANCIENT_CITY_TYPE, 0x063970);
        associateStructureMarker(ModTags.IGLOO, ModMapMarkers.IGLOO_TYPE, 0x99bdc2);
        associateStructureMarker(StructureTags.RUINED_PORTAL, ModMapMarkers.RUINED_PORTAL_TYPE, 0x5f30b5);
        associateStructureMarker(StructureTags.VILLAGE, ModMapMarkers.VILLAGE_TYPE, 0xba8755);
        associateStructureMarker(StructureTags.OCEAN_RUIN, ModMapMarkers.OCEAN_RUIN_TYPE, 0x3a694d);
        associateStructureMarker(ModTags.PILLAGER_OUTPOST, ModMapMarkers.PILLAGER_OUTPOST_TYPE, 0x1f1100);
        associateStructureMarker(ModTags.DESERT_PYRAMID, ModMapMarkers.DESERT_PYRAMID_TYPE, 0x806d3f);
        associateStructureMarker(ModTags.JUNGLE_TEMPLE, ModMapMarkers.JUNGLE_TEMPLE_TYPE, 0x526638);
        associateStructureMarker(ModTags.BASTION_REMNANT, ModMapMarkers.BASTION_TYPE, 0x2c292f);
        associateStructureMarker(ModTags.END_CITY, ModMapMarkers.END_CITY_TYPE, 0x9c73ab);
        associateStructureMarker(ModTags.SWAMP_HUT, ModMapMarkers.SWAMP_HUT_TYPE, 0x1b411f);
        associateStructureMarker(ModTags.NETHER_FORTRESS, ModMapMarkers.NETHER_FORTRESS, 0x3c080b);
        associateStructureMarker(StructureTags.MINESHAFT, ModMapMarkers.MINESHAFT_TYPE, 0x808080);
    }

    private static Pair<ResourceLocation, Integer> getStructureMarker(Holder<Structure> structure) {
        ResourceLocation res = structure.unwrapKey().get().location();
        int color = 0;
        for (var v : DEFAULT_STRUCTURE_MARKERS.entrySet()) {
            if (structure.is(v.getKey())) {
                res = v.getValue().getFirst();
                color = v.getValue().getSecond();
                break;
            }
        }
        return Pair.of(res, color);
    }

    public static void addTradesCallback() {

        RegHelper.registerVillagerTrades(VillagerProfession.CARTOGRAPHER, 1, itemListings ->
                maybeAddCustomMap(itemListings, 1)
        );

        RegHelper.registerVillagerTrades(VillagerProfession.CARTOGRAPHER, 2, itemListings -> {
            if (CommonConfigs.Tweaks.RANDOM_ADVENTURER_MAPS.get()) {
                itemListings.add(new RandomAdventureMapTrade());
            }
            maybeAddCustomMap(itemListings, 2);
        });

        RegHelper.registerVillagerTrades(VillagerProfession.CARTOGRAPHER, 3, itemListings -> {
            maybeAddCustomMap(itemListings, 3);
        });

        RegHelper.registerVillagerTrades(VillagerProfession.CARTOGRAPHER, 4, itemListings -> {
            maybeAddCustomMap(itemListings, 4);
        });

        RegHelper.registerVillagerTrades(VillagerProfession.CARTOGRAPHER, 5, itemListings -> {
            maybeAddCustomMap(itemListings, 5);
        });
    }

    private static void maybeAddCustomMap(List<VillagerTrades.ItemListing> listings, int level) {
        for (var data : CUSTOM_MAPS_TRADES) {
            if (level == data.villagerLevel()) {
                listings.add(data);
            }
        }
    }

    private static class RandomAdventureMapTrade implements VillagerTrades.ItemListing {

        @Override
        public MerchantOffer getOffer(@NotNull Entity entity, @NotNull RandomSource random) {
            int maxPrice = 11;
            int minPrice = 6;
            int price = random.nextInt(maxPrice - minPrice + 1) + minPrice;

            ItemStack itemstack = createMap(entity.level, entity.blockPosition());
            if (itemstack.isEmpty()) return null;

            int uses = CommonConfigs.Tweaks.QUILL_MAX_TRADES.get();
            int x = 6;
            int xp = (int) ((x * 12) / (float) uses);
            int cost = (int) (price * CommonConfigs.Tweaks.QUILL_TRADE_PRICE_MULT.get());

            return new MerchantOffer(new ItemStack(Items.EMERALD, cost), new ItemStack(Items.COMPASS), itemstack,
                    uses, xp, 0.2F);
        }

        private ItemStack createMap(Level level, BlockPos pos) {
            if (level instanceof ServerLevel serverLevel) {
                return createMapOrQuill(pos, serverLevel, null,
                        2, null, "filled_map.adventure", 0x78151a);
            }
            return ItemStack.EMPTY;
        }
    }

    private static ItemStack createMapOrQuill(BlockPos pos, ServerLevel serverLevel,@Nullable TagKey<Structure> tag,
                                              int zoom, @Nullable ResourceLocation mapMarker,
                                              @Nullable String name, int color) {

        if (!serverLevel.getServer().getWorldData().worldGenSettings().generateStructures())
            return ItemStack.EMPTY;

        var found = StructureLocator.findNearestRandomMapFeature(
                serverLevel, tag, pos, SEARCH_RADIUS, true);

        if (found != null) {
            BlockPos toPos = found.getFirst();
            return createStructureMap(serverLevel, toPos, found.getSecond(), zoom, mapMarker, name, color);
        }
        return ItemStack.EMPTY;
    }

    @NotNull
    public static ItemStack createStructureMap(ServerLevel level, BlockPos pos, Holder<Structure> structure, int zoom,
                                               @Nullable ResourceLocation decoration, @Nullable String name,
                                               int color) {
        ItemStack stack = MapItem.create(level, pos.getX(), pos.getZ(), (byte) zoom, true, true);
        MapItem.renderBiomePreviewMap(level, stack);

        //adds custom decoration
        if (decoration == null) {
            var s = getStructureMarker(structure);
            decoration = s.getFirst();
            if (color == 0) {
                color = s.getSecond();
            }
        }
        MapHelper.addDecorationToMap(stack, pos, decoration, color);

        if (name != null) {
            stack.setHoverName(Component.translatable(name));
        }
        return stack;
    }


    public static ItemStack createCustomMap(Level world, BlockPos pos, ResourceLocation structureName,
                                            @Nullable String mapName, int mapColor, @Nullable ResourceLocation mapMarker) {

        if (world instanceof ServerLevel serverLevel) {
            var destination = TagKey.create(Registry.STRUCTURE_REGISTRY, structureName);
            String name = mapName == null ?
                    "filled_map." + structureName.getPath().toLowerCase(Locale.ROOT) : mapName;

            return createMapOrQuill(pos, serverLevel, destination,
                    2, mapMarker, name, mapColor);
        }
        return ItemStack.EMPTY;
    }


}
