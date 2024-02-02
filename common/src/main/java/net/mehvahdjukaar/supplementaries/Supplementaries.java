package net.mehvahdjukaar.supplementaries;

import net.mehvahdjukaar.moonlight.api.events.IFireConsumeBlockEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.client.WallLanternTexturesManager;
import net.mehvahdjukaar.supplementaries.common.block.blocks.NoticeBoardBlock;
import net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetBehaviorsManager;
import net.mehvahdjukaar.supplementaries.common.block.hourglass.HourglassTimesManager;
import net.mehvahdjukaar.supplementaries.common.block.tiles.NoticeBoardBlockTile;
import net.mehvahdjukaar.supplementaries.common.events.ServerEvents;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.CapturedMobHandler;
import net.mehvahdjukaar.supplementaries.common.misc.songs.SongsManager;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.common.utils.Credits;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.dynamicpack.ClientDynamicResourcesGenerator;
import net.mehvahdjukaar.supplementaries.dynamicpack.ServerDynamicResourcesGenerator;
import net.mehvahdjukaar.supplementaries.common.items.crafting.SpecialRecipeDisplays;
import net.mehvahdjukaar.supplementaries.reg.*;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.item.crafting.Recipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class Supplementaries {

    public static final String MOD_ID = "supplementaries";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);


    public static ResourceLocation res(String n) {
        return new ResourceLocation(MOD_ID, n);
    }

    public static String str(String n) {
        return MOD_ID + ":" + n;
    }


    //called on mod creation
    public static void commonInit() {

        Credits.fetchFromServer();

        CommonConfigs.init();
            ClientConfigs.init();

        NetworkHandler.registerMessages();

        RegHelper.registerSimpleRecipeCondition(res("flag"), CommonConfigs::isEnabled);

        MoonlightEventsHelper.addListener(ServerEvents::onFireConsume, IFireConsumeBlockEvent.class);

        ModSounds.init();
        ModRegistry.init();
        ModRecipes.init();
        ModMenuTypes.init();
        ModEntities.init();
        ModParticles.init();
        ModCommands.init();
        ModWorldgenRegistry.init();

        ServerDynamicResourcesGenerator.INSTANCE.register();

        PlatformHelper.addServerReloadListener(SongsManager.RELOAD_INSTANCE, res("flute_songs"));
        PlatformHelper.addServerReloadListener(HourglassTimesManager.RELOAD_INSTANCE, res("hourglass_data"));
        PlatformHelper.addServerReloadListener(FaucetBehaviorsManager.RELOAD_INSTANCE, res("faucet_interactions"));
        PlatformHelper.addServerReloadListener(CapturedMobHandler.RELOAD_INSTANCE, res("catchable_mobs_properties"));

        if (PlatformHelper.getEnv().isClient()) {

            ClientDynamicResourcesGenerator.INSTANCE.register();

            ClientPlatformHelper.addClientReloadListener(WallLanternTexturesManager.RELOAD_INSTANCE, res("wall_lanterns"));
            try {
                ClientPlatformHelper.registerOptionalTexturePack(res("darker_ropes"));
            } catch (Exception e) {
                Supplementaries.LOGGER.error(e);
            }
        }
    }


    //yes this is where I write crap. deal with it XD

    //rope slide animation and sound instance
    //jeed multi
    //finish bedbugs
    //blaze head ghast
    //spring launcher sand auto launch

    //bellows push hanging signs and globe
    //create sprout support
    //quark gui sack open
    //heartstone highlight and pulse when nearby

    //enchantable horse armor
    //cow remodel
    //sheep animations and textyres
    //3d particle mod
    //quiver not rendering in curio
    //pulley flint
    //mod to wax anything to prevent interaction
    //sugar block fall in water
    //soap in water makes soap particles
    //yeet java models in favor or json ones
    //chains pull down candle holders and lanterns


    //punching swings lanterns
    //wind physics for wind vane

    //ash makes mobs jump
    //squishy piston launcher. also rework them and fix on servers
    //TODO: readd nethers delight stuff & maybe IW planter
    //camera mod with screenshots

    //clicking on cage with lead will put the leashed animal inside
    //wrench rotation overlay
    //create moving dynamic blocks like rope knot
    //jei villagers addon
    //corona mod
    //trollium interaction mod
    //animated lantern textures
    //ash jei plugin
    //bubble sound for bellows
    //bundle sound for sacks

    //todo: fix projectile hitbox being a single point on y = 0
    //divining rod
    //add chain knot

    //enderman hold block in rain
    //horizontal shearable ropes

    //TODO: more flywheel stuff

    //TODO: improve feather particle

    //use feather particle on spriggans

    //TODO: fix JER loot tables percentages

    //GLOBE inv model
    //TODO: goblet & jars dynamic baked model
    //ghast fireball mob griefing


    //firefly glow block

    //TODO: bugs: bell ropes(add to flywheel instance), brewing stand colors(?)

    //TODO: mod ideas: particle block, blackboard banners and flags, lantern holding

    //TODO: add stick window loggable clipping

    //flute animation fix

    //add shift middle click to swap to correct tool

    //mod idea: blackboard banners and flags with villager

    //throwable slimeballs

    //simple mode for doors and trapdoors

    //animated pulley texture

    //TODO: faucets create sprout

    // randomium item particle when drop

    //TODO: xp bottling whose cost depends on player total xp

    //randomium can give onl stuff already obtained by a player in survival

    //golden carrots to breed baby pignis

    //directional books fixed
    //particles for randomium

    //TODO: credist screen

    //TODO: way signs as villages pieces

    //small honey slime in cage

    //idea: Increase range of enchantment table

    //IRON gate connected model

    //hud mod. armor broken hud, items offhadn crafting

    //ash auto bonemeal, improve bubbles

    //better badlands kindling gunpowder compat (whenevr it updates lol)
    //better fodder pathfinding
    //TODO fix randomium recipe jei extensin

    //blackboard otline gui+

    //soap signs & finish notice board dye (add dye interface)
    //snow real magic compat
    //bugs: spring launcher broken on servers


}
