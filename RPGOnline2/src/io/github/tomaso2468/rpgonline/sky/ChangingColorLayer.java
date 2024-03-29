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
 * A sky layer with a changing color.
 * @author Tomaso2468
 *
 */
public abstract class ChangingColorLayer implements SkyLayer {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void render(Renderer renderer, Game game, double x, double y, double z, World world, Color light) {
		renderer.drawQuad(0, 0, game.getWidth(), game.getHeight(), getColor(renderer, game, x, y, z, world));
	}
	
	/**
	 * Gets the color of the sky layer.
	 * @param g The current graphics context.
	 * @param c The current game container.
	 * @param x The player X position.
	 * @param y The player Y position.
	 * @param z The player Z position.
	 * @param world The current world.
	 * @return A color object.
	 */
	public abstract Color getColor(Renderer renderer, Game game, double x, double y, double z, World world);
}
