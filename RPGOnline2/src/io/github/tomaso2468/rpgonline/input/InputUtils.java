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
package io.github.tomaso2468.rpgonline.input;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Input;

import io.github.tomaso2468.rpgonline.RPGConfig;

/**
 * A class with utilities for input.
 * @author Tomas
 *
 */
public final class InputUtils {
	/**
	 Prevent instantiation
	 */
	private InputUtils() {
		
	}
	/**
	 * The binding for walking north.
	 */
	public static final String WALK_NORTH = "walk_north";
	/**
	 * The binding for walking east.
	 */
	public static final String WALK_EAST = "walk_east";
	/**
	 * The binding for walking south.
	 */
	public static final String WALK_SOUTH = "walk_south";
	/**
	 * The binding for walking west.
	 */
	public static final String WALK_WEST = "walk_west";
	/**
	 * The binding for exiting the game.
	 */
	public static final String EXIT = "exit";
	/**
	 * The binding for toggling the GUI.
	 */
	public static final String GUI_TOGGLE = "gui_toggle";
	/**
	 * The binding for the sprint the key.
	 */
	public static final String SPRINT = "sprint";
	/**
	 * The binding for moving north in the bullet menu.
	 */
	public static final String BULLET_NORTH = "bullet_north";
	/**
	 * The binding for moving east in the bullet menu.
	 */
	public static final String BULLET_EAST = "bullet_east";
	/**
	 * The binding for moving south in the bullet menu.
	 */
	public static final String BULLET_SOUTH = "bullet_south";
	/**
	 * The binding for moving west in the bullet menu.
	 */
	public static final String BULLET_WEST = "bullet_west";
	
	/**
	 * Determines if the binding is pressed.
	 * @param in The current input.
	 * @param func The binding to check.
	 * @return {@code true} if the binding is pressed, {@code false} otherwise.
	 */
	public static boolean isActionPressed(Input in, String func) {
		if (in.getControllerCount() > 0) {
			return Keyboard.isKeyDown(RPGConfig.getKeyInput().getKeyCodeForAction(func)) 
					|| ControllerInputProvider.isButtonPressed(in, RPGConfig.getControllerInput().getBinding(func));
		} else {
			return Keyboard.isKeyDown(RPGConfig.getKeyInput().getKeyCodeForAction(func));
		}
	}
	
}
