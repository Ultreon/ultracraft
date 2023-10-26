/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *   http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.ultreon.craft.util;

import com.ultreon.libs.commons.v0.vector.Vec3d;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** Encapsulates an axis aligned bounding box represented by a minimum and a maximum Vector. Additionally you can query for the
 * bounding box's center, dimensions and corner points.
 *
 * @author badlogicgames@gmail.com, Xoppa */
@SuppressWarnings({"ManualMinMaxCalculation", "UnusedReturnValue", "GrazieInspection", "SpellCheckingInspection", "BooleanMethodIsAlwaysInverted"})
public class BoundingBox implements Serializable {
	@Serial
	private static final long serialVersionUID = -1286036817192127343L;

	/** Minimum vector. All XYZ components should be inferior to corresponding {@link #max} components. Call {@link #update()} if
	 * you manually change this vector. */
	public final Vec3d min = new Vec3d();
	/** Maximum vector. All XYZ components should be superior to corresponding {@link #min} components. Call {@link #update()} if
	 * you manually change this vector. */
	public final Vec3d max = new Vec3d();

	private final Vec3d cnt = new Vec3d();
	private final Vec3d dim = new Vec3d();

	/** @param out The {@link Vec3d} to receive the center of the bounding box.
	 * @return The vector specified with the out argument. */
	public Vec3d getCenter (Vec3d out) {
		return out.set(this.cnt);
	}

	public double getCenterX () {
		return this.cnt.x;
	}

	public double getCenterY () {
		return this.cnt.y;
	}

	public double getCenterZ () {
		return this.cnt.z;
	}

	public Vec3d getCorner000 (final Vec3d out) {
		return out.set(this.min.x, this.min.y, this.min.z);
	}

	public Vec3d getCorner001 (final Vec3d out) {
		return out.set(this.min.x, this.min.y, this.max.z);
	}

	public Vec3d getCorner010 (final Vec3d out) {
		return out.set(this.min.x, this.max.y, this.min.z);
	}

	public Vec3d getCorner011 (final Vec3d out) {
		return out.set(this.min.x, this.max.y, this.max.z);
	}

	public Vec3d getCorner100 (final Vec3d out) {
		return out.set(this.max.x, this.min.y, this.min.z);
	}

	public Vec3d getCorner101 (final Vec3d out) {
		return out.set(this.max.x, this.min.y, this.max.z);
	}

	public Vec3d getCorner110 (final Vec3d out) {
		return out.set(this.max.x, this.max.y, this.min.z);
	}

	public Vec3d getCorner111 (final Vec3d out) {
		return out.set(this.max.x, this.max.y, this.max.z);
	}

	/** @param out The {@link Vec3d} to receive the dimensions of this bounding box on all three axis.
	 * @return The vector specified with the out argument */
	public Vec3d getDimensions (final Vec3d out) {
		return out.set(this.dim);
	}

	public double getWidth () {
		return this.dim.x;
	}

	public double getHeight () {
		return this.dim.y;
	}

	public double getDepth () {
		return this.dim.z;
	}

	/** @param out The {@link Vec3d} to receive the minimum values.
	 * @return The vector specified with the out argument */
	public Vec3d getMin (final Vec3d out) {
		return out.set(this.min);
	}

	/** @param out The {@link Vec3d} to receive the maximum values.
	 * @return The vector specified with the out argument */
	public Vec3d getMax (final Vec3d out) {
		return out.set(this.max);
	}

	/** Constructs a new bounding box with the minimum and maximum vector set to zeros. */
	public BoundingBox () {
		this.clr();
	}

	/** Constructs a new bounding box from the given bounding box.
	 *
	 * @param bounds The bounding box to copy */
	public BoundingBox (BoundingBox bounds) {
		this.set(bounds);
	}

	/** Constructs the new bounding box using the given minimum and maximum vector.
	 *
	 * @param minimum The minimum vector
	 * @param maximum The maximum vector */
	public BoundingBox (Vec3d minimum, Vec3d maximum) {
		this.set(minimum, maximum);
	}

