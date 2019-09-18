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

import io.github.tomaso2468.rpgonline.net.PacketType;

/**
 * A packet indicating the mode of the connection.
 * @author Tomas
 *
 */
public class ModePacket implements NetPacket {
	/**
	 * The ID of this packet.
	 */
	public static final byte PACKET_ID = (byte) 0xFF - 15;
	/**
	 * The ID for serialisation.
	 */
	private static final long serialVersionUID = -6976795378405555984L;

	/**
	 * Mode byte indicating a request for server information.
	 */
	public static final byte MODE_INFO = 1;
	
	/**
	 * Mode byte indicating a request for a server connection.
	 */
	public static final byte MODE_CONNECT = 2;
	
	/**
	 * The mode indicated by this packet.
	 */
	public final byte mode;

	/**
	 * Constructs a new ModePacket.
	 * @param mode The mode of the packet.
	 */
	public ModePacket(byte mode) {
		super();
		this.mode = mode;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(DataOutputStream out) throws IOException {
		out.write(PACKET_ID);
		out.writeByte(mode);
	}
	
	/**
	 * Packet type declaration.
	 * @author Tomas
	 *
	 */
	public static class Type implements PacketType {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public NetPacket readPacket(DataInputStream in) throws IOException, ClassNotFoundException {
			return new ModePacket(in.readByte());
		}
	}
}
