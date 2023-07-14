package com.ultreon.craft.datastorage;

import com.badlogic.gdx.files.FileHandle;
import com.ultreon.craft.Constants;
import com.ultreon.craft.GamePlatform;
import com.ultreon.craft.world.SavedWorld;

import java.util.*;
import java.util.stream.Collectors;

public class SaveManager {
    private static final Map<String, SavedWorld> SAVES = new HashMap<>();

    public static Collection<SavedWorld> reloadWorlds() {
        FileHandle[] list = GamePlatform.data(Constants.WORLDS_DIR).list();

        Set<String> oldSaves = new HashSet<>(SAVES.keySet());

        for (FileHandle save : list) {
            if (!save.isDirectory()) {
                continue;
            }

            String name = save.name();
            if (oldSaves.contains(name)) {
                SAVES.get(name).unloadWorldInfo();
                SAVES.put(name, new SavedWorld(save));
                oldSaves.remove(name);
            }

        }

        for (String name : oldSaves) {
            SAVES.get(name).unloadWorldInfo();
            oldSaves.remove(name);
        }

        return SAVES.values();
    }

    public static Set<String> names() {
        FileHandle[] list = GamePlatform.data(Constants.WORLDS_DIR).list();
        return Arrays.stream(list).map(FileHandle::name).collect(Collectors.toSet());
    }
}
