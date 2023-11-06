package com.ultreon.craft.client.gui;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.widget.StaticWidget;
import com.ultreon.craft.util.Color;
import com.ultreon.libs.crash.v0.CrashCategory;
import com.ultreon.libs.crash.v0.CrashLog;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ManualCrashOverlay implements StaticWidget {
    private long endTime;

    /**
     * Creates a new manual crash overlay.
     *
     * @param client the Ultracraft client
     */
    public ManualCrashOverlay(UltracraftClient client) {
        super();
    }

    /**
     * Resets the manual crash timer.
     */
    public void reset() {
        this.endTime = System.currentTimeMillis() + 10000; // Crash the game in 10 seconds.
    }

    @Override
    public void render(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        long millis = System.currentTimeMillis();
        if (millis > this.endTime) {
            this.crash();
        }

        var width = UltracraftClient.get().getScaledWidth();
        var height = UltracraftClient.get().getScaledHeight();

        renderer.fill(0, 0, width, height, Color.rgb(0x101010));
        renderer.fill(0, 0, width, 2, Color.rgb(0xff0000));

        renderer.drawTextScaledCenter("Manual Initiating Crash", 3, width / 2, height / 2 - 100);
        renderer.drawTextCenter("Crashing the game in " + (this.endTime - millis) / 1000 + " seconds.", width / 2, height / 2 - 50);
        renderer.drawTextCenter("If you didn't meant to trigger this, release any ctrl, alt or shift key.", width / 2, height / 2 - 35);
        renderer.drawTextCenter("If you continue, it will crash the game with all thread states in the crash log.", width / 2, height / 2 - 10);
    }

    private void crash() {
        CrashLog log = new CrashLog("Manually Initiated Crash", new Throwable(":("));
        UltracraftClient.get().fillGameInfo(log);

        for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            StackTraceElement[] stackTrace = entry.getValue();
            String name = entry.getKey().getName();
            long id = entry.getKey().getId();

            Throwable throwable = new Throwable();
            throwable.setStackTrace(stackTrace);

            CrashCategory threadCategory = new CrashCategory("Thread #" + id + ": " + name, throwable);
            log.addCategory(threadCategory);
        }

        UltracraftClient.crash(log);
    }
}
