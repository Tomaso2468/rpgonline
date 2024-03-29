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
package io.github.tomaso2468.rpgonline.world2d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.math3.util.FastMath;
import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Transform;

import io.github.tomaso2468.rpgonline.BaseScaleState;
import io.github.tomaso2468.rpgonline.Game;
import io.github.tomaso2468.rpgonline.GameState;
import io.github.tomaso2468.rpgonline.Image;
import io.github.tomaso2468.rpgonline.RPGConfig;
import io.github.tomaso2468.rpgonline.UpdateHook;
import io.github.tomaso2468.rpgonline.audio.AmbientMusic;
import io.github.tomaso2468.rpgonline.debug.Debugger;
import io.github.tomaso2468.rpgonline.gui.GUI;
import io.github.tomaso2468.rpgonline.input.Input;
import io.github.tomaso2468.rpgonline.input.InputUtils;
import io.github.tomaso2468.rpgonline.lighting.LightingDisabled;
import io.github.tomaso2468.rpgonline.lighting.LightingEngine;
import io.github.tomaso2468.rpgonline.net.ServerManager;
import io.github.tomaso2468.rpgonline.particle.Particle;
import io.github.tomaso2468.rpgonline.post.HDRPost;
import io.github.tomaso2468.rpgonline.post.PostProcessing;
import io.github.tomaso2468.rpgonline.render.Graphics;
import io.github.tomaso2468.rpgonline.render.RenderException;
import io.github.tomaso2468.rpgonline.render.RenderMode;
import io.github.tomaso2468.rpgonline.render.Renderer;
import io.github.tomaso2468.rpgonline.sky.SkyLayer;
import io.github.tomaso2468.rpgonline.world2d.entity.Entity;
import io.github.tomaso2468.rpgonline.world2d.net.Client2D;
import io.github.tomaso2468.rpgonline.world2d.texture.TileTexture;
import io.github.tomaso2468.rpgonline.world2d.texture.WindTexture;
import io.github.tomaso2468.rpgonline.world2d.texture.entity.EntityTexture;

/**
 * A state for rendering worlds
 * 
 * @author Tomaso2468
 */
public class WorldState implements GameState, BaseScaleState {
	/**
	 * This state's id.
	 */
	private long id;
	/**
	 * The cached {@code x} position of the player.
	 */
	protected double x;
	/**
	 * The cached {@code y} position of the player.
	 */
	protected double y;
	/**
	 * Previous X position of the player used for velocity calculations.
	 */
	protected double px;
	/**
	 * Previous Y position of the player used for velocity calculations.
	 */
	protected double py;
	/**
	 * The world zoom.
	 */
	protected float zoom = 1f;
	/**
	 * The scale to scale the graphics by. This also effects GUI.
	 */
	protected float base_scale = 1f;
	/**
	 * The GUI to use.
	 */
	protected GUI guis = null;
	/**
	 * A list of tasks to run on game update.
	 */
	protected List<UpdateHook> hooks = new ArrayList<UpdateHook>();
	/**
	 * A buffer for shader effects.
	 */
	protected Image buffer;

	/**
	 * A second buffer for shader effects.
	 */
	protected Image buffer2;

	/**
	 * The GUI toggle.
	 */
	protected boolean gui = true;

	/**
	 * Cooldown for GUI buttons.
	 */
	protected float gui_cooldown = 0.25f;

	/**
	 * The background for this world.
	 */
	protected SkyLayer sky;

	/**
	 * Currently displaying particles.
	 */
	protected List<Particle> particles = Collections.synchronizedList(new ArrayList<Particle>(128));

	/**
	 * If shaders are enabled.
	 */
	protected boolean post_enable = true;

	/**
	 * The post processing effect to use for this state.
	 */
	protected PostProcessing post;
	
	/**
	 * The lighting engine for this state.
	 */
	protected LightingEngine lighting = new LightingDisabled();

