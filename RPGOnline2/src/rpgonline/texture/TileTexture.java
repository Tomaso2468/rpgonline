package rpgonline.texture;

import org.newdawn.slick.Graphics;

import rpgonline.tile.Tile;
import rpgonline.world.World;

public interface TileTexture {
	public default boolean isPure() {
		return true;
	}
	
	public default boolean isCustom() {
		return false;
	}
	
	public int getTexture(long x, long y, long z, World w, String state, Tile t);
	
	public default TileTexture[] getTextures() {
		return new TileTexture[] {this};
	}
	
	public default void render(Graphics g, long x, long y, long z, World w, String state, Tile t, float sx, float sy, float wind) {
		
	}
	
	public default float getX() {
		return 0;
	}
	
	public default float getY() {
		return 0;
	}
	
	public default boolean isLightAffected() {
		return true;
	}
}
