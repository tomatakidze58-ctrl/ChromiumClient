package dev.chromium.client.mixin;

import dev.chromium.client.ChromiumClient;
import dev.chromium.client.module.PvpModules.LowFire;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {
    @Inject(method = "renderFireOverlay", at = @At("HEAD"), require = 0)
    private static void chromium$fireLow(MatrixStack matrices, VertexConsumerProvider vcp, CallbackInfo ci) {
        LowFire m = ChromiumClient.mod(LowFire.class);
        matrices.push();
        if (m != null && m.enabled) matrices.translate(0, -m.height.n, 0);
    }

    @Inject(method = "renderFireOverlay", at = @At("RETURN"), require = 0)
    private static void chromium$firePop(MatrixStack matrices, VertexConsumerProvider vcp, CallbackInfo ci) {
        matrices.pop();
    }
}
