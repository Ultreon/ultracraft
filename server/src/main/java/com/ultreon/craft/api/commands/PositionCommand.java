package com.ultreon.craft.api.commands;

import com.ultreon.craft.api.commands.output.CommandOutput;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.Location;
import com.ultreon.craft.world.World;

public class PositionCommand extends Command {
    public PositionCommand() {
        super();
        this.data().aliases("/pos", "/position");
    }

    @SubCommand(value = "(1|1st|first)", comment = "Sets first region selection to current position")
    public CommandOutput executeFirst(CommandSender sender, CommandContext commandCtx, String alias) {
        if (!(sender instanceof Entity entity)) {
            return this.needEntity();
        }
        PositionSelection positions = Selections.get(sender).positions;
        if (!positions.world.equals(entity.getWorld())) {
            positions.reset(entity.getWorld());
        }
        BlockPos firstLoc = positions.first;
        String firstStr = "null";
        if (firstLoc != null) {
            firstStr = firstLoc.x() + "," + firstLoc.y() + "," + firstLoc.z();
        }
        BlockPos secondLoc = positions.second;
        String secondStr = null;
        if (secondLoc != null) {
            secondStr = secondLoc.x() + "," + secondLoc.y() + "," + secondLoc.z();
        }
        if (firstLoc != null) {
            if (secondLoc != null) {
                int ax = Math.abs(firstLoc.x() - secondLoc.x()) + 1;
                int ay = Math.abs(firstLoc.y() - secondLoc.y()) + 1;
                int az = Math.abs(firstLoc.z() - secondLoc.z()) + 1;
                int size = ax * ay * az;
                return this.editModeMessage("Current region is " + firstStr + " to " + secondStr + " (" + size + " blocks)");
            }
            return this.editModeMessage("Region selection incomplete. 1st position: " + firstStr);
        }
        if (secondLoc != null) return this.editModeMessage("Region selection incomplete. 2nd position: " + firstStr);
        return this.editModeMessage("No region selected yet.");
    }

    @SubCommand(value = "(2|2nd|second)", comment = "Sets second region selection to current position")
    public CommandOutput executeSecond(CommandSender sender, CommandContext commandCtx, String alias) {
        if (!(sender instanceof Entity entity)) {
            return this.needEntity();
        }
        PositionSelection positions = Selections.get(sender).positions;
        if (!positions.world.equals(entity.getWorld())) {
            positions.reset(entity.getWorld());
        }
        Location loc = sender.getLocation();
        positions.second = loc.getBlockPos();
        return this.editModeMessage("Second position set to " + loc.x + "," + loc.y + "," + loc.z);
    }

    static class PositionSelection {
        private World world = null;
        private BlockPos first = null;
        private BlockPos second = null;

        public int getSize() {
            int adx = Math.abs(this.first.x() - this.second.x()) + 1;
            int ady = Math.abs(this.first.y() - this.second.y()) + 1;
            int adz = Math.abs(this.first.z() - this.second.z()) + 1;
            return adx * ady * adz;
        }

        public void reset(World world) {
            this.first = null;
            this.second = null;
            this.world = world;
        }
    }
}