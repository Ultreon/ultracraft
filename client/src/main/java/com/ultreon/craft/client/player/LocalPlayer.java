package com.ultreon.craft.client.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.screens.DeathScreen;
import com.ultreon.craft.client.input.GameInput;
import com.ultreon.craft.client.input.util.ControllerButton;
import com.ultreon.craft.client.world.ClientWorld;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.entity.damagesource.DamageSource;
import com.ultreon.craft.network.packets.c2s.C2SPlayerMovePacket;
import com.ultreon.craft.world.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class LocalPlayer extends Player {
    private final UltracraftClient client = UltracraftClient.get();
    private final ClientWorld world;
    private UUID uuid;

    public LocalPlayer(EntityType<? extends Player> entityType, ClientWorld world, UUID uuid) {
        super(entityType, world);
        this.world = world;
        this.uuid = uuid;
    }

    @Override
    public void tick() {
        this.jumping = !this.isDead() && (Gdx.input.isKeyPressed(Input.Keys.SPACE) && Gdx.input.isCursorCatched() || GameInput.isControllerButtonDown(ControllerButton.A));

        super.tick();

        if (this.x != this.ox || this.y != this.oy || this.z != this.oz) {
            if (this.world.getChunk(this.getChunkPos()) == null) {
                this.x = this.ox;
                this.z = this.oz;
            }
            this.client.connection.send(new C2SPlayerMovePacket(this.x - this.ox, this.y - this.oy, this.z - this.oz));
            this.ox = this.x;
            this.oy = this.y;
            this.oz = this.z;
        }
    }

    @Override
    protected void move() {
        if (this.world.getChunk(this.getChunkPos()) == null) return;

        super.move();
    }

    @Override
    protected void onMoved() {
        super.onMoved();
    }

    @Override
    protected void hurtFromVoid() {

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
    public @NotNull UUID getUuid() {
        return this.uuid;
    }

    @Override
    protected void setUuid(@NotNull UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void playSound(@Nullable SoundEvent sound, float volume) {
        super.playSound(sound, volume);
        if (sound != null) {
            this.client.playSound(sound, volume);
        }
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
}
