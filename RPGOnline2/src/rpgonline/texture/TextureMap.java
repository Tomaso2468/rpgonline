package rpgonline.texture;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.apache.commons.math3.util.FastMath;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.imageout.ImageOut;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.Log;

import rpgonline.RPGConfig;

public class TextureMap {
	private static List<Image> texturesFast = new ArrayList<>();
	private static Map<String, Image> textures = new HashMap<>();
	private static Map<String, BufferedImage> texturesMapped = new HashMap<>();
	public static Map<Image, Image> sheets = new HashMap<>();
	
	public static void addTexture(String s, Image img) {
		textures.put(s.intern(), img);
		texturesFast.add(img);
	}
	
	public static void loadTexture(String s, URL loc) throws SlickException {
		Log.debug("Loading texture " + s + " from " + loc);
		Texture tex;
		try {
			tex = TextureLoader.getTexture("PNG", new BufferedInputStream(loc.openStream()));
		} catch (IOException e) {
			Log.error(e);
			throw new SlickException(e.toString());
		}
		Image img = new Image(tex);
		img.setFilter(Image.FILTER_NEAREST);

		addTexture(s, img);
	}
	
	public static void loadMappedTexture(String s, URL loc) throws SlickException {
		if(!RPGConfig.isMapped()) {
			loadTexture(s, loc);
			return;
		}
		
		Log.debug("Loading mapped texture " + s + " from " + loc);
		
		BufferedImage img;
		try {
			img = ImageIO.read(loc);
		} catch (IOException e) {
			throw new SlickException(e.toString());
		}
		
		addMappedTexture(s, img);
	}
	
	public static void addMappedTexture(String s, BufferedImage img) {
		texturesMapped.put(s, img);
	}
	
	/**
	 * <p>
	 * Adds an image as a sprite map.
	 * </p>
	 * <p>
	 * The textures are bounds as {@code s + "." + i} where i is the index in the
	 * sprite map starting at the top left corner and travelling horizontally.
	 * <p>
	 * 
	 * @param s   The prefix of the texture ID to bind to.
	 * @param img The image holding the sprite map.
	 * @param tw  The width of one sprite.
	 * @param th  The height of one sprite.
	 */
	public static void addSpriteMap(String s, Image img, int tw, int th) {
		SpriteSheet map = new SpriteSheet(img, tw, th);
		int id = 0;
		Log.debug("Map size " + map.getHorizontalCount() + " x " + map.getVerticalCount());
		for (int y = 0; y < map.getVerticalCount(); y++) {
			for (int x = 0; x < map.getHorizontalCount(); x++) {
				Log.debug("Loading sprite map part " + s + "." + id + " @ " + x + " " + y);
				addTexture(s + "." + id, map.getSprite(x, y));
				id += 1;
			}
		}
	}

	/**
	 * <p>
	 * Loads a texture then adds it as a sprite map.
	 * </p>
	 * <p>
	 * The textures are bounds as {@code s + "." + i} where i is the index in the
	 * sprite map starting at the top left corner and travelling horizontally.
	 * <p>
	 * 
	 * @param s   The prefix of the texture ID to bind to.
	 * @param loc A URL pointing to a .png image.
	 * @param tw  The width of one sprite.
	 * @param th  The height of one sprite.
	 * @throws SlickException if an error occurs loading the sprite map.
	 */
	public static void loadSpriteMap(String s, URL loc, int tw, int th) throws SlickException {
		Log.debug("Loading sprite map texture " + s + " from " + loc);
		Texture tex;
		try {
			tex = TextureLoader.getTexture("PNG", new BufferedInputStream(loc.openStream()));
		} catch (IOException e) {
			Log.error(e);
			throw new SlickException(e.toString());
		}
		Image img = new Image(tex);
		img.setFilter(Image.FILTER_NEAREST);

		addSpriteMap(s, img, tw, th);
	}
	
	public static int getTextureIndex(String s) {
		return texturesFast.indexOf(textures.get(s.intern()));
	}
	
	public static Image getTexture(int i) {
		if(i < 0) {
			return null;
		}
		return texturesFast.get(i);
	}
	
	public static Image getTexture(String s) {
		return textures.get(s.intern());
	}
	
