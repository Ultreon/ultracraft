package com.ultreon.craft.api.commands.error;

import com.ultreon.craft.api.commands.MessageCode;
import com.ultreon.craft.world.World;

public class NotFoundInWorldError extends CommandError {

    public NotFoundInWorldError(String WHAT) {
        super(MessageCode.NOT_FOUND_IN_WORLD, "There's no " + WHAT + " found in this world!");
    }

    public NotFoundInWorldError(String WHAT, int index) {
        super(MessageCode.NOT_FOUND_IN_WORLD, "There's no " + WHAT + " found in this world!", index);
    }

    public NotFoundInWorldError(String WHAT, World world) {
        super(MessageCode.NOT_FOUND_IN_WORLD, "There's no " + WHAT + " found in world: " + world.getDimension().getId());
    }

    public NotFoundInWorldError(String WHAT, World world, int index) {
        super(MessageCode.NOT_FOUND_IN_WORLD, "There's no " + WHAT + " found in world: " + world.getDimension().getId(), index);
    }

    @Override
    public String getName() {
        return "NotFound";
    }
}