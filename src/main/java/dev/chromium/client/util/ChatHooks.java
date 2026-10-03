package dev.chromium.client.util;

import dev.chromium.client.ChromiumClient;
import dev.chromium.client.module.UtilityModules.*;
import net.minecraft.text.Text;

public final class ChatHooks {
    private ChatHooks() {}
    public static Text process(Text t) {
        if (ChromiumClient.modules == null) return t;
        try {
            ChatNotifications cn = ChromiumClient.mod(ChatNotifications.class);
            if (cn != null && cn.enabled) cn.inspect(t.getString());
            ChatTimestamps ts = ChromiumClient.mod(ChatTimestamps.class);
            if (ts != null && ts.enabled) return ts.stamp(t);
        } catch (Throwable ignored) {}
        return t;
    }
}
