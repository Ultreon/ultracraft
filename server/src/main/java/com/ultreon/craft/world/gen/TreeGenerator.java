package com.ultreon.craft.world.gen;

import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseConfig;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import com.ultreon.libs.commons.v0.vector.Vec2f;
import com.ultreon.libs.commons.v0.vector.Vec2i;
import it.unimi.dsi.fastutil.doubles.Double2BooleanFunction;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.ultreon.craft.world.World.CHUNK_SIZE;

public class TreeGenerator {
    @MonotonicNonNull
    private NoiseInstance noise;

    @NotNull
    private final NoiseConfig treeNoiseConfig;

    @NotNull
    private final DomainWarping domainWrapping;

    public TreeGenerator(@NotNull NoiseConfig treeNoiseConfig, @NotNull DomainWarping domainWrapping) {
        this.treeNoiseConfig = treeNoiseConfig;
        this.domainWrapping = domainWrapping;
    }

    public void create(long seed) {
        this.noise = this.treeNoiseConfig.create(seed);
    }

    public static List<Vec2i> findLocalMaxima(double[][] dataMatrix, int x, int z) {
        var maxima = new ArrayList<Vec2i>();
        for (var matrixX = 0; matrixX < dataMatrix.length; matrixX++) {
            for (var matrixZ = 0; matrixZ < dataMatrix[matrixX].length; matrixZ++) {
                var noiseVal = dataMatrix[matrixX][matrixZ];
                if (TreeGenerator.checkNeighbours(dataMatrix, matrixX, matrixZ, (neighbourNoise) -> neighbourNoise < noiseVal)) {
                    maxima.add(new Vec2i(x + matrixX, z + matrixZ));
                }

            }
        }
        return maxima;
    }

    private static boolean checkNeighbours(double[][] matrix, int x, int y, Double2BooleanFunction successCondition) {
        for (var direction : Neighbour8Direction.values()) {
            var dir = direction.vec();
            var newPost = new Vec2f(x + dir.x, y + dir.y);

            if (newPost.x < 0 || newPost.x >= matrix.length || newPost.y < 0 || newPost.y >= matrix[0].length) {
                continue;
            }

            if (successCondition.get(matrix[x + dir.x][y + dir.y])) {
                return false;
            }
        }
        return true;
    }

    public TreeData generateTreeData(Chunk chunkData) {
        var noise = this.noise;
        var treeData = new TreeData();
        var noiseMatrix = this.generateMatrix(chunkData, noise);
        treeData.treePositions = TreeGenerator.findLocalMaxima(noiseMatrix, chunkData.getOffset().x, chunkData.getOffset().z);

        return treeData;
    }

    private double[][] generateMatrix(Chunk chunkData, NoiseInstance noise) {
        var noiseMax = new double[CHUNK_SIZE][CHUNK_SIZE];
        var xMax = chunkData.getOffset().x + CHUNK_SIZE;
        var xMin = chunkData.getOffset().x;
        var zMax = chunkData.getOffset().z + CHUNK_SIZE;
        var zMin = chunkData.getOffset().z;
        int xIndex = 0, zIndex = 0;

        for (var x = xMin; x < xMax; x++) {
            for (var z = zMin; z < zMax; z++) {
                noiseMax[xIndex][zIndex] = this.domainWrapping.generateDomainNoise(x, z, noise);
                zIndex++;
            }

            xIndex++;
            zIndex = 0;
        }

        return noiseMax;
    }

}
