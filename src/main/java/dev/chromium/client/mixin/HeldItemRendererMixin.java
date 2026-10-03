package dev.chromium.client.mixin;

import dev.chromium.client.ChromiumClient;
import dev.chromium.client.module.PvpModules.LowShield;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    // float args: tickDelta(0), pitch(1), swingProgress(2), equipProgress(3)
    @ModifyVariable(method = "renderFirstPersonItem", at = @At("HEAD"), argsOnly = true, ordinal = 3, require = 0)
    private float chromium$lowShield(float equipProgress) {
        LowShield m = ChromiumClient.mod(LowShield.class);
        var p = MinecraftClient.getInstance().player;
        if (m == null || !m.enabled || p == null) return equipProgress;
        if (p.getOffHandStack().isOf(Items.SHIELD) || p.getMainHandStack().isOf(Items.SHIELD))
            return Math.max(equipProgress, m.amount.f());
        return equipProgress;
    }
}
