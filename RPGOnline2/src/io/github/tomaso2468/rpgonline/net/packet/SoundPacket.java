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
package io.github.tomaso2468.rpgonline.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import io.github.tomaso2468.rpgonline.Game;
import io.github.tomaso2468.rpgonline.net.PacketType;

/**
 * Packet used for sound data.
 * @author Tomaso2468
 *
 */
public class SoundPacket implements NetPacket {
	/**
	 * The packet ID.
	 */
	public static final byte PACKET_ID = (byte) 0xFF - 9;

	/**
	 * The serialisation ID.
	 */
	private static final long serialVersionUID = 1589226011832111169L;
	private final String id;
	private final float v;
	private final float p;
	private final float x;
	private final float y;
	private final float z;
	private final float dx;
	private final float dy;
	private final float dz;

	public SoundPacket(String id, float v, float p, float x, float y, float z, float dx, float dy, float dz) {
		super();
		this.id = id;
		this.v = v;
		this.p = p;
		this.x = x;
		this.y = y;
		this.z = z;
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
	}

	/**
	 * Apply the packet.
	 * 
	 * @param game The game.
	 */
	public void apply(Game game) {
		game.getAudio().playSound(id, v, p, x, y, z, false, dx, dy, dz);
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.write(PACKET_ID);
		out.writeUTF(id);
		out.writeFloat(v);
		out.writeFloat(p);
		out.writeFloat(x);
		out.writeFloat(y);
		out.writeFloat(z);
		out.writeFloat(dx);
		out.writeFloat(dy);
		out.writeFloat(dz);
	}

	public static class Type implements PacketType {
		@Override
		public NetPacket readPacket(DataInputStream in) throws IOException, ClassNotFoundException {
			return new SoundPacket(in.readUTF(), in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat(),
					in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat());
		}
	}
}
