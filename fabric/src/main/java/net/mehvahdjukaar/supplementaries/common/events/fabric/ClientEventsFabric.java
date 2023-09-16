package net.mehvahdjukaar.supplementaries.common.events.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.mehvahdjukaar.supplementaries.common.events.ClientEvents;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public class ClientEventsFabric {

    public static void init() {
        ItemTooltipCallback.EVENT.register(ClientEvents::onItemTooltip);
        ScreenEvents.AFTER_INIT.register((m, s, x, y) -> {
            if (CompatHandler.CLOTH_CONFIG) {
                List<? extends GuiEventListener> listeners = s.children();
                ClientEvents.addConfigButton(s, listeners, e -> {
                    List<GuiEventListener> c = (List<GuiEventListener>) s.children();
                    c.add(e);
                });
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::onClientTick);
    }
}
