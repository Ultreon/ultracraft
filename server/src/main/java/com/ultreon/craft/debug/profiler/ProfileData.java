package com.ultreon.craft.debug.profiler;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public class ProfileData {
    private final Map<Thread, ThreadSection.FinishedThreadSection> profileMap;

    public ProfileData(Map<Thread, ThreadSection.FinishedThreadSection> profileMap) {
        this.profileMap = profileMap;
    }

    public ThreadSection.FinishedThreadSection getThreadSection(Thread thread) {
        return this.profileMap.get(thread);
    }

    @Nullable
    public Section.FinishedSection getSection(ThreadSection.FinishedThreadSection section, String path) {
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);

        if (!path.matches("(/" + Profiler.SECTION_REGEX + ")+/?"))
            throw new IllegalArgumentException("Invalid path: " + path);

        String[] pathSegments = path.substring(1).split("/");
        Section.FinishedSection sect = null;
        for (String name : pathSegments) {
            if (sect == null) sect = section.getData().get(name);
            else sect = sect.getData().get(name);

            if (sect == null) return null;
        }

        if (sect == null) throw new InternalError("Profile section not found: " + path);

        return sect;
    }

    public Set<Thread> getThreads() {
        return this.profileMap.keySet();
    }
}
