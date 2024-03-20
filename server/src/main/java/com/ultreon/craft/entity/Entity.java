package com.ultreon.craft.entity;

import com.ultreon.craft.api.commands.CommandSender;
import com.ultreon.craft.api.commands.perms.Permission;
import com.ultreon.craft.entity.util.EntitySize;
import com.ultreon.craft.events.EntityEvents;
import com.ultreon.craft.events.api.ValueEventResult;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.server.util.Utils;
import com.ultreon.craft.text.LanguageBootstrap;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.text.Translations;
import com.ultreon.craft.util.BoundingBox;
import com.ultreon.craft.util.BoundingBoxUtils;
import com.ultreon.craft.util.Identifier;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.Location;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.rng.JavaRandomSource;
import com.ultreon.craft.world.rng.RandomSource;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.commons.v0.vector.Vec2f;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Entity implements CommandSender {
    private final EntityType<? extends Entity> type;
    protected final World world;
    protected double x;
    protected double y;
    protected double z;
    public float xRot;
    public float yRot;
    private int id = -1;
    public boolean onGround;
    public double velocityX;
    public double velocityY;
    public double velocityZ;
    public float gravity = 0.08f;
    public float drag = 0.98f;
    public boolean noGravity;
    public boolean isColliding;
    public boolean isCollidingX;
    public boolean isCollidingY;
    public boolean isCollidingZ;

    public boolean noClip;
    protected double fallDistance = 0;
    private boolean wasInFluid = false;
    private boolean swimUp;
    protected double ox, oy, oz;
    private @Nullable String formatName;
    private @Nullable TextObject customName;
    private UUID uuid = Utils.ZEROED_UUID;
    protected AttributeMap attributes = new AttributeMap();
    private final RandomSource random = new JavaRandomSource();
    private MapType pipeline = new MapType();
    private boolean markedForRemoval;

    /**
     * Creates a new entity.
     *
     * @param entityType the entity type
     * @param world      the world to create the entity in
     */
    public Entity(EntityType<? extends Entity> entityType, World world) {
        this.type = entityType;
        this.world = world;

        this.setupAttributes();
    }

    protected void setupAttributes() {

    }

    /**
     * Loads the entire entity data from a MapType object.
     *
     * @param world the world to load the entity in
     * @param data the MapType object to load the data from
     * @return the loaded entity
     */
    public static @NotNull Entity loadFrom(World world, MapType data) {
        Identifier typeId = Identifier.parse(data.getString("type"));
        EntityType<?> type = Registries.ENTITY_TYPE.get(typeId);
        Entity entity = type.create(world);

        entity.id = data.getInt("id");

        entity.loadWithPos(data);
        return entity;
    }

    /**
     * Loads the data of the object including position from a MapType object.
     *
     * @param data the MapType object to load the data from
     */
    public void loadWithPos(MapType data) {
        MapType position = data.getMap("Position", new MapType());
        this.x = position.getDouble("x", this.x);
        this.y = position.getDouble("y", this.y);
        this.z = position.getDouble("z", this.z);

        this.load(data);
    }

    /**
     * Loads the data of the object including position, rotation, velocity, id, type, fall distance, gravity,
     * drag, noGravity, and noClip from a MapType object.
     *
     * @param data the MapType object to load the data from
     */
    public void load(MapType data) {
        MapType rotation = data.getMap("Rotation", new MapType());
        this.xRot = rotation.getFloat("x", this.xRot);
        this.yRot = rotation.getFloat("y", this.yRot);

        MapType velocity = data.getMap("Velocity", new MapType());
        this.velocityX = velocity.getDouble("x", this.velocityX);
        this.velocityY = velocity.getDouble("y", this.velocityY);
        this.velocityZ = velocity.getDouble("z", this.velocityZ);

        this.fallDistance = data.getDouble("fallDistance", this.fallDistance);
        this.gravity = data.getFloat("gravity", this.gravity);
        this.drag = data.getFloat("drag", this.drag);
        this.noGravity = data.getBoolean("noGravity", this.noGravity);
        this.noClip = data.getBoolean("noClip", this.noClip);
    }

    /**
     * Saves the data of the object including position, rotation, velocity, id, type, fall distance, gravity,
     * drag, noGravity, and noClip to a MapType object.
     *
     * @param data the MapType object to save the data to
     * @return the MapType object containing all the saved data
     */
    public MapType save(MapType data) {
        MapType position = new MapType();
        position.putDouble("x", this.x);
        position.putDouble("y", this.y);
        position.putDouble("z", this.z);
        data.put("Position", position);

        MapType rotation = new MapType();
        rotation.putFloat("x", this.xRot);
        rotation.putFloat("y", this.yRot);
        data.put("Rotation", rotation);

        MapType velocity = new MapType();
        velocity.putDouble("x", this.velocityX);
        velocity.putDouble("y", this.velocityY);
        velocity.putDouble("z", this.velocityZ);
        data.put("Velocity", velocity);

        data.putInt("id", this.id);
        data.putString("type", Objects.requireNonNull(Registries.ENTITY_TYPE.getId(this.type)).toString());

        data.putDouble("fallDistance", this.fallDistance);
        data.putFloat("gravity", this.gravity);
        data.putFloat("drag", this.drag);
        data.putBoolean("noGravity", this.noGravity);
        data.putBoolean("noClip", this.noClip);

        return data;
    }

    public EntitySize getSize() {
        return this.type.getSize();
    }

    public void markRemoved() {
        this.markedForRemoval = true;
    }

    public boolean isMarkedForRemoval() {
        return this.markedForRemoval;
    }

    /**
     * Updates the entity's state and movement.
     */
    public void tick() {
        // Apply gravity if not in the noGravity state
        if (!this.noGravity) {
            // If affected by fluid and swimming up, stop swimming up
            if (this.isAffectedByFluid() && this.swimUp) {
                this.swimUp = false;
            } else {
                // Apply gravity to the velocityY
                this.velocityY -= this.gravity;
            }
        }

        // Move the entity
        this.move();

        // Apply fluid drag if affected by fluid
        if (this.isAffectedByFluid()) {
            this.velocityX *= 0.56f;
            this.velocityY *= 0.56f;
            this.velocityZ *= 0.56f;
        } else {
            // Apply regular drag if not affected by fluid
            this.velocityX *= 0.6F;
            this.velocityY *= this.noGravity ? 0.6F : this.drag;
            this.velocityZ *= 0.6F;
        }

        // Slow down the entity on ground
        if (this.onGround) {
            this.velocityX *= 0.9f;
            this.velocityZ *= 0.9f;
        }
    }

    protected void move() {
        this.move(this.velocityX, this.velocityY, this.velocityZ);
    }

    public boolean isAffectedByFluid() {
        return this.isInWater();
    }

    public boolean isInWater() {
        return this.world.get(this.getBlockPos()).isWater();
    }

    /**
     * Moves the entity upward if affected by fluid.
     */
    protected void swimUp() {
        // If affected by fluid, swim up
        if (this.isAffectedByFluid()) {
            this.swimUp = true;
        }

        // Check if the entity was previously in fluid
        if (!this.wasInFluid && this.isAffectedByFluid()) {
            this.wasInFluid = true;
            return;
        }

        // If not affected by fluid, reset the flag and set the vertical velocity
        if (!this.wasInFluid || this.isAffectedByFluid()) return;
        this.wasInFluid = false;
        this.velocityY = 0.3;
    }

    /**
     * Moves the entity by the specified deltas.
     *
     * @param deltaX the change in x-coordinate
     * @param deltaY the change in y-coordinate
     * @param deltaZ the change in z-coordinate
     * @return true if the entity is colliding after the move, false otherwise
     */
    public boolean move(double deltaX, double deltaY, double deltaZ) {
        // Store the original deltas
        double originalDeltaX = deltaX, originalDeltaY = deltaY, originalDeltaZ = deltaZ;

        // Calculate the absolute values of the deltas
        double absDeltaX = Math.abs(deltaX);
        double absDeltaY = Math.abs(deltaY);
        double absDeltaZ = Math.abs(deltaZ);

        // Check if the deltas are too small to cause a significant move
        if (absDeltaX < 0.001 && absDeltaY < 0.001 && absDeltaZ < 0.001) {
            return this.isColliding;
        }

        // Trigger an event to allow modification of the move
        ValueEventResult<Vec3d> eventResult = EntityEvents.MOVE.factory().onEntityMove(this, deltaX, deltaY, deltaZ);
        Vec3d modifiedValue = eventResult.getValue();

        // If the event is canceled and a modified value is provided, update the deltas
        if (eventResult.isCanceled()) {
            if (modifiedValue != null) {
                deltaX = modifiedValue.x;
                deltaY = modifiedValue.y;
                deltaZ = modifiedValue.z;
            } else {
                return this.isColliding;
            }
        }

        // Store the original deltas after potential modification
        double originalDeltaXModified = deltaX;
        double originalDeltaYModified = deltaY;
        double originalDeltaZModified = deltaZ;

        // Update the bounding box based on the modified deltas
        BoundingBox updatedBoundingBox = this.getBoundingBox().updateByDelta(deltaX, deltaY, deltaZ);

        // Move the entity based on the updated bounding box and deltas
        if (this.noClip) {
            this.x += deltaX;
            this.y += deltaY;
            this.z += deltaZ;
            this.onMoved();
        } else {
            this.moveWithCollision(updatedBoundingBox, deltaX, deltaY, deltaZ, originalDeltaXModified, originalDeltaYModified, originalDeltaZModified);
            this.onMoved();
        }

        return this.isColliding;
    }

    /**
     * Moves the entity with collision detection and response.
     *
     * @param ext Bounding box of the entity
     * @param dx Change in x-coordinate
     * @param dy Change in y-coordinate
     * @param dz Change in z-coordinate
     * @param oDx Original change in x-coordinate
     * @param oDy Original change in y-coordinate
     * @param oDz Original change in z-coordinate
     */
    private void moveWithCollision(BoundingBox ext, double dx, double dy, double dz, double oDx, double oDy, double oDz) {
        // Get list of bounding boxes the entity collides with
        List<BoundingBox> boxes = this.world.collide(ext, false);

        BoundingBox pBox = this.getBoundingBox(); // Get the entity's bounding box

        this.isColliding = false;
        this.isCollidingY = false;

        // Check collision and update y-coordinate
        for (BoundingBox box : boxes) {
            double dy2 = BoundingBoxUtils.clipYCollide(box, pBox, dy);
            this.isColliding |= dy != dy2;
            this.isCollidingY |= dy != dy2;
            dy = dy2;
        }

        // Update the y-coordinate of the bounding box
        pBox.min.add(0.0f, dy, 0.0f);
        pBox.max.add(0.0f, dy, 0.0f);
        pBox.update();

        this.isCollidingX = false;

        // Check collision and update x-coordinate
        for (BoundingBox box : boxes) {
            double dx2 = BoundingBoxUtils.clipXCollide(box, pBox, dx);
            this.isColliding |= dx != dx2;
            this.isCollidingX |= dx != dx2;
            dx = dx2;
        }

        // Update the x-coordinate of the bounding box
        pBox.min.add(dx, 0.0f, 0.0f);
        pBox.max.add(dx, 0.0f, 0.0f);
        pBox.update();

        this.isCollidingZ = false;

        // Check collision and update z-coordinate
        for (BoundingBox box : boxes) {
            double dz2 = BoundingBoxUtils.clipZCollide(box, pBox, dz);
            this.isColliding |= dz != dz2;
            this.isCollidingZ |= dz != dz2;
            dz = dz2;
        }

        // Update the z-coordinate of the bounding box
        pBox.min.add(0.0f, 0.0f, dz);
        pBox.max.add(0.0f, 0.0f, dz);
        pBox.update();

        // Check if entity is on the ground
        this.onGround = oDy != dy && oDy < 0.0f;

        // Reset velocity if there was a collision in x-coordinate
        if (oDx != dx) {
            this.velocityX = 0.0f;
        }

        // Reset fall distance if entity is moving upwards
        if (dy >= 0) {
            this.fallDistance = 0.0F;
        }

        // Handle collision responses and update fall distance
        if (oDy != dy) {
            this.hitGround();
            this.fallDistance = 0.0F;
            this.velocityY = 0.0f;
        } else if (dy < 0) {
            this.fallDistance -= dy;
        }

        // Reset velocity if there was a collision in z-coordinate
        if (oDz != dz) {
            this.velocityZ = 0.0f;
        }

        // Update entity's position
        this.x = (pBox.min.x + pBox.max.x) / 2.0f;
        this.y = pBox.min.y;
        this.z = (pBox.min.z + pBox.max.z) / 2.0f;
    }

    /**
     * Handles the entity movement.
     */
    protected void onMoved() {
        // Impl reasons
    }

    /**
     * Handles the entity ground hit.
     */
    protected void hitGround() {
        // Impl reasons
    }

    /**
     * @return true if the entity is in the void, false otherwise.
     */
    public boolean isInVoid() {
        return this.y < World.WORLD_DEPTH - 64;
    }

    public BoundingBox getBoundingBox() {
        return this.getBoundingBox(this.getSize());
    }

    @ApiStatus.OverrideOnly
    public BoundingBox getBoundingBox(EntitySize size) {
        return Entity.getBoundingBox(this.getPosition(), size);
    }

    public static BoundingBox getBoundingBox(Vec3d pos, EntitySize size) {
        double x1 = pos.x - size.width() / 2;
        double y1 = pos.y;
        double z1 = pos.z - size.width() / 2;
        double x2 = pos.x + size.width() / 2;
        double y2 = pos.y + size.height();
        double z2 = pos.z + size.width() / 2;
        return new BoundingBox(new Vec3d(x1, y1, z1), new Vec3d(x2, y2, z2));
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getXRot() {
        return this.xRot;
    }

    public void setXRot(float xRot) {
        this.xRot = xRot;
    }

    public float getYRot() {
        return this.yRot;
    }

    public void setYRot(float yRot) {
        this.yRot = Mth.clamp(yRot, -90, 90);
    }

    public Vec3d getPosition() {
        return new Vec3d(this.x, this.y, this.z);
    }

    @ApiStatus.Internal
    public void setPosition(Vec3d position) {
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
        this.ox = position.x;
        this.oy = position.y;
        this.oz = position.z;
    }

    @ApiStatus.Internal
    public void setPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.ox = x;
        this.oy = y;
        this.oz = z;
    }

    public BlockPos getBlockPos() {
        return new BlockPos(this.x, this.y, this.z);
    }

    public Vec2f getRotation() {
        return new Vec2f(this.xRot, this.yRot);
    }

    public Vec3d getLookVector() {
        // Calculate the direction vector
        Vec3d direction = new Vec3d();

        this.yRot = Mth.clamp(this.yRot, -89.9F, 89.9F);
        direction.x = (float) (Math.cos(Math.toRadians(this.yRot)) * Math.sin(Math.toRadians(this.xRot)));
        direction.z = (float) (Math.cos(Math.toRadians(this.yRot)) * Math.cos(Math.toRadians(this.xRot)));
        direction.y = (float) (Math.sin(Math.toRadians(this.yRot)));

        // Normalize the direction vector
        direction.nor();
        return direction;
    }

    public void setRotation(Vec2f position) {
        this.xRot = position.x;
        this.yRot = Mth.clamp(position.y, -90, 90);
    }

    public Vec3d getVelocity() {
        return new Vec3d(this.velocityX, this.velocityY, this.velocityZ);
    }

    public void setVelocity(Vec3d velocity) {
        this.velocityX = velocity.x;
        this.velocityY = velocity.y;
        this.velocityZ = velocity.z;
    }

    @ApiStatus.OverrideOnly
    public void onPrepareSpawn(MapType spawnData) {

    }

    /**
     * Teleports the entity to the specified coordinates.
     *
     * @param x the x-coordinate to teleport to
     * @param y the y-coordinate to teleport to
     * @param z the z-coordinate to teleport to
     */
    public void teleportTo(int x, int y, int z) {
        this.setPosition(x + 0.5, y, z + 0.5);
    }

    /**
     * Teleports the player to the specified coordinates.
     *
     * @param x the x-coordinate to teleport to
     * @param y the y-coordinate to teleport to
     * @param z the z-coordinate to teleport to
     */
    public void teleportTo(double x, double y, double z) {
        this.setPosition(x, y, z);
    }

    public World getWorld() {
        return this.world;
    }

    public int getId() {
        return this.id;
    }

    @ApiStatus.Internal
    public void setId(int id) {
        this.id = id;
    }

    public float getGravity() {
        return this.gravity;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public void rotate(Vec2f rotation) {
        this.xRot = this.xRot + rotation.x;
        this.yRot = Mth.clamp(this.yRot + rotation.y, -90, 90);
    }

    public void rotate(float x, float y) {
        this.xRot += x;
        this.yRot += y;
    }

    public EntityType<?> getType() {
        return this.type;
    }

    /**
     * Retrieves the location of the entity.
     *
     * @return
     */
    @Override
    public @NotNull Location getLocation() {
        return new Location(this.world, this.x, this.y, this.z, this.xRot, this.yRot);
    }

    /**
     * Retrieves the name of the entity.
     * This is official name of the entity, not the explicit public name.
     *
     * @return the name of the entity
     */
    @Override
    public String getName() {
        Identifier typeId = this.getType().getId();

        // If the type ID is null, return a default null object translation
        if (typeId == null) return "NULL";

        // Generate a display name based on the entity's type ID
        return LanguageBootstrap.translate("%s.entity.%s.name".formatted(
                typeId.namespace(),
                typeId.path().replace('/', '.')
        ));
    }

    /**
     * Retrieves the public name for the entity.
     * This is the name that is publicly visible.
     * For example, in the chat (for players).
     *
     * @return the public name for the entity
     */
    @Override
    public @Nullable String getPublicName() {
        return this.getDisplayName().getText();
    }

    /**
     * Retrieves the display name for the entity.
     * If a custom name is set, it returns the custom name.
     * Otherwise, it uses the entity type translation.
     *
     * @return the display name for the entity
     */
    @Override
    public TextObject getDisplayName() {
        // Check if a custom name is set and return it if available
        if (this.customName != null) return this.customName;

        Identifier typeId = this.getType().getId();

        // If the type ID is null, return a default null object translation
        if (typeId == null) return Translations.NULL_OBJECT;

        // Generate a display name based on the entity's type ID
        return TextObject.translation("%s.entity.%s.name".formatted(
                typeId.namespace(),
                typeId.path().replace('/', '.')
        ));
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public void sendMessage(@NotNull String message) {

    }

    @Override
    public void sendMessage(@NotNull TextObject component) {

    }

    @Override
    public boolean hasExplicitPermission(@NotNull Permission permission) {
        return false;
    }

    @Override
    public boolean isAdmin() {
        return false;
    }

    public AttributeMap getAttributes() {
        return this.attributes;
    }

    public RandomSource getRng() {
        return this.random;
    }

    /**
     * Gets the pipeline data for this entity.
     * This will be sent to the client when the entity calls {@link #sendPipeline()}.
     *
     * @return the pipeline
     */
    public MapType getPipeline() {
        MapType copy = this.pipeline;
        this.pipeline = new MapType();
        return copy;
    }

    /**
     * Sends the pipeline data for this entity to the client.
     */
    public void sendPipeline() {
        if (this.world instanceof ServerWorld serverWorld) {
            // Send the entity to all tracking players
            serverWorld.sendAllTracking((int) this.x, (int) this.y, (int) this.z, new S2CEntityPipeline(this.getId(), this.getPipeline()));
        }
    }

    /**
     * Called when a pipeline packet is received for this entity.
     *
     * @param pipeline the pipeline data
     */
    public void onPipeline(MapType pipeline) {

    }

    /**
     * Teleports the player to the target entity
     *
     * @param target the target entity
     */
    public void teleportTo(Entity target) {
        this.teleportTo(target.getX(), target.getY(), target.getZ());
    }

    /**
     * Teleports the player to the target position
     *
     * @param target the target position
     */
    public void teleportTo(Vec3d target) {
        this.teleportTo(target.x, target.y, target.z);
    }

    public void teleportDimension(Vec3d position, ServerWorld world) {
        this.getWorld().despawn(this);
        this.teleportTo(position);
        world.spawn(this);
    }
}
