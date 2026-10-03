package dev.chromium.client.mixin;

import dev.chromium.client.ChromiumClient;
import dev.chromium.client.module.VisualModules.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "renderVignetteOverlay", at = @At("HEAD"), cancellable = true, require = 0)
    private void chromium$noVignette(CallbackInfo ci) {
        if (ChromiumClient.on(NoVignette.class)) ci.cancel();
    }

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true, require = 0)
    private void chromium$noPortal(CallbackInfo ci) {
        if (ChromiumClient.on(NoPortalOverlay.class)) ci.cancel();
    }

    @Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true, require = 0)
    private void chromium$noPumpkin(DrawContext ctx, Identifier texture, float opacity, CallbackInfo ci) {
        if (texture != null && texture.getPath().contains("pumpkin") && ChromiumClient.on(NoPumpkinOverlay.class)) ci.cancel();
    }
}
