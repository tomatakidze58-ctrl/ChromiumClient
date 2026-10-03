package dev.chromium.client.mixin;

import dev.chromium.client.ChromiumClient;
import dev.chromium.client.module.PerfModules.AnimationOptimization;
import net.minecraft.client.texture.SpriteAtlasTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteAtlasTexture.class)
public class SpriteAtlasTextureMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, require = 0)
    private void chromium$throttle(CallbackInfo ci) {
        AnimationOptimization m = ChromiumClient.mod(AnimationOptimization.class);
        if (m != null && m.enabled && m.skip()) ci.cancel();
    }
}
