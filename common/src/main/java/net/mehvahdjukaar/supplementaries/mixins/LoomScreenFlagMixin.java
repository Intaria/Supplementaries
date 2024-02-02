package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.FlagBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.items.FlagItem;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LoomScreen.class)
public abstract class LoomScreenFlagMixin extends AbstractContainerScreen<LoomMenu> {

    @Shadow
    private List<Pair<Holder<BannerPattern>, DyeColor>> resultBannerPatterns;
    @Shadow
    private boolean hasMaxPatterns;
    @Shadow
    private ItemStack bannerStack;

    protected LoomScreenFlagMixin(LoomMenu loomMenu, Inventory inventory, Component component) {
        super(loomMenu, inventory, component);
    }

    @WrapOperation(method = "containerChanged",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;",
                    ordinal = 0))
    public Item containerChanged(ItemStack instance, Operation<Item> original) {
        Item i = instance.getItem();
        if (i instanceof FlagItem fi) {
            //hax
            return BannerBlock.byColor(fi.getColor()).asItem();
        }
        return original.call(instance);
    }


    @Inject(method = "renderBg", at = @At("TAIL"))
    public void renderBg(PoseStack matrixStack, float ticks, int mouseX, int mouseY, CallbackInfo ci){
        if (this.resultBannerPatterns != null && !this.hasMaxPatterns && this.bannerStack.getItem() instanceof FlagItem) {
            int i = this.leftPos;
            int j = this.topPos;
            MultiBufferSource.BufferSource renderTypeBuffer = this.minecraft.renderBuffers().bufferSource();
            matrixStack.pushPose();

            matrixStack.translate(i + 139d, j + 52d, 0.0D);
            matrixStack.scale(24.0F, -24.0F, 1.0F);
            matrixStack.translate(0.5D, 0.5D, 0.5D);
            matrixStack.mulPose(RotHlpr.Y90);
            matrixStack.mulPose(RotHlpr.X90);
            matrixStack.scale(1.125F, 1.125F, 1.125F);
            matrixStack.translate(-1, -0.5, -1.1875);
            Lighting.setupForFlatItems();

            FlagBlockTileRenderer.renderPatterns(matrixStack, renderTypeBuffer,this.resultBannerPatterns,15728880);

            Lighting.setupFor3DItems();
            matrixStack.popPose();
            renderTypeBuffer.endBatch();
        }
    }
}
