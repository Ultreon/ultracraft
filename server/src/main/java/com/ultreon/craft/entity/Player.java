package com.ultreon.craft.entity;

import com.google.common.base.Preconditions;
import com.ultreon.craft.entity.player.PlayerAbilities;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.menu.ContainerMenu;
import com.ultreon.craft.menu.Inventory;
import com.ultreon.craft.menu.MenuTypes;
import com.ultreon.craft.network.packets.AbilitiesPacket;
import com.ultreon.craft.sound.event.SoundEvents;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Gamemode;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.util.Intersector;
import com.ultreon.craft.util.Ray;
import com.ultreon.craft.world.SoundEvent;
import com.ultreon.craft.world.World;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.commons.v0.vector.Vec2f;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

public abstract class Player extends LivingEntity {
    public int selected;
    public Inventory inventory;
    private boolean running = false;
    private float walkingSpeed = .09F;
    private float flyingSpeed = 0.5F;
    public float runModifier = 1.5F;
    public float crouchModifier = 0.5F;
    public final PlayerAbilities abilities = new PlayerAbilities();
    private boolean crouching = false;
    @Nullable private ContainerMenu openMenu;
    private ItemStack cursor = new ItemStack();
    private final String name;
    private Gamemode gamemode = Gamemode.SURVIVAL;

    protected Player(EntityType<? extends Player> entityType, World world, String name) {
        super(entityType, world);

        this.inventory = new Inventory(MenuTypes.INVENTORY, world, this, null);
        this.name = name;
        this.inventory.build();
    }

    @Override
    public double getSpeed() {
        return this.isFlying() ? this.getFlyingSpeed() : this.getWalkingSpeed();
    }

    @Override
    protected void setupAttributes() {
        this.attributes.setBase(Attribute.SPEED, this.getWalkingSpeed());
    }

    @Override
    public boolean isInvincible() {
        return this.abilities.invincible;
    }

    @Override
    public void setInvincible(boolean invincible) {
        this.abilities.invincible = invincible;
        this.sendAbilities();
    }

    public void selectBlock(int i) {
        int toSelect = i % 9;
        if (toSelect < 0) toSelect += 9;
        this.selected = toSelect;
    }

    public ItemStack getSelectedItem() {
        if (this.selected < 0) this.selected = 0;
        return this.selected >= 9 ? ItemStack.empty() : this.inventory.getHotbarSlot(this.selected).getItem();
    }

    @Override
    public void tick() {
        if (this.jumping && !this.abilities.flying) this.swimUp();

        this.x = Mth.clamp(this.x, -30000000, 30000000);
        this.z = Mth.clamp(this.z, -30000000, 30000000);

        super.tick();
    }

    protected void onVoidDamage() {

    }

    @Override
    public boolean isAffectedByFluid() {
        return !(this.abilities.flying || this.noClip) && super.isAffectedByFluid();
    }

    @Override
    public void setRotation(Vec2f position) {
        super.setRotation(position);
        this.xHeadRot = position.x;
    }

    @Override
    public void setPosition(Vec3d position) {
        position.x = Mth.clamp(position.x, -30000000, 30000000);
        position.z = Mth.clamp(position.z, -30000000, 30000000);
        super.setPosition(position);
    }

    @Override
    public TextObject getDisplayName() {
        return TextObject.literal(this.name);
    }

    public void rotateHead(float x, float y) {
        this.xHeadRot += x;
        this.yRot += y;
        this.xRot = Mth.clamp(this.xRot, this.xHeadRot - 50, this.xHeadRot + 50);
    }

