/*
BSD 3-Clause License

Copyright (c) 2019, Tomas
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package io.github.tomaso2468.rpgonline.noise;

import java.util.Random;

/**
 * An implementation of simplex noise.
 * @author Tomaso2468
 * @see se.liu.itn.stegu.simplexnoise.SimplexNoise
 */
public class SimplexNoise implements Noise {
	/**
	 * The size of the seeds effect on the noise offset. Setting this too high will cause artifacts to appear due to the limits of double values.
	 */
	public static final double SEED_AREA = 10000000;
	/**
	 * The X offset of the noise.
	 */
	public final double offsetX;
	/**
	 * The Y offset of the noise.
	 */
	public final double offsetY;
	/**
	 * The Z offset of the noise.
	 */
	public final double offsetZ;
	/**
	 * The W offset of the noise.
	 */
	public final double offsetW;
	/**
	 * The X scale factor of the noise.
	 */
	public final double scaleX;
	/**
	 * The Y scale factor of the noise.
	 */
	public final double scaleY;
	/**
	 * The Z scale factor of the noise.
	 */
	public final double scaleZ;
	/**
	 * The W scale factor of the noise.
	 */
	public final double scaleW;
	
	/**
	 * Constructs a new simplex noise implementation.
	 * @param seed The seed to use.
	 * @param offsetX The X offset of the noise.
	 * @param offsetY The Y offset of the noise.
	 * @param offsetZ The Z offset of the noise.
	 * @param offsetW The W offset of the noise.
	 * @param scaleX The X scale factor of the noise.
	 * @param scaleY The Y scale factor of the noise.
	 * @param scaleZ The Z scale factor of the noise.
	 * @param scaleW The W scale factor of the noise.
	 */
	public SimplexNoise(long seed, double offsetX, double offsetY, double offsetZ, double offsetW, double scaleX, double scaleY, double scaleZ, double scaleW) {
		super();
		Random r = new Random(seed);
		
		offsetX += r.nextDouble() * SEED_AREA;
		offsetY += r.nextDouble() * SEED_AREA;
		offsetZ += r.nextDouble() * SEED_AREA;
		offsetW += r.nextDouble() * SEED_AREA;
		
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
		this.offsetW = offsetW;
		
		
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.scaleZ = scaleZ;
		this.scaleW = scaleW;
	}
	
	/**
	 * Constructs a new simplex noise implementation.
	 * @param seed The seed to use.
	 * @param offsetX The X offset of the noise.
	 * @param offsetY The Y offset of the noise.
	 * @param offsetZ The Z offset of the noise.
	 * @param scaleX The X scale factor of the noise.
	 * @param scaleY The Y scale factor of the noise.
	 * @param scaleZ The Z scale factor of the noise.
	 */
	public SimplexNoise(long seed, double offsetX, double offsetY, double offsetZ, double scaleX, double scaleY, double scaleZ) {
		this(seed, offsetX, offsetY, offsetZ, 0, scaleX, scaleY, scaleZ, 1);
	}
	
	/**
	 * Constructs a new simplex noise implementation.
	 * @param seed The seed to use.
	 * @param offsetX The X offset of the noise.
	 * @param offsetY The Y offset of the noise.
	 * @param scaleX The X scale factor of the noise.
	 * @param scaleY The Y scale factor of the noise.
	 */
	public SimplexNoise(long seed, double offsetX, double offsetY, double scaleX, double scaleY) {
		this(seed, offsetX, offsetY, 0, scaleX, scaleY, 1);
	}
	
	/**
	 * Constructs a new simplex noise implementation.
	 * @param seed The seed to use.
	 * @param offsetX The X offset of the noise.
	 * @param scaleX The X scale factor of the noise.
	 */
	public SimplexNoise(long seed, double offsetX, double scaleX) {
		this(seed, offsetX, 0, scaleX, 1);
	}
	
