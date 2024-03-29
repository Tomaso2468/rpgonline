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
import io.github.tomaso2468.rpgonline.Image;
import io.github.tomaso2468.rpgonline.render.Renderer;
import io.github.tomaso2468.rpgonline.world2d.World;

/**
 * A sky layer made of a grid of images.
 * @author Tomaso2468
 *
 */
public abstract class ImageMeshLayer implements SkyLayer {
	/**
	 * The width of one image.
	 */
	private final int imageWidth;
	/**
	 * The height of one image.
	 */
	private final int imageHeight;
	
	/**
	 * Constructs a new ImageMeshLayer.
	 * @param imageWidth The width of one image.
	 * @param imageHeight The height of one image.
	 */
	public ImageMeshLayer(int imageWidth, int imageHeight) {
		super();
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void render(Renderer renderer, Game game, double x, double y, double z, World world, Color light) {
		renderer.pushTransform();
		
		renderer.translate2D(game.getWidth() / 2, game.getHeight() / 2);
		renderer.translate2D((float) x % imageWidth, (float) y % imageHeight);
		
		long dist_x = game.getWidth() / 2 / imageWidth + 1;
		long dist_y = game.getHeight() / 2 / imageHeight + 1;
		
		Image current = null;
		for (long tx = -dist_x; tx <= dist_x; tx++) {
			for (long ty = -dist_y; ty <= dist_y; ty++) {
				Image img = getImageAt(tx - (long) (x / imageWidth), ty - (long) (y / imageHeight));
				if (img != null) {
					if (game.getTextures().getSheet(img) != current) {
						if (current != null) renderer.endUse(img);
						current = game.getTextures().getSheet(img);
						renderer.startUse(img);
					}
					renderer.bindColor(light);
					renderer.renderEmbedded(img, tx * imageWidth, ty * imageHeight, imageWidth, imageHeight);
				}
			}
		}
		if (current != null) renderer.endUse(current);
		
		renderer.popTransform();
	}
	
	/**
	 * Gets the image at the specified position.
	 * @param x The X position of the image.
	 * @param y The Y position of the image.
	 * @return An image object.
	 */
	public abstract Image getImageAt(long x, long y);
}