	/** Sets the given bounding box.
	 *
	 * @param bounds The bounds.
	 * @return This bounding box for chaining. */
	public BoundingBox set (BoundingBox bounds) {
		return this.set(bounds.min, bounds.max);
	}

	/** Sets the given minimum and maximum vector.
	 *
	 * @param minimum The minimum vector
	 * @param maximum The maximum vector
	 * @return This bounding box for chaining. */
	public BoundingBox set (Vec3d minimum, Vec3d maximum) {
		this.min.set(minimum.x < maximum.x ? minimum.x : maximum.x, minimum.y < maximum.y ? minimum.y : maximum.y, minimum.z < maximum.z ? minimum.z : maximum.z);
		this.max.set(minimum.x > maximum.x ? minimum.x : maximum.x, minimum.y > maximum.y ? minimum.y : maximum.y, minimum.z > maximum.z ? minimum.z : maximum.z);
		this.update();
		return this;
	}

	/** Should be called if you modify {@link #min} and/or {@link #max} vectors manually. */
	public void update () {
		this.cnt.set(this.min).add(this.max).mul(0.5);
		this.dim.set(this.max).sub(this.min);
	}

	/** Sets the bounding box minimum and maximum vector from the given points.
	 *
	 * @param points The points.
	 * @return This bounding box for chaining. */
	public BoundingBox set (Vec3d[] points) {
		this.inf();
		for (Vec3d l_point : points)
			this.ext(l_point);
		return this;
	}

	/** Sets the bounding box minimum and maximum vector from the given points.
	 *
	 * @param points The points.
	 * @return This bounding box for chaining. */
	public BoundingBox set (List<Vec3d> points) {
		this.inf();
		for (Vec3d l_point : points)
			this.ext(l_point);
		return this;
	}