	/**
	 * Constructs a new simplex noise implementation.
	 * @param seed The seed to use.
	 * @param scaleX The X scale factor of the noise.
	 */
	public SimplexNoise(long seed, double scaleX) {
		this(seed, 0, scaleX);
	}
	
	/**
	 * Constructs a new simplex noise implementation with a random seed.
	 * @param offsetX The X offset of the noise.
	 * @param offsetY The Y offset of the noise.
	 * @param offsetZ The Z offset of the noise.
	 * @param offsetW The W offset of the noise.
	 * @param scaleX The X scale factor of the noise.
	 * @param scaleY The Y scale factor of the noise.
	 * @param scaleZ The Z scale factor of the noise.
	 * @param scaleW The W scale factor of the noise.
	 */
	public SimplexNoise(double offsetX, double offsetY, double offsetZ, double offsetW, double scaleX, double scaleY, double scaleZ, double scaleW) {
		this(System.currentTimeMillis(), offsetX, offsetY, offsetZ, offsetW, scaleX, scaleY, scaleZ, scaleW);
	}
	
	/**
	 * Constructs a new simplex noise implementation with a random seed.
	 * @param offsetX The X offset of the noise.
	 * @param offsetY The Y offset of the noise.
	 * @param offsetZ The Z offset of the noise.
	 * @param scaleX The X scale factor of the noise.
	 * @param scaleY The Y scale factor of the noise.
	 * @param scaleZ The Z scale factor of the noise.
	 */
	public SimplexNoise(double offsetX, double offsetY, double offsetZ, double scaleX, double scaleY, double scaleZ) {
		this(System.currentTimeMillis(), offsetX, offsetY, offsetZ, 0, scaleX, scaleY, scaleZ, 1);
	}
	
	/**
	 * Constructs a new simplex noise implementation with a random seed.
	 * @param offsetX The X offset of the noise.
	 * @param offsetY The Y offset of the noise.
	 * @param scaleX The X scale factor of the noise.
	 * @param scaleY The Y scale factor of the noise.
	 */
	public SimplexNoise(double offsetX, double offsetY, double scaleX, double scaleY) {
		this(System.currentTimeMillis(), offsetX, offsetY, 0, scaleX, scaleY, 1);
	}
	
	/**
	 * Constructs a new simplex noise implementation with a random seed.
	 * @param offsetX The X offset of the noise.
	 * @param scaleX The X scale factor of the noise.
	 */
	public SimplexNoise(double offsetX, double scaleX) {
		this(System.currentTimeMillis(), offsetX, 0, scaleX, 1);
	}
	
	/**
	 * Constructs a new simplex noise implementation with a random seed.
	 * @param scaleX The X scale factor of the noise.
	 */
	public SimplexNoise(double scaleX) {
		this(System.currentTimeMillis(), 0, scaleX);
	}
	
	/**
	 * Constructs a new simplex noise implementation.
	 * @param seed The seed to use.
	 */
	public SimplexNoise(long seed) {
		this(seed, 1);
	}
	
	/**
	 * Constructs a new simplex noise implementation with a random seed.
	 */
	public SimplexNoise() {
		this(System.currentTimeMillis());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double get(double x, double y, double z, double w) {
		return (se.liu.itn.stegu.simplexnoise.SimplexNoise.noise((x + offsetX) * scaleX, (y + offsetY) * scaleY, (z + offsetZ) * scaleZ, (w + offsetW) * scaleW) + 1) / 2;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public double get(double x, double y, double z) {
		return (se.liu.itn.stegu.simplexnoise.SimplexNoise.noise((x + offsetX) * scaleX, (y + offsetY) * scaleY, (z + offsetZ) * scaleZ) + 1) / 2;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public double get(double x, double y) {
		return (se.liu.itn.stegu.simplexnoise.SimplexNoise.noise((x + offsetX) * scaleX, (y + offsetY) * scaleY) + 1) / 2;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public double get(double x) {
		return (se.liu.itn.stegu.simplexnoise.SimplexNoise.noise((x + offsetX) * scaleX, 0) + 1) / 2;
	}
}
