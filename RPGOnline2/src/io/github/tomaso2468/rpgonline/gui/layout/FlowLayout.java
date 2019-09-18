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

import io.github.tomaso2468.rpgonline.debug.Debugger;
import io.github.tomaso2468.rpgonline.gui.Component;

/**
 * A layout that fills components in the same way English text is written (left to right, going down to the next line when a line is full).
 * @author Tomas
 *
 */
public class FlowLayout extends Layout {
	/**
	 * The spacing of the layout.
	 */
	private final float spacing;
	
	/**
	 * Constructs a new FlowLayout.
	 * @param spacing The spacing of the layout.
	 */
	public FlowLayout(float spacing) {
		super();
		this.spacing = spacing;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(Component c) {
		components.add(c);
		layout();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(Component c) {
		super.remove(c);
		layout();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onResize(float x, float y, float w, float h) {
		super.onResize(x, y, w, h);
		layout();
	}
	
	/**
	 * Updates the layout of this component.
	 */
	protected void layout() {
		Debugger.start("gui-layout");
		float x = spacing;
		float y = spacing;
		float rh = 0;
		
		for (Component c : components) {
			rh = Math.max(rh, c.getDefaultBounds(this).getHeight());
			
			if (c.getDefaultBounds(this).getWidth() + x + spacing > getW()) {
				y += rh + spacing;
				x = spacing;
			}
			
			c.setBounds(x, y, c.getDefaultBounds(this).getWidth(), c.getDefaultBounds(this).getHeight());
			x += c.getDefaultBounds(this).getWidth() + spacing;
		}
		Debugger.stop("gui-layout");
	}
}
