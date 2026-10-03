package dev.chromium.client.mixin;

import dev.chromium.client.ChromiumClient;
import dev.chromium.client.module.VisualModules.Zoom;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true, require = 0)
    private void chromium$zoom(CallbackInfoReturnable<Float> cir) {
        if (Zoom.factor > 1.001f && ChromiumClient.on(Zoom.class)) cir.setReturnValue(cir.getReturnValueF() / Zoom.factor);
    }
}
