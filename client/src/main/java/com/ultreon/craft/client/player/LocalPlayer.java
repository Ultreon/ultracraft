package com.ultreon.craft.client.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.api.commands.perms.Permission;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.screens.DeathScreen;
import com.ultreon.craft.client.input.GameInput;
import com.ultreon.craft.client.input.util.ControllerButton;
import com.ultreon.craft.client.registry.MenuRegistry;
import com.ultreon.craft.client.world.ClientWorld;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.entity.damagesource.DamageSource;
import com.ultreon.craft.menu.ContainerMenu;
import com.ultreon.craft.menu.MenuType;
import com.ultreon.craft.network.packets.AbilitiesPacket;
import com.ultreon.craft.network.packets.c2s.*;
import com.ultreon.craft.network.packets.s2c.C2SAbilitiesPacket;
import com.ultreon.craft.network.packets.s2c.S2CPlayerHurtPacket;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.Location;
import com.ultreon.craft.world.SoundEvent;
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class LocalPlayer extends ClientPlayer {
    private final UltracraftClient client = UltracraftClient.get();
    private final ClientWorld world;
    private int oldSelected;
    private final ClientPermissionMap permissions = new ClientPermissionMap();

    public LocalPlayer(EntityType<? extends Player> entityType, ClientWorld world, UUID uuid) {
        super(entityType, world);
        this.world = world;
        this.setUuid(uuid);
    }

    /**
     * Overrides the tick method to handle player actions and movement in the game.
     */
    @Override
    public void tick() {
        // Do not execute if not rendering the world
        if (!this.client.renderWorld) return;

        // Determine if the player is jumping based on input
        this.jumping = !this.isDead() && (Gdx.input.isKeyPressed(Input.Keys.SPACE) && Gdx.input.isCursorCatched() || GameInput.isControllerButtonDown(ControllerButton.A));

        // Call the superclass tick method
        super.tick();

        // Send a packet when the selected item changes
        if (this.selected != this.oldSelected) {
            this.client.connection.send(new C2SHotbarIndexPacket(this.selected));
            this.oldSelected = this.selected;
        }

        // Handle player movement if there is a significant change in position
        if (Math.abs(this.x - this.ox) >= 0.01 || Math.abs(this.y - this.oy) >= 0.01 || Math.abs(this.z - this.oz) >= 0.01) {
            this.handleMove();
        } else {
            // Update previous position if no significant change
            this.ox = this.x;
            this.oy = this.y;
            this.oz = this.z;
        }
    }


    /**
     * Handles the player movement by sending the new coordinates to the server.
     */
    private void handleMove() {
        // Send the player's new coordinates to the server
        this.client.connection.send(new C2SPlayerMovePacket(this.x, this.y, this.z));

        // Update the old coordinates to the current ones
        this.ox = this.x;
        this.oy = this.y;
        this.oz = this.z;
    }

    /**
     * {@inheritDoc}
     * This will do nothing on the client side of the player.
     */
    @Override
    protected void hitGround() {
        // Do nothing on the client side of the player.
    }

    @Override
    public boolean isWalking() {
        return this.client.playerInput.isWalking();
    }

    @Override
    protected void hurtFromVoid() {
        // The server should handle player void damage.
    }

    @Override
    public void jump() {
        if (this.isInWater()) return;
        this.velocityY = this.jumpVel;

        if (this.isRunning()) {
            this.velocityX *= 1.2;
            this.velocityZ *= 1.2;
        }
    }

    @Override
    public boolean onHurt(float damage, @NotNull DamageSource source) {
        if (source == DamageSource.FALLING) {
            GameInput.startVibration(50, 1.0F);
        }

        return super.onHurt(damage, source);
    }

    @Override
    public void onDeath() {
        super.onDeath();

        this.client.showScreen(new DeathScreen());
    }

    @Override
    protected void onVoidDamage() {
        GameInput.startVibration(200, 1.0F);

        super.onVoidDamage();
    }

    @Override
    public float getWalkingSpeed() {
        return this.isRunning() ? super.getWalkingSpeed() * this.runModifier : super.getWalkingSpeed();
    }

    /**
     * {@inheritDoc}
     * If the sound event is not null, it also plays the sound using the client.
     *
     * @param sound  The sound event to be played. Can be null.
     * @param volume The volume at which the sound should be played.
     */
    @Override
    public void playSound(@Nullable SoundEvent sound, float volume) {
        // Call the superclass method to play the sound event and volume
        super.playSound(sound, volume);

        // If the sound event is not null, play the sound using the client
        if (sound != null) {
            this.client.playSound(sound, volume);
        }
    }

    @Override
    protected void sendAbilities() {
        this.client.connection.send(new C2SAbilitiesPacket(this.abilities));
    }

    /**
     * Updates the player's abilities based on the information received in the AbilitiesPacket.
     *
     * @param packet The AbilitiesPacket containing the player's abilities information.
     */
    @Override
    public void onAbilities(@NotNull AbilitiesPacket packet) {
        // Update the player's abilities
        this.abilities.flying = packet.isFlying();
        this.abilities.allowFlight = packet.allowFlight();
        this.abilities.instaMine = packet.isInstaMine();
        this.abilities.invincible = packet.isInvincible();

        // Call superclass method
        super.onAbilities(packet);
    }

    @Override
    public void openMenu(@NotNull ContainerMenu menu) {
        if (this.openMenu != null) {
            return;
        }

        super.openMenu(menu);

        this.client.connection.send(new C2SOpenMenuPacket(menu.getType().getId(), menu.getPos()));
    }

    @Override
    public void closeMenu() {
        super.closeMenu();

        this.client.connection.send(new C2SCloseMenuPacket());
    }

    @Override
    public void openInventory() {
        super.openInventory();
        this.client.connection.send(new C2SOpenInventoryPacket());
    }

    @Override
    public @NotNull ClientWorld getWorld() {
        return this.world;
    }

    public void onHealthUpdate(float newHealth) {
        this.oldHealth = this.health;
        this.health = newHealth;
    }

    public void resurrect() {
        this.setHealth(this.getMaxHealth());
        this.isDead = false;
    }

    public void onOpenMenu(MenuType<?> menuType) {
        ContainerMenu openedBefore = openMenu;
        if (openedBefore != null && menuType != openedBefore.getType()) {
            CommonConstants.LOGGER.warn("Opened menu {} instead of {}", menuType, openMenu);
        } else if (openedBefore == null) {
            CommonConstants.LOGGER.warn("Opened server menu {} before opening any on client side", menuType);
            openedBefore = menuType.create(this.world, this, this.getBlockPos());
        }
        this.client.showScreen(MenuRegistry.getScreen(openedBefore));
    }

    @Override
    public @NotNull Location getLocation() {
        return new Location(this.world, this.x, this.y, this.z, this.xRot, this.yRot);
    }

    @Override
    public boolean hasExplicitPermission(@NotNull Permission permission) {
        return this.permissions.has(permission);
    }

    @Override
    public void dropItem() {
        this.client.connection.send(new C2SDropItemPacket());
    }

    public ClientPermissionMap getPermissions() {
        return this.permissions;
    }

    public void onHurt(S2CPlayerHurtPacket packet) {
        this.hurt(packet.getDamage(), packet.getSource());
    }

    /**
     * Returns the position with interpolation based on the partial tick.
     *
     * @param partialTick the partial tick for interpolation
     * @return the interpolated position as a Vec3d object
     */
    public Vec3d getPosition(float partialTick) {
        return new Vec3d(
                Mth.lerp(this.ox, this.x, partialTick),
                Mth.lerp(this.oy, this.y, partialTick),
                Mth.lerp(this.oz, this.z, partialTick)
        );
    }

    @Override
    @Deprecated
    public Vec3d getPosition() {
        return super.getPosition();
    }
}
