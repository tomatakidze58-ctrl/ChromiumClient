package dev.chromium.client.mixin;

import dev.chromium.client.ChromiumClient;
import dev.chromium.client.module.VisualModules.CustomSky;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class WorldMixin {
    @Inject(method = "getSkyAngle", at = @At("RETURN"), cancellable = true, require = 0)
    private void chromium$sky(CallbackInfoReturnable<Float> cir) {
        if (!((Object) this instanceof ClientWorld)) return;
        CustomSky m = ChromiumClient.mod(CustomSky.class);
        if (m != null && m.enabled) cir.setReturnValue(m.angle());
    }
}
