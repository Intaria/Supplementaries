package net.mehvahdjukaar.supplementaries.integration.forge;


import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.redstone.displayLink.AllDisplayBehaviours;
import com.simibubi.create.content.redstone.displayLink.DisplayBehaviour;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.PercentOrProgressBarDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTarget;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.content.trains.display.FlapDisplaySection;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.infrastructure.ponder.AllPonderTags;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.HourGlassBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.block.ITextHolderProvider;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.HourGlassBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PulleyBlock;
import net.mehvahdjukaar.supplementaries.common.block.hourglass.HourglassTimeData;
import net.mehvahdjukaar.supplementaries.common.block.hourglass.HourglassTimesManager;
import net.mehvahdjukaar.supplementaries.common.block.tiles.*;
import net.mehvahdjukaar.supplementaries.common.items.BlackboardItem;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableObject;

import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class CreateCompatImpl {

    public static void setup() {
        try {
            AllMovementBehaviours.registerBehaviour(ModRegistry.HOURGLASS.get(), new HourglassBehavior());
            AllMovementBehaviours.registerBehaviour(ModRegistry.PULLEY_BLOCK.get(), new PulleyBehavior());

            AllDisplayBehaviours.assignBlockEntity(AllDisplayBehaviours.register(
                    Supplementaries.res("notice_board_display_target"),
                    new NoticeBoardDisplayTarget()), ModRegistry.NOTICE_BOARD_TILE.get());

            DisplayBehaviour textHolderTarget = AllDisplayBehaviours.register(
                    Supplementaries.res("text_holder_display_target"), new TextHolderDisplayTarget());

            AllDisplayBehaviours.assignBlockEntity(textHolderTarget, ModRegistry.SIGN_POST_TILE.get());
            AllDisplayBehaviours.assignBlockEntity(textHolderTarget, ModRegistry.HANGING_SIGN_TILE.get());
            AllDisplayBehaviours.assignBlockEntity(textHolderTarget, ModRegistry.DOORMAT_TILE.get());
            AllDisplayBehaviours.assignBlockEntity(textHolderTarget, ModRegistry.DOORMAT_TILE.get());

            AllDisplayBehaviours.assignBlockEntity(AllDisplayBehaviours.register(
                    Supplementaries.res("speaker_block_display_target"),
                    new SpeakerBlockDisplayTarget()), ModRegistry.SPEAKER_BLOCK_TILE.get());

            AllDisplayBehaviours.assignBlockEntity(AllDisplayBehaviours.register(
                    Supplementaries.res("blackboard_display_target"),
                    new BlackboardDisplayTarget()), ModRegistry.BLACKBOARD_TILE.get());

            //sources
            AllDisplayBehaviours.assignBlockEntity(AllDisplayBehaviours.register(
                    Supplementaries.res("globe_display_source"),
                    new GlobeDisplaySource()), ModRegistry.GLOBE_TILE.get());

            AllDisplayBehaviours.assignBlockEntity(AllDisplayBehaviours.register(
                    Supplementaries.res("notice_board_display_source"),
                    new NoticeBoardDisplaySource()), ModRegistry.NOTICE_BOARD_TILE.get());

            AllDisplayBehaviours.assignBlockEntity(AllDisplayBehaviours.register(
                    Supplementaries.res("clock_source"),
                    new ClockDisplaySource()), ModRegistry.CLOCK_BLOCK_TILE.get());

            DisplayBehaviour itemDisplaySource = AllDisplayBehaviours.register(
                    Supplementaries.res("item_display_source"),
                    new ItemDisplayDisplaySource());

            AllDisplayBehaviours.assignBlock(itemDisplaySource, ModRegistry.PEDESTAL.get());
            AllDisplayBehaviours.assignBlockEntity(itemDisplaySource, ModRegistry.STATUE_TILE.get());
            AllDisplayBehaviours.assignBlockEntity(itemDisplaySource, ModRegistry.HOURGLASS_TILE.get());

            AllDisplayBehaviours.assignBlockEntity(AllDisplayBehaviours.register(
                    Supplementaries.res("fluid_tank_source"),
                    new FluidFillLevelDisplaySource()), ModRegistry.JAR_TILE.get());

        } catch (Exception e) {
            Supplementaries.LOGGER.warn("failed to register supplementaries create behaviors: " + e);
        }
    }

    public static void setupClient() {
        PonderRegistry.TAGS.forTag(AllPonderTags.DISPLAY_TARGETS).add(ModRegistry.NOTICE_BOARD.get());
        PonderRegistry.TAGS.forTag(AllPonderTags.DISPLAY_TARGETS).add(ModRegistry.SIGN_POST_ITEMS.get(WoodTypeRegistry.OAK_TYPE));
        PonderRegistry.TAGS.forTag(AllPonderTags.DISPLAY_TARGETS).add(ModRegistry.HANGING_SIGNS.get(WoodTypeRegistry.OAK_TYPE));
        PonderRegistry.TAGS.forTag(AllPonderTags.DISPLAY_TARGETS).add(ModRegistry.SPEAKER_BLOCK.get());
        PonderRegistry.TAGS.forTag(AllPonderTags.DISPLAY_TARGETS).add(ModRegistry.BLACKBOARD.get());
        PonderRegistry.TAGS.forTag(AllPonderTags.DISPLAY_SOURCES).add(ModRegistry.NOTICE_BOARD.get());
        PonderRegistry.TAGS.forTag(AllPonderTags.DISPLAY_SOURCES).add(ModRegistry.GLOBE_ITEM.get());
        PonderRegistry.TAGS.forTag(AllPonderTags.DISPLAY_SOURCES).add(ModRegistry.PEDESTAL.get());
        PonderRegistry.TAGS.forTag(AllPonderTags.DISPLAY_SOURCES).add(ModRegistry.JAR.get());
        //PonderRegistry.TAGS.forTag(PonderTag.DISPLAY_SOURCES).add(ModRegistry.CLOCK_BLOCK.get());
        //PonderRegistry.TAGS.forTag(PonderTag.DISPLAY_TARGETS).add(ModRegistry.DOORMAT.get());

    }

    private static void changeState(MovementContext context, BlockState newState) {
        Map<BlockPos, StructureTemplate.StructureBlockInfo> blocks = context.contraption.getBlocks();
        if (blocks.containsKey(context.localPos)) {
            context.state = newState;
            StructureTemplate.StructureBlockInfo info = blocks.get(context.localPos);
            StructureTemplate.StructureBlockInfo newInfo = new StructureTemplate.StructureBlockInfo(info.pos, newState, info.nbt);
            blocks.replace(context.localPos, newInfo);
        }
    }

    private static Rotation isClockWise(UnaryOperator<Vec3> rot, Direction dir) {
        Vec3 v = MthUtils.V3itoV3(dir.getNormal());
        Vec3 v2 = rot.apply(v);
        var dot = v2.dot(new Vec3(0, 1, 0)); //??
        if (dot > 0) return Rotation.CLOCKWISE_90;
        else if (dot < 0) return Rotation.COUNTERCLOCKWISE_90;
        return Rotation.NONE;
    }

    private static class HourglassBehavior implements MovementBehaviour {

        private HourglassTimeData sandData;
        private float progress;
        private float prevProgress;

        private TextureAtlasSprite cachedTexture;

        @Override
        public void tick(MovementContext context) {
            UnaryOperator<Vec3> rot = context.rotation;
            BlockState state = context.state;
            Direction dir = state.getValue(HourGlassBlock.FACING);
            Rotation rotation = isClockWise(rot, dir);

            CompoundTag com = context.blockEntityData;
            this.sandData = HourglassTimeData.EMPTY;
            var prevSandData = this.sandData;
            NonNullList<ItemStack> l = NonNullList.create();
            ContainerHelper.loadAllItems(com, l);
            if (l.isEmpty()) return;
            this.sandData = HourglassTimesManager.getData(l.get(0).getItem());
            if (prevSandData != sandData && context.world.isClientSide) {
                this.cachedTexture = sandData.computeSprite(l.get(0), context.world);
            }
            this.progress = com.getFloat("Progress");
            this.prevProgress = com.getFloat("PrevProgress");


            if (!sandData.isEmpty()) {
                prevProgress = progress;

                if (rotation == Rotation.CLOCKWISE_90 && progress != 1) {
                    progress = Math.min(progress + sandData.getIncrement(), 1f);
                } else if (rotation == Rotation.COUNTERCLOCKWISE_90 && progress != 0) {
                    progress = Math.max(progress - sandData.getIncrement(), 0f);
                }

            }

            com.remove("Progress");
            com.remove("PrevProgress");
            com.putFloat("Progress", progress);
            com.putFloat("PrevProgress", prevProgress);
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld, ContraptionMatrices matrices, MultiBufferSource buffer) {
            float partialTicks = 1;
            if (sandData.isEmpty()) return;

            Vec3 v = context.position;
            if (v == null) {
                v = new Vec3(0, 0, 0);
            }
            BlockPos pos = new BlockPos(v);

            int light = LevelRenderer.getLightColor(context.world, pos);

            float h = Mth.lerp(partialTicks, prevProgress, progress);
            Direction dir = context.state.getValue(HourGlassBlock.FACING);
            HourGlassBlockTileRenderer.renderSand(matrices.getModelViewProjection(), buffer, light, 0, cachedTexture, h, dir);
        }

    }

    //TODO: fix
    private static class PulleyBehavior implements MovementBehaviour {

        private static final PulleyBlockTile DUMMY = new PulleyBlockTile(BlockPos.ZERO, ModRegistry.PULLEY_BLOCK.get().defaultBlockState());

        @Override
        public void visitNewPosition(MovementContext context, BlockPos pos) {
            BlockState state = context.state;
            var axis = state.getValue(PulleyBlock.AXIS);
            if (axis == Direction.Axis.Y) return;
            changeState(context, state.cycle(PulleyBlock.FLIPPED));
            Direction dir = null;
            if (axis == Direction.Axis.X) {
                dir = Direction.NORTH;
            } else if (axis == Direction.Axis.Z) {
                dir = Direction.WEST;
            }
            if (dir == null) return;

            DUMMY.load(context.blockEntityData);
            DUMMY.setLevel(context.world);

            Rotation rot = context.relativeMotion.length() > 0 ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90;
            DUMMY.handleRotation(rot, pos);
            context.blockEntityData = DUMMY.saveWithFullMetadata();

            DUMMY.setLevel(null);
        }

    }

    private static class NoticeBoardDisplayTarget extends DisplayTarget {

        @Override
        public void acceptText(int line, List<MutableComponent> text, DisplayLinkContext context) {
            BlockEntity te = context.getTargetBlockEntity();
            if (te instanceof NoticeBoardBlockTile lectern) {
                ItemStack book = lectern.getDisplayedItem();
                if (!book.isEmpty()) {
                    if (book.is(Items.WRITABLE_BOOK)) {
                        lectern.setDisplayedItem(book = this.signBook(book));
                    }

                    if (book.is(Items.WRITTEN_BOOK)) {
                        ListTag tag = book.getTag().getList("pages", 8);
                        boolean changed = false;

                        for (int i = 0; i - line < text.size() && i < 50; ++i) {
                            if (tag.size() <= i) {
                                tag.add(StringTag.valueOf(i < line ? "" : Component.Serializer.toJson(text.get(i - line))));
                            } else if (i >= line) {
                                if (i - line == 0) {
                                    reserve(i, lectern, context);
                                }

                                if (i - line > 0 && this.isReserved(i - line, lectern, context)) {
                                    break;
                                }

                                tag.set(i, StringTag.valueOf(Component.Serializer.toJson(text.get(i - line))));
                            }

                            changed = true;
                        }

                        book.getTag().put("pages", tag);
                        lectern.setDisplayedItem(book);
                        if (changed) {
                            context.level().sendBlockUpdated(context.getTargetPos(), lectern.getBlockState(), lectern.getBlockState(), 2);
                        }
                    }
                }
            }
        }

        @Override
        public DisplayTargetStats provideStats(DisplayLinkContext context) {
            return new DisplayTargetStats(50, 256, this);
        }

        @Override
        public Component getLineOptionText(int line) {
            return Lang.translateDirect("display_target.page", line + 1);
        }

        private ItemStack signBook(ItemStack book) {
            ItemStack written = new ItemStack(Items.WRITTEN_BOOK);
            CompoundTag compoundtag = book.getTag();
            if (compoundtag != null) {
                written.setTag(compoundtag.copy());
            }

            written.addTagElement("author", StringTag.valueOf("Data Gatherer"));
            written.addTagElement("filtered_title", StringTag.valueOf("Printed Book"));
            written.addTagElement("title", StringTag.valueOf("Printed Book"));
            return written;
        }
    }

    private static class TextHolderDisplayTarget extends DisplayTarget {

        @Override
        public void acceptText(int line, List<MutableComponent> text, DisplayLinkContext context) {
            BlockEntity te = context.getTargetBlockEntity();
            if (te instanceof ITextHolderProvider th) {
                var textHolder = th.getTextHolder();
                boolean changed = false;

                if (th instanceof HangingSignBlockTile hs && (hs.isEmpty() || hs.hasFakeItem())) {
                    var source = context.getSourceBlockEntity();
                    ItemStack copyStack = getDisplayedItem(context, source, i -> !i.isEmpty());
                    hs.setItem(copyStack);
                    hs.setFakeItem(true);
                    changed = true;
                } else {
                    for (int i = 0; i < text.size() && i + line < textHolder.size(); ++i) {
                        if (i == 0) {
                            reserve(i + line, te, context);
                        }
                        if (i > 0 && this.isReserved(i + line, te, context)) {
                            break;
                        }
                        textHolder.setLine(i + line, text.get(i));
                        changed = true;
                    }
                }
                if (changed) {
                    context.level().sendBlockUpdated(context.getTargetPos(), te.getBlockState(), te.getBlockState(), 2);
                }
            }
        }

        @Override
        public DisplayTargetStats provideStats(DisplayLinkContext context) {
            var textHolder = ((ITextHolderProvider) context.getTargetBlockEntity()).getTextHolder();
            return new DisplayTargetStats(textHolder.size(), textHolder.getMaxLineCharacters(), this);
        }
    }

    private static class SpeakerBlockDisplayTarget extends DisplayTarget {

        @Override
        public void acceptText(int line, List<MutableComponent> text, DisplayLinkContext context) {
            BlockEntity te = context.getTargetBlockEntity();
            if (te instanceof SpeakerBlockTile tile && text.size() > 0) {
                reserve(line, te, context);
                tile.setMessage(text.get(0).getString());
                context.level().sendBlockUpdated(context.getTargetPos(), te.getBlockState(), te.getBlockState(), 2);
            }
        }

        @Override
        public DisplayTargetStats provideStats(DisplayLinkContext context) {
            return new DisplayTargetStats(1, 32, this);
        }
    }

    private static ItemStack getDisplayedItem(DisplayLinkContext context, BlockEntity source,
                                              Predicate<ItemStack> predicate) {
        if (source instanceof ItemDisplayTile display) {
            var stack = display.getDisplayedItem();
            if (predicate.test(stack)) return stack;
        } else {
            for (int i = 0; i < 32; ++i) {
                var pos = context.getSourcePos();
                TransportedItemStackHandlerBehaviour behaviour = BlockEntityBehaviour.get(
                        context.level(), pos, TransportedItemStackHandlerBehaviour.TYPE
                );
                if (behaviour == null) {
                    break;
                }
                MutableObject<ItemStack> stackHolder = new MutableObject<>();
                behaviour.handleCenteredProcessingOnAllItems(0.25F, tis -> {
                    stackHolder.setValue(tis.stack);
                    return TransportedItemStackHandlerBehaviour.TransportedResult.doNothing();
                });
                ItemStack stack = stackHolder.getValue();
                if (stack != null && predicate.test(stack)) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static class BlackboardDisplayTarget extends DisplayTarget {

        @Override
        public void acceptText(int line, List<MutableComponent> text, DisplayLinkContext context) {
            BlockEntity te = context.getTargetBlockEntity();
            if (te instanceof BlackboardBlockTile tile && text.size() > 0 && !tile.isWaxed()) {
                var source = context.getSourceBlockEntity();
                if (!parseText(text.get(0).getString(), tile)) {
                    ItemStack copyStack = getDisplayedItem(context, source, i -> i.getItem() instanceof BlackboardItem);
                    if (!copyStack.isEmpty() && copyBlackboard(line, context, te, tile, copyStack)) return;
                    var pixels = BlackboardBlockTile.unpackPixelsFromStringWhiteOnly(text.get(0).getString());
                    tile.setPixels(BlackboardBlockTile.unpackPixels(pixels));
                }
                context.level().sendBlockUpdated(context.getTargetPos(), te.getBlockState(), te.getBlockState(), 2);
                reserve(line, te, context);
            }
        }


        private static final Pattern PATTERN = Pattern.compile("\\((\\d\\d?),(\\d\\d?)\\)->(\\S+)");

        private boolean parseText(String string, BlackboardBlockTile tile) {
            var m = PATTERN.matcher(string);
            if (m.matches()) {
                int x = Integer.parseInt(m.group(1));
                int y = Integer.parseInt(m.group(2));
                DyeColor dye = DyeColor.byName(m.group(3), null);
                if (x >= 0 && x <= 15 && y >= 0 && y <= 15 && dye != null) {
                    if (dye != DyeColor.WHITE && dye != DyeColor.BLACK && !CommonConfigs.Building.BLACKBOARD_COLOR.get())
                        return false;
                    tile.setPixel(x, y, BlackboardBlock.colorToByte(dye));
                    return true;
                }
            }
            return false;
        }

        private static boolean copyBlackboard(int line, DisplayLinkContext context, BlockEntity te, BlackboardBlockTile tile, ItemStack stack) {
            CompoundTag cmp = stack.getTagElement("BlockEntityTag");
            if (cmp != null && cmp.contains("Pixels")) {
                tile.setPixels(BlackboardBlockTile.unpackPixels(cmp.getLongArray("Pixels")));
                context.level().sendBlockUpdated(context.getTargetPos(), te.getBlockState(), te.getBlockState(), 2);
                reserve(line, te, context);
                return true;
            }
            return false;
        }

        @Override
        public DisplayTargetStats provideStats(DisplayLinkContext context) {
            return new DisplayTargetStats(1, 32, this);
        }
    }

    private static class ClockDisplaySource extends SingleLineDisplaySource {
        public static final MutableComponent EMPTY_TIME = Components.literal("--:--");

        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            Level level = context.level();
            if (level instanceof ServerLevel sLevel) {
                if (context.getSourceBlockEntity() instanceof ClockBlockTile tile) {
                    boolean c12 = context.sourceConfig().getInt("Cycle") == 0;
                    boolean isNatural = sLevel.dimensionType().natural();
                    int dayTime = (int) (sLevel.getDayTime() % 24000L);
                    int hours = (dayTime / 1000 + 6) % 24;
                    int minutes = dayTime % 1000 * 60 / 1000;
                    MutableComponent suffix = Lang.translateDirect("generic.daytime." + (hours > 11 ? "pm" : "am"), new Object[0]);
                    minutes = minutes / 5 * 5;
                    if (c12) {
                        hours %= 12;
                        if (hours == 0) {
                            hours = 12;
                        }
                    }
                    if (!isNatural) {
                        hours = Create.RANDOM.nextInt(70) + 24;
                        minutes = Create.RANDOM.nextInt(40) + 60;
                    }
                    MutableComponent component = Components.literal(
                            (hours < 10 ? " " : "") + hours + ":" + (minutes < 10 ? "0" : "") + minutes + (c12 ? " " : "")
                    );
                    return c12 ? component.append(suffix) : component;
                }
            }
            return EMPTY_TIME;
        }

        @Override
        protected String getFlapDisplayLayoutName(DisplayLinkContext context) {
            return "Instant";
        }

        @Override
        protected FlapDisplaySection createSectionForValue(DisplayLinkContext context, int size) {
            return new FlapDisplaySection((float) size * 7.0F, "instant", false, false);
        }

        @Override
        protected String getTranslationKey() {
            return "time_of_day";
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
            super.initConfigurationWidgets(context, builder, isFirstLine);
            if (!isFirstLine) {
                builder.addSelectionScrollInput(
                        0,
                        60,
                        (si, l) -> si.forOptions(Lang.translatedOptions("display_source.time", "12_hour", "24_hour"))
                                .titled(Lang.translateDirect("display_source.time.format")),
                        "Cycle"
                );
            }
        }

        @Override
        protected boolean allowsLabeling(DisplayLinkContext context) {
            return true;
        }
    }


    private static class GlobeDisplaySource extends SingleLineDisplaySource {
        public static final MutableComponent EMPTY = Components.literal("--,--");

        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            if (context.getSourceBlockEntity() instanceof GlobeBlockTile tile) {
                BlockPos pos = context.getSourcePos();
                return Component.literal("X: " + pos.getX() + ", Z: " + pos.getZ());
            } else {
                return EMPTY;
            }
        }

        @Override
        protected boolean allowsLabeling(DisplayLinkContext context) {
            return true;
        }

        @Override
        protected String getFlapDisplayLayoutName(DisplayLinkContext context) {
            return "Instant";
        }

        @Override
        protected FlapDisplaySection createSectionForValue(DisplayLinkContext context, int size) {
            return new FlapDisplaySection((float) size * 7.0F, "instant", false, false);
        }

        @Override
        protected String getTranslationKey() {
            return "world_position";
        }
    }


    private static class NoticeBoardDisplaySource extends SingleLineDisplaySource {

        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            if (context.getSourceBlockEntity() instanceof NoticeBoardBlockTile tile) {
                tile.updateText();
                return Component.literal(tile.getText());
            } else {
                return Component.empty();
            }
        }

        @Override
        protected boolean allowsLabeling(DisplayLinkContext context) {
            return false;
        }

        @Override
        protected String getFlapDisplayLayoutName(DisplayLinkContext context) {
            return "Instant";
        }

        @Override
        protected FlapDisplaySection createSectionForValue(DisplayLinkContext context, int size) {
            return new FlapDisplaySection((float) size * 7.0F, "instant", false, false);
        }

        @Override
        protected String getTranslationKey() {
            return "notice_board";
        }
    }

    private static class ItemDisplayDisplaySource extends SingleLineDisplaySource {

        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            MutableComponent combined = EMPTY_LINE.copy();

            if (context.getSourceBlockEntity() instanceof ItemDisplayTile te && !te.isEmpty()) {
                combined = combined.append(te.getDisplayedItem().getHoverName());
            }
            //else if(context.level().getBlockState(context.getSourcePos()) instanceof WorldlyContainerHolder wc){
            //    combined = combined.append(wc.getContainer())
            //}
            return combined;
        }

        @Override
        public int getPassiveRefreshTicks() {
            return 20;
        }

        @Override
        protected String getTranslationKey() {
            return "item_name";
        }

        @Override
        protected boolean allowsLabeling(DisplayLinkContext context) {
            return true;
        }

        @Override
        protected String getFlapDisplayLayoutName(DisplayLinkContext context) {
            return "Number";
        }
    }

    private static class FluidFillLevelDisplaySource extends PercentOrProgressBarDisplaySource {

        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            if (context.sourceConfig().getInt("Mode") == 2) {
                if (context.getSourceBlockEntity() instanceof ISoftFluidTankProvider tp) {
                    return Components.literal(tp.getSoftFluidTank().getCount() + " mBtl");
                }
            }
            return super.provideLine(context, stats);
        }

        @Override
        protected Float getProgress(DisplayLinkContext context) {
            if (context.getSourceBlockEntity() instanceof ISoftFluidTankProvider tp) {
                return tp.getSoftFluidTank().getHeight(1);
            }
            return null;
        }

        @Override
        protected boolean progressBarActive(DisplayLinkContext context) {
            return context.sourceConfig().getInt("Mode") == 1;
        }

        @Override
        protected String getTranslationKey() {
            return "fluid_amount";
        }

        @OnlyIn(Dist.CLIENT)
        public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
            super.initConfigurationWidgets(context, builder, isFirstLine);
            if (!isFirstLine) {
                builder.addSelectionScrollInput(
                        0,
                        120,
                        (si, l) -> si.forOptions(Lang.translatedOptions("display_source.fill_level", "percent", "progress_bar", "fluid_amount"))
                                .titled(Lang.translateDirect("display_source.fill_level.display")),
                        "Mode"
                );
            }
        }

        @Override
        protected boolean allowsLabeling(DisplayLinkContext context) {
            return true;
        }
    }

}
