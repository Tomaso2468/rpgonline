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
package io.github.tomaso2468.rpgonline.gui.layout;

import io.github.tomaso2468.rpgonline.Game;
import io.github.tomaso2468.rpgonline.debug.Debugger;
import io.github.tomaso2468.rpgonline.gui.Component;

/**
 * A layout that centres components with a border within another component.
 * @author Tomaso2468
 *
 */
public class Border extends Layout {
	/**
	 * The border.
	 */
	private float border;
	/**
	 * Constructs a new Border.
	 * @param border The border.
	 */
	public Border(float border) {
		this.border = border;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(Game game, Component c) {
		components.add(c);
		c.setBounds(game, border, border, c.getDefaultBounds(game, this).getWidth() - 2 * border, c.getDefaultBounds(game, this).getHeight() - 2 * border);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onResize(Game game, float x, float y, float w, float h) {
		Debugger.start("gui-layout");
		for (Component c : components) {
			c.setBounds(game, border, border, c.getDefaultBounds(game, this).getWidth() - 2 * border, c.getDefaultBounds(game, this).getHeight() - 2 * border);
		}
		Debugger.stop("gui-layout");
	}
}
