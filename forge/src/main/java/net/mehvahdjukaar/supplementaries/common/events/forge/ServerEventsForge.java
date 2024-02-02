package net.mehvahdjukaar.supplementaries.common.events.forge;

import com.simibubi.create.infrastructure.worldgen.BuiltinRegistration;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.renderers.CapturedMobCache;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RakedGravelBlock;
import net.mehvahdjukaar.supplementaries.common.capabilities.CapabilityHandler;
import net.mehvahdjukaar.supplementaries.common.entities.PearlMarker;
import net.mehvahdjukaar.supplementaries.common.events.ServerEvents;
import net.mehvahdjukaar.supplementaries.common.items.CandyItem;
import net.mehvahdjukaar.supplementaries.common.misc.songs.SongsManager;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSendLoginPacket;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.forge.VibeCheck;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.event.*;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.level.NoteBlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class ServerEventsForge {

    public static void init() {
        MinecraftForge.EVENT_BUS.register(ServerEventsForge.class);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onUseBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.isCanceled()) {
            var ret = ServerEvents.onRightClickBlock(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
            if (ret != InteractionResult.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(ret);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onUseBlockHP(PlayerInteractEvent.RightClickBlock event) {
        if (!event.isCanceled()) {
            var ret = ServerEvents.onRightClickBlockHP(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
            if (ret != InteractionResult.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(ret);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onUseItem(PlayerInteractEvent.RightClickItem event) {
        if (!event.isCanceled()) {
            var ret = ServerEvents.onUseItem(event.getEntity(), event.getLevel(), event.getHand());
            if (ret.getResult() != InteractionResult.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(ret.getResult());
            }
        }
    }

    @SubscribeEvent
    public static void onAttachTileCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        CapabilityHandler.attachBlockEntityCapabilities(event);
    }

    //TODO: soap tool event
    @SubscribeEvent
    public static void toolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.getToolAction() == ToolActions.HOE_TILL && CommonConfigs.Tweaks.RAKED_GRAVEL.get()) {
            LevelAccessor world = event.getLevel();
            BlockPos pos = event.getPos();
            if (event.getFinalState().is(net.minecraft.world.level.block.Blocks.GRAVEL)) {
                BlockState raked = ModRegistry.RAKED_GRAVEL.get().defaultBlockState();
                if (raked.canSurvive(world, pos)) {
                    event.setFinalState(RakedGravelBlock.getConnectedState(raked, world, pos, event.getContext().getHorizontalDirection()));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            try {
                NetworkHandler.CHANNEL.sendToClientPlayer(player,
                        new ClientBoundSendLoginPacket(UsernameCache.getMap()));
            } catch (Exception exception) {
                Supplementaries.LOGGER.warn("failed to send login message: " + exception);
            }
            ServerEvents.onPlayerLoggedIn(player);
        }
    }

    @SubscribeEvent
    public static void onDataSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            ServerEvents.onDataSyncToPlayer(event.getPlayer(), true);
        } else {
            for (var p : event.getPlayerList().getPlayers()) {
                ServerEvents.onDataSyncToPlayer(p, true);
            }
        }
    }


    @SubscribeEvent
    public static void onTagUpdate(TagsUpdatedEvent event) {
        ServerEvents.onCommonTagUpdate(event.getRegistryAccess(), PlatformHelper.getEnv().isClient());
    }

    //for flute and cage. fabric calls directly
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        var res = ServerEvents.onRightClickEntity(event.getEntity(), event.getLevel(),
                event.getHand(), event.getTarget(), null);
        if (res != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(res);
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        var level = event.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            ServerEvents.onEntityLoad(event.getEntity(), serverLevel);
        }
    }

    @SubscribeEvent
    public static void onAddLootTables(LootTableLoadEvent event) {
        ServerEvents.injectLootTables(event.getLootTableManager(), event.getName(), (b) -> event.getTable().addPool(b.build()));
    }

    //TODO: add these on fabric
    //forge only

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onProjectileImpact(final ProjectileImpactEvent event) {
        PearlMarker.onProjectileImpact(event.getProjectile(), event.getRayTraceResult());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.START) {
            CandyItem.checkSweetTooth(event.player);
        }
    }

    @SubscribeEvent
    public static void noteBlockEvent(final NoteBlockEvent.Play event) {
        SongsManager.recordNote(event.getLevel(), event.getPos());
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            CapturedMobCache.clear();
        }
    }

    private static boolean checked = false;
    private static int counter = 0;

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (!checked) {
            if (event.level.isClientSide) checked = true;
            if (counter++ > 20 && event.level instanceof ServerLevel l && l.dimensionType().natural()) {
                VibeCheck.checkVibe(event.level);
                checked = true;
            }
        }
    }
}
