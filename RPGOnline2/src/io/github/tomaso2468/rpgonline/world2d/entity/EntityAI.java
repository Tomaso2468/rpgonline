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
package io.github.tomaso2468.rpgonline.world2d.entity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.commons.math3.util.FastMath;
import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.PathFindingContext;
import org.newdawn.slick.util.pathfinding.TileBasedMap;

import io.github.tomaso2468.rpgonline.world2d.Tile;
import io.github.tomaso2468.rpgonline.world2d.World;
import io.github.tomaso2468.rpgonline.world2d.pathfinding.PathFindingManager;

/**
 * An interface representing an EntityAI. One instance of this is shared between all entities of this type.
 * @author Tomaso2468
 *
 */
public interface EntityAI {
	/**
	 * Initialises entity variables.
	 * @param e The entity to initialise.
	 */
	public default void init(Entity e) {
		
	}
	
	/**
	 * Performs AI calculations for an entity.
	 * @param e The entity to compute AI for.
	 * @param w The current world.
	 * @param wind The current wind value.
	 * @param entities A list of all entities in the world.
	 */
	public void doAI(Entity e, World w, float wind, List<Entity> entities);

	/**
	 * A method used to find a path between tiles.
	 * 
	 * @param e              The entity to run pathfinding for.
	 * @param w              The current world.
	 * @param tx             The target X position.
	 * @param ty             The target Y position.
	 * @param searchDistance The distance in tiles to search.
	 * @param entities       A list of all entities in the world.
	 * @param brave          Determines how the entity reacts to danger.
	 * @param tileRegistry   The tile registry for the world.
	 * @param em             The current entity manager.
	 * @return A path from the entities location to the target location or null if
	 *         no path could be found.
	 */
	public default Future<Path> pathfind(Entity e, World w, double tx, double ty, int searchDistance,
			List<Entity> entities, boolean brave, Map<String, Tile> tileRegistry, EntityManager em) {
		// XXX Apologies to any sane being that tries to read this.
		return PathFindingManager
				.pathfind(new AStarPathFinder(
						constructMapFromWorld(e, (int) e.getX(), (int) e.getY(), w, searchDistance + 5,
								getValuedTiles(tileRegistry), getDislikedTiles(tileRegistry),
								getDangerousTiles(tileRegistry), brave, entities, getScaredEntities(em)),
						searchDistance, false), (int) e.getX(), (int) e.getY(), (int) tx, (int) ty);
	}
	
	/**
	 * A method used to locate the nearest entity with the specified ID to this entity.
	 * @param e The entity to compute AI for.
	 * @param entities A list of all entities in the world.
	 * @param entity_id The entity ID to find.
	 * @param searchDistance The distance to search.
	 * @return A entity or null if no entity could be found.
	 */
	public static Entity locateNearest(Entity e, List<Entity> entities, String entity_id, int searchDistance) {
		return locateNearest(e, entities, new Comparable<Entity>() {

			@Override
			public int compareTo(Entity o) {
				return o.getEntityID().compareTo(entity_id);
			}
		}, searchDistance);
	}
	
	/**
	 * A method used to locate the nearest entity with the specified ID to this entity.
	 * @param e The entity to compute AI for.
	 * @param entities A list of all entities in the world.
	 * @param c The condition used to determine entities for eligibility.
	 * @param searchDistance The distance to search.
	 * @return A entity or null if no entity could be found.
	 */
	public static Entity locateNearest(Entity e, List<Entity> entities, Comparable<Entity> c, int searchDistance) {
		Entity nearest = null;
		double ndist = Double.POSITIVE_INFINITY;
		
		for (Entity entity : entities) {
			if(c.compareTo(entity) == 0) {
				double dist = FastMath.hypot(entity.getX() - e.getX(), entity.getY() - e.getY());
				
				if (dist <= searchDistance) {
					if(dist < ndist) {
						nearest = entity;
						ndist = dist;
					}
				}
			}
		}
		
		return nearest;
	}
	
	/**
	 * Computes the distance between 2 points.
	 * @param x1 The X position of the first point.
	 * @param y1 The Y position of the first point.
	 * @param x2 The X position of the second point.
	 * @param y2 The Y position of the second point.
	 * @return The distance between the 2 points.
	 */
	public static double dist(double x1, double y1, double x2, double y2) {
		return FastMath.hypot(x1 - x2, y1 - y2);
	}

	/**
	 * Computes the distance between 2 entities.
	 * @param e The first entity.
	 * @param e2 The second entity.
	 * @return The distance between the 2 entities.
	 */
	public static double dist(Entity e, Entity e2) {
		return dist(e.getX(), e.getY(), e2.getX(), e2.getY());
	}
	
