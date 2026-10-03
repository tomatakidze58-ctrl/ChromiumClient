package dev.chromium.client.util;

import dev.chromium.client.module.Setting;
import java.util.ArrayList;
import java.util.List;

public final class Theme {
    public static final List<Setting> settings = new ArrayList<>();
    public static final Setting accent    = add(Setting.color("Accent Color", 0xFF4DA3FF));
    public static final Setting hudBg     = add(Setting.bool("HUD Background", true));
    public static final Setting bgAlpha   = add(Setting.num("Background Opacity", 110, 0, 255, 5));
    public static final Setting shadow    = add(Setting.bool("Text Shadow", true));
    public static final Setting toasts    = add(Setting.bool("Notifications", true));
    public static final Setting watermark = add(Setting.bool("Watermark", true));
    public static final Setting rainbow   = add(Setting.bool("Rainbow Accent", false));

    private Theme() {}
    private static Setting add(Setting s) { settings.add(s); return s; }

    public static int accent() {
        if (rainbow.b) {
            float h = (System.currentTimeMillis() % 4000L) / 4000f;
            return 0xFF000000 | net.minecraft.util.math.MathHelper.hsvToRgb(h, 0.65f, 1f);
        }
        return accent.color;
    }
    public static int withAlpha(int argb, int a) { return (a << 24) | (argb & 0xFFFFFF); }
}
