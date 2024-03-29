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
package io.github.tomaso2468.rpgonline.audio;

import java.net.URL;

/**
 * <p>
 * A class for managing audio. This class acts as a wrapper around the paulscode
 * sound system. system. The following formats are supported: {@code .ogg},
 * {@code .wav}, {@code .xm}, {@code .mod}, {@code .s3m}.
 * </p>
 * <p>
 * Sounds are mapped as strings to urls locating audio files. They are only
 * loaded once they are played. Music will be streamed. Volumes canm be set for
 * different categories of sounds.
 * </p>
 * <p>
 * For most audio settings (pitch & volume) the default value is 1 representing
 * normal pitch and full volume. When sound positions are set they are assumed
 * to be in real world coordinates.
 * </p>
 * 
 * @author Tomaso2468
 *
 */
public interface AudioSystem {
	/**
	 * Initialise the AudioSystem.
	 */
	public void init();

	/**
	 * Adjusts the pitch by a random amount with a range either side of base.
	 * 
	 * @param base  The base pitch (usually 1)
	 * @param range The range (on either side) of the pitch that will be possible.
	 * @return A float value.
	 */
	public default float pitchAdjust(float base, float range) {
		return (float) ((Math.random() * 2 - 1) * range + base);
	}

	/**
	 * Creates a pitch with around 1 with a range in either direction.
	 * 
	 * @param range The range (on either side) of the pitch that will be possible.
	 * @return A float value.
	 */
	public default float pitchAdjust(float range) {
		return pitchAdjust(1, range);
	}

	/**
	 * Deletes the sound system.
	 */
	public void dispose();

	/**
	 * Gets the current class of the sound library.
	 * 
	 * @return A class.
	 */
	public Class<?> getSoundLibraryClass();

	/**
	 * Gets the currently playing piece of music.
	 * 
	 * @return A ambient music object or null if no music is playing.
	 */
	public AmbientMusic getMusic();

	/**
	 * Sets the currently playing piece of music.
	 * 
	 * @param m A music object or null to stop all music.
	 */
	public void setMusic(AmbientMusic m);

	/**
	 * sets the currently playing music based off of a sound ID.
	 * 
	 * @param s A sound ID
	 */
	public void setMusic(String s);

	/**
	 * Sets the currently playing music based on a music ID.
	 * 
	 * @param s A music ID.
	 */
	public void setMusicID(String s);

	/**
	 * Sets the volume of a specified sound group.
	 * 
	 * @param g A sound group ID.
	 * @param v The desired volume.
	 */
	public void setGroupVolume(String g, float v);

	/**
	 * Gets the current volume of a sound group.
	 * 
	 * @param g A sound group ID.
	 * @return The current volume of a sound group.
	 */
	public float getGroupVolume(String g);

	/**
	 * Sets the current volume of all audio (this is multiplied with all channels).
	 * 
	 * @param v The desired volume.
	 */
	public void setMasterVolume(float v);

	/**
	 * Gets the current volume of all audio.
	 * 
	 * @return The current master volume.
	 */
	public float getMasterVolume();

	/**
	 * Sets the current volume of music.
	 * 
	 * @param v The desired volume.
	 */
	public void setMusicVolume(float v);

	/**
	 * Gets the current volume of music.
	 * 
	 * @return The current volume of music.
	 */
	public float getMusicVolume();

	/**
	 * Sets the volume of normal (non-ambient) sounds.
	 * 
	 * @param v The desired volume.
	 */
	public void setSoundVolume(float v);

	/**
	 * Gets the current volume of normal (non-ambient) sounds.
	 * 
	 * @return The current volume.
	 */
	public float getSoundVolume();

	/**
	 * Sets the current volume of ambient sounds.
	 * 
	 * @param v The desired volume.
	 */
	public void setAmbientVolume(float v);

	/**
	 * Gets the current volume of ambient sounds.
	 * 
	 * @return The current volume.
	 */
	public float getAmbientVolume();

	/**
	 * Plays a sound at a specified location with a pitch, volume and velocity.
	 * 
	 * @param name The sound ID to play.
	 * @param v    The volume of the sound.
	 * @param p    The pitch of the sound.
	 * @param x    The position of the sound (horizontal).
	 * @param y    The position of the sound (vertical).
	 * @param z    The position of the sound (depth).
	 * @param loop Set to true if the sound should be looped.
	 * @param dx   The horizontal velocity of the sound.
	 * @param dy   The vertical velocity of the sound.
	 * @param dz   The depth velocity of the sound.
	 * @return The internal sound ID.
	 */
	public String playSound(String name, float v, float p, float x, float y, float z, boolean loop, float dx, float dy,
			float dz);

