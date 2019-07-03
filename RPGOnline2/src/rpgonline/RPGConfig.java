package rpgonline;

import rpgonline.input.ControllerInputProvider;
import rpgonline.input.KeyboardInputProvider;
import rpgonline.input.MapControllerProvider;
import rpgonline.input.MapKeyProvider;

/**
 * A class for storing configuration details about the game engine.
 * 
 * @author Tomas
 */
public class RPGConfig {
	/**
	 * The size used by sprite maps generated by TextureMap
	 * 
	 * @see rpgonline.texture.TextureMap
	 */
	private static int autoSpriteMapSize = 1024;
	/**
	 * Determines whether an additional thread is used to manage audio fading. A
	 * thread will still be used when needed for features that cannot be done with
	 * just SoundSystem
	 * 
	 * @see rpgonline.audio.AudioManager
	 * @see paulscode.sound.SoundSystem
	 */
	private static boolean soundSystemDaemon = true;
	/**
	 * Determines if the wind effect will be used by the rendering engine. If set to
	 * {@code false} any information provided by the game engine about wind will be
	 * set to 0.
	 * 
	 * @see rpgonline.texture.WindTexture
	 * @see rpgonline.texture.entity.WindEntityTexture
	 * @see rpgonline.state.WorldState
	 * @see rpgonline.net.Client
	 */
	private static boolean wind = false;
	/**
	 * Determines if the the shader effect will be used by the rendering engine.
	 * 
	 * @see rpgonline.state.WorldState
	 */
	private static boolean shadow = false;
	/**
	 * The size used by tiles as the grid size.
	 * 
	 * @see rpgonline.tile.Tile
	 * @see rpgonline.texture.TileTexture
	 * @see rpgonline.state.WorldState
	 */
	private static int tileSize = 16;
	/**
	 * A handler for storing bindings on keyboards. This defaults to a
	 * {@code MapKeyProvider}
	 * 
	 * @see rpgonline.input.MapKeyProvider
	 * @see rpgonline.input.InputUtils
	 */
	private static KeyboardInputProvider keyInput = new MapKeyProvider();
	/**
	 * The sensitivity for interpreting an axis button as a normal button. If the
	 * value passes this it will be considered as true. This is applied to the
	 * absolute value of the axis.
	 * 
	 * @see rpgonline.input.InputUtils
	 * @see rpgonline.input.ControllerInputProvider
	 */
	private static float controllerActuation = 0.5f;
	/**
	 * A handler for storing bindings on controllers. This defaults to a
	 * {@code MapControllerProvider}
	 * 
	 * @see rpgonline.input.MapControllerProvider
	 * @see rpgonline.input.InputUtils
	 */
	private static ControllerInputProvider controllerInput = new MapControllerProvider();
	
	private static boolean basicPipeline = true;
	
	private static boolean mapped = true;

	/**
	 * Gets the size used by sprite maps generated by TextureMap.
	 * 
	 * @return An int that is greater than 0.
	 * 
	 * @see rpgonline.texture.TextureMap
	 */
	public static int getAutoSpriteMapSize() {
		return autoSpriteMapSize;
	}

	/**
	 * Sets the size used by sprite maps generated by TextureMap.
	 * 
	 * @param autoSpriteMapSize An int that is greater than 0.
	 * 
	 * @see rpgonline.texture.TextureMap
	 */
	public static void setAutoSpriteMapSize(int autoSpriteMapSize) {
		if (autoSpriteMapSize <= 0) {
			throw new IllegalArgumentException("autoSpriteMapSize must be greater than 0.");
		}
		RPGConfig.autoSpriteMapSize = autoSpriteMapSize;
	}

	/**
	 * Determines whether an additional thread is used to manage audio fading. A
	 * thread will still be used when needed for features that cannot be done with
	 * just SoundSystem
	 * 
	 * @return true if an additional thread should be used false otherwise.
	 * 
	 * @see rpgonline.audio.AudioManager
	 * @see paulscode.sound.SoundSystem
	 */
	public static boolean isSoundSystemDaemon() {
		return soundSystemDaemon;
	}

	/**
	 * Determines whether an additional thread is used to manage audio fading. A
	 * thread will still be used when needed for features that cannot be done with
	 * just SoundSystem
	 * 
	 * @param soundSystemDaemon true if an additional thread should be used false
	 *                          otherwise.
	 * 
	 * @see rpgonline.audio.AudioManager
	 * @see paulscode.sound.SoundSystem
	 */
	public static void setSoundSystemDaemon(boolean soundSystemDaemon) {
		RPGConfig.soundSystemDaemon = soundSystemDaemon;
	}

	/**
	 * Determines if the wind effect will be used by the rendering engine. If set to
	 * {@code false} any information provided by the game engine about wind will be
	 * set to 0.
	 * 
	 * @return true if the wind effect is enabled. false otherwise.
	 * 
	 * @see rpgonline.texture.WindTexture
	 * @see rpgonline.texture.entity.WindEntityTexture
	 * @see rpgonline.state.WorldState
	 * @see rpgonline.net.Client
	 */
	public static boolean isWind() {
		return wind;
	}

