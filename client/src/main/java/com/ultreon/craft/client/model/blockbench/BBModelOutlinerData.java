package com.ultreon.craft.client.model.blockbench;

import com.ultreon.craft.util.Color;
import com.ultreon.libs.commons.v0.vector.Vec3f;

import java.util.List;
import java.util.UUID;

public record BBModelOutlinerData(String name, Vec3f origin, Color color, UUID uuid, boolean export, boolean mirrorUV,
                                  boolean isOpen, boolean visibility, int autouv, List<UUID> children) {
}
