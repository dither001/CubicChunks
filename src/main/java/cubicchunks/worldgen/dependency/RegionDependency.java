/*
 *  This file is part of Cubic Chunks Mod, licensed under the MIT License (MIT).
 *
 *  Copyright (c) 2015 contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package cubicchunks.worldgen.dependency;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import cubicchunks.util.CubeCoords;
import cubicchunks.world.ICubeCache;
import cubicchunks.world.cube.Cube;
import cubicchunks.world.dependency.Dependency;
import cubicchunks.world.dependency.DependencyManager;
import cubicchunks.world.dependency.Requirement;
import cubicchunks.worldgen.GeneratorStage;
import net.minecraft.util.math.Vec3i;

/*
 * TODO: Commenting
 */
/**
 * Specifies the dependency of a cube to other cubes in its vicinity during terrain generation.
 * @see DependencyManager
 */
public class RegionDependency implements Dependency {

	private GeneratorStage targetStage;

	private int xLow;
	private int xHigh;
	private int yLow;
	private int yHigh;
	private int zLow;
	private int zHigh;


	public RegionDependency(GeneratorStage targetStage, int radius) {

		this.targetStage = targetStage;

		this.xLow = -radius;
		this.xHigh = radius;
		this.yLow = -radius;
		this.yHigh = radius;
		this.zLow = -radius;
		this.zHigh = radius;
	}
	
	public RegionDependency(GeneratorStage targetStage, Vec3i relA, Vec3i relB) {

		this.targetStage = targetStage;

		this.xLow = Math.min(relA.getX(), relB.getX());
		this.xHigh = Math.max(relA.getX(), relB.getX());
		this.yLow = Math.min(relA.getY(), relB.getY());
		this.yHigh = Math.max(relA.getY(), relB.getY());
		this.zLow = Math.min(relA.getZ(), relB.getZ());
		this.zHigh = Math.max(relA.getZ(), relB.getZ());
	}

	public boolean update(DependencyManager manager, DependentCube dependentCube, Cube requiredCube) {
		return !requiredCube.getCurrentStage().precedes(targetStage);
	}

	public Collection<Requirement> getRequirements(Cube cube) {

		Set<Requirement> requirements = new HashSet<>();

		int cubeX = cube.getX();
		int cubeY = cube.getY();
		int cubeZ = cube.getZ();

		for (int x = this.xLow; x <= this.xHigh; ++x) {
			for (int y = this.yLow; y <= this.yHigh; ++y) {
				for (int z = this.zLow; z <= this.zHigh; ++z) {
					if (x != 0 || y != 0 || z != 0) {
						CubeCoords coords = new CubeCoords(cubeX + x, cubeY + y, cubeZ + z);
						requirements.add(new Requirement(coords, targetStage));
					}
				}
			}
		}

		return requirements;
	}

	/**
	 * Returns true iff the given cube's dependencies defined by this object are satisfied.
	 *
	 * @param cube The cube for which satisfaction is to be checked.
	 * @param cubeProvider A cube provider granting access to all cubes of the world.
	 * @return True iff the cube's dependencies defined by this object are satisfied.
	 */
	public boolean isSatisfied(Cube cube, ICubeCache cubeProvider) {
		// For this dependency to be satisfied, all required cubes must be loaded and their stage must not precede the target stage.
		for (Requirement requirement : this.getRequirements(cube)) {
			if (!cubeProvider.cubeExists(requirement.getCoords()) || cubeProvider.getCube(requirement.getCoords()).getCurrentStage().precedes(targetStage)) {
				return false;
			}
		}
		return true;
	}
}
