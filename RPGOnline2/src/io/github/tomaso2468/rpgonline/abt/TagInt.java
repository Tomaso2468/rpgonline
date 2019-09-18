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
package io.github.tomaso2468.rpgonline.abt;

/**
 * A tag holding an integer value.
 * @author Tomas
 */
public class TagInt extends Tag {
	/**
	 * ID for serilization.
	 */
	private static final long serialVersionUID = 1440056949202502049L;
	/**
	 * The data held in this tag.
	 */
	private int data;
	
	/**
	 * Constructs a new int tag.
	 * @param name The name of this tag.
	 * @param data The value of this tag.
	 */
	public TagInt(String name, int data) {
		super(name, 0x04);
	}
	
	/**
	 * Gets the data held in this tag.
	 * @return An int value.
	 */
	public int getData() {
		return data;
	}
	
	/**
	 * Sets the data held in this tag.
	 * @param data An int value.
	 */
	public void setData(int data) {
		this.data = data;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{\n");

		appendStr(sb, "name", getName());
		appendSep(sb);
		appendNum(sb, "type", getType());
		appendSep(sb);
		appendNum(sb, "data", data);
		
		sb.append("\n}");

		return sb.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public TagInt clone() {
		return new TagInt(name, getData());
	}

}
