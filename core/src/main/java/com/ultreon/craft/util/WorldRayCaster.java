package com.ultreon.craft.util;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.world.CompletedChunk;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3i;

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
	public static HitResult rayCast(HitResult result, World map) {
		result.collide = false;

		final Ray ray = result.ray;

		dir.set(ray.direction.x > 0 ? 1 : -1,
				ray.direction.y > 0 ? 1 : -1,
				ray.direction.z > 0 ? 1 : -1);
		ext.set(ray.direction.x > 0 ? 1 : 0,
				ray.direction.y > 0 ? 1 : 0,
				ray.direction.z > 0 ? 1 : 0);

		CompletedChunk chunk = null;

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

			if(chunk == null){
				Chunk rawChunk = map.getChunkAt(abs.x, abs.y, abs.z);
				if (rawChunk instanceof CompletedChunk) {
					CompletedChunk chunk1 = (CompletedChunk) rawChunk;
					chunk = chunk1;
				}
				if(chunk == null) return result;
			}
			loc.set(abs).sub((int) chunk.getOffset().x, (int) chunk.getOffset().y, (int) chunk.getOffset().z);
			if(loc.x < 0 || loc.y < 0 || loc.z < 0 ||
					loc.x >= chunk.size || loc.y >= chunk.height || loc.z >= chunk.size){
				chunk = null;
				continue;
			}
			Block block = chunk.get(loc.cpy());
			if(block != null && block != Blocks.AIR){
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
					abs.x += dir.x;
				}else{
					tMaxZ += tDeltaZ;
					abs.z += dir.z;
				}
			}else if(tMaxY < tMaxZ){
				tMaxY += tDeltaY;
				abs.y += dir.y;
			}else{
				tMaxZ += tDeltaZ;
				abs.z += dir.z;
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