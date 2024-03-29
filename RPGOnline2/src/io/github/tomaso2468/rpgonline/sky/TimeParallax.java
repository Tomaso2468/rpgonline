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
package io.github.tomaso2468.rpgonline.sky;

import org.newdawn.slick.Color;

import io.github.tomaso2468.rpgonline.Game;
import io.github.tomaso2468.rpgonline.render.Renderer;
import io.github.tomaso2468.rpgonline.world2d.World;

/**
 * A sky layer that applies a movement effect based on time.
 * @author Tomaso2468
 *
 */
public class TimeParallax implements SkyLayer {
	/**
	 * The X parallax factor.
	 */
	private final double factorX;
	/**
	 * The Y parallax factor.
	 */
	private final double factorY;
	/**
	 * The sky layer to apply the time parallax effect to.
	 */
	private final SkyLayer l;
	
	/**
	 * Constructs a new TimeParralax object.
	 * @param factorX The X parallax factor.
	 * @param factorY The Y parallax factor.
	 * @param l The sky layer to apply the time parallax effect to.
	 */
	public TimeParallax(double factorX, double factorY, SkyLayer l) {
		super();
		this.factorX = factorX;
		this.factorY = factorY;
		this.l = l;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void render(Renderer renderer, Game game, double x, double y, double z, World world, Color light) {
		l.render(renderer, game, x + ((System.currentTimeMillis() - 1561284966000L) * factorX), y + ((System.currentTimeMillis() - 1561284966000L) * factorY), z, world, light);
	}

} 
