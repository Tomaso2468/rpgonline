package rpgonline.audio;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.newdawn.slick.util.Log;

import paulscode.sound.Library;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.codecs.CodecJOgg;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.libraries.LibraryJavaSound;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;
import rpgonline.RPGConfig;

public final class AudioManager {
	private static SoundSystem system;
	private static float rf;
	
	private static final Map<String, URL> sounds = new HashMap<>();
	
	private static AmbientMusic music = null;
	
	private static Thread fadeThread;
	
	private static final Map<String, Float> volumes = new HashMap<>();
	
	private static final List<String> ambient = new ArrayList<>(16);
	
	static {
		boolean aLCompatible = SoundSystem.libraryCompatible(LibraryLWJGLOpenAL.class);
		boolean jSCompatible = SoundSystem.libraryCompatible(LibraryJavaSound.class);

		Class<?> libraryType;
		if (aLCompatible) {
			libraryType = LibraryLWJGLOpenAL.class; // OpenAL
		} else if (jSCompatible) {
			libraryType = LibraryJavaSound.class; // Java Sound
		} else {
			libraryType = Library.class; // "No Sound, Silent Mode"
		}
		try {
			Log.info("Attempting to use " + libraryType);
			system = new SoundSystem(libraryType);
		} catch (SoundSystemException sse) {
			Log.error("Error loading audio library: " + libraryType);
		}

		try {
			SoundSystemConfig.setCodec("wav", CodecWav.class);
		} catch (SoundSystemException e) {
			Log.error("Error linking with the CodecWav plug-in.", e);
		}
		try {
			SoundSystemConfig.setCodec("ogg", CodecJOgg.class);
		} catch (SoundSystemException e) {
			Log.error("Error linking with the CodecJOgg plug-in.", e);
		}
		
		rf = SoundSystemConfig.getDefaultRolloff();
		Log.info("Roll-off distance set to: " + rf);
		
		volumes.put("music", 1f);
		volumes.put("sound", 1f);
		volumes.put("ambient", 1f);
		
		if (RPGConfig.isSoundSystemDaemon()) {
			Thread t = new Thread("Sound Daemon") {
				@Override
				public void run() {
					while(true) {
						system.checkFadeVolumes();
						
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							Log.error(e);
						}
					}
				}
			};
			t.setDaemon(true);
			t.start();
		}
	}

	public static void dispose() {
		system.cleanup();
	}
	
	public static Class<?> getSoundLibraryClass() {
		return SoundSystem.currentLibrary();
	}
	
	public static void setSoundLibrary(Class<?> c) throws SoundSystemException {
		system.switchLibrary(c);
	}
	
	private static String playBackgroundMusic(URL url, boolean loop, float volume) {
		String name = Long.toHexString(System.nanoTime()).toUpperCase() + ":BM:" + Long.toHexString(System.currentTimeMillis()).toUpperCase() + ":" + url.toString();
		
		system.backgroundMusic(name, url, url.getPath(), loop);
		system.setVolume(name, volume);
		
		return name;
	}
	
	public static AmbientMusic getMusic() {
		return music;
	}

	@SuppressWarnings("deprecation")
	public static void setMusic(AmbientMusic m) {
		if(fadeThread != null) {
			fadeThread.stop();
		}
		
		fadeThread = new Thread() {
			@Override
			public void run() {
				if (music != null) {
					for(int i = 0; i < 1000 / 50; i++) {
						for(String s : music.refs) {
							system.setVolume(s, system.getVolume(s) * 0.8f);
						}
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							Log.error(e);
						}
					}
					for(String s : music.refs) {
						system.stop(s);
					}
				}
				music = m;
				
				String[] sounds = music.getSounds();
				
				for (int i = 0; i < sounds.length; i++) {
					String s = sounds[i];
					String g = music.getGroups()[i];
					float v = music.getVolumes()[i];
					
					music.refs[i] = playBackgroundMusic(AudioManager.sounds.get(s), true, volumes.get(g) * v);
				}
				
				fadeThread = null;
			}
		};
		fadeThread.start();
	}
	
	public static void setMusic(String s) {
		if (music.getSounds().length == 1 && music.getSounds()[0].equals(s)) {
			return;
		}
		
		setMusic(new AmbientMusic(s));
	}
	
	public static void setGroupVolume(String g, float v) {
		volumes.put(g, v);
	}
	
	public static float getGroupVolume(String g) {
		return volumes.get(g);
	}
	
	public static void setMasterVolume(float v) {
		system.setMasterVolume(v);
	}
	
	public static float getMasterVolume() {
		return system.getMasterVolume();
	}
	
	public static void setMusicVolume(float v) {
		float old = getMusicVolume();
		
		setGroupVolume("music", v);
		
		float factor = v / old;
		
		for(String s : music.refs) {
			system.setVolume(s, system.getVolume(s) * factor);
		}
	}
	
	public static float getMusicVolume() {
		return getGroupVolume("music");
	}
	
	public static void setSoundVolume(float v) {
		setGroupVolume("sound", v);
	}
	
	public static float getSoundVolume() {
		return getGroupVolume("sound");
	}
	
	public static void setAmbientVolume(float v) {
		float old = getAmbientVolume();
		
		setGroupVolume("ambient", v);
		
		float factor = v / old;
		
		for (String s : ambient) {
			system.setVolume(s, system.getVolume(s) * factor);
		}
	}
	
	public static float getAmbientVolume() {
		return getGroupVolume("ambient");
	}
	
	public static String playSound(String name, float v, float p, float x, float y, float z, boolean loop, float dx, float dy, float dz) {
		String s = system.quickPlay(false, sounds.get(name), sounds.get(name).getPath(), loop, x, y, z, SoundSystemConfig.ATTENUATION_ROLLOFF, rf);
		system.setVolume(s, v);
		system.setPitch(s, p);
		system.setVelocity(s, dx, dy, dz);
		
		return s;
	}
	
	public static String playSound(String name, float v, float p, float x, float y, float z, float dx, float dy, float dz) {
		return playSound(name, v, p, x, y, z, false, dx, dy, dz);
	}
	
	public static String playSound(String name, float v, float p, float x, float y, float z) {
		return playSound(name, v, p, x, y, z, false, 0, 0, 0);
	}
	
	public static void setPlayerPos(float x, float y, float z) {
		system.setListenerPosition(x, z, y);
	}
	
	public static void setPlayerVelocity(float x, float y, float z) {
		system.setListenerVelocity(x, z, y);
	}

	public static float getDistanceFactor() {
		return rf;
	}

	public static void setDistanceFactor(float rf) {
		AudioManager.rf = rf;
	}
	
	public static void addSound(String id, URL loc) {
		sounds.put(id, loc);
	}

	public static SoundSystem getSystem() {
		return system;
	}
}
