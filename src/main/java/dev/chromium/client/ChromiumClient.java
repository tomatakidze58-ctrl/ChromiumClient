package dev.chromium.client;

import dev.chromium.client.gui.ClickGuiScreen;
import dev.chromium.client.gui.HudEditorScreen;
import dev.chromium.client.module.*;
import dev.chromium.client.module.PvpModules.CrosshairCustomizer;
import dev.chromium.client.util.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ChromiumClient implements ClientModInitializer {
    public static final String NAME = "ChromiumClient";
    public static final long START = System.currentTimeMillis();
    public static ModuleManager modules;
    public static KeyBinding guiKey, hudKey, zoomKey, folderKey;

    public static <T extends Module> T mod(Class<T> c) { return modules == null ? null : modules.get(c); }
    public static boolean on(Class<? extends Module> c) { Module m = mod(c); return m != null && m.enabled; }

    @Override
    public void onInitializeClient() {
        modules = new ModuleManager();
        Config.load();

        KeyBinding.Category cat = KeyBinding.Category.create(Identifier.of("chromiumclient", "main"));
        guiKey    = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.chromiumclient.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, cat));
        hudKey    = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.chromiumclient.hud", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_CONTROL, cat));
        zoomKey   = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.chromiumclient.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, cat));
        folderKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.chromiumclient.folder", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, cat));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (guiKey.wasPressed()) mc.setScreen(new ClickGuiScreen());
            while (hudKey.wasPressed()) mc.setScreen(new HudEditorScreen());
            while (folderKey.wasPressed()) UtilityModules.ScreenshotManager.openFolder();
            if (mc.player == null) return;
            HitTracker.tick(mc);
            modules.tick(mc);
        });

        // Main HUD layer
        HudElementRegistry.addLast(Identifier.of("chromiumclient", "hud"), (ctx, tick) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            Clicks.poll(mc);
            if (mc.options.hudHidden || mc.player == null) return;
            if (Theme.watermark.b && !mc.getDebugHud().shouldShowDebugHud()) {
                int x = mc.getWindow().getScaledWidth() - 4 - mc.textRenderer.getWidth("cr ChromiumClient");
                ctx.drawText(mc.textRenderer, "cr", x, mc.getWindow().getScaledHeight() - 12, Theme.accent(), true);
                ctx.drawText(mc.textRenderer, " ChromiumClient", x + mc.textRenderer.getWidth("cr"), mc.getWindow().getScaledHeight() - 12, 0xFFAAAAAA, true);
            }
            if (!mc.getDebugHud().shouldShowDebugHud()) modules.renderHud(ctx, mc);
            Notifications.render(ctx, mc);
        });

        // Replace the vanilla crosshair with the customizable one
        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, original -> (ctx, tick) -> {
            CrosshairCustomizer cc = mod(CrosshairCustomizer.class);
            if (cc != null && cc.enabled) cc.drawCrosshair(ctx);
            else original.render(ctx, tick);
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> Config.save());
    }
}