	/**
	 * Creates a new {@code WorldState}.
	 * 
	 * @param id the ID of the state.
	 */
	public WorldState(long id) {
		this.id = id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(Game game) throws RenderException {
		if (guis != null) {
			guis.init(game, game.getWidth(), game.getHeight(), base_scale);
		}
	}

	/**
	 * The HDR map for this world state.
	 */
	protected PostProcessing tonemapping;

	public void renderPostEffects(Game game, Image buffer, Renderer renderer) throws RenderException {
		Debugger.start("effects");
		if (buffer2 == null) {
			buffer2 = new Image(renderer, game.getWidth(), game.getHeight());
		} else if (game.getWidth() != buffer2.getWidth() || game.getHeight() != buffer2.getHeight()) {
			buffer2.destroy();
			buffer2 = new Image(renderer, game.getWidth(), game.getHeight());
		}
		renderer.setRenderTarget(buffer2);
		if (game.isClearEveryFrame())
			renderer.clear();
		if (post != null) {
			Debugger.start("post-" + post.getClass());
			post.postProcess(buffer, buffer2, renderer);
			Debugger.stop("post-" + post.getClass());
		} else {
			renderer.drawImage(buffer, 0, 0);
		}
		renderer.setRenderTarget(null);
		if (!renderer.isBuiltInTonemapping()) {
			Debugger.start("hdr");

			if (tonemapping == null) {
				tonemapping = new HDRPost();
			}
			tonemapping.postProcess(buffer2, null, renderer);

			Debugger.stop("hdr");
		} else {
			renderer.drawImage(buffer2, 0, 0);
		}

		Debugger.stop("effects");
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void render(Game game, Renderer renderer) throws RenderException {
		Debugger.start("render");

		if (post_enable) {
			if (buffer == null) {
				buffer = new Image(renderer, game.getWidth(), game.getHeight());
			} else if (game.getWidth() != buffer.getWidth() || game.getHeight() != buffer.getHeight()) {
				buffer.destroy();
				buffer = new Image(renderer, game.getWidth(), game.getHeight());
			}
			renderer.setRenderTarget(buffer);
			if (game.isClearEveryFrame())
				renderer.clear();
		}

		Debugger.start("game-render");
		render2(game, renderer);
		Debugger.stop("game-render");

		renderer.resetTransform();

		if (post_enable) {
			renderPostEffects(game, buffer, renderer);
		}

		renderGUI(game, renderer);

		Debugger.stop("render");
	}
	
	public void renderGUI(Game game, Renderer renderer) throws RenderException {
		if (gui && guis != null) {
			Debugger.start("gui");

			renderer.resetTransform();

			Graphics g2 = renderer.getGUIGraphics();

			game.getTheme().predraw(game, g2);
			guis.paint(game, g2, base_scale);

			Debugger.stop("gui");
		}
	}

	/**
	 * A reusable list of tile textures for faster rendering.
	 */
	protected List<TileTexture> textures = new ArrayList<>();
	/**
	 * A reusable list of entity textures for faster rendering.
	 */
	protected List<EntityTexture> entityTextures = new ArrayList<>();

	/**
	 * Computes lighting.
	 * 
	 * @return A list of lights or null if lighting is off.
	 */
	public List<LightSource> computeLights() {
		Debugger.start("light-compute");

		List<LightSource> lights = ((Client2D) ServerManager.getClient()).getWorld().getLights();

		if (lights.size() != 0) {
			lights.sort(new Comparator<LightSource>() {
				@Override
				public int compare(LightSource o1, LightSource o2) {
					double dist1 = FastMath.hypot(x - o1.getLX(), y - o1.getLY());
					double dist2 = FastMath.hypot(x - o2.getLX(), y - o2.getLY());

					if (dist1 < dist2) {
						return -1;
					}

					if (dist1 > dist2) {
						return 1;
					}

					return 0;
				}
			});

			for (int i = 0; i < lights.size(); i++) {
				if (i < lights.size()) {
					LightSource l = lights.get(i);
					double dist = FastMath.hypot(x - l.getLX(), y - l.getLY());
					if (dist > 50 * l.getBrightness()) {
						lights.remove(l);
						i -= 1;
					}
				}
			}

			while (lights.size() > 50) {
				lights.remove(lights.size() - 1);
			}
		}

		Debugger.stop("light-compute");

		return lights;
	}

	protected void setupTransform(Game game, Renderer renderer) throws RenderException {
		renderer.translate2D(game.getWidth() / 2, game.getHeight() / 2);

		renderer.scale2D(zoom * base_scale, zoom * base_scale);
	}

	protected void preRenderLighting(Game game, Renderer renderer, List<LightSource> lights, World world, float sx,
			float sy) throws RenderException {
		Debugger.start("lighting");
		lighting.preRender(game, renderer, lights, world, sx, sy, zoom, base_scale);
		Debugger.stop("lighting");
	}

	protected List<Entity> computeEntities(Game game, float dist_x, float dist_y, Rectangle screen_bounds) {
		Debugger.start("entity-compute");
		List<Entity> entities1 = ((Client2D) ServerManager.getClient()).getWorld().getEntities();
		List<Entity> entities = new ArrayList<Entity>();

		synchronized (entities1) {
			for (Entity e : entities1) {
				if (screen_bounds.contains((float) e.getX(), (float) e.getY())) {
					entities.add(e);
				}
			}
		}
		Debugger.stop("entity-compute");

		return entities;
	}

	@SuppressWarnings("unchecked")
	protected List<Particle>[] computeParticles(Game game, float dist_x, float dist_y, Rectangle screen_bounds) {
		List<Particle> particles_light = null;
		List<Particle> particles_nolight = null;

		if (RPGConfig.isParticles()) {
			Debugger.start("particle-compute");
			particles_light = new ArrayList<Particle>(particles.size());
			particles_nolight = new ArrayList<Particle>(32);

			for (int i = 0; i < particles.size(); i++) {
				Particle p = particles.get(i);

				if (screen_bounds.contains(p.getX(), p.getY())) {
					if (p.isLightAffected()) {
						particles_light.add(p);
					} else {
						particles_nolight.add(p);
					}
				}
			}
			Debugger.stop("particle-compute");
		}

		return new List[] { particles_light, particles_nolight };
	}

	protected Image renderTile(Game game, Renderer renderer, float dist_x, float dist_y, World world, float sx,
			float sy, Tile t, String state, Image current, long x, long y, long z, float wind) throws RenderException {
		expandTexture(t.getTexture(), textures, x, y, z, world, t, state);

		for (TileTexture tex : textures) {
			if (tex.isCustom()) {
				// Optimise wind textures to use embedded drawing.
				if (tex instanceof WindTexture) {
					Image img = game.getTextures().getTexture(tex.getTexture(x, y, z, world, state, t));

					if (img != null) {
						if (game.getTextures().getSheet(img) != current) {
							if (current != null)
								renderer.endUse(current);
							current = game.getTextures().getSheet(img);
							renderer.startUse(current);
						}
						float amount = ((WindTexture) tex).windAmount(x, y, wind);
						renderer.renderShearedEmbedded(img, x * RPGConfig.getTileSize() + tex.getX() - sx - amount,
								y * RPGConfig.getTileSize() + tex.getY() - sy, img.getWidth(), img.getHeight(), amount,
								0);
					}
				} else {
					Debugger.start("custom-tile");
					if (current != null)
						renderer.endUse(current);

					tex.render(game, renderer, x, y, z, world, state, t, x * RPGConfig.getTileSize() + tex.getX() - sx,
							y * RPGConfig.getTileSize() + tex.getY() - sy, wind);

					if (current != null)
						renderer.startUse(current);
					Debugger.stop("custom-tile");
				}
			} else {
				Image img = game.getTextures().getTexture(tex.getTexture(x, y, z, world, state, t));

				if (img != null) {
					if (game.getTextures().getSheet(img) != current) {
						if (current != null)
							renderer.endUse(current);
						current = game.getTextures().getSheet(img);
						renderer.startUse(current);
					}
					renderer.renderEmbedded(img, x * RPGConfig.getTileSize() + tex.getX() - sx,
							y * RPGConfig.getTileSize() + tex.getY() - sy, img.getWidth(), img.getHeight());
				}
			}
		}

		textures.clear();

		return current;
	}

	protected Image renderWorld(Game game, Renderer renderer, float dist_x, float dist_y, World world, float sx,
			float sy, List<Entity> entities) throws RenderException {
		Debugger.start("world");
		long mix = (long) (x - dist_x);
		long max = (long) (x + dist_x);
		long miy = (long) (y - dist_y);
		long may = (long) (y + dist_y);
		long miz = world.getMinZ();
		long maz = world.getMaxZ();

		float wind = ((Client2D) ServerManager.getClient()).getWind();

		Image current = null;

		for (long z = maz; z >= FastMath.min(-1, miz); z--) {
			for (long y = miy; y <= may; y++) {
				for (long x = mix; x <= max; x++) {
					Tile t = world.getTile(x, y, z);
					String state = world.getTileState(x, y, z);

					current = renderTile(game, renderer, dist_x, dist_y, world, sx, sy, t, state, current, x, y, z,
							wind);

					current = renderEntitiesAtTile(game, renderer, dist_x, dist_y, world, sx, sy, entities, current,
							wind, x, y, z);
				}
			}
		}

		current = renderEntitiesFlying(game, renderer, dist_x, dist_y, world, sx, sy, entities, current, wind);

		Debugger.stop("world");

		return current;
	}

	protected Image renderEntitiesAtTile(Game game, Renderer renderer, float dist_x, float dist_y, World world,
			float sx, float sy, List<Entity> entities, Image current, float wind, long x, long y, long z) {
		if (z == -1) {
			Debugger.start("entity");
			for (Entity e : entities) {
				if (!e.isFlying()) {
					if (FastMath.round(e.getX() + 0.5f) == x && FastMath.floor(e.getY() - 0.25) == y) {
						entityTextures.clear();

						current = renderEntity(game, renderer, dist_x, dist_y, world, sx, sy, e, current, wind);
					}
				}
			}
			Debugger.stop("entity");
		}

		return current;
	}

	protected Image renderEntity(Game game, Renderer renderer, float dist_x, float dist_y, World world, float sx,
			float sy, Entity e, Image current, float wind) {
		expandTexture(e.getTexture(), entityTextures, e.getX(), e.getY(), world, wind, e);

		for (EntityTexture tex : entityTextures) {
			if (tex.isCustom()) {
				if (current != null)
					renderer.endUse(current);

				tex.render(game, renderer, x, y, -3, world, e, sx, sy, wind);

				if (current != null)
					renderer.startUse(current);
			} else {
				Image img = game.getTextures().getTexture(tex.getTexture(e.getX(), e.getY(), -3, world, e, wind));

				if (img != null) {
					if (game.getTextures().getSheet(img) != current) {
						if (current != null)
							renderer.endUse(current);
						current = game.getTextures().getSheet(img);
						renderer.startUse(current);
					}
					renderer.renderEmbedded(img, (float) e.getX() * RPGConfig.getTileSize() + tex.getX() - sx,
							(float) e.getY() * RPGConfig.getTileSize() + tex.getY() - sy, img.getWidth(),
							img.getHeight());
				}
			}
		}

		return current;
	}

	protected Image renderEntitiesFlying(Game game, Renderer renderer, float dist_x, float dist_y, World world,
			float sx, float sy, List<Entity> entities, Image current, float wind) {
		Debugger.start("entity");
		for (Entity e : entities) {
			if (e.isFlying()) {
				entityTextures.clear();

				current = renderEntity(game, renderer, dist_x, dist_y, world, sx, sy, e, current, wind);
			}
		}
		Debugger.stop("entity");

		return current;
	}

	protected Image renderParticles(Game game, Renderer renderer, List<Particle> particles, Image current, float sx,
			float sy) {
		if (RPGConfig.isParticles()) {
			Debugger.start("particles");
			for (Particle particle : particles) {
				if (particle.isCustom()) {
					if (current != null)
						renderer.endUse(current);
					particle.render(game, renderer, particle.getX() * RPGConfig.getTileSize() - sx,
							particle.getY() * RPGConfig.getTileSize() - sy);
					if (current != null)
						renderer.startUse(current);
				} else {
					Image img = game.getTextures().getTexture(particle.getTexture());

					if (img != null) {
						if (game.getTextures().getSheet(img) != current) {
							if (current != null)
								renderer.endUse(current);
							current = game.getTextures().getSheet(img);
							renderer.startUse(current);
						}
						renderer.bindColor(new Color(1, 1, 1, particle.getAlpha()));
						renderer.renderEmbedded(img, particle.getX() * RPGConfig.getTileSize() - sx,
								particle.getY() * RPGConfig.getTileSize() - sy, img.getWidth(), img.getHeight());
					}
				}
			}

			Debugger.stop("particles");
		}

		return current;
	}

	protected void doLighting(Game game, Renderer renderer, List<LightSource> lights, World world, float sx, float sy)
			throws RenderException {
		Debugger.start("lighting");
		lighting.postRender(game, renderer, lights, world, sx, sy, zoom, base_scale);
		Debugger.stop("lighting");
	}

	protected void renderHitboxes(Game game, Renderer renderer, World world, List<Entity> entities, long dist_x,
			long dist_y, float sx, float sy) {
		if (RPGConfig.isHitbox()) {
			Debugger.start("hitbox");

			renderer.setMode(RenderMode.MODE_2D_LINES_NOVBO);

			renderer.translate2D(game.getWidth() / 2, game.getHeight() / 2);

			renderer.scale2D(zoom * base_scale, zoom * base_scale);

			long mix = (long) (x - dist_x);
			long max = (long) (x + dist_x);
			long miy = (long) (y - dist_y);
			long may = (long) (y + dist_y);
			for (long y = miy; y <= may; y++) {
				for (long x = mix; x <= max; x++) {
					Tile t = world.getTile(x, y, -1);
					String state = world.getTileState(x, y, -1);

					if (t.isSolid(state)) {
						boolean collision = false;
						for (Entity e : entities) {
							if (((x - e.getX()) * (x - e.getX()) + (y - e.getY()) * (y - e.getY())) <= (4 * 4)) {
								if (e.isSolid()) {
									collision = true;
									break;
								}

							}
						}
						if (collision) {
							renderer.draw(t.getHitBox()
									.transform(Transform.createScaleTransform(RPGConfig.getTileSize(),
											RPGConfig.getTileSize()))
									.transform(Transform.createTranslateTransform(x * RPGConfig.getTileSize() - sx,
											y * RPGConfig.getTileSize() - sy)),
									Color.orange);
						}
					}
				}
			}

			for (Entity e : entities) {
				renderer.draw(e.getHitBox()
						.transform(Transform.createScaleTransform(RPGConfig.getTileSize(), RPGConfig.getTileSize()))
						.transform(Transform.createTranslateTransform(-sx, -sy)), Color.red);
			}
			Debugger.stop("hitbox");
		}
	}

	/**
	 * A method that renders the world.
	 */
	public void render2(Game game, Renderer renderer) throws RenderException {
		List<LightSource> lights = computeLights();

		World world = ((Client2D) ServerManager.getClient()).getWorld();
		float sx = (float) (x * RPGConfig.getTileSize());
		float sy = (float) (y * RPGConfig.getTileSize());

		preRenderLighting(game, renderer, lights, world, sx, sy);

		Debugger.start("sky");
		if (sky != null) {
			sky.render(renderer, game, x, y, 0, world, Color.white);
		}
		Debugger.stop("sky");

		setupTransform(game, renderer);

		long dist_x = (long) (game.getWidth() / base_scale / zoom / RPGConfig.getTileSize() / 2) + 3;
		long dist_y = (long) (game.getHeight() / base_scale / zoom / RPGConfig.getTileSize() / 2) + 7;

		Rectangle screen_bounds = new Rectangle((float) (x - dist_x), (float) (y - dist_y), (float) (dist_x * 2),
				(float) (dist_y * 2));

		List<Entity> entities = computeEntities(game, dist_x, dist_y, screen_bounds);

		List<Particle>[] particles = computeParticles(game, dist_x, dist_y, screen_bounds);
		List<Particle> particles_light = particles[0];
		List<Particle> particles_nolight = particles[1];

		Image current = renderWorld(game, renderer, dist_x, dist_y, world, sx, sy, entities);

		current = renderParticles(game, renderer, particles_light, current, sx, sy);

		if (current != null) {
			renderer.endUse(current);
		}
		current = null;

		renderer.resetTransform();

		doLighting(game, renderer, lights, world, sx, sy);

		renderer.resetTransform();

		renderParticles(game, renderer, particles_nolight, current, sx, sy);

		renderHitboxes(game, renderer, world, entities, dist_x, dist_y, sx, sy);
	}

	/**
	 * Expands a single tile texture into all its subtextures.
	 * 
	 * @param t     The texture to expand.
	 * @param l     The list to expand into.
	 * @param x     The tile X position.
	 * @param y     The tile Y position.
	 * @param z     The tile Z position.
	 * @param world The current world.
	 * @param tile  The current tile.
	 * @param state The tile's current state.
	 */
	protected void expandTexture(TileTexture t, List<TileTexture> l, long x, long y, long z, World world, Tile tile,
			String state) {
		if (t.isPure()) {
			l.add(t);
		} else {
			TileTexture[] ta = t.getTextures(x, y, z, world, state, tile);

			for (TileTexture t2 : ta) {
				expandTexture(t2, l, x, y, z, world, tile, state);
			}
		}
	}

	/**
	 * Expands a single entity texture into all its subtextures.
	 * 
	 * @param t     The texture to expand.
	 * @param l     The list to expand into.
	 * @param x     The entity X position.
	 * @param y     The entity Y position.
	 * @param world The current world.
	 * @param wind  The current wind.
	 * @param e     The current entity.
	 */
	protected void expandTexture(EntityTexture t, List<EntityTexture> l, double x, double y, World world, float wind,
			Entity e) {
		if (t.isPure()) {
			l.add(t);
		} else {
			EntityTexture[] ta = t.getTextures(x, y, -1, world, e, wind);

			for (EntityTexture t2 : ta) {
				expandTexture(t2, l, x, y, world, wind, e);
			}
		}
	}

	/**
	 * Mouse positions.
	 */
	private float mx, my = 0;

	/**
	 * Updates the GUI.
	 * 
	 * @param in        The current input.
	 * @param container The current game container.
	 * @param game      The game.
	 * @param delta     The delta value.
	 */
	public void updateGUI(Input in, Game game, float delta) {
		if (guis != null) {
			Debugger.start("gui");
			if (gui) {
				Debugger.start("gui-container");
				guis.containerUpdate(game);
				Debugger.stop("gui-container");

				Debugger.start("gui-mouse");
				float ox = mx;
				float oy = my;

				mx = in.getMouseX();
				my = in.getMouseY();

				if (mx != ox || my != oy) {
					guis.mouseMoved(mx / base_scale, my / base_scale);
				}

				boolean[] data = new boolean[Math.max(3, in.getButtonCount())];
				for (int i = 0; i < in.getButtonCount(); i++) {
					data[i] = in.isButtonDown(i);
				}
				guis.mouseState(mx / base_scale, my / base_scale, data);

				if (in.hasWheel()) {
					guis.mouseWheel(in.getDWheel());
				}
				Debugger.stop("gui-mouse");

				Debugger.start("gui-update");
				guis.update(delta / 1000f, in);
				Debugger.stop("gui-update");
			}
			Debugger.stop("gui");
		}

		gui_cooldown -= delta;
		if (InputUtils.isActionPressed(in, InputUtils.GUI_TOGGLE) && gui_cooldown <= 0) {
			gui = !gui;
			gui_cooldown = 0.25f;
		}
		if (in.isKeyDown(Input.KEY_F3) && gui_cooldown <= 0) {
			RPGConfig.setDebug(!RPGConfig.isDebug());
			gui_cooldown = 0.25f;
		}
		if (in.isKeyDown(Input.KEY_F4) && gui_cooldown <= 0) {
			RPGConfig.setHitbox(!RPGConfig.isHitbox());
			gui_cooldown = 0.25f;
		}
	}

	/**
	 * Updates the GUI controls.
	 * 
	 * @param in
	 * @param container The game container.
	 * @param game      The game.
	 * @param delf      The delta value in seconds.
	 * @throws SlickException If an error occurs.
	 */
	public void updateControls(Input in, Game game, float delta) throws RenderException {
		Debugger.start("input");

		double walk_x = 0;
		double walk_y = 0;

		Debugger.start("keyboard");
		if (in.isKeyDown(RPGConfig.getKeyInput().getKeyCodeForAction(InputUtils.WALK_NORTH))) {
			walk_y += -1;
		}
		if (in.isKeyDown(RPGConfig.getKeyInput().getKeyCodeForAction(InputUtils.WALK_SOUTH))) {
			walk_y += 1;
		}

		if (in.isKeyDown(RPGConfig.getKeyInput().getKeyCodeForAction(InputUtils.WALK_EAST))) {
			walk_x += 1;
		}
		if (in.isKeyDown(RPGConfig.getKeyInput().getKeyCodeForAction(InputUtils.WALK_WEST))) {
			walk_x -= 1;
		}
		Debugger.stop("keyboard");

		if (walk_x > 1) {
			walk_x = 1;
		}
		if (walk_x < -1) {
			walk_x = -1;
		}
		if (walk_y > 1) {
			walk_y = 1;
		}
		if (walk_y < -1) {
			walk_y = -1;
		}

		((Client2D) ServerManager.getClient()).walkX(walk_x, delta);
		((Client2D) ServerManager.getClient()).walkY(walk_y, delta);

		((Client2D) ServerManager.getClient()).setSprint(InputUtils.isActionPressed(in, InputUtils.SPRINT));

		x = ((Client2D) ServerManager.getClient()).getPlayerX();
		y = ((Client2D) ServerManager.getClient()).getPlayerY();

		if (InputUtils.isActionPressed(in, InputUtils.EXIT)) {
			game.exit(0);
		}

		if (in.isKeyDown(Keyboard.KEY_SPACE)) {
			post_enable = false;
		} else {
			post_enable = true;
		}

		if (in.isKeyDown(Input.KEY_EQUALS)) {
			zoom *= 1 + (1 * delta);
		}
		if (in.isKeyDown(Input.KEY_MINUS)) {
			zoom *= 1 - (1 * delta);
		}
		if (in.isKeyDown(RPGConfig.getKeyInput().getKeyCodeForAction(InputUtils.WALK_WEST))) {
			walk_x -= 1;
		}

		Debugger.stop("input");
	}

	/**
	 * Updates the graphical effects.
	 * 
	 * @param delf The delta value in seconds.
	 * @throws SlickException If an error occurs.
	 */
	public void updateEffects(float delta) throws RenderException {
		Debugger.start("effects");

		Debugger.start("particles");
		float wind = ((Client2D) ServerManager.getClient()).getWind();
		World world = ((Client2D) ServerManager.getClient()).getWorld();
		for (int i = 0; i < particles.size(); i++) {
			particles.get(i).doBehaviour(world, wind, particles, delta);
		}

		particles.sort(new Comparator<Particle>() {
			@Override
			public int compare(Particle o1, Particle o2) {
				int a = o1.getClass().equals(o2.getClass()) ? 0 : 1;

				if (!o1.isLightAffected()) {
					a += 2;
				}

				return a;
			}
		});
		Debugger.stop("particles");

		Debugger.stop("effects");
	}

	/**
	 * Updates the world.
	 * 
	 * @param delf The delta value in seconds.
	 */
	public void updateWorld(float delta) {
		Debugger.start("world");
		((Client2D) ServerManager.getClient()).getWorld().doUpdateClient();
		Debugger.stop("world");
	}

	/**
	 * Updates the state hooks.
	 * 
	 * @param container The game container.
	 * @param game      The game.
	 * @param delta     The delta value.
	 * @throws SlickException If an error occurs.
	 */
	public void updateHooks(Game game, float delta) throws RenderException {
		Debugger.start("hooks");
		for (UpdateHook hook : hooks) {
			hook.update(game, delta);
		}
		Debugger.stop("hooks");
	}

	/**
	 * Updates the audio engine.
	 * 
	 * @param container The game container.
	 * @param game      The game.
	 * @param delta     The delta value.
	 * @throws SlickException If an error occurs.
	 */
	public void updateAudio(Game game, float delta) throws RenderException {
		Debugger.start("audio");
		AmbientMusic music = ServerManager.getClient().getMusic();
		game.getAudio().setMusic(music);

		game.getAudio().setPlayerPos((float) x, 0, (float) y);
		game.getAudio().setPlayerVelocity((float) (x - px) / delta, 0, (float) (y - py) / delta);
		Debugger.stop("audio");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(Game game, float delta) throws RenderException {
		Debugger.initRender();

		Input in = game.getInput();

		updateControls(in, game, delta);
		updateWorld(delta);
		updateGUI(in, game, delta);
		updateHooks(game, delta);
		updateAudio(game, delta);
		updateEffects(delta);

		px = x;
		py = y;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getID() {
		return id;
	}

	/**
	 * Gets the current GUI.
	 * 
	 * @return A GUI object.
	 */
	public GUI getGUI() {
		return guis;
	}

	/**
	 * Sets the current GUI.
	 * 
	 * @param gui a GUI object.
	 */
	public void setGUI(GUI gui) {
		this.guis = gui;
	}

	/**
	 * Adds a task to perform during a game update.
	 * 
	 * @param e An update hook.
	 */
	public void addHook(UpdateHook e) {
		hooks.add(e);
	}

	/**
	 * Removes a task from the state.
	 * 
	 * @param o An update hook.
	 */
	public void removeHook(UpdateHook o) {
		hooks.remove(o);
	}

	/**
	 * Adds an task at a specific point.
	 * 
	 * @param index   The index of the element.
	 * @param element An update hook.
	 */
	public void addHook(int index, UpdateHook element) {
		hooks.add(index, element);
	}

	/**
	 * Gets the current shader effect.
	 * 
	 * @return A {@code PostProcessing} object.
	 */
	public PostProcessing getPost() {
		return post;
	}

	/**
	 * Sets the current shader effect.
	 * 
	 * @param post A {@code PostProcessing} object.
	 */
	public void setPost(PostProcessing post) {
		this.post = post;
	}
	
	/**
	 * Gets the current tone mapping effect.
	 * 
	 * @return A {@code PostProcessing} object.
	 */
	public PostProcessing getToneMapping() {
		return tonemapping;
	}

	/**
	 * Sets the current tone mapping effect.
	 * 
	 * @param post A {@code PostProcessing} object.
	 */
	public void setToneMapping(PostProcessing post) {
		this.tonemapping = post;
	}

	/**
	 * Determines if the GUI is currently displayed.
	 * 
	 * @return {@code true} if the GUI is enabled, {@code false} otherwise.
	 */
	public boolean isGuiShown() {
		return gui;
	}

	/**
	 * Sets if the GUI is currently displayed.
	 * 
	 * @param gui {@code true} if the GUI should be enabled, {@code false}
	 *            otherwise.
	 */
	public void setGuiShown(boolean gui) {
		this.gui = gui;
	}

	/**
	 * Gets the current sky.
	 * 
	 * @return A {@code SkyLayer} or {@code null} if no sky is set.
	 */
	public SkyLayer getSky() {
		return sky;
	}

	/**
	 * Sets the current sky.
	 * 
	 * @param sky A {@code SkyLayer} or {@code null} to disable sky rendering.
	 */
	public void setSky(SkyLayer sky) {
		this.sky = sky;
	}

	/**
	 * Adds a particle to this state.
	 * 
	 * @param p The particle to add.
	 */
	public void addParticle(Particle p) {
		particles.add(p);
	}

	/**
	 * Adds a collection of particles to this state.
	 * 
	 * @param p The particles to add.
	 */
	public void addParticles(Collection<? extends Particle> p) {
		particles.addAll(p);
	}

	/**
	 * Clears all particles.
	 */
	public void clearParticles() {
		particles.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void scale(Game game, float base) {
		base_scale = base;
		if (guis != null)
			guis.init(game, game.getWidth(), game.getHeight(), base_scale);
	}

	/**
	 * Gets the zoom for this state.
	 * @return A float value.
	 */
	public float getZoom() {
		return zoom;
	}

	/**
	 * Sets the zoom for this state.
	 * @param zoom A float value.
	 */
	public void setZoom(float zoom) {
		this.zoom = zoom;
	}

	/**
	 * Gets the lighting engine for this state.
	 * @return A lighting engine object.
	 */
	public LightingEngine getLighting() {
		return lighting;
	}

	/**
	 * Sets the lighting engine for this state.
	 * @param lighting A lighting engine object.
	 */
	public void setLighting(LightingEngine lighting) {
		this.lighting = lighting;
	}
}
