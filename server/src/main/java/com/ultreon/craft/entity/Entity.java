package com.ultreon.craft.entity;

import com.ultreon.craft.api.commands.CommandSender;
import com.ultreon.craft.api.commands.perms.Permission;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.entity.util.EntitySize;
import com.ultreon.craft.events.EntityEvents;
import com.ultreon.craft.events.api.ValueEventResult;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.server.util.Utils;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.text.Translations;
import com.ultreon.craft.util.BoundingBox;
import com.ultreon.craft.util.BoundingBoxUtils;
import com.ultreon.craft.util.Identifier;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.Location;
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

    public Entity(EntityType<? extends Entity> entityType, World world) {
        this.type = entityType;
        this.world = world;

        this.setupAttributes();
    }

    protected void setupAttributes() {

    }

    public static @NotNull Entity loadFrom(World world, MapType data) {
        Identifier typeId = Identifier.parse(data.getString("type"));
        EntityType<?> type = Registries.ENTITY_TYPE.get(typeId);
        Entity entity = type.create(world);

        entity.id = data.getInt("id");

        entity.loadWithPos(data);
        return entity;
    }

    public void loadWithPos(MapType data) {
        MapType position = data.getMap("Position", new MapType());
        this.x = position.getDouble("x", this.x);
        this.y = position.getDouble("y", this.y);
        this.z = position.getDouble("z", this.z);

        this.load(data);
    }

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

    public void tick() {
        if (!this.noGravity) {
            if (this.isAffectedByFluid() && this.swimUp) {
                this.swimUp = false;
            } else {
                this.velocityY -= this.gravity;
            }
        }

        this.move();

        if (this.isAffectedByFluid()) {
            this.velocityX *= 0.56f;
            this.velocityY *= 0.56f;
            this.velocityZ *= 0.56f;
        } else {
            this.velocityX *= 0.6F;
            this.velocityY *= this.noGravity ? 0.6F : this.drag;
            this.velocityZ *= 0.6F;
        }

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

    private boolean isInWater() {
        return this.world.get(this.getBlockPos()) == Blocks.WATER;
    }

    protected void swimUp() {
        if (this.isAffectedByFluid()) {
            this.swimUp = true;
        }

        if (!this.wasInFluid && this.isAffectedByFluid()) {
            this.wasInFluid = true;
            return;
        }
        if (!this.wasInFluid || this.isAffectedByFluid()) return;
        this.wasInFluid = false;
        this.velocityY = 0.3;
    }

    public boolean move(double deltaX, double deltaY, double deltaZ) {
        double dx = deltaX, dy = deltaY, dz = deltaZ;

        double absX = Math.abs(deltaX);
        double absY = Math.abs(deltaY);
        double absZ = Math.abs(deltaZ);

        if (absX < 0.001 && absY < 0.001 && absZ < 0.001) {
            return this.isColliding;
        }

        ValueEventResult<Vec3d> eventResult = EntityEvents.MOVE.factory().onEntityMove(this, deltaX, deltaY, deltaZ);
        Vec3d value = eventResult.getValue();
        if (eventResult.isCanceled()) {
            if (value != null) {
                dx = value.x;
                dy = value.y;
                dz = value.z;
            } else {
                return this.isColliding;
            }
        }

        double oDx = dx;
        double oDy = dy;
        double oDz = dz;

        BoundingBox ext = this.getBoundingBox();
        if (dx < 0) ext.min.x += dx;
        else ext.max.x += dx;
        if (dy < 0) ext.min.y += dy;
        else ext.max.y += dy;
        if (dz < 0) ext.min.z += dz;
        else ext.max.z += dz;
        ext.update();
        if (this.noClip) {
            this.x += dx;
            this.y += dy;
            this.z += dz;
            this.onMoved();
        } else {
            this.move0(ext, dx, dy, dz, oDx, oDy, oDz);
            this.onMoved();
        }

        return this.isColliding;
    }

    private void move0(BoundingBox ext, double dx, double dy, double dz, double oDx, double oDy, double oDz) {
        List<BoundingBox> boxes = this.world.collide(ext, false);
        BoundingBox pBox = this.getBoundingBox();
        this.isColliding = false;
        this.isCollidingY = false;
        for (BoundingBox box : boxes) {
            double dy2 = BoundingBoxUtils.clipYCollide(box, pBox, dy);
            this.isColliding |= dy != dy2;
            this.isCollidingY |= dy != dy2;
            dy = dy2;
        }
        pBox.min.add(0.0f, dy, 0.0f);
        pBox.max.add(0.0f, dy, 0.0f);
        pBox.update();
        this.isCollidingX = false;
        for (BoundingBox box : boxes) {
            double dx2 = BoundingBoxUtils.clipXCollide(box, pBox, dx);
            this.isColliding |= dx != dx2;
            this.isCollidingX |= dx != dx2;
            dx = dx2;
        }
        pBox.min.add(dx, 0.0f, 0.0f);
        pBox.max.add(dx, 0.0f, 0.0f);
        pBox.update();
        this.isCollidingZ = false;
        for (BoundingBox box : boxes) {
            double dz2 = BoundingBoxUtils.clipZCollide(box, pBox, dz);
            this.isColliding |= dz != dz2;
            this.isCollidingZ |= dz != dz2;
            dz = dz2;
        }
        pBox.min.add(0.0f, 0.0f, dz);
        pBox.max.add(0.0f, 0.0f, dz);
        pBox.update();
        this.onGround = oDy != dy && oDy < 0.0f;
        if (oDx != dx) {
            this.velocityX = 0.0f;
        }

        if (dy >= 0) this.fallDistance = 0.0F;

        if (oDy != dy) {
            this.hitGround();
            this.fallDistance = 0.0F;
            this.velocityY = 0.0f;
        } else if (dy < 0) {
            this.fallDistance -= dy;
        }
        if (oDz != dz) {
            this.velocityZ = 0.0f;
        }
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
     * @deprecated the void doesn't exist anymore.
     */
    @Deprecated(forRemoval = true)
    public boolean isInVoid() {
        return false;
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

    public void setPosition(Vec3d position) {
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
        this.ox = position.x;
        this.oy = position.y;
        this.oz = position.z;
    }

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
        direction.x = (float)(Math.cos(Math.toRadians(this.yRot)) * Math.sin(Math.toRadians(this.xRot)));
        direction.z = (float)(Math.cos(Math.toRadians(this.yRot)) * Math.cos(Math.toRadians(this.xRot)));
        direction.y = (float)(Math.sin(Math.toRadians(this.yRot)));

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

    @Override
    public @NotNull Location getLocation() {
        return new Location(this.world, this.x, this.y, this.z, this.xRot, this.yRot);
    }

    @Override
    public String getName() {
        return this.getDisplayName().getText();
    }

    @Override
    public @Nullable String getPublicName() {
        return null;
    }

    @Override
    public TextObject getDisplayName() {
        if (this.customName != null) return this.customName;
        Identifier id1 = this.getType().getId();
        if (id1 == null) return Translations.NULL_OBJECT;
        return TextObject.translation("%s.entity.%s.name".formatted(
                id1.namespace(),
                id1.path().replace('/', '.')
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

    public MapType getPipeline() {
        MapType copy = this.pipeline;
        this.pipeline = new MapType();
        return copy;
    }

    public void onPipeline(MapType pipeline) {

    }
}
