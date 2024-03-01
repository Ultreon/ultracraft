package com.ultreon.craft.client.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.ultreon.craft.api.commands.perms.Permission;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.screens.DeathScreen;
import com.ultreon.craft.client.input.GameInput;
import com.ultreon.craft.client.input.util.ControllerButton;
import com.ultreon.craft.client.registry.MenuRegistry;
import com.ultreon.craft.client.world.ClientChunk;
import com.ultreon.craft.client.world.ClientWorld;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.entity.damagesource.DamageSource;
import com.ultreon.craft.menu.ContainerMenu;
import com.ultreon.craft.network.packets.AbilitiesPacket;
import com.ultreon.craft.network.packets.c2s.C2SDropItemPacket;
import com.ultreon.craft.network.packets.c2s.C2SHotbarIndexPacket;
import com.ultreon.craft.network.packets.c2s.C2SOpenInventoryPacket;
import com.ultreon.craft.network.packets.c2s.C2SPlayerMovePacket;
import com.ultreon.craft.network.packets.s2c.C2SAbilitiesPacket;
import com.ultreon.craft.network.packets.s2c.S2CPlayerHurtPacket;
import com.ultreon.craft.world.Location;
import com.ultreon.craft.world.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class LocalPlayer extends ClientPlayer {
    private final UltracraftClient client = UltracraftClient.get();
    private final ClientWorld world;
    public @Nullable ContainerMenu openMenu;
    private int oldSelected;
    private final ClientPermissionMap permissions = new ClientPermissionMap();

    public LocalPlayer(EntityType<? extends Player> entityType, ClientWorld world, UUID uuid) {
        super(entityType, world);
        this.world = world;
        this.setUuid(uuid);
    }

    @Override
    public void tick() {
        if (!this.client.renderWorld) return;

        this.jumping = !this.isDead() && (Gdx.input.isKeyPressed(Input.Keys.SPACE) && Gdx.input.isCursorCatched() || GameInput.isControllerButtonDown(ControllerButton.A));

        super.tick();

        if (this.selected != this.oldSelected) {
            this.client.connection.send(new C2SHotbarIndexPacket(this.selected));
            this.oldSelected = this.selected;
        }

        ClientChunk chunk = this.world.getChunk(this.getChunkPos());
        if (chunk != null && chunk.isReady()) {
            if (Math.abs(this.x - this.ox) >= 0.01 || Math.abs(this.y - this.oy) >= 0.01 || Math.abs(this.z - this.oz) >= 0.01)
                this.handleMove();
        } else {
            this.x = this.ox;
            this.y = this.oy;
            this.z = this.oz;
        }

    }

    private void handleMove() {
        this.client.connection.send(new C2SPlayerMovePacket(this.x, this.y, this.z));
        this.ox = this.x;
        this.oy = this.y;
        this.oz = this.z;
    }

    @Override
    protected void hitGround() {

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

    @Override
    public void playSound(@Nullable SoundEvent sound, float volume) {
        super.playSound(sound, volume);
        if (sound != null) {
            this.client.playSound(sound, volume);
        }
    }

    @Override
    protected void sendAbilities() {
        this.client.connection.send(new C2SAbilitiesPacket(this.abilities));
    }

    @Override
    public void onAbilities(@NotNull AbilitiesPacket packet) {
        this.abilities.flying = packet.isFlying();
        this.abilities.allowFlight = packet.allowFlight();
        this.abilities.instaMine = packet.isInstaMine();
        this.abilities.invincible = packet.isInvincible();
        super.onAbilities(packet);
    }

    @Override
    public void openInventory() {
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
        this.setHealth(this.getMaxHeath());
        this.isDead = false;
    }

    public void onOpenMenu(ContainerMenu menu) {
        this.openMenu = menu;
        this.client.showScreen(MenuRegistry.getScreen(menu));
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
}
