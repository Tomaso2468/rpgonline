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
package io.github.tomaso2468.rpgonline;

import java.util.Properties;

import org.newdawn.slick.util.Log;
import org.newdawn.slick.util.ResourceLoader;

/**
 * A class that represents version info of the RPGOnline library.
 * 
 * @author Tomas
 */
public final class RPGOnline {
	/**
	 * Prevent instantiation
	 */
	private RPGOnline() {

	}

	/**
	 * The current version of the RPGOnline library.
	 */
	public static final Version VERSION = new Version("0.9.0-s1+1573666826");
	/**
	 * The version of java that this library was compiled with.
	 */
	public static final Version JAVA_BUILD_VERSION = new Version("1.8.0_191");
	/**
	 * The current java version used by the JRE.
	 */
	public static final Version JAVA_VERSION = new Version(System.getProperty("java.version"));
	/**
	 * The minimum version that is required for java to work. Versions below this
	 * may still work but are unlikely to even run.
	 */
	public static final Version MIN_SUPPORTED_VERSION = new Version("1.8.0");

	/**
	 * The current version of LWJGL.
	 */
	public static final Version LWJGL_VERSION = new Version(org.lwjgl.Sys.getVersion());

	/**
	 * The current version of Slick2D.
	 */
	public static final Version SLICK_VERSION;

	// Load anything that requires a method call.
	static {
		int build = 0;
		try {
			Properties props = new Properties();
			props.load(ResourceLoader.getResourceAsStream("version"));

			build = Integer.parseInt(props.getProperty("build"));
		} catch (Exception e) {
			Log.error("Unable to determine Slick build number");
			build = 0;
		}
		SLICK_VERSION = new Version("1." + build);
	}

	/**
	 * Prints version info and displays warnings in the case of unsupported java
	 * versions.
	 */
	public static void queryVersionData() {
		Log.info("RPGOnline version: " + RPGOnline.VERSION.toDatedString());
		Log.info("RPGOnline java build version: " + RPGOnline.JAVA_BUILD_VERSION);
		Log.info("Java version: " + RPGOnline.JAVA_VERSION);
		Log.info("LWJGL version: " + RPGOnline.LWJGL_VERSION);
		Log.info("Slick2D version: " + SLICK_VERSION);
		if (JAVA_VERSION.compareTo(JAVA_BUILD_VERSION) < 0) {
			Log.warn(
					"The version of java in use is older than the version used to build the engine. Some problems may occur.");
		}
		if (JAVA_VERSION.compareTo(MIN_SUPPORTED_VERSION) < 0) {
			Log.warn("Java 8 or higher is required for this program to operate correctly.");
		}
	}
}
