package com.ultreon.craft.client.model.blockbench;

import java.util.List;
import java.util.UUID;

public record BBModelOutliner(BBModelOutlinerData data, List<UUID> uuids) {
    public BBModelOutliner(BBModelOutlinerData data) {
        this(data, List.of());
    }
}
