package net.mehvahdjukaar.supplementaries.configs;

import com.google.common.base.Suppliers;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.LightableLanternBlock;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.reg.ModConstants;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CommonConfigs {

    private static final Map<String, Supplier<Boolean>> FEATURE_TOGGLES = new HashMap<>();

    public static final ConfigSpec SPEC;

    private static final WeakReference<ConfigBuilder> builderReference;

    static {
        ConfigBuilder builder = ConfigBuilder.create(Supplementaries.res("common"), ConfigType.COMMON);

        builderReference = new WeakReference<>(builder);

        Redstone.init();
        Functional.init();
        Building.init();
        Tools.init();
        General.init();
        Tweaks.init();

        builder.setSynced();
        builder.onChange(CommonConfigs::onRefresh);

        SPEC = builder.buildAndRegister();
        SPEC.loadFromFile();
    }


    private static Supplier<Holder.Reference<Block>> ropeOverride = () -> null;
    private static boolean stasisEnabled = true;

    private static void onRefresh() {
        //this isn't safe. refresh could happen sooner than item registration for fabric
        ropeOverride = Suppliers.memoize(() -> {
            var o = Registry.BLOCK.getHolder(ResourceKey.create(Registry.BLOCK.key(), Functional.ROPE_OVERRIDE.get()));
            if (o.isPresent() && o.get() instanceof Holder.Reference<Block> hr && hr.value() != ModRegistry.ROPE.get()) {
                return hr;
            }
            return null;
        });

        stasisEnabled = Tools.STASIS_ENABLED.get() && (Tools.BUBBLE_BLOWER_ENABLED.get());
    }

    @Nullable
    public static Block getSelectedRope() {
        var override = getRopeOverride();
        if (override != null) return override.value();
        else if (Functional.ROPE_ENABLED.get()) return ModRegistry.ROPE.get();
        return null;
    }

    @Nullable
    public static Holder.Reference<Block> getRopeOverride() {
        return ropeOverride.get();
    }

    public static boolean stasisEnabled() {
        return stasisEnabled;
    }

    public enum Hands {
        MAIN_HAND, OFF_HAND, BOTH, NONE
    }


    public static class Redstone {

        public static void init() {
        }

        static {
            ConfigBuilder builder = builderReference.get();

            builder.push("redstone");

            builder.push("speaker_block");
            SPEAKER_BLOCK_ENABLED = feature(builder);
            SPEAKER_NARRATOR = builder.comment("Enable/disable speaker block narrator mode")
                    .define("narrator_enabled", true);
            SPEAKER_RANGE = builder.comment("Maximum block range")
                    .define("range", 64, 0, 100000000);
            builder.pop();

            builder.push("bellows");
            BELLOWS_ENABLED = feature(builder);
            BELLOWS_PERIOD = builder.comment("""
                            bellows pushes air following this equation:\s
                            air=(sin(2PI*ticks/period)<0), with period = base_period-(redstone_power-1)*power_scaling\s
                            represents base period at 1 power""")
                    .define("base_period", 78, 1, 512);
            BELLOWS_POWER_SCALING = builder.comment("how much the period changes in relation to the block redstone power")
                    .define("power_scaling", 3, 0, 128);
            BELLOWS_BASE_VEL_SCALING = builder.comment("""
                            velocity increase uses this equation:\s
                            vel = base_vel*((range-entity_distance)/range) with base_vel = base_velocity_scaling/period\s
                            note that the block will push further the faster it's pulsing""")
                    .define("base_velocity_scaling", 5.0, 0.0, 64);
            BELLOWS_MAX_VEL = builder.comment("entities with velocity greater than this won't be pushed")
                    .define("power_scaling", 2.0, 0.0, 16);
            BELLOWS_FLAG = builder.comment("sets velocity changed flag when pushing entities +\n" +
                            "causes pushing animation to be smooth client side but also restricts player movement when being pushed")
                    .define("velocity_changed_flag", true);
            BELLOWS_RANGE = builder.comment("maximum range")
                    .comment("note that it will still only keep alive the two fire blocks closer to it")
                    .define("range", 5, 0, 16);
            builder.pop();

            builder.push("spring_launcher");
            PISTON_LAUNCHER_ENABLED = feature(builder);
            LAUNCHER_VEL = builder.comment("spring launcher launch speed")
                    .define("velocity", 1.5D, 0, 16);
            LAUNCHER_HEIGHT = builder.comment("fall distance needed to trigger the automatic spring launch")
                    .define("fall_height_required", 5, 0, 512);
            builder.pop();

            builder.push("pulley_block");

            PULLEY_ENABLED = feature(builder);
            MINESHAFT_ELEVATOR = builder.comment("Chance for a new mineshaft elevator piece to spawn")
                    .define("mineshaft_elevator", 0.02, 0, 1);
            builder.pop();


            WIND_VANE_ENABLED = feature(builder, ModConstants.WIND_VANE_NAME);
            CLOCK_ENABLED = feature(builder, ModConstants.CLOCK_BLOCK_NAME);
            ILLUMINATOR_ENABLED = feature(builder, ModConstants.REDSTONE_ILLUMINATOR_NAME);
            CRANK_ENABLED = feature(builder, ModConstants.CRANK_NAME);
            FAUCET_ENABLED = feature(builder, ModConstants.FAUCET_NAME);
            GOLD_DOOR_ENABLED = feature(builder, ModConstants.GOLD_DOOR_NAME);
            GOLD_TRAPDOOR_ENABLED = feature(builder, ModConstants.GOLD_TRAPDOOR_NAME);
            LOCK_BLOCK_ENABLED = feature(builder, ModConstants.LOCK_BLOCK_NAME);
            DISPENSER_MINECART_ENABLED = feature(builder, ModConstants.DISPENSER_MINECART_NAME);
            CRYSTAL_DISPLAY_ENABLED = feature(builder, ModConstants.CRYSTAL_DISPLAY_NAME);

            builder.pop();
        }

        public static final Supplier<Boolean> SPEAKER_BLOCK_ENABLED;
        public static final Supplier<Integer> SPEAKER_RANGE;
        public static final Supplier<Boolean> SPEAKER_NARRATOR;

        public static final Supplier<Boolean> BELLOWS_ENABLED;
        public static final Supplier<Integer> BELLOWS_PERIOD;
        public static final Supplier<Integer> BELLOWS_POWER_SCALING;
        public static final Supplier<Double> BELLOWS_MAX_VEL;
        public static final Supplier<Double> BELLOWS_BASE_VEL_SCALING;
        public static final Supplier<Boolean> BELLOWS_FLAG;
        public static final Supplier<Integer> BELLOWS_RANGE;

        public static final Supplier<Boolean> PISTON_LAUNCHER_ENABLED;
        public static final Supplier<Double> LAUNCHER_VEL;
        public static final Supplier<Integer> LAUNCHER_HEIGHT;

        public static final Supplier<Boolean> WIND_VANE_ENABLED;

        public static final Supplier<Boolean> CLOCK_ENABLED;

        public static final Supplier<Boolean> ILLUMINATOR_ENABLED;

        public static final Supplier<Boolean> CRANK_ENABLED;

        public static final Supplier<Boolean> FAUCET_ENABLED;

 
        public static final Supplier<Boolean> GOLD_TRAPDOOR_ENABLED;

        public static final Supplier<Boolean> GOLD_DOOR_ENABLED;

        public static final Supplier<Boolean> LOCK_BLOCK_ENABLED;

        public static final Supplier<Boolean> DISPENSER_MINECART_ENABLED;

        public static final Supplier<Boolean> CRYSTAL_DISPLAY_ENABLED;

        public static final Supplier<Boolean> PULLEY_ENABLED;
        public static final Supplier<Double> MINESHAFT_ELEVATOR;

    }


    public static class Building {
        public static void init() {
        }

        static {
            ConfigBuilder builder = builderReference.get();

            builder.push("building");

            builder.push("blackboard");
            BLACKBOARD_ENABLED = feature(builder);
            BLACKBOARD_COLOR = builder.comment("Enable to draw directly on a blackboard using any dye. Gui still only works in black and white")
                    .define("colored_blackboard", PlatformHelper.isModLoaded("chalk"));
            BLACKBOARD_MODE = builder.comment("Interaction mode for blackboards")
                    .define("interaction_mode", BlackboardBlock.UseMode.BOTH);
            builder.pop();

            builder.push("timber_frame");
            TIMBER_FRAME_ENABLED = feature(builder);
            SWAP_TIMBER_FRAME = builder.comment("Allow placing a timber frame directly on a block by holding shift")
                    .define("swap_on_shift", false);
            AXE_TIMBER_FRAME_STRIP = builder.comment("Allows axes to remove a framed block leaving the contained block intact")
                    .define("axes_strip", true);
            REPLACE_DAUB = builder.comment("Replace a timber frame with wattle and daub block when daub is placed in it")
                    .define("replace_daub", true);
            builder.pop();

            builder.push("iron_gate");
            IRON_GATE_ENABLED = feature(builder);
            DOUBLE_IRON_GATE = builder.comment("Allows two iron gates to be opened simultaneously when on top of the other")
                    .define("double_opening", true);
            CONSISTENT_GATE = builder.comment("Makes iron (ang gold) gates behave like their door counterpart so for example iron gates will only be openable by redstone")
                    .define("door-like_gates", false);
            builder.pop();

            builder.push("sugar_cube");
            SUGAR_CUBE_ENABLED = feature(builder);
            SUGAR_BLOCK_HORSE_SPEED_DURATION = builder.comment("Duration in seconts of speed effect garanted to horses that eat a sugar cube")
                    .define("horse_speed_duration", 10, 0, 1000);
            builder.pop();

            builder.push("planter");
            PLANTER_ENABLED = feature(builder);
            PLANTER_BREAKS = builder.comment("Makes so saplings that grow in a planter will break it turning into rooted dirt")
                    .define("broken_by_sapling", true);
            builder.pop();

            builder.push("notice_board");
            NOTICE_BOARD_ENABLED = feature(builder);
            NOTICE_BOARDS_UNRESTRICTED = builder.comment("Allows notice boards to accept and display any item, not just maps and books")
                    .define("allow_any_item", false);
            builder.pop();

            builder.push("pedestal");
            PEDESTAL_ENABLED = feature(builder);
            CRYSTAL_ENCHANTING = builder.comment("If enabled end crystals placed on a pedestals will provide an enchantment power bonus equivalent to 3 bookshelves")
                    .define("crystal_enchanting", 3, 0, 100);
            builder.pop();

            builder.push("ash");
            ASH_ENABLED = feature(builder);
            ASH_BURN = builder.comment("Burnable blocks will have a chance to create ash layers when burned")
                    .define("ash_from_fire", true);
            ASH_RAIN = builder.comment("Allows rain to wash away ash layers overtime")
                    .define("rain_wash_ash", true);
            builder.push("basalt_ash");
            BASALT_ASH_ENABLED = builder.define("enabled", true);
            BASALT_ASH_TRIES = builder.comment("Attempts at every patch to spawn 1 block. Increases average patch size")
                    .define("attempts_per_patch", 36, 1, 1000);
            BASALT_ASH_PER_CHUNK = builder.comment("Spawn attempts per chunk. Increases spawn frequency")
                    .define("spawn_attempts", 15, 0, 100);
            builder.pop();
            builder.pop();

            builder.push("flag");
            FLAG_ENABLED = feature(builder);
            FLAG_POLE = builder.comment("Allows right/left clicking on a stick to lower/raise a flag attached to it")
                    .define("stick_pole", true);
            FLAG_POLE_LENGTH = builder.comment("Maximum allowed pole length")
                    .define("pole_length", 16, 0, 256);
            builder.pop();

            builder.push("goblet");
            GOBLET_ENABLED = feature(builder);
            GOBLET_DRINK = builder.comment("Allows drinking from goblets").define("allow_drinking", true);
            builder.pop();

            builder.push("globe");
            GLOBE_ENABLED = feature(builder);
            GLOBE_SEPIA = feature(builder, "sepia_globe");
            GLOBE_TRADES = builder.comment("How many globe trades to give to the wandering trader. This will effectively increase the chance of him having a globe trader. Increase this if you have other mods that add stuff to that trader")
                    .define("chance", 2, 0, 50);
            builder.pop();

            builder.push("sign_post");
            SIGN_POST_ENABLED = feature(builder);

            builder.pop();

            DAUB_ENABLED = feature(builder, ModConstants.DAUB_NAME);
            ASH_BRICKS_ENABLED = feature(builder, ModConstants.ASH_BRICK_NAME + "s");
            LAPIS_BRICKS_ENABLED = feature(builder, ModConstants.LAPIS_BRICKS_NAME);
            DEEPSLATE_LAMP_ENABLED = feature(builder, ModConstants.DEEPSLATE_LAMP_NAME);
            END_STONE_LAMP_ENABLED = feature(builder, ModConstants.END_STONE_LAMP_NAME);
            BLACKSTONE_LAMP_ENABLED = feature(builder, ModConstants.BLACKSTONE_LAMP_NAME);
            STONE_LAMP_ENABLED = feature(builder, ModConstants.STONE_LAMP_NAME);
            TILE_ENABLED = feature(builder, ModConstants.STONE_TILE_NAME);
            BLACKSTONE_TILE_ENABLED = feature(builder, ModConstants.BLACKSTONE_TILE_NAME);
            SCONCE_ENABLED = feature(builder, ModConstants.SCONCE_NAME);
            SCONCE_LEVER_ENABLED = feature(builder, ModConstants.SCONCE_LEVER_NAME);
            PANCAKES_ENABLED = feature(builder, ModConstants.PANCAKE_NAME);
            NETHERITE_DOOR_ENABLED = feature(builder, ModConstants.NETHERITE_DOOR_NAME);
            NETHERITE_TRAPDOOR_ENABLED = feature(builder, ModConstants.NETHERITE_TRAPDOOR_NAME);
            SILVER_DOOR_ENABLED = feature(builder, ModConstants.SILVER_DOOR_NAME);
            SILVER_TRAPDOOR_ENABLED = feature(builder, ModConstants.SILVER_TRAPDOOR_NAME);
            LEAD_DOOR_ENABLED = feature(builder, ModConstants.LEAD_DOOR_NAME);
            LEAD_TRAPDOOR_ENABLED = feature(builder, ModConstants.LEAD_TRAPDOOR_NAME);
            HANGING_SIGN_ENABLED = feature(builder, ModConstants.HANGING_SIGN_NAME);
            CRIMSON_LANTERN_ENABLED = feature(builder, ModConstants.CRIMSON_LANTERN_NAME);
            COPPER_LANTERN_ENABLED = feature(builder, ModConstants.COPPER_LANTERN_NAME);
            RAKED_GRAVEL_ENABLED = feature(builder, ModConstants.RAKED_GRAVEL_NAME);
            FEATHER_BLOCK_ENABLED = feature(builder, ModConstants.FEATHER_BLOCK_NAME);
            STATUE_ENABLED = feature(builder, ModConstants.STATUE_NAME);
            DOORMAT_ENABLED = feature(builder, ModConstants.DOORMAT_NAME);
            FLINT_BLOCK_ENABLED = feature(builder, ModConstants.FLINT_BLOCK_NAME);
            CANDLE_HOLDER_ENABLED = feature(builder, ModConstants.CANDLE_HOLDER_NAME);

            builder.pop();
        }

        public static final Supplier<Boolean> BLACKBOARD_ENABLED;
        public static final Supplier<Boolean> BLACKBOARD_COLOR;
        public static final Supplier<BlackboardBlock.UseMode> BLACKBOARD_MODE;

        public static final Supplier<Boolean> IRON_GATE_ENABLED;
        public static final Supplier<Boolean> DOUBLE_IRON_GATE;
        public static final Supplier<Boolean> CONSISTENT_GATE;

        public static final Supplier<Boolean> ASH_BURN;
        public static final Supplier<Boolean> ASH_RAIN;
        public static final Supplier<Boolean> BASALT_ASH_ENABLED;
        public static final Supplier<Integer> BASALT_ASH_TRIES;
        public static final Supplier<Integer> BASALT_ASH_PER_CHUNK;

        public static final Supplier<Boolean> SUGAR_CUBE_ENABLED;
        public static final Supplier<Integer> SUGAR_BLOCK_HORSE_SPEED_DURATION;

        public static final Supplier<Boolean> NOTICE_BOARD_ENABLED;
        public static final Supplier<Boolean> NOTICE_BOARDS_UNRESTRICTED;

        public static final Supplier<Boolean> FLAG_ENABLED;
        public static final Supplier<Boolean> FLAG_POLE;
        public static final Supplier<Integer> FLAG_POLE_LENGTH;

        public static final Supplier<Boolean> GOBLET_ENABLED;
        public static final Supplier<Boolean> GOBLET_DRINK;

        public static final Supplier<Boolean> PLANTER_ENABLED;
        public static final Supplier<Boolean> PLANTER_BREAKS;

        public static final Supplier<Boolean> SIGN_POST_ENABLED;

        public static final Supplier<Boolean> GLOBE_ENABLED;
        public static final Supplier<Boolean> GLOBE_SEPIA;
        public static final Supplier<Integer> GLOBE_TRADES;

        public static final Supplier<Boolean> PEDESTAL_ENABLED;
        public static final Supplier<Integer> CRYSTAL_ENCHANTING;

        public static final Supplier<Boolean> TIMBER_FRAME_ENABLED;
        public static final Supplier<Boolean> REPLACE_DAUB;
        public static final Supplier<Boolean> SWAP_TIMBER_FRAME;
        public static final Supplier<Boolean> AXE_TIMBER_FRAME_STRIP;

        public static final Supplier<Boolean> DAUB_ENABLED;

        public static final Supplier<Boolean> ASH_ENABLED;

        public static final Supplier<Boolean> ASH_BRICKS_ENABLED;

        public static final Supplier<Boolean> HANGING_SIGN_ENABLED;

        public static final Supplier<Boolean> SCONCE_ENABLED;

        public static final Supplier<Boolean> SCONCE_LEVER_ENABLED;

        public static final Supplier<Boolean> STONE_LAMP_ENABLED;

        public static final Supplier<Boolean> END_STONE_LAMP_ENABLED;

        public static final Supplier<Boolean> BLACKSTONE_LAMP_ENABLED;

        public static final Supplier<Boolean> DEEPSLATE_LAMP_ENABLED;

        public static final Supplier<Boolean> COPPER_LANTERN_ENABLED;

        public static final Supplier<Boolean> NETHERITE_TRAPDOOR_ENABLED;

        public static final Supplier<Boolean> NETHERITE_DOOR_ENABLED;

        public static final Supplier<Boolean> PANCAKES_ENABLED;

        public static final Supplier<Boolean> CRIMSON_LANTERN_ENABLED;

        public static final Supplier<Boolean> TILE_ENABLED;

        public static final Supplier<Boolean> RAKED_GRAVEL_ENABLED;

        public static final Supplier<Boolean> STATUE_ENABLED;

        public static final Supplier<Boolean> FEATHER_BLOCK_ENABLED;

        public static final Supplier<Boolean> FLINT_BLOCK_ENABLED;

        public static final Supplier<Boolean> DOORMAT_ENABLED;

        public static final Supplier<Boolean> BLACKSTONE_TILE_ENABLED;

        public static final Supplier<Boolean> SILVER_TRAPDOOR_ENABLED;

        public static final Supplier<Boolean> SILVER_DOOR_ENABLED;

        public static final Supplier<Boolean> LEAD_TRAPDOOR_ENABLED;

        public static final Supplier<Boolean> LEAD_DOOR_ENABLED;

        public static final Supplier<Boolean> LAPIS_BRICKS_ENABLED;

        public static final Supplier<Boolean> CANDLE_HOLDER_ENABLED;

    }


    public static class Functional {
        public static void init() {
        }

        static {
            ConfigBuilder builder = builderReference.get();

            builder.push("functional");

            builder.push("rope");
            ROPE_ENABLED = feature(builder);
            ROPE_UNRESTRICTED = builder.comment("Allows ropes to be supported & attached to solid block sides")
                    .define("block_side_attachment", true);
            ROPE_SLIDE = builder.comment("Makes sliding down ropes as fast as free falling, still negating fall damage")
                    .define("slide_on_fall", true);
            ROPE_OVERRIDE = builder.comment("In case you want to disable supplementaries ropes you can specify here another mod rope and they will be used for rope arrows and in mineshafts instead")
                    .define("rope_override", Supplementaries.res("rope"));
            builder.pop();

            builder.push("jar");
            JAR_ENABLED = feature(builder);
            JAR_CAPACITY = builder.comment("Jar liquid capacity: leave at 12 for pixel accuracy")
                    .define("capacity", 12, 0, 1024);
            JAR_EAT = builder.comment("Allow right click to instantly eat or drink food or potions inside a placed jar.\n" +
                            "Disable if you think this ability is op (honey for example). Cookies are excluded")
                    .define("drink_from_jar", false);
            JAR_ITEM_DRINK = builder.comment("Allows the player to directly drink from jar items")
                    .define("drink_from_jar_item", false);
            JAR_AUTO_DETECT = builder.comment("Dynamically allows all small mobs inside jars depending on their hitbox size. Tinted jars can accept hostile mbos too")
                    .define("jar_auto_detect", false);
            JAR_CAPTURE = builder.comment("Allow Jars to capture small mobs")
                    .define("jar_capture", true);
            JAR_COOKIES = builder.comment("Allow Jars to hold cookies")
                    .define("jar_cookies", true);
            JAR_LIQUIDS = builder.comment("Allow Jars to hold liquids from bottles, buckets and bowls")
                    .define("jar_liquids", true);
            builder.pop();

            builder.push("cage");
            CAGE_ENABLED = feature(builder);
            CAGE_ALL_MOBS = builder.comment("Allows all entities to be captured by cages and jars. Not meant for survival")
                    .define("allow_all_mobs", false);
            CAGE_ALL_BABIES = builder.comment("Allows all baby mobs to be captured by cages")
                    .define("cage_allow_all_babies", false);
            CAGE_AUTO_DETECT = builder.comment("Dynamically allows all small mobs inside cages depending on their hitbox size")
                    .define("cage_auto_detect", false);
            CAGE_PERSISTENT_MOBS = builder.comment("Makes it so all (hostile) mobs captured by cages and jars will be set to persistent so they won't despawn when released")
                    .define("persistent_mobs", false);
            CAGE_HEALTH_THRESHOLD = builder.comment("Health percentage under which mobs will be allowed to be captured by cages and jars. Leave at 100 to accept any health level")
                    .define("health_threshold", 100, 1, 100);
            builder.pop();

            builder.push("safe");
            SAFE_ENABLED = feature(builder);
            SAFE_UNBREAKABLE = builder.comment("Makes safes only breakable by their owner or by a player in creative")
                    .define("prevent_breaking", false);
            SAFE_SIMPLE = builder.comment("Make safes simpler so they do not require keys:\n" +
                            "they will be bound to the first person that opens one and only that person will be able to interact with them")
                    .define("simple_safes", false);
            builder.pop();

            builder.push("sack");
            SACK_ENABLED = feature(builder);
            SACK_PENALTY = builder.comment("Penalize the player with slowness effect when carrying too many sacks")
                    .define("sack_penalty", true);
            SACK_INCREMENT = builder.comment("Maximum number of sacks after which the overencumbered effect will be applied. Each multiple of this number will increase the effect strength by one")
                    .define("sack_increment", 2, 0, 50);
            SACK_SLOTS = builder.comment("How many slots should a sack have")
                    .define("slots", 9, 1, 27);
            builder.pop();

            builder.push("urn");
            URN_ENABLED = feature(builder);
            URN_ENTITY_SPAWN_CHANCE = builder.comment("Chance for an urn to spawn a critter from the urn_spawn tag")
                    .define("critter_spawn_chance", 0.01f, 0, 1);
            builder.push("cave_urns");
            URN_PILE_ENABLED = builder.worldReload().define("enabled", true);
            URN_PATCH_TRIES = builder.worldReload().comment("Attempts at every patch to spawn 1 block. Increases average patch size")
                    .define("attempts_per_patch", 4, 1, 100);
            URN_PER_CHUNK = builder.worldReload().comment("Spawn attempts per chunk. Increases spawn frequency")
                    .define("spawn_attempts", 7, 0, 100);
            builder.pop();
            builder.pop();

            builder.push("soap");
            SOAP_ENABLED = feature(builder);
            SOAP_DYE_CLEAN_BLACKLIST = builder.comment("Dyed Bock types that cannot be cleaned with soap")
                    .define("clean_blacklist", List.of("minecraft:glazed_terracotta"));
            builder.pop();

            builder.push("flax");
            FLAX_ENABLED = feature(builder);
            builder.push("wild_flax");
            WILD_FLAX_ENABLED = builder.worldReload().define("enabled", true);
            FLAX_AVERAGE_EVERY = builder.worldReload().comment("Spawn wild flax on average every 'x' chunks. Increases spawn frequency")
                    .define("rarity", 6, 1, 100);
            FLAX_PATCH_TRIES = builder.worldReload().comment("Attempts at every patch to spawn 1 block. Increases average patch size")
                    .define("attempts_per_patch", 35, 1, 100);
            builder.pop();
            builder.pop();

            FODDER_ENABLED = feature(builder, ModConstants.FODDER_NAME);
            HOURGLASS_ENABLED = feature(builder, ModConstants.HOURGLASS_NAME);

            builder.push("present");
            PRESENT_ENABLED = feature(builder);
            TRAPPED_PRESENT_ENABLED = feature(builder, ModConstants.TRAPPED_PRESENT_NAME);
            builder.pop();

            builder.pop();
        }


        public static final Supplier<Boolean> SAFE_ENABLED;
        public static final Supplier<Boolean> SAFE_UNBREAKABLE;
        public static final Supplier<Boolean> SAFE_SIMPLE;

        public static final Supplier<Boolean> SACK_ENABLED;
        public static final Supplier<Boolean> SACK_PENALTY;
        public static final Supplier<Integer> SACK_INCREMENT;
        public static final Supplier<Integer> SACK_SLOTS;

        public static final Supplier<Boolean> JAR_ENABLED;
        public static final Supplier<Integer> JAR_CAPACITY;
        public static final Supplier<Boolean> JAR_EAT;
        public static final Supplier<Boolean> JAR_CAPTURE;
        public static final Supplier<Boolean> JAR_COOKIES;
        public static final Supplier<Boolean> JAR_LIQUIDS;
        public static final Supplier<Boolean> JAR_ITEM_DRINK;
        public static final Supplier<Boolean> JAR_AUTO_DETECT;

        public static final Supplier<Boolean> CAGE_ENABLED;
        public static final Supplier<Boolean> CAGE_ALL_MOBS;
        public static final Supplier<Boolean> CAGE_ALL_BABIES;
        public static final Supplier<Boolean> CAGE_AUTO_DETECT;
        public static final Supplier<Boolean> CAGE_PERSISTENT_MOBS;
        public static final Supplier<Integer> CAGE_HEALTH_THRESHOLD;

        public static final Supplier<Boolean> SOAP_ENABLED;
        public static final Supplier<List<String>> SOAP_DYE_CLEAN_BLACKLIST;

        public static final Supplier<Boolean> ROPE_ENABLED;
        public static final Supplier<ResourceLocation> ROPE_OVERRIDE;
        public static final Supplier<Boolean> ROPE_UNRESTRICTED;
        public static final Supplier<Boolean> ROPE_SLIDE;

        public static final Supplier<Boolean> URN_ENABLED;
        public static final Supplier<Double> URN_ENTITY_SPAWN_CHANCE;
        public static final Supplier<Boolean> URN_PILE_ENABLED;
        public static final Supplier<Integer> URN_PATCH_TRIES;
        public static final Supplier<Integer> URN_PER_CHUNK;

        public static final Supplier<Boolean> FLAX_ENABLED;
        public static final Supplier<Boolean> WILD_FLAX_ENABLED;
        public static final Supplier<Integer> FLAX_PATCH_TRIES;
        public static final Supplier<Integer> FLAX_AVERAGE_EVERY;

        public static final Supplier<Boolean> FODDER_ENABLED;

        public static final Supplier<Boolean> PRESENT_ENABLED;
        public static final Supplier<Boolean> TRAPPED_PRESENT_ENABLED;

        public static final Supplier<Boolean> HOURGLASS_ENABLED;

    }


    public static class Tools {
        public static void init() {
        }

        static {
            ConfigBuilder builder = builderReference.get();

            builder.push("tools");

            builder.push("bubble_blower");
            BUBBLE_BLOWER_ENABLED = feature(builder);
            BUBBLE_BLOWER_COST = builder.comment("Amount of soap consumed per bubble block placed")
                    .define("stasis_cost", 5, 1, 25);
            builder.push("bubble_block");
            BUBBLE_LIFETIME = builder.comment("Max lifetime of bubble blocks. Set to 10000 to have it infinite")
                    .define("lifetime", 20 * 60, 1, 10000);
            BUBBLE_BREAK = builder.comment("Can bubble break when touched on?")
                    .define("break_when_touched", true);
            BUBBLE_FEATHER_FALLING = builder.comment("If true feather falling prevents breaking bubbles when stepping on them")
                    .define("feather_falling_prevents_breaking", true);
            builder.pop();

            builder.pop();

            //flute
            builder.push("flute");
            FLUTE_ENABLED = feature(builder);
            FLUTE_RADIUS = builder.comment("Radius in which an unbound flute will search pets")
                    .define("unbound_radius", 64, 0, 500);
            FLUTE_DISTANCE = builder.comment("Max distance at which a bound flute will allow a pet to teleport")
                    .define("bound_distance", 64, 0, 500);

            builder.pop();

            ANTIQUE_INK_ENABLED = feature(builder, ModConstants.ANTIQUE_INK_NAME);
            CANDY_ENABLED = feature(builder, ModConstants.CANDY_NAME);
            STASIS_ENABLED = feature(builder, ModConstants.STASIS_NAME);

            builder.pop();
        }


        public static final Supplier<Boolean> BUBBLE_BLOWER_ENABLED;
        public static final Supplier<Integer> BUBBLE_BLOWER_COST;
        public static final Supplier<Integer> BUBBLE_LIFETIME;
        public static final Supplier<Boolean> BUBBLE_BREAK;
        public static final Supplier<Boolean> BUBBLE_FEATHER_FALLING;

        public static final Supplier<Boolean> FLUTE_ENABLED;
        public static final Supplier<Integer> FLUTE_RADIUS;
        public static final Supplier<Integer> FLUTE_DISTANCE;

        public static final Supplier<Boolean> ANTIQUE_INK_ENABLED;

        public static final Supplier<Boolean> CANDY_ENABLED;

        public static final Supplier<Boolean> STASIS_ENABLED;

    }


    public static class Tweaks {

        public static void init() {
        }


        static {
            ConfigBuilder builder = builderReference.get();

            builder.comment("Vanilla tweaks").push("tweaks");

            builder.push("shulker_helmet");
            SHULKER_HELMET_ENABLED = feature(builder.comment("Allows wearing shulker shells"));
            builder.pop();

            builder.push("traders_open_doors");
            WANDERING_TRADER_DOORS = builder.comment("Allows traders to open doors (because they couldnt aparently)")
                    .define("enabled", true);
            builder.pop();


            builder.push("dispenser_tweaks");
            AXE_DISPENSER_BEHAVIORS = builder.comment("Allows dispensers to use axes on blocks to strip logs and scrape off copper oxidation and wax")
                    .define("axe_strip", true);
            ENDER_PEAR_DISPENSERS = builder.comment("Enables shooting ender pearls with dispensers")
                    .define("shoot_ender_pearls", true);
            builder.pop();

            builder.push("dye_blocks");
            DYE_BLOCKS = builder.comment("Allows using dyes on blocks just like soap")
                    .define("enabled", false);
            builder.pop();

            //double cake
            builder.push("cake_tweaks");
            DOUBLE_CAKE_PLACEMENT = builder.comment("Allows you to place a cake on top of another")
                    .define("double_cake", true);
            DIRECTIONAL_CAKE = builder.comment("Allows eating a cake from every side")
                    .define("directional_cake", true);
            builder.pop();

            //skulls stuff
            builder.push("mob_head_tweaks");
            SKULL_PILES = builder.comment("Allows you to place two mob heads on top of each other")
                    .define("skull_piles", true);
            SKULL_CANDLES = builder.comment("Allows candles to be placed on top of skulls")
                    .define("skull_candles", true);
            SKULL_CANDLES_MULTIPLE = builder.comment("Allows placing more than one candle ontop of each skull")
                    .define("multiple_candles", true);
            builder.pop();

            //hanging pot
            builder.push("hanging_flower_pots");
            HANGING_POT_PLACEMENT = builder.comment("allows you to place hanging flower pots. Works with any modded pot too")
                    .define("enabled", true);
            builder.pop();

            //throwable bricks
            builder.push("throwable_bricks");
            THROWABLE_BRICKS_ENABLED = builder.comment("Throw bricks at your foes! Might break glass blocks")
                    .define("enabled", true);
            builder.pop();

            //wall lantern
            builder.push("lantern_tweaks");
            WALL_LANTERN_PLACEMENT = builder.comment("Allow wall lanterns placement")
                    .define("enabled", true);

            WALL_LANTERN_HIGH_PRIORITY = builder.comment("Gives high priority to wall lantern placement. Enable to override other wall lanterns placements, disable if it causes issues with other mods that use lower priority block click events")
                    .define("high_priority", true);

            List<String> modBlacklist = Arrays.asList("extlights", "betterendforge", "tconstruct", "enigmaticlegacy");
            WALL_LANTERN_BLACKLIST = builder.comment("Mod ids of mods that have lantern block that extend the base lantern class but don't look like one")
                    .define("mod_blacklist", modBlacklist);
            FALLING_LANTERNS = builder.comment("Allows ceiling lanterns to fall if their support is broken." +
                            "Additionally if they fall from high enough they will break creating a fire where they land")
                    .define("fallin_lanterns", LightableLanternBlock.FallMode.ON);
            builder.pop();
            //bells
            builder.push("bells_tweaks");
            BELL_CHAIN = builder.comment("Ring a bell by clicking on a chain that's connected to it")
                    .define("chain_ringing", true);
            BELL_CHAIN_LENGTH = builder.comment("Max chain length that allows a bell to ring")
                    .define("chain_length", 16, 0, 256);
            builder.pop();

            builder.push("placeable_sticks");
            PLACEABLE_STICKS = builder.comment("Allow placeable sticks")
                    .define("sticks", true);
            PLACEABLE_RODS = builder.comment("Allow placeable blaze rods")
                    .define("blaze_rods", true);
            builder.pop();

            builder.push("raked_gravel");
            RAKED_GRAVEL = builder.comment("allow gravel to be raked with a hoe")
                    .define("enabled", true);
            builder.pop();

            builder.push("bottle_xp");
            BOTTLE_XP = builder.comment("Allow bottling up xp by using a bottle on an enchanting table")
                    .define("enabled", false);
            BOTTLING_COST = builder.comment("bottling health cost")
                    .define("cost", 2, 0, 20);
            builder.pop();

            builder.push("map_tweaks");
            RANDOM_ADVENTURER_MAPS = builder.comment("Cartographers will sell 'adventurer maps' that will lead to a random vanilla structure (choosen from a thought out preset list).\n" +
                            "Best kept disabled if you are adding custom adventurer maps with datapack (check the wiki for more)")
                    .define("random_adventurer_maps", true);
            MAP_MARKERS = builder.comment("Enables beacons, lodestones, respawn anchors, beds, conduits, portals to be displayed on maps by clicking one of them with a map")
                    .define("block_map_markers", true);
            if (PlatformHelper.getPlatform().isForge()) {
                QUARK_QUILL = builder.comment("If Quark is installed adventurer maps will be replaced by adventurer quills. These will not lag the server when generating")
                        .define("quill_adventurer_maps", true);
                REPLACE_VANILLA_MAPS = builder.comment("If Quark is installed replaces buried treasure and mansion maps with their equivalent quill form. This removes the lag spike they create when generating")
                        .define("quill_vanilla_maps", true);
                QUILL_TRADE_PRICE_MULT = builder.comment("These maps will roll a difference structure every time. Increase their price to balance them")
                        .define("map_trade_price_multiplier", 2d, 1, 10);
                QUILL_MAX_TRADES = builder.comment("These maps will roll a difference structure every time. Decrease their max trades to balance them")
                        .define("map_trade_max_trades", 2, 1, 12);
                QUILL_MIN_SEARCH_RADIUS = builder.comment("Increases any search done with a quoll to be at least this radius. Vanilla locate is 100 while buried treasure is 50 chunks for reference")
                        .define("minimum_search_radius", 100, 0, 500);
            } else {
                QUARK_QUILL = () -> false;
                REPLACE_VANILLA_MAPS = () -> false;
                QUILL_MAX_TRADES = () -> 1;
                QUILL_TRADE_PRICE_MULT = () -> 1d;
                QUILL_MIN_SEARCH_RADIUS = ()->50;
            }
            builder.pop();

            builder.push("ceiling_banners");
            CEILING_BANNERS = builder.comment("Allow banners to be placed on ceilings")
                    .define("enabled", true);
            builder.pop();

            builder.push("placeable_books");
            WRITTEN_BOOKS = builder.comment("Allows written books to be placed down. Requires shift clicking")
                    .define("enabled", true);
            PLACEABLE_BOOKS = builder.comment("Allow books and enchanted books to be placed on the ground")
                    .define("enabled", true);
            BOOK_POWER = builder.comment("Enchantment power bonus given by normal book piles with 4 books. Piles with less books will have their respective fraction of this total. For reference a vanilla bookshelf provides 1")
                    .define("book_power", 1d, 0, 5);
            ENCHANTED_BOOK_POWER = builder.comment("Enchantment power bonus given by normal book piles with 4 books. Piles with less books will have their respective fraction of this total. For reference a vanilla bookshelf provides 1")
                    .define("enchanted_book_power", 1.334d, 0, 5);
            MIXED_BOOKS = builder.comment("Allow all books to be placed both vertically and horizontally")
                    .define("mixed_books", false);
            builder.pop();

            builder.push("zombie_horse");
            ZOMBIE_HORSE = builder.comment("Feed a stack of rotten flesh to a skeleton horse to buff him up to a zombie horse")
                    .define("zombie_horse_conversion", true);
            ZOMBIE_HORSE_COST = builder.comment("Amount of rotten flesh needed")
                    .define("rotten_flesh", 64, 1, 1000);
            ZOMBIE_HORSE_UNDERWATER = builder.comment("Allows zombie horses to be ridden underwater")
                    .define("rideable_underwater", true);
            ZOMBIE_HORSE_CONVERSION = builder.comment("Convert a zombie horse back by feeding it a golden carrot")
                    .define("zombie_horse_inverse_conversion", true);
            builder.pop();


            builder.pop();
        }

        public static final Supplier<Boolean> SHULKER_HELMET_ENABLED;
        public static final Supplier<Boolean> DYE_BLOCKS;

        public static final Supplier<Boolean> ENDER_PEAR_DISPENSERS;
        public static final Supplier<Boolean> AXE_DISPENSER_BEHAVIORS;
        public static final Supplier<Boolean> DIRECTIONAL_CAKE;
        public static final Supplier<Boolean> DOUBLE_CAKE_PLACEMENT;
        public static final Supplier<Boolean> HANGING_POT_PLACEMENT;
        public static final Supplier<Boolean> WALL_LANTERN_PLACEMENT;
        public static final Supplier<Boolean> WALL_LANTERN_HIGH_PRIORITY;
        public static final Supplier<Boolean> THROWABLE_BRICKS_ENABLED;
        public static final Supplier<List<String>> WALL_LANTERN_BLACKLIST;
        public static final Supplier<LightableLanternBlock.FallMode> FALLING_LANTERNS;
        public static final Supplier<Boolean> BELL_CHAIN;
        public static final Supplier<Integer> BELL_CHAIN_LENGTH;
        public static final Supplier<Boolean> PLACEABLE_STICKS;
        public static final Supplier<Boolean> PLACEABLE_RODS;
        public static final Supplier<Boolean> RAKED_GRAVEL;
        public static final Supplier<Boolean> BOTTLE_XP;
        public static final Supplier<Integer> BOTTLING_COST;
        public static final Supplier<Boolean> RANDOM_ADVENTURER_MAPS;
        public static final Supplier<Boolean> MAP_MARKERS;
        public static final Supplier<Boolean> QUARK_QUILL;
        public static final Supplier<Double> QUILL_TRADE_PRICE_MULT;
        public static final Supplier<Integer> QUILL_MAX_TRADES;
        public static final Supplier<Integer> QUILL_MIN_SEARCH_RADIUS ;
        public static final Supplier<Boolean> REPLACE_VANILLA_MAPS;
        public static final Supplier<Boolean> CEILING_BANNERS;
        public static final Supplier<Boolean> PLACEABLE_BOOKS;
        public static final Supplier<Boolean> WRITTEN_BOOKS;
        public static final Supplier<Double> BOOK_POWER;
        public static final Supplier<Double> ENCHANTED_BOOK_POWER;
        public static final Supplier<Boolean> ZOMBIE_HORSE_CONVERSION;
        public static final Supplier<Boolean> ZOMBIE_HORSE;
        public static final Supplier<Integer> ZOMBIE_HORSE_COST;
        public static final Supplier<Boolean> ZOMBIE_HORSE_UNDERWATER;
        public static final Supplier<Boolean> MIXED_BOOKS;
        public static final Supplier<Boolean> SKULL_PILES;
        public static final Supplier<Boolean> SKULL_CANDLES;
        public static final Supplier<Boolean> SKULL_CANDLES_MULTIPLE;
        public static final Supplier<Boolean> WANDERING_TRADER_DOORS;

    }


    public static class General {
        public static void init() {
        }

        static {
            ConfigBuilder builder = builderReference.get();

            builder.comment("General settings")
                    .push("general");
            CREATIVE_TAB = builder.comment("Enable Creative Tab").define("creative_tab", false);

            DISPENSERS = builder.comment("Set to false to disable custom dispenser behaviors (i.e: filling jars) if for some reason they are causing trouble").define("dispensers", true);

            JAR_TAB = builder.gameRestart().comment("Creates a creative tab full of filled jars")
                    .define("jar_tab", false);

            DEBUG_RESOURCES = builder.comment("Save generated resources to disk in a 'debug' folder in your game directory. Mainly for debug purposes but can be used to generate assets in all wood types for your mods :0")
                    .define("debug_save_dynamic_pack", false);
            SERVER_PROTECTION = builder.comment("Turn this on to disable any interaction on blocks placed by other players. This affects item shelves, signs, flower pots, and boards. " +
                            "Useful for protected servers. Note that it will affect only blocks placed after this is turned on and such blocks will keep being protected after this option is disabled")
                    .define("server_protection", false);
            builder.pop();
        }

        public static final Supplier<Boolean> JAR_TAB;
        public static final Supplier<Boolean> CREATIVE_TAB;
        public static final Supplier<Boolean> DISPENSERS;
        public static final Supplier<Boolean> DEBUG_RESOURCES;
        public static final Supplier<Boolean> SERVER_PROTECTION;
    }

    private static Supplier<Boolean> feature(ConfigBuilder builder) {
        return feature(builder, "enabled", builder.currentCategory(), true);
    }

    private static Supplier<Boolean> feature(ConfigBuilder builder, String name) {
        return feature(builder, name, name, true);
    }

    private static Supplier<Boolean> feature(ConfigBuilder builder, String name, String key, boolean value) {
        var config = builder.gameRestart().define(name, value);
        FEATURE_TOGGLES.put(key, config);
        return config;
    }

    //TODO: cleanup
    public static boolean isEnabled(String key) {
        if (!SPEC.isLoaded()) throw new AssertionError("Config isn't loaded. How?");
        if (key.contains("daub")) return Building.DAUB_ENABLED.get();
        return switch (key) {
            case "adventurer_map" -> Tweaks.RANDOM_ADVENTURER_MAPS.get();
            case ModConstants.TRAPPED_PRESENT_NAME ->
                    Functional.PRESENT_ENABLED.get() && Functional.TRAPPED_PRESENT_ENABLED.get();
            case ModConstants.FLAX_BLOCK_NAME, ModConstants.FLAX_WILD_NAME -> Functional.FLAX_ENABLED.get();
            case ModConstants.SOAP_BLOCK_NAME -> Functional.SOAP_ENABLED.get();
            case "planter_rich", "planter_rich_soul" -> Building.PLANTER_ENABLED.get();
            case "vertical_slabs" -> CompatHandler.isVerticalSlabEnabled();
            case ModConstants.GLOBE_SEPIA_NAME -> Building.GLOBE_SEPIA.get() && Tools.ANTIQUE_INK_ENABLED.get();
            case ModConstants.KEY_NAME ->
                    Building.NETHERITE_DOOR_ENABLED.get() || Building.NETHERITE_TRAPDOOR_ENABLED.get() || Functional.SAFE_ENABLED.get();
            default -> FEATURE_TOGGLES.getOrDefault(key, () -> true).get();
        };
    }


    public static void init() {
        int disabled = 0;
        for (var c : FEATURE_TOGGLES.values()) {
            if (!c.get()) disabled++;
        }
        float percentage = disabled / (float) FEATURE_TOGGLES.size();
        if (percentage > 0.66f) {
            Supplementaries.LOGGER.error("You have disabled more than {}% of Supplementaries content. Consider uninstalling the mod", String.format("%.0f", percentage * 100));
        }
    }
}
