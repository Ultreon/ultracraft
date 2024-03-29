package com.ultreon.craft.util;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3i;

import static com.ultreon.craft.world.World.CHUNK_HEIGHT;
import static com.ultreon.craft.world.World.CHUNK_SIZE;

@SuppressWarnings("UnqualifiedStaticUsage")
public class WorldRayCaster {
	private static final Vec3i abs = new Vec3i();
	private static final Vec3i origin = new Vec3i();
	private static final Vec3i loc = new Vec3i();
	private static final Vec3d dir = new Vec3d();
	private static final Vec3d ext = new Vec3d();
	private static final Vec3d intersection = new Vec3d();
	private static final Vec3d local = new Vec3d();
	private static final BoundingBox box = new BoundingBox();

	public static HitResult rayCast(World map) {
		return rayCast(new HitResult(), map);
	}

	// sources : https://www.researchgate.net/publication/2611491_A_Fast_Voxel_Traversal_Algorithm_for_Ray_Tracing
	// and https://www.gamedev.net/blogs/entry/2265248-voxel-traversal-algorithm-ray-casting/
	public static HitResult rayCast(HitResult result, World world) {
		return rayCast(result, world, BlockPredicate.NON_FLUID);
	}

	// sources : https://www.researchgate.net/publication/2611491_A_Fast_Voxel_Traversal_Algorithm_for_Ray_Tracing
	// and https://www.gamedev.net/blogs/entry/2265248-voxel-traversal-algorithm-ray-casting/
	public static HitResult rayCast(HitResult result, World world, BlockPredicate predicate) {
		result.collide = false;

		final Ray ray = result.ray;

		dir.set(ray.direction.x > 0 ? 1 : -1,
				ray.direction.y > 0 ? 1 : -1,
				ray.direction.z > 0 ? 1 : -1);
		ext.set(ray.direction.x > 0 ? 1 : 0,
				ray.direction.y > 0 ? 1 : 0,
				ray.direction.z > 0 ? 1 : 0);

		Chunk chunk = null;

		origin.set((int) Math.floor(ray.origin.x), (int) Math.floor(ray.origin.y), (int) Math.floor(ray.origin.z));
		abs.set(origin);

		final double nextX = abs.x + ext.x;
		final double nextY = abs.y + ext.y;
		final double nextZ = abs.z + ext.z;

		double tMaxX = ray.direction.x == 0 ? Float.MAX_VALUE : (nextX - ray.origin.x) / ray.direction.x;
		double tMaxY = ray.direction.y == 0 ? Float.MAX_VALUE : (nextY - ray.origin.y) / ray.direction.y;
		double tMaxZ = ray.direction.z == 0 ? Float.MAX_VALUE : (nextZ - ray.origin.z) / ray.direction.z;


		final double tDeltaX = ray.direction.x == 0 ? Float.MAX_VALUE : dir.x / ray.direction.x;
		final double tDeltaY = ray.direction.y == 0 ? Float.MAX_VALUE : dir.y / ray.direction.y;
		final double tDeltaZ = ray.direction.z == 0 ? Float.MAX_VALUE : dir.z / ray.direction.z;


		for(;;){
			if(abs.dst(origin) > result.distanceMax) return result;

			if(chunk == null || chunk.isDisposed()){
				chunk = world.getChunkAt(abs.x, abs.y, abs.z);
				if(chunk == null || chunk.isDisposed()) return result;
			}

			loc.set(abs).sub(chunk.getOffset().x, chunk.getOffset().y, chunk.getOffset().z);

			if (loc.y >= World.WORLD_HEIGHT && dir.y >= 0) return result;
			if (loc.y < 0 && dir.y < 0) return result;

			if(loc.x < 0 || loc.y < 0 || loc.z < 0 ||
					loc.x >= CHUNK_SIZE || loc.y >= CHUNK_HEIGHT || loc.z >= CHUNK_SIZE){
				chunk = null;
				continue;
			}

			Block block = chunk.get(loc.cpy());
			if(block != null && !block.isAir() && predicate.test(block)) {
				box.set(box.min.set(abs.x, abs.y, abs.z), box.max.set(abs.x+1,abs.y+1,abs.z+1));
				box.update();

				if(Intersector.intersectRayBounds(ray, box, intersection)){
					double dst = intersection.dst(ray.origin);
					result.collide = true;
					result.distance = dst;
					result.position.set(intersection);
					result.pos.set(abs);
					result.block = block;

					computeFace(result);
				}

				return result;
			}

			// increment
			if(tMaxX < tMaxY){
				if(tMaxX < tMaxZ){
					tMaxX += tDeltaX;
					abs.x += (int) dir.x;
				}else{
					tMaxZ += tDeltaZ;
					abs.z += (int) dir.z;
				}
			}else if(tMaxY < tMaxZ){
				tMaxY += tDeltaY;
				abs.y += (int) dir.y;
			}else{
				tMaxZ += tDeltaZ;
				abs.z += (int) dir.z;
			}
		}
	}


	private static void computeFace(HitResult result) {
		// compute face
		local.set(result.position)
			.sub(result.pos.x,result.pos.y,result.pos.z)
			.sub(.5f);

		double absX = Math.abs(local.x);
		double absY = Math.abs(local.y);
		double absZ = Math.abs(local.z);

		if(absY > absX){
			if(absZ > absY){
				// face Z+
				result.normal.set(0,0,local.z < 0 ? -1 : 1);
			}else{
				result.normal.set(0,local.y < 0 ? -1 : 1,0);
			}
		}else{
			if(absZ > absX){
				result.normal.set(0,0,local.z < 0 ? -1 : 1);
			}else{
				result.normal.set(local.x < 0 ? -1 : 1,0,0);
			}
		}

		result.next.set(result.pos).add(
				(int) Math.floor(result.normal.x),
				(int) Math.floor(result.normal.y),
				(int) Math.floor(result.normal.z));
	}

}