	public static void genTextureMap(int sw, int sh) throws SlickException {
		if(!RPGConfig.isMapped()) {
			return;
		}
		Log.debug("Preparing to map textures of size " + sw + " x " + sh);
		
		// Get largest texture size.
		int mts = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
		
		// Limit to the config size for auto-generated sprite maps to not use all available RAM.
		mts = FastMath.min(mts, RPGConfig.getAutoSpriteMapSize());
		
		if (sw > mts) {
			throw new IllegalArgumentException("Sprite width is larger than maximum size specified by GL_MAX_TEXTURE_SIZE and RPGConfig.getAutoSpriteMapSize(): " + sw + "/" + mts);
		}
		if (sh > mts) {
			throw new IllegalArgumentException("Sprite height is larger than maximum size specified by GL_MAX_TEXTURE_SIZE and RPGConfig.getAutoSpriteMapSize(): " + sh + "/" + mts);
		}
		
		// Find largest multiple of sw and sh to insure no unneeded space is allocated.
		int w = largestMultiple(sw, mts);
		int h = largestMultiple(sh, mts);
		
		// Compute number of sprites in grid.
		int wc = w / sw;
		int hc = h / sh;
		
		Log.debug("Grabbing textures to map.");
		
		// List of textures to put in map.
		List<Entry<String, BufferedImage>> toMap = new ArrayList<>();
		
		for (Entry<String, BufferedImage> e : texturesMapped.entrySet()) {
			if(e.getValue().getWidth() == sw && e.getValue().getHeight() == sh) {
				toMap.add(e);
			}
		}
		
		// Remove mapped textures
		for (Entry<String, BufferedImage> t : toMap) {
			texturesMapped.remove(t.getKey());
		}
		
		Log.info("Mapping textures of size " + sw + " x " + sh);
		
		Map<String, Integer> textureX = new HashMap<>();
		Map<String, Integer> textureY = new HashMap<>();
		Map<BufferedImage, List<String>> textureImg = new HashMap<>();
		Map<BufferedImage, File> textureFile = new HashMap<>();
		
		try {
			BufferedImage img = null;
			int tx = 0;
			int ty = 0;
			Graphics2D g = null;
			for (Entry<String, BufferedImage> t : toMap) {
				Log.debug("Mapping " + t.getKey());
				if (tx >= wc) {
					tx = 0;
					ty += 1;
				}
				if (ty >= hc) {
					File f = File.createTempFile("rpgonline_tilesheet_", ".png");
					ImageIO.write(img, "PNG", f);
					textureFile.put(img, f);
					
					img = null;
					tx = 0;
					ty = 0;
				}
				if(img == null) {
					img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
					textureImg.put(img, new ArrayList<String>(64));
					g = img.createGraphics();
				}
				
				textureX.put(t.getKey(), tx);
				textureY.put(t.getKey(), ty);
				textureImg.get(img).add(t.getKey());
				
				g.drawImage(t.getValue(), tx * sh, ty * sh, null);
				
				tx += 1;
			}
			
			if (img != null) {
				File f = File.createTempFile("rpgonline_tilesheet_", ".png");
				ImageIO.write(img, "PNG", f);
				textureFile.put(img, f);
			}
			
			Log.debug("Storing data in temporary file");
			
			int i = 0;
			for (Entry<BufferedImage, List<String>> e : textureImg.entrySet()) {
				File f = textureFile.get(e.getKey());
				
				Texture tex;
				tex = TextureLoader.getTexture("PNG", new BufferedInputStream(f.toURI().toURL().openStream()));
				Image img2 = new Image(tex);
				img2.setFilter(Image.FILTER_NEAREST);
				
				SpriteSheet ss = new SpriteSheet(img2, sw, sh);
				
				for(String s : e.getValue()) {
					addTexture(s, ss.getSubImage(textureX.get(s), textureY.get(s)));
					
					sheets.put(ss.getSubImage(textureX.get(s), textureY.get(s)), ss);
				}
				
				ImageOut.write(ss, "map" + sw + "-" + sh + "-" + i + ".png");
				
				i += 1;
			}
			
			Log.debug("Mapping complete");
		} catch(IOException e) {
			Log.error(e);
			throw new SlickException(e.toString());
		}
	}
	
	public static void generateAllMaps() throws SlickException {
		Log.info("Generating all texture maps.");
		while (!texturesMapped.isEmpty()) {
			Entry<String, BufferedImage> e = texturesMapped.entrySet().iterator().next();
			genTextureMap(e.getValue().getWidth(), e.getValue().getHeight());
		}
		Log.info("Map generation complete.");
	}
	
	private static int largestMultiple(int x, int max) {
		int m = 0;
		int i = 1;
		
		while (m <= max) {
			m = x * i;
			i += 1;
		}
		
		return (i - 2) * x;
	}

	public static Image getSheet(Image img) {
		Image sheet = sheets.get(img);
		if (sheet == null) {
			sheet = img;
		}
		return sheet;
	}
}
