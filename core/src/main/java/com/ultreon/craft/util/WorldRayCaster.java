package com.ultreon.craft.util;

import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.World;

public class WorldRayCaster {
	private static final GridPoint3 abs = new GridPoint3();
	private static final GridPoint3 origin = new GridPoint3();
	private static final GridPoint3 loc = new GridPoint3();
	private static final Vector3 dir = new Vector3();
	private static final Vector3 ext = new Vector3();
	private static final Vector3 intersection = new Vector3();
	private static final Vector3 local = new Vector3();
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

		Chunk chunk = null;

		origin.set(MathUtils.floor(ray.origin.x), MathUtils.floor(ray.origin.y), MathUtils.floor(ray.origin.z));
		abs.set(origin);

		final float nextX = abs.x + ext.x;
		final float nextY = abs.y + ext.y;
		final float nextZ = abs.z + ext.z;

		float tMaxX = ray.direction.x == 0 ? Float.MAX_VALUE : (nextX - ray.origin.x) / ray.direction.x;
		float tMaxY = ray.direction.y == 0 ? Float.MAX_VALUE : (nextY - ray.origin.y) / ray.direction.y;
		float tMaxZ = ray.direction.z == 0 ? Float.MAX_VALUE : (nextZ - ray.origin.z) / ray.direction.z;


		final float tDeltaX = ray.direction.x == 0 ? Float.MAX_VALUE : dir.x / ray.direction.x;
		final float tDeltaY = ray.direction.y == 0 ? Float.MAX_VALUE : dir.y / ray.direction.y;
		final float tDeltaZ = ray.direction.z == 0 ? Float.MAX_VALUE : dir.z / ray.direction.z;


		for(;;){
			if(abs.dst(origin) > result.distanceMax) return result;

			if(chunk == null){
				var rawChunk = map.getChunkAt(abs.x, abs.y, abs.z);
				if (rawChunk instanceof Chunk chunk1) chunk = chunk1;
				if(chunk == null) return result;
			}
			loc.set(abs).sub((int) chunk.offset.x, (int) chunk.offset.y, (int) chunk.offset.z);
			if(loc.x < 0 || loc.y < 0 || loc.z < 0 ||
					loc.x >= chunk.size || loc.y >= chunk.height || loc.z >= chunk.size){
				chunk = null;
				continue;
			}
			Block voxel = chunk.get(new GridPoint3(loc));
			if(voxel != null && voxel != Blocks.AIR){
				box.set(box.min.set(abs.x, abs.y, abs.z), box.max.set(abs.x+1,abs.y+1,abs.z+1));
				box.update();
				if(Intersector.intersectRayBounds(ray, box, intersection)){
					float dst = intersection.dst(ray.origin);
					result.collide = true;
					result.distance = dst;
					result.position.set(intersection);
					result.pos.set(abs);
					result.block = voxel;

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

		float absX = Math.abs(local.x);
		float absY = Math.abs(local.y);
		float absZ = Math.abs(local.z);

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
				MathUtils.floor(result.normal.x),
				MathUtils.floor(result.normal.y),
				MathUtils.floor(result.normal.z));
	}

}