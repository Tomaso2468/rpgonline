package io.github.tomaso2468.rpgonline.entity;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Transform;

import io.github.tomaso2468.rpgonline.debug.Debugger;
import io.github.tomaso2468.rpgonline.tile.Tile;
import io.github.tomaso2468.rpgonline.world.World;

/**
 * An EntityAI extension based on velocity.
 * @author Tomas
 *
 */
public class VelocityAI implements EntityAI {
	/**
	 * The minimum speed before velocity is set to 0.
	 */
	public static double SPEED_MIN = 1e-5;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doAI(Entity e, World w, float wind, List<Entity> entities) {

	}

	/**
	 * Computes velocity based movement.
	 * @param e The entity to move.
	 * @param w The current world.
	 * @param entities A list of all entities.
	 */
	public void doVelocity(Entity e, World w, List<Entity> entities) {
		tryMove(e.getX() + e.getDX(), e.getY() + e.getDY(), e, w, entities);

		e.setDX(e.getDX() / e.getDouble("weight_f"));
		e.setDY(e.getDY() / e.getDouble("weight_f"));

		if (FastMath.abs(e.getDX()) < SPEED_MIN) {
			e.setDX(0);
		}
		if (FastMath.abs(e.getDY()) < SPEED_MIN) {
			e.setDY(0);
		}
	}

	/**
	 * Test is movement is valid and, if so, moves to the specified tile.
	 * @param x The x position to move to.
	 * @param y The y position to move to.
	 * @param e The entity to move.
	 * @param w The current world.
	 * @param entities A list of all entities.
	 */
	public void tryMove(double x, double y, Entity e, World w, List<Entity> entities) {
		if (!isCollision(x, y, e, w, entities)) {
			e.setX(x);
			e.setY(y);
		} else {
			doCollision(e, x, y, w, entities);
		}
	}

	/**
	 * Code to run on collision.
	 * @param e The entity to affect.
	 * @param x The x position of attempted movement.
	 * @param y The y position of attempted movement.
	 * @param w The current world.
	 * @param entities A list of all entities.
	 */
	public void doCollision(Entity e, double x, double y, World w, List<Entity> entities) {
		e.setDX(0);
		e.setDY(0);
	}

	/**
	 * A method ran to determine collision.
	 * @param x The x position to move to.
	 * @param y The y position to move to.
	 * @param e The entity to affect.
	 * @param w The current world.
	 * @param entities A list of all entities.
	 * @return {@code true} if collision occurs, {@code false} otherwise.
	 */
	public static boolean isCollision(double x, double y, Entity e, World w, List<Entity> entities) {
		long wx = FastMath.round(x);
		long wy = FastMath.round(y);
		
		if (!e.isSolid()) {
			return false;
		}
		
		Debugger.start("collision");

		List<Shape> hitboxes = new ArrayList<Shape>();
		for (int tx = (int) (Math.round(x) - 1); tx <= (int) (Math.round(x) + 1); tx++) {
			for (int ty = (int) (Math.round(y) - 1); ty <= (int) (Math.round(y) + 1); ty++) {
				Tile t = w.getTile(tx, ty, -1);
				
				if (t.isSolid(w.getTileState(tx, ty, -1))) {
					hitboxes.add(t.getHitBox().transform(Transform.createTranslateTransform(tx, ty)));
				}
			}
		}
		
		for (Entity e2 : entities) {
			if (e2 != e) {
				if(e2.isSolid()) {
					hitboxes.add(e2.getHitBox());
				}
			}
		}
		
		for (Shape s : hitboxes) {
			if (s.contains(e.getHitBox()) || s.intersects(e.getHitBox())) {
				Debugger.stop("collision");
				return true;
			}
		}
		
		if (!w.getTile(wx, wy, 0).isSolid(w.getTileState(wx, wy, 0))) {
			Debugger.stop("collision");
			return true;
		}

		Debugger.stop("collision");
		return false;
	}

	/**
	 * Determines if the entity is above valid ground tiles.
	 * @param e The entity to move.
	 * @param w The current world.
	 * @param entities A list of all entities in the world.
	 * @return {@code true} if the entity is above ground, {@code false} otherwise.
	 */
	public static boolean onGround(Entity e, World w, List<Entity> entities) {
		long wx = FastMath.round(e.getX());
		long wy = FastMath.round(e.getY());

		return w.getTile(wx, wy, 0).isSolid(w.getTileState(wx, wy, 0));
	}

}