	/**
	 * Determines if the wind effect will be used by the rendering engine. If set to
	 * {@code false} any information provided by the game engine about wind will be
	 * set to 0.
	 * 
	 * @param wind true if the wind effect is enabled. false otherwise.
	 * 
	 * @see rpgonline.texture.WindTexture
	 * @see rpgonline.texture.entity.WindEntityTexture
	 * @see rpgonline.state.WorldState
	 * @see rpgonline.net.Client
	 */
	public static void setWind(boolean wind) {
		RPGConfig.wind = wind;
	}

	/**
	 * Determines if the the shader effect will be used by the rendering engine.
	 * 
	 * @return true if the shadow effect should be used. false otherwise.
	 * 
	 * @see rpgonline.state.WorldState
	 */
	public static boolean isShadow() {
		return shadow;
	}

	/**
	 * Determines if the the shader effect will be used by the rendering engine.
	 * 
	 * @param shadow true if the shadow effect should be used. false otherwise.
	 * 
	 * @see rpgonline.state.WorldState
	 */
	public static void setShadow(boolean shadow) {
		RPGConfig.shadow = shadow;
	}

	/**
	 * The size used by tiles as the grid size.
	 * 
	 * @return an int that is greater that 0.
	 * 
	 * @see rpgonline.tile.Tile
	 * @see rpgonline.texture.TileTexture
	 * @see rpgonline.state.WorldState
	 */
	public static int getTileSize() {
		return tileSize;
	}

	/**
	 * The size used by tiles as the grid size.
	 * 
	 * @param tileSize an int that is greater that 0.
	 * 
	 * @see rpgonline.tile.Tile
	 * @see rpgonline.texture.TileTexture
	 * @see rpgonline.state.WorldState
	 */
	public static void setTileSize(int tileSize) {
		if (tileSize <= 0) {
			throw new IllegalArgumentException("tileSize must be greater than 0.");
		}
		RPGConfig.tileSize = tileSize;
	}

	/**
	 * A handler for storing bindings on keyboards. This defaults to a
	 * {@code MapKeyProvider}
	 * 
	 * @return A KeyboardInputProvider.
	 * 
	 * @see rpgonline.input.MapKeyProvider
	 * @see rpgonline.input.InputUtils
	 */
	public static KeyboardInputProvider getKeyInput() {
		return keyInput;
	}

	/**
	 * A handler for storing bindings on keyboards. This defaults to a
	 * {@code MapKeyProvider}
	 * 
	 * @param keyInput A KeyboardInputProvider. This cannot be null.
	 * 
	 * @see rpgonline.input.MapKeyProvider
	 * @see rpgonline.input.InputUtils
	 */
	public static void setKeyInput(KeyboardInputProvider keyInput) {
		if (keyInput == null) {
			throw new NullPointerException("keyInput");
		}
		RPGConfig.keyInput = keyInput;
	}

	/**
	 * The sensitivity for interpreting an axis button as a normal button. If the
	 * value passes this it will be considered as true. This is applied to the
	 * absolute value of the axis.
	 * 
	 * @return A float that is equal to or greater than 0. A float value of Infinity
	 *         or NaN may be used to disable axis that are used as buttons.
	 * 
	 * @see rpgonline.input.InputUtils
	 * @see rpgonline.input.ControllerInputProvider
	 */
	public static float getControllerActuation() {
		return controllerActuation;
	}

	/**
	 * The sensitivity for interpreting an axis button as a normal button. If the
	 * value passes this it will be considered as true. This is applied to the
	 * absolute value of the axis.
	 * 
	 * @param controllerActuation A float that is equal to or greater than 0. A
	 *                            float value of Infinity or NaN may be used to
	 *                            disable axis that are used as buttons.
	 * 
	 * @see rpgonline.input.InputUtils
	 * @see rpgonline.input.ControllerInputProvider
	 */
	public static void setControllerActuation(float controllerActuation) {
		if (controllerActuation < 0) {
			throw new IllegalArgumentException("Actuation point cannot be less than 0.");
		}
		RPGConfig.controllerActuation = controllerActuation;
	}

	/**
	 * A handler for storing bindings on controllers. This defaults to a
	 * {@code MapControllerProvider}
	 * 
	 * @return A ControllerInputProvider.
	 * 
	 * @see rpgonline.input.MapControllerProvider
	 * @see rpgonline.input.InputUtils
	 */
	public static ControllerInputProvider getControllerInput() {
		return controllerInput;
	}

	/**
	 * A handler for storing bindings on controllers. This defaults to a
	 * {@code MapControllerProvider}
	 * 
	 * @param controllerInput A ControllerInputProvider. This cannot be null.
	 * 
	 * @see rpgonline.input.MapControllerProvider
	 * @see rpgonline.input.InputUtils
	 */
	public static void setControllerInput(ControllerInputProvider controllerInput) {
		if (controllerInput == null) {
			throw new NullPointerException("controllerInput");
		}
		RPGConfig.controllerInput = controllerInput;
	}

	public static boolean isBasicPipeline() {
		return basicPipeline;
	}

	public static void setBasicPipeline(boolean basicPipeline) {
		RPGConfig.basicPipeline = basicPipeline;
	}

	public static boolean isMapped() {
		return mapped;
	}

	public static void setMapped(boolean mapped) {
		RPGConfig.mapped = mapped;
	}

	
}