	/** Sets the minimum and maximum vector to positive and negative infinity.
	 *
	 * @return This bounding box for chaining. */
	public BoundingBox inf () {
		this.min.set(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
		this.max.set(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
		this.cnt.set(0, 0, 0);
		this.dim.set(0, 0, 0);
		return this;
	}

	/** Extends the bounding box to incorporate the given {@link Vec3d}.
	 * @param point The vector
	 * @return This bounding box for chaining. */
	public BoundingBox ext (Vec3d point) {
		return this.set(this.min.set(BoundingBox.min(this.min.x, point.x), BoundingBox.min(this.min.y, point.y), BoundingBox.min(this.min.z, point.z)), this.max.set(Math.max(this.max.x, point.x), Math.max(this.max.y, point.y), Math.max(this.max.z, point.z)));
	}

	/** Sets the minimum and maximum vector to zeros.
	 * @return This bounding box for chaining. */
	public BoundingBox clr () {
		return this.set(this.min.set(0, 0, 0), this.max.set(0, 0, 0));
	}

	/** Returns whether this bounding box is valid. This means that {@link #max} is greater than or equal to {@link #min}.
	 * @return True in case the bounding box is valid, false otherwise */
	public boolean isValid () {
		return this.min.x <= this.max.x && this.min.y <= this.max.y && this.min.z <= this.max.z;
	}

	/** Extends this bounding box by the given bounding box.
	 *
	 * @param a_bounds The bounding box
	 * @return This bounding box for chaining. */
	public BoundingBox ext (BoundingBox a_bounds) {
		return this.set(this.min.set(BoundingBox.min(this.min.x, a_bounds.min.x), BoundingBox.min(this.min.y, a_bounds.min.y), BoundingBox.min(this.min.z, a_bounds.min.z)), this.max.set(BoundingBox.max(this.max.x, a_bounds.max.x), BoundingBox.max(this.max.y, a_bounds.max.y), BoundingBox.max(this.max.z, a_bounds.max.z)));
	}

	/** Extends this bounding box by the given sphere.
	 *
	 * @param center Sphere center
	 * @param radius Sphere radius
	 * @return This bounding box for chaining. */
	public BoundingBox ext (Vec3d center, double radius) {
		return this.set(this.min.set(BoundingBox.min(this.min.x, center.x - radius), BoundingBox.min(this.min.y, center.y - radius), BoundingBox.min(this.min.z, center.z - radius)), this.max.set(BoundingBox.max(this.max.x, center.x + radius), BoundingBox.max(this.max.y, center.y + radius), BoundingBox.max(this.max.z, center.z + radius)));
	}

	/** Returns whether the given bounding box is contained in this bounding box.
	 * @param b The bounding box
	 * @return Whether the given bounding box is contained */
	public boolean contains (BoundingBox b) {
		return !this.isValid() || (this.min.x <= b.min.x && this.min.y <= b.min.y && this.min.z <= b.min.z && this.max.x >= b.max.x && this.max.y >= b.max.y && this.max.z >= b.max.z);
	}

	/** Returns whether the given bounding box is intersecting this bounding box (at least one point in).
	 * @param b The bounding box
	 * @return Whether the given bounding box is intersected */
	public boolean intersects (BoundingBox b) {
		if (!this.isValid()) return false;

		// test using SAT (separating axis theorem)

		double lx = Math.abs(this.cnt.x - b.cnt.x);
		double sumx = (this.dim.x / 2.0) + (b.dim.x / 2.0);

		double ly = Math.abs(this.cnt.y - b.cnt.y);
		double sumy = (this.dim.y / 2.0) + (b.dim.y / 2.0);

		double lz = Math.abs(this.cnt.z - b.cnt.z);
		double sumz = (this.dim.z / 2.0) + (b.dim.z / 2.0);

		return (lx <= sumx && ly <= sumy && lz <= sumz);

	}

	/**
	 * Returns whether the given bounding box is intersecting this bounding box (at least one point in).
	 *
	 * @param b The bounding box
	 * @return Whether the given bounding box is intersected
	 */
	public boolean intersectsExclusive(BoundingBox b) {
		if (!this.isValid()) return false;

		// test using SAT (separating axis theorem)

		double lx = Math.abs(this.cnt.x - b.cnt.x);
		double sumx = (this.dim.x / 2.0) + (b.dim.x / 2.0);

		double ly = Math.abs(this.cnt.y - b.cnt.y);
		double sumy = (this.dim.y / 2.0) + (b.dim.y / 2.0);

		double lz = Math.abs(this.cnt.z - b.cnt.z);
		double sumz = (this.dim.z / 2.0) + (b.dim.z / 2.0);

		return (lx < sumx && ly < sumy && lz < sumz);

	}

	/**
	 * Returns whether the given vector is contained in this bounding box.
	 *
	 * @param v The vector
	 * @return Whether the vector is contained or not.
	 */
	public boolean contains(Vec3d v) {
		return this.min.x <= v.x && this.max.x >= v.x && this.min.y <= v.y && this.max.y >= v.y && this.min.z <= v.z && this.max.z >= v.z;
	}

	@Override
	public String toString() {
		return "[" + this.min + "|" + this.max + "]";
	}

	/**
	 * Extends the bounding box by the given vector.
	 *
	 * @param x The x-coordinate
	 * @param y The y-coordinate
	 * @param z The z-coordinate
	 * @return This bounding box for chaining.
	 */
	public BoundingBox ext(double x, double y, double z) {
		return this.set(this.min.set(BoundingBox.min(this.min.x, x), BoundingBox.min(this.min.y, y), BoundingBox.min(this.min.z, z)), this.max.set(BoundingBox.max(this.max.x, x), BoundingBox.max(this.max.y, y), BoundingBox.max(this.max.z, z)));
	}

	static double min (final double a, final double b) {
		return a > b ? b : a;
	}

	static double max (final double a, final double b) {
		return a > b ? a : b;
	}
}
