package net.mehvahdjukaar.supplementaries.dynamicpack;

import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.SimpleTagBuilder;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynServerResourcesProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicDataPack;
import net.mehvahdjukaar.moonlight.api.resources.recipe.IRecipeTemplate;
import net.mehvahdjukaar.moonlight.api.resources.recipe.TemplateRecipeManager;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModConstants;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biomes;
import org.apache.logging.log4j.Logger;

public class ServerDynamicResourcesGenerator extends DynServerResourcesProvider {

    public static final ServerDynamicResourcesGenerator INSTANCE = new ServerDynamicResourcesGenerator();

    public ServerDynamicResourcesGenerator() {
        super(new DynamicDataPack(Supplementaries.res("generated_pack")));
        this.dynamicPack.generateDebugResources = PlatformHelper.isDev() || CommonConfigs.General.DEBUG_RESOURCES.get();
    }

    @Override
    public Logger getLogger() {
        return Supplementaries.LOGGER;
    }

    @Override
    public boolean dependsOnLoadedPacks() {
        return true;
    }

    @Override
    public void regenerateDynamicAssets(ResourceManager manager) {
        /*
        try {
           var r = Utils.hackyGetRegistryAccess();
           // var j = PlacedFeature.DIRECT_CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE,r), ModWorldgenRegistry.PLACED_CAVE_URNS.get());
          //  ServerDynamicResourcesHandler.INSTANCE.dynamicPack.addJson(Supplementaries.res("placed_urns"), j.get().orThrow(), ResType.GENERIC);

            var jj = ConfiguredFeature.DIRECT_CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE,r),
                    ModWorldgenRegistry.WILD_FLAX_PATCH.get());
            ServerDynamicResourcesHandler.INSTANCE.dynamicPack.addJson(Supplementaries.res("flax"), jj.get().orThrow(), ResType.GENERIC);
            int aa = 1;
        }catch (Exception e){

        }*/


        //recipes
        if (CommonConfigs.Building.SIGN_POST_ENABLED.get()) {
            addSignPostRecipes(manager);
        }
        if (CommonConfigs.Building.HANGING_SIGN_ENABLED.get()) {
            addHangingSignRecipes(manager);
        }

        //cave urns tag

        {
            SimpleTagBuilder builder = SimpleTagBuilder.of(ModTags.HAS_CAVE_URNS);

            if (CommonConfigs.Functional.URN_PILE_ENABLED.get() && CommonConfigs.Functional.URN_ENABLED.get()) {
                builder.addTag(BiomeTags.IS_OVERWORLD);
            }
            dynamicPack.addTag(builder, Registry.BIOME_REGISTRY);
        }

        //wild flax tag

        {
            SimpleTagBuilder builder = SimpleTagBuilder.of(ModTags.HAS_WILD_FLAX);

            if (CommonConfigs.Functional.WILD_FLAX_ENABLED.get()) {
                builder.addTag(BiomeTags.IS_OVERWORLD);
            }
            dynamicPack.addTag(builder, Registry.BIOME_REGISTRY);
        }

        //ash

        {
            SimpleTagBuilder builder = SimpleTagBuilder.of(ModTags.HAS_BASALT_ASH);

            if (CommonConfigs.Building.BASALT_ASH_ENABLED.get()) {
                builder.add(Biomes.BASALT_DELTAS.location());
            }
            dynamicPack.addTag(builder, Registry.BIOME_REGISTRY);
        }

    }

    @Override
    public void generateStaticAssetsOnStartup(ResourceManager manager) {

        //hanging signs
        {
            SimpleTagBuilder builder = SimpleTagBuilder.of(Supplementaries.res("hanging_signs"));
            //loot table
            ModRegistry.HANGING_SIGNS.forEach((wood, sign) -> {
                dynamicPack.addSimpleBlockLootTable(sign);
                builder.addEntry(sign);
            });
            //tag
            dynamicPack.addTag(builder, Registry.BLOCK_REGISTRY);
            dynamicPack.addTag(builder, Registry.ITEM_REGISTRY);
        }
        //sing posts
        {
            SimpleTagBuilder builder = SimpleTagBuilder.of(Supplementaries.res("sign_posts"));
            builder.addEntries(ModRegistry.SIGN_POST_ITEMS.values());
            dynamicPack.addTag(builder, Registry.ITEM_REGISTRY);
        }
    }


    private void addHangingSignRecipes(ResourceManager manager) {
        IRecipeTemplate<?> template = RPUtils.readRecipeAsTemplate(manager,
                ResType.RECIPES.getPath(Supplementaries.res("hanging_sign_oak")));

        ModRegistry.HANGING_SIGNS.forEach((w, b) -> {
            if (w != WoodTypeRegistry.OAK_TYPE) {
                Item i = b.asItem();
                //check for disabled ones. Will actually crash if its null since vanilla recipe builder expects a non-null one
                if (i.getItemCategory() != null) {
                    FinishedRecipe newR = template.createSimilar(WoodTypeRegistry.OAK_TYPE, w, w.mainChild().asItem());
                    if (newR == null) return;
                    newR = ForgeHelper.addRecipeConditions(newR, template.getConditions());
                    this.dynamicPack.addRecipe(newR);
                }
            }
        });
    }

    private void addSignPostRecipes(ResourceManager manager) {
        IRecipeTemplate<?> template = RPUtils.readRecipeAsTemplate(manager,
                ResType.RECIPES.getPath(Supplementaries.res("sign_post_oak")));

        WoodType oak = WoodTypeRegistry.OAK_TYPE;

        if (signPostTemplate2 == null) {
            ShapedRecipeBuilder.shaped(ModRegistry.SIGN_POST_ITEMS.get(oak), 3)
                    .pattern("   ")
                    .pattern("222")
                    .pattern(" 1 ")
                    .define('1', Items.STICK)
                    .define('2', oak.planks)
                    .group(ModConstants.SIGN_POST_NAME)
                    .unlockedBy("has_plank", InventoryChangeTrigger.TriggerInstance.hasItems(oak.planks))
                    .save(s -> signPostTemplate2 = TemplateRecipeManager.read(s.serializeRecipe()));
        }

        ModRegistry.SIGN_POST_ITEMS.forEach((w, i) -> {
            if (w != oak && i.getItemCategory() != null) {
                //check for disabled ones. Will actually crash if its null since vanilla recipe builder expects a non-null one
                IRecipeTemplate<?> recipeTemplate = w.getChild("sign") == null ? signPostTemplate2 : template;

                FinishedRecipe newR = recipeTemplate.createSimilar(WoodTypeRegistry.OAK_TYPE, w, w.mainChild().asItem());
                if (newR == null) return;
                newR = ForgeHelper.addRecipeConditions(newR, template.getConditions());
                this.dynamicPack.addRecipe(newR);
            }
        });
    }

    private IRecipeTemplate<?> signPostTemplate2;


}