    public float getEyeHeight() {
        return this.crouching ? 1.15F : 1.63F;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public float getWalkingSpeed() {
        return this.walkingSpeed;
    }

    public void setWalkingSpeed(float walkingSpeed) {
        this.walkingSpeed = walkingSpeed;
    }

    public float getFlyingSpeed() {
        return this.flyingSpeed;
    }

    public void setFlyingSpeed(float flyingSpeed) {
        this.flyingSpeed = flyingSpeed;
    }

    public boolean isFlying() {
        return this.abilities.flying;
    }

    public void setFlying(boolean flying) {
        this.noGravity = this.abilities.flying = flying;
        this.sendAbilities();
    }

    public boolean isCrouching() {
        return this.crouching;
    }

    public void setCrouching(boolean crouching) {
        this.crouching = crouching;
    }

    public boolean isSpectator() {
        return this.gamemode == Gamemode.SPECTATOR;
    }

    @Deprecated
    public boolean isSpectating() {
        return this.isSpectator();
    }

    @Deprecated
    public void setSpectating(boolean spectating) {
        this.setGamemode(spectating ? Gamemode.SPECTATOR : Gamemode.SURVIVAL);
    }

    @Override
    public SoundEvent getHurtSound() {
        return SoundEvents.PLAYER_HURT;
    }

    public HitResult rayCast() {
        return this.world.rayCast(new Ray(this.getPosition().add(0, this.getEyeHeight(), 0), this.getLookVector()));
    }

    @Override
    public void load(MapType data) {
        super.load(data);

        this.selected = data.getByte("selectedItem", (byte) this.selected);
        this.crouching = data.getBoolean("crouching", this.crouching);
        this.running = data.getBoolean("running", this.running);
        this.walkingSpeed = data.getFloat("walkingSpeed", this.walkingSpeed);
        this.flyingSpeed = data.getFloat("flyingSpeed", this.flyingSpeed);
        this.crouchModifier = data.getFloat("crouchingModifier", this.crouchModifier);
        this.runModifier = data.getFloat("runModifier", this.runModifier);
        this.gamemode = Objects.requireNonNullElse(Gamemode.byOrdinal(data.getByte("gamemode", (byte) 0)), Gamemode.SURVIVAL);
        this.abilities.load(data.getMap("Abilities"));
    }

    @Override
    public MapType save(MapType data) {
        data = super.save(data);

        data.putByte("selectedItem", this.selected);
        data.putBoolean("crouching", this.crouching);
        data.putBoolean("running", this.running);
        data.putFloat("walkingSpeed", this.walkingSpeed);
        data.putFloat("flyingSpeed", this.flyingSpeed);
        data.putFloat("crouchingModifier", this.crouchModifier);
        data.putFloat("runModifier", this.runModifier);
        data.putByte("gamemode", (byte) this.gamemode.ordinal());
        data.put("Abilities", this.abilities.save(new MapType()));

        return data;
    }

    public void playSound(@Nullable SoundEvent sound, float volume) {

    }

    public boolean isAllowFlight() {
        return this.abilities.allowFlight;
    }

    public void setAllowFlight(boolean allowFlight) {
        this.abilities.allowFlight = allowFlight;
        this.sendAbilities();
    }

    protected abstract void sendAbilities();

    protected void onAbilities(AbilitiesPacket packet) {
        this.noGravity = packet.isFlying();
    }

    public @Nullable ContainerMenu getOpenMenu() {
        return this.openMenu;
    }

    public void openMenu(ContainerMenu menu) {
        this.openMenu = menu;
        menu.addWatcher(this);
    }

    public void closeMenu() {
        if (this.openMenu == null) return;

        this.openMenu.removeWatcher(this);
        this.openMenu = null;
    }

    @Override
    protected void onMoved() {
        this.x = Mth.clamp(this.x, -30000000, 30000000);
        this.z = Mth.clamp(this.z, -30000000, 30000000);

        if (this.y < -64) this.y = -64;

        super.onMoved();
    }

    public ItemStack getCursor() {
        return this.cursor;
    }

    /**
     * Set the item cursor for menu containers.
     * <p style="color:red;"><b>Not recommended to be called on client side, only do it when you know what you're doing.</b></p>
     *
     * @param cursor the item stack to set.
     */
    public void setCursor(ItemStack cursor) {
        Preconditions.checkNotNull(cursor, "cursor");
        this.cursor = cursor;
    }

    public void openInventory() {
        this.openMenu(this.inventory);
    }

    public @Nullable Entity rayCast(Collection<Entity> entities) {
        Ray ray = new Ray(this.getPosition(), this.getLookVector());
        return entities.stream()
                .filter(entity -> Intersector.intersectRayBounds(ray, entity.getBoundingBox(), null))
                .sorted(Comparator.comparing(entity -> entity.getPosition().dst(ray.origin)))
                .findFirst()
                .orElse(null);
    }

    public @Nullable Entity nearestEntity() {
        return this.world.getEntities()
                .stream()
                .sorted(Comparator.comparing(entity -> entity.getPosition().dst(this.getPosition())))
                .findFirst()
                .orElse(null);
    }

    public @Nullable <T extends Entity> T nearestEntity(Class<T> clazz) {
        return this.world.getEntities()
                .stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .sorted(Comparator.comparing(entity -> entity.getPosition().dst(this.getPosition())))
                .findFirst()
                .orElse(null);
    }

    public void setGamemode(Gamemode gamemode) {
        this.gamemode = gamemode;
        switch (gamemode) {
            case SURVIVAL -> {
                this.abilities.allowFlight = false;
                this.abilities.instaMine = false;
                this.abilities.invincible = false;
                this.abilities.blockBreak = true;
                this.abilities.flying = false;
                this.noClip = false;
            }
            case BUILDER, BUILDER_PLUS -> {
                this.abilities.allowFlight = true;
                this.abilities.instaMine = true;
                this.abilities.invincible = true;
                this.abilities.blockBreak = true;
                this.noClip = false;
            }
            case MINI_GAME -> {
                this.abilities.allowFlight = false;
                this.abilities.instaMine = false;
                this.abilities.invincible = false;
                this.abilities.blockBreak = false;
                this.abilities.flying = false;
                this.noClip = false;
            }
            case SPECTATOR -> {
                this.abilities.allowFlight = true;
                this.abilities.instaMine = false;
                this.abilities.invincible = true;
                this.abilities.blockBreak = false;
                this.abilities.flying = true;
                this.noClip = true;
            }
        }

        this.sendAbilities();
    }

    public Gamemode getGamemode() {
        return this.gamemode;
    }

    public boolean isBuilder() {
        return this.gamemode == Gamemode.BUILDER || this.gamemode == Gamemode.BUILDER_PLUS;
    }

    public boolean isSurvival() {
        return this.gamemode == Gamemode.SURVIVAL || this.gamemode == Gamemode.MINI_GAME;
    }
}
