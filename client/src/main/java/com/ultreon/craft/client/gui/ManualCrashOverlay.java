package com.ultreon.craft.client.gui;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.util.Color;
import com.ultreon.libs.crash.v0.CrashCategory;
import com.ultreon.libs.crash.v0.CrashLog;

import java.util.Map;

public class ManualCrashOverlay extends GuiComponent {
    private long endTime;

    /**
     * Creates a new manual crash overlay.
     *
     * @param client the Ultracraft client
     */
    public ManualCrashOverlay(UltracraftClient client) {
        super(0, 0, client.getScaledWidth(), client.getScaledHeight());
    }

    public void reset() {
        this.endTime = System.currentTimeMillis() + 10000; // Crash the game in 10 seconds.
    }

    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        long millis = System.currentTimeMillis();
        if (millis > this.endTime) {
            this.crash();
        }

        renderer.fill(0, 0, this.width, this.height, Color.rgb(0x101010));
        renderer.fill(0, 0, this.width, 2, Color.rgb(0xff0000));

        renderer.drawCenteredTextScaled("Manual Initiating Crash", 3, this.width / 2, this.height / 2 - 100);
        renderer.drawTextCenter("Crashing the game in " + (this.endTime - millis) / 1000 + " seconds.", this.width / 2, this.height / 2 - 50);
        renderer.drawTextCenter("If you didn't meant to trigger this, release any ctrl, alt or shift key.", this.width / 2, this.height / 2 - 35);
        renderer.drawTextCenter("If you continue, it will crash the game with all thread states in the crash log.", this.width / 2, this.height / 2 - 10);
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