	/**
	 * Computes the square distance between 2 points.
	 * @param x1 The X position of the first point.
	 * @param y1 The Y position of the first point.
	 * @param x2 The X position of the second point.
	 * @param y2 The Y position of the second point.
	 * @return The square distance between the 2 points.
	 */
	public static double sqrdist(double x1, double y1, double x2, double y2) {
		return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
	}

	/**
	 * Computes the square distance between 2 entities.
	 * @param e The first entity.
	 * @param e2 The second entity.
	 * @return The square distance between the 2 entities.
	 */
	public static double sqrdist(Entity e, Entity e2) {
		return sqrdist(e.getX(), e.getY(), e2.getX(), e2.getY());
	}
	
	/**
	 * Get a list of tiles this entity prefers to walk on (used for weighted pathfinding).
	 * @param tileRegistry The tile registry to use.
	 * @return A list of tiles.
	 */
	public default List<Tile> getValuedTiles(Map<String, Tile> tileRegistry) {
		return Collections.emptyList();
	}

	/**
	 * Get a list of tiles this entity dislikes walking on (used for weighted pathfinding).
	 * @param tileRegistry The tile registry to use.
	 * @return A list of tiles.
	 */
	public default List<Tile> getDislikedTiles(Map<String, Tile> tileRegistry) {
		return Collections.emptyList();
	}

	/**
	 * Get a list of tiles that are dangerous to this entity (used for weighted pathfinding).
	 * @param tileRegistry The tile registry to use.
	 * @return A list of tiles.
	 */
	public default List<Tile> getDangerousTiles(Map<String, Tile> tileRegistry) {
		return Collections.emptyList();
	}

	/**
	 * Gets all entities this entity is avoiding.
	 * @param m The entity manager to use.
	 * @return A list of entity_ids.
	 */
	public default List<String> getScaredEntities(EntityManager m) {
		return Collections.emptyList();
	}

	/**
	 * Constructs a weighted map from a world for use in pathfinding.
	 * @param e The entity to pathfind for.
	 * @param cx The X position of the entity.
	 * @param cy The Y position of the entity.
	 * @param w The world to use.
	 * @param searchDistance The distance to search.
	 * @param valued The list of valued tiles.
	 * @param disliked The list of disliked tiles.
	 * @param dangerous The list of dangerous tiles.
	 * @param brave Determines if the entity will walk on dangerous tiles.
	 * @param entities A list of all entities in the world.
	 * @param scared_entities The list of entity types this entity is avoiding.
	 * @return A {@code TileBasedMap} instance.
	 */
	public static TileBasedMap constructMapFromWorld(Entity e, final int cx, final int cy, final World w,
			final int searchDistance, final List<Tile> valued, final List<Tile> disliked, final List<Tile> dangerous,
			final boolean brave, final List<Entity> entities, final List<String> scared_entities) {
		final int offsetX = cx - searchDistance;
		final int offsetY = cy - searchDistance;
		return new TileBasedMap() {
			@Override
			public int getWidthInTiles() {
				return searchDistance * 2;
			}

			@Override
			public int getHeightInTiles() {
				return searchDistance * 2;
			}

			@Override
			public void pathFinderVisited(int x, int y) {

			}

			@Override
			public boolean blocked(PathFindingContext context, int tx, int ty) {
				if(e.isFlying()) {
					return false;
				}
				return w.getTile(tx + offsetX, ty + offsetY, -1).isSolid(w.getTileState(tx + offsetX, ty + offsetY, -1))
						|| !w.getTile(tx + offsetX, ty + offsetY, 0)
								.isSolid(w.getTileState(tx + offsetX, ty + offsetY, 0));
			}

			@Override
			public float getCost(PathFindingContext context, int tx, int ty) {
				float ex = tx + offsetX;
				float ey = ty + offsetY;

				Tile t = w.getTile(tx + offsetX, ty + offsetY, -1);

				float c = 1;

				if(!e.isFlying()) {
					if (valued.contains(t)) {
						c *= 0.75f;
					}

					if (disliked.contains(t)) {
						c *= brave ? 2.5f : 5f;
					}

					if (dangerous.contains(t)) {
						c *= brave ? 50f : 200f;
					}
				}

				for (Entity e2 : entities) {
					if (e2.isSolid()) {
						if (FastMath.hypot(e2.getX() - ex, e2.getY() - ey) < 0.25) {
							c *= 2f;
						}
					}
					if (scared_entities.contains(e2.getEntityID())) {
						c *= FastMath.max(1 / FastMath.hypot(e2.getX() - ex, e2.getY() - ey), 1);
					}
				}

				return c;
			}
		};
	}
}
