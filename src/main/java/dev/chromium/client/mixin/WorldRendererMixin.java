package dev.chromium.client.mixin;

import dev.chromium.client.ChromiumClient;
import dev.chromium.client.module.VisualModules.CustomBlockOutline;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    // The only int argument of drawBlockOutline is the ARGB colour.
    @ModifyVariable(method = "drawBlockOutline", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
    private int chromium$outline(int color) {
        CustomBlockOutline m = ChromiumClient.mod(CustomBlockOutline.class);
        return m != null && m.enabled ? m.argb() : color;
    }
}
