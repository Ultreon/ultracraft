package com.ultreon.craft.client;

import com.badlogic.gdx.Gdx;
import com.ultreon.craft.client.gui.Bounds;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.widget.SelectionList;
import com.ultreon.craft.client.util.Utils;
import com.ultreon.craft.crash.CrashLog;
import com.ultreon.craft.text.ChatColor;
import com.ultreon.craft.text.TextObject;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class CrashScreen extends Screen {
    private static final List<String> UNUSABLE_STACK_ELEMENTS = List.of(
            "com.ultreon.craft.crash.", // Crash handling
            "java.", "javax.", "javafx.", // Java packages
            "kotlin.", "kotlinx.",        // Kotlin packages
            "sun.", "jdk.internal.",      // JDK internal packages
            "org.openjdk.", "org.junit.", // Misc. packages
            "com.badlogic.gdx."           // libGDX packages
    );

    private final List<CrashLog> crashes;
    private SelectionList<CrashLog> list;

    public CrashScreen(List<CrashLog> crashes) {
        super(TextObject.translation("ultracraft.screen.crash"));

        this.crashes = crashes;
    }

    private static boolean isUsableStackElement(StackTraceElement stackTraceElement) {
        String className = stackTraceElement.getClassName();
        for (String unusableStackElement : UNUSABLE_STACK_ELEMENTS) {
            if (className.startsWith(unusableStackElement)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void build(GuiBuilder builder) {
        final int itemSize = 75;
        this.list = builder.add(new SelectionList<CrashLog>(itemSize).entries(crashes).itemRenderer(this::renderItem).bounds(() -> new Bounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight())));

        File destination = new File("crash-reports/" + CrashLog.getFileNameWithoutExt() + "-loading.txt");
        crashes.forEach(Utils.with(destination, CrashLog::writeToFile));
        crashes.forEach(CrashLog::writeToLog);
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, int mouseX, int mouseY, @IntRange(from = 0) float deltaTime) {
        super.renderWidget(renderer, mouseX, mouseY, deltaTime);

        this.list.width(UltracraftClient.get().getScaledWidth());
    }

    private void renderItem(Renderer renderer, CrashLog value, int y, int mouseX, int mouseY, boolean selected, float deltaTime) {
        Throwable throwable = value.getThrowable();
        String throwableName = throwable.getClass().getSimpleName();
        String message = throwable.getMessage();
        String errorMessage = message == null ? "<No message>" : message.trim().stripIndent().replace("\n", " ").replace("\t", "").replace("java.lang.", "");

        String usableStackTrace = Arrays.stream(throwable.getStackTrace()).filter(CrashScreen::isUsableStackElement).map(StackTraceElement::toString).findFirst().orElse("<Unknown>").trim();

        renderer.textLeft(value.getDetails().trim(), 20, y + 20, ChatColor.RED);
        renderer.textLeft(throwableName + ": " + errorMessage, 20, y + 32, ChatColor.GRAY);
        renderer.textLeft("    " + usableStackTrace, 20, y + 44, ChatColor.DARK_GRAY);
    }
}