	/**
	 * Plays a sound at a specified location with a pitch, volume and velocity
	 * without looping.
	 * 
	 * @param name The sound ID to play.
	 * @param v    The volume of the sound.
	 * @param p    The pitch of the sound.
	 * @param x    The position of the sound (horizontal).
	 * @param y    The position of the sound (vertical).
	 * @param z    The position of the sound (depth).
	 * @param dx   The horizontal velocity of the sound.
	 * @param dy   The vertical velocity of the sound.
	 * @param dz   The depth velocity of the sound.
	 * @return The internal sound ID.
	 */
	public String playSound(String name, float v, float p, float x, float y, float z, float dx, float dy, float dz);

	/**
	 * Plays a sound at a specified location with a pitch and volume without
	 * velocity or looping.
	 * 
	 * @param name The sound ID to play.
	 * @param v    The volume of the sound.
	 * @param p    The pitch of the sound.
	 * @param x    The position of the sound (horizontal).
	 * @param y    The position of the sound (vertical).
	 * @param z    The position of the sound (depth).
	 * @return The internal sound ID.
	 */
	public String playSound(String name, float v, float p, float x, float y, float z);

	/**
	 * Plays a sound at a specified location at the specified volume without
	 * velocity or looping and with a normal pitch.
	 * 
	 * @param name The sound ID to play.
	 * @param v    The volume of the sound.
	 * @param x    The position of the sound (horizontal).
	 * @param y    The position of the sound (vertical).
	 * @param z    The position of the sound (depth).
	 * @return The internal sound ID.
	 */
	public String playSound(String name, float v, float x, float y, float z);

	/**
	 * Plays a sound at a specified location without velocity or looping and with a
	 * normal pitch and full volume.
	 * 
	 * @param name The sound ID to play.
	 * @param x    The position of the sound (horizontal).
	 * @param y    The position of the sound (vertical).
	 * @param z    The position of the sound (depth).
	 * @return The internal sound ID.
	 */
	public String playSound(String name, float x, float y, float z);

	/**
	 * Plays an ambient sound with a specified volume, pitch and position.
	 * 
	 * @param name The sound ID to play.
	 * @param v    The volume of the sound.
	 * @param p    The pitch of the sound.
	 * @param x    The position of the sound (horizontal).
	 * @param y    The position of the sound (vertical).
	 * @param z    The position of the sound (depth).
	 * @param loop If the sound should be looped.
	 * @return The internal sound ID.
	 */
	public String playAmbient(String name, float v, float p, float x, float y, float z, boolean loop);

	/**
	 * Plays an ambient sound with a specified volume, pitch and position without
	 * looping.
	 * 
	 * @param name The sound ID to play.
	 * @param v    The volume of the sound.
	 * @param p    The pitch of the sound.
	 * @param x    The position of the sound (horizontal).
	 * @param y    The position of the sound (vertical).
	 * @param z    The position of the sound (depth).
	 * @return The internal sound ID.
	 */
	public String playAmbient(String name, float v, float p, float x, float y, float z);

	/**
	 * Sets the position of the player in 3D coordinates.
	 * 
	 * @param x The horizontal position of the player.
	 * @param y The vertical position of the player.
	 * @param z The depth position of the player.
	 */
	public void setPlayerPos(float x, float y, float z);

	/**
	 * Sets the velocity of the player in 3D coordinates.
	 * 
	 * @param x The horizontal velocity of the player.
	 * @param y The vertical velocity of the player.
	 * @param z The depth velocity of the player.
	 */
	public void setPlayerVelocity(float x, float y, float z);

	/**
	 * Gets the factor used to determine how the volume of a sound rolls-off with
	 * distance.
	 * 
	 * @return A float value.
	 */
	public float getDistanceFactor();

	/**
	 * Sets the factor used to determine how the volume of a sound rolls-off with
	 * distance.
	 * 
	 * @param rf The roll-off factor
	 */
	public void setDistanceFactor(float rf);

	/**
	 * Adds a sound.
	 * 
	 * @param id  The sound ID.
	 * @param loc The location of the sound.
	 */
	public void addSound(String id, URL loc);

	/**
	 * Gets a piece of ambient music with the specified ID.
	 * 
	 * @param id The music ID.
	 * @return An ambient music object or null.
	 */
	public AmbientMusic getAmbientMusic(String id);

	/**
	 * Maps a piece of ambient music to an ID.
	 * 
	 * @param id The music ID.
	 * @param m  An ambient music object
	 */
	public void setAmbientMusic(String id, AmbientMusic m);

	/**
	 * Stops all ambient sounds.
	 */
	public void stopAmbient();
}
