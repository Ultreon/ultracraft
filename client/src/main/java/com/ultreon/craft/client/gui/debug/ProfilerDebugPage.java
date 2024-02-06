package com.ultreon.craft.client.gui.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.ultreon.craft.debug.profiler.ProfileData;
import com.ultreon.craft.debug.profiler.Section;
import com.ultreon.craft.debug.profiler.ThreadSection;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ProfilerDebugPage implements DebugPage {
    ProfileData profile;
    private @NotNull String currentPath = "/";
    private @Nullable Thread currentThread = null;
    private String idxInput = "";

    @Override
    public void render(DebugRenderContext context) {
        String path = this.currentPath;
        Thread thread = this.currentThread;

        if (this.profile == null) return;

        Comparator<Section.FinishedSection> comparator = new FinishedSectionComparator();

        context.entryLine(TextObject.literal(this.idxInput).setColor(Color.WHITE));

        if (this.renderView(context, thread, path, comparator)) return;
        this.inputHandling(thread, path, comparator);
    }

    private void inputHandling(Thread thread, String path, Comparator<Section.FinishedSection> comparator) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_ENTER)) {
            String input = this.idxInput;
            try {
                this.navigate(input, thread, path, comparator);
            } catch (NumberFormatException ignored) {
                this.idxInput = "";
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            this.goBack(path);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_DOT)) {
            this.idxInput = "";
        } else for (int num = 0; num < 10; num++) {
            int key = Input.Keys.NUMPAD_0 + num;
            if (Gdx.input.isKeyJustPressed(key)) {
                this.handleInput(num);
                break;
            }
        }
    }

    private boolean renderView(DebugRenderContext context, Thread thread, String path, Comparator<Section.FinishedSection> comparator) {
        if (thread == null) {
            return this.renderThreadView(context);
        } else {
            return this.renderSectionView(context, thread, path, comparator);
        }
    }

    private void handleInput(int num) {
        this.idxInput += String.valueOf(num);
    }

    private void goBack(String path) {
        if (this.currentPath.equals("/")) {
            this.currentThread = null;
            return;
        }

        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        this.currentPath = path.substring(0, path.lastIndexOf("/")) + '/';
    }

    private void navigate(String input, Thread thread, String path, Comparator<Section.FinishedSection> comparator) {
        int idx = Integer.parseInt(input);
        if (thread == null) {
            this.navThreadView(idx);
        } else {
            if (this.navSectionView(thread, path, comparator, idx)) return;
        }
        this.idxInput = "";
    }

    private boolean navSectionView(Thread thread, String path, Comparator<Section.FinishedSection> comparator, int idx) {
        ThreadSection.FinishedThreadSection threadSection = this.profile.getThreadSection(thread);
        List<Section.FinishedSection> data;

        if (path.equals("/")) {
            data = threadSection.getData().values().stream().sorted(comparator).collect(Collectors.toList());
        } else {
            Section.FinishedSection section = this.profile.getSection(threadSection, path);
            if (section == null) {
                this.currentThread = null;
                this.currentPath = "/";
                return true;
            }
            data = section.getData().values().stream().sorted(comparator).collect(Collectors.toList());
        }

        if (data.isEmpty()) return true;

        if (idx >= 0 && idx < data.size()) {
            path += data.get(idx).getName() + '/';
            this.currentPath = path;
        }
        return false;
    }

    private void navThreadView(int idx) {
        Thread thread;
        List<Thread> threads = this.profile.getThreads().stream().sorted((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName())).collect(Collectors.toList());
        if (idx >= 0 && idx < threads.size()) {
            thread = threads.get(idx);
            this.currentThread = thread;
            this.currentPath = "/";
        }
    }

    private boolean renderSectionView(DebugRenderContext context, Thread thread, String path, Comparator<Section.FinishedSection> comparator) {
        ThreadSection.FinishedThreadSection threadSection = this.profile.getThreadSection(thread);
        List<Section.FinishedSection> data;

        context.entryLine(TextObject.literal(path).setColor(Color.AZURE).setBold(true).setUnderlined(true))
                .entryLine();

        if (path.equals("/")) {
            data = threadSection.getData().values().stream().sorted(comparator).collect(Collectors.toList());
        } else {
            Section.FinishedSection section = this.profile.getSection(threadSection, path);
            if (section == null) {
                this.currentThread = null;
                this.currentPath = "/";
                return true;
            }

            data = section.getData().values().stream().sorted(comparator).collect(Collectors.toList());
        }
        for (int i = 0, sectionsSize = data.size(); i < sectionsSize; i++) {
            Section.FinishedSection s = data.get(i);
            context.entryLine(i, s.getName(), s.getNanos());
        }
        return false;
    }

    private boolean renderThreadView(DebugRenderContext context) {
        context.entryLine(TextObject.literal("Thread View").setColor(Color.GREEN).setBold(true).setUnderlined(true))
                .entryLine();

        List<Thread> threads = this.profile.getThreads().stream().sorted((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName())).collect(Collectors.toList());
        if (threads.isEmpty()) return true;

        for (int i = 0, threadsSize = threads.size(); i < threadsSize; i++) {
            Thread t = threads.get(i);
            context.entryLine(i, t.getName());
        }
        return false;
    }

    private static class FinishedSectionComparator implements Comparator<Section.FinishedSection> {
        @Override
        public int compare(Section.FinishedSection o1, Section.FinishedSection o2) {
            int compare = Long.compare(o1.getNanos(), o2.getNanos());
            if (compare == 0) {
                return o1.getName().compareTo(o2.getName());
            }

            return compare;
        }
    }
}
