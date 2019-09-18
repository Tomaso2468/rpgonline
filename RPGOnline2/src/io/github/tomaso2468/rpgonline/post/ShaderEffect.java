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
package io.github.tomaso2468.rpgonline.post;

import java.net.URL;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import slickshader.Shader;

/**
 * An effect based on a shader.
 * 
 * @author Tomas
 */
public class ShaderEffect implements PostEffect {
	/**
	 * The vertex shader.
	 */
	private final URL vertex;
	/**
	 * The fragment shader.
	 */
	private final URL fragment;
	/**
	 * The shader.
	 */
	protected Shader shader;

	/**
	 * Creates a shader effect with a basic vertex shader that returns the inputed coordinates.
	 * 
	 * @param fragment The fragment shader.
	 */
	public ShaderEffect(URL fragment) {
		super();
		this.vertex = ShaderEffect.class.getResource("/generic.vrt");
		this.fragment = fragment;
	}
	
	/**
	 * Creates a shader effect.
	 * 
	 * @param vertex   The vertex shader.
	 * @param fragment The fragment shader.
	 */
	public ShaderEffect(URL vertex, URL fragment) {
		super();
		this.vertex = vertex;
		this.fragment = fragment;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doPostProcess(GameContainer container, StateBasedGame game, Image buffer, Graphics g)
			throws SlickException {
		g.clear();

		if (shader == null) {
			shader = Shader.makeShader(vertex, fragment);
			shader.startShader();
			initShader(shader, container);
		} else {
			shader.startShader();
		}

		updateShader(shader, container);

		g.drawImage(buffer, 0, 0);

		Shader.forceFixedShader(); // Return to the default fixed pipeline shader
	}

	/**
	 * Updates any shader uniforms
	 * 
	 * @param shader The shader to update.
	 * @param c The game container.
	 */
	protected void updateShader(Shader shader, GameContainer c) {

	}
	
	/**
	 * Sets up the shader.
	 * @param shader The shader to set up.
	 * @param c The game container.
	 */
	protected void initShader(Shader shader, GameContainer c) {
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		if (shader != null) {
			shader.deleteShader();
		}
	}
}
