package io.github.tomaso2468.rpgonline.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.math3.util.FastMath;
import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Transform;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import io.github.tomaso2468.rpgonline.RPGConfig;
import io.github.tomaso2468.rpgonline.audio.AmbientMusic;
import io.github.tomaso2468.rpgonline.audio.AudioManager;
import io.github.tomaso2468.rpgonline.debug.Debugger;
import io.github.tomaso2468.rpgonline.entity.Entity;
import io.github.tomaso2468.rpgonline.input.InputUtils;
import io.github.tomaso2468.rpgonline.net.Client2D;
import io.github.tomaso2468.rpgonline.net.ServerManager;
import io.github.tomaso2468.rpgonline.part.Particle;
import io.github.tomaso2468.rpgonline.post.MultiEffect;
import io.github.tomaso2468.rpgonline.post.NullPostProcessEffect;
import io.github.tomaso2468.rpgonline.post.PostEffect;
import io.github.tomaso2468.rpgonline.sky.SkyLayer;
import io.github.tomaso2468.rpgonline.texture.TextureMap;
import io.github.tomaso2468.rpgonline.texture.TileTexture;
import io.github.tomaso2468.rpgonline.texture.entity.EntityTexture;
import io.github.tomaso2468.rpgonline.tile.Tile;
import io.github.tomaso2468.rpgonline.world.LightSource;
import io.github.tomaso2468.rpgonline.world.World;

/**
 * A state for rendering worlds
 * 
 * @author Tomas
 */
public class WorldState extends BasicGameState implements BaseScaleState {
	/**
	 * This state's id.
	 */
	private int id;
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
	public float zoom = 1f;
	/**
	 * The strength of the shake effect.
	 */
	public float shake = 0f;
	/**
	 * The scale to scale the graphics by. This also effects GUI.
	 */
	public float base_scale = 1f;
	/**
	 * A list of GUI elements.
	 */
	private List<GUIItem> guis = new ArrayList<GUIItem>();
	/**
	 * A list of tasks to run on game update.
	 */
	private List<UpdateHook> hooks = new ArrayList<UpdateHook>();
	/**
	 * The last game delta.
	 */
	public int last_delta;
	/**
	 * A buffer for shader effects.
	 */
	protected Image buffer;

	/**
	 * A second (smaller) buffer for pixel perfect effects.
	 */
	protected Image buffer2;

	/**
	 * A buffer for lighting.
	 */
	protected Image lightBuffer;

	/**
	 * The current shader effect.
	 */
	protected PostEffect post = null;

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
	 * Creates a new {@code WorldState}.
	 * 
	 * @param id the ID of the state.
	 */
	public WorldState(int id) {
		this.id = id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		Debugger.start("render");

		Debugger.start("game-render");
		render2(container, game, g);
		Debugger.stop("game-render");

		g.resetTransform();

		if (RPGConfig.isSnapToPixel() && post_enable) {
			Debugger.start("snap");
			if (buffer == null) {
				buffer = new Image(container.getWidth(), container.getHeight());
			} else if (container.getWidth() != buffer.getWidth() || container.getHeight() != buffer.getHeight()) {
				buffer.destroy();
				buffer = new Image(container.getWidth(), container.getHeight());
			}
			if (buffer2 == null) {
				buffer2 = new Image((int) container.getWidth() / (int) (zoom), container.getHeight() / (int) (zoom));
			} else if (container.getWidth() / (int) (zoom) != buffer2.getWidth()
					|| container.getHeight() / (int) (zoom) != buffer2.getHeight()) {
				buffer2.destroy();
				buffer2 = new Image((int) container.getWidth() / (int) (zoom), container.getHeight() / (int) (zoom));
			}

			buffer.setFilter(Image.FILTER_NEAREST);
			buffer2.setFilter(Image.FILTER_NEAREST);

			g.copyArea(buffer, 0, 0);
			g.drawImage(buffer.getScaledCopy(buffer2.getWidth(), buffer2.getHeight()), 0, 0);
			g.copyArea(buffer2, 0, 0);
			g.drawImage(buffer2.getScaledCopy(buffer.getWidth(), buffer.getHeight()), 0, 0);

			buffer.setFilter(Image.FILTER_LINEAR);

			Debugger.stop("snap");
		}

		if (post != null && post_enable) {
			Debugger.start("effects");

			if (buffer == null) {
				buffer = new Image(container.getWidth(), container.getHeight());
			} else if (container.getWidth() != buffer.getWidth() || container.getHeight() != buffer.getHeight()) {
				buffer.destroy();
				buffer = new Image(container.getWidth(), container.getHeight());
			}

			g.copyArea(buffer, 0, 0);

			if (!(post instanceof MultiEffect)) {
				Debugger.start("post-" + post.getClass());
			}
			post.doPostProcess(container, game, buffer, g);
			if (!(post instanceof MultiEffect)) {
				Debugger.stop("post-" + post.getClass());
			}

			Debugger.stop("effects");
		}

		if (gui) {
			Debugger.start("gui");

			Rectangle world_clip = g.getWorldClip();
			Rectangle clip = g.getClip();
			for (GUIItem gui : guis) {
				if (gui.isCentered()) {
					g.translate(container.getWidth() / 2, container.getHeight() / 2);
					g.scale(base_scale, base_scale);
				} else {
					g.scale(base_scale, base_scale);
				}
				gui.render(g, container, game, container.getWidth() / base_scale, container.getHeight() / base_scale);

				g.resetTransform();
				g.setWorldClip(world_clip);
				g.setClip(clip);
			}

			Debugger.stop("gui");
		}

		g.flush();

		Debugger.stop("render");
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
	 * A method that renders the world.
	 */
	public void render2(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		List<LightSource> lights = null;
		if (RPGConfig.isLighting()) {
			Debugger.start("light-compute");

			lights = ((Client2D) ServerManager.getClient()).getWorld().getLights();

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
						if (dist > 40 * l.getBrightness()) {
							lights.remove(l);
							i -= 1;
						}
					}
				}

				while (lights.size() > 32) {
					lights.remove(lights.size() - 1);
				}
			}

			Debugger.stop("light-compute");
		}

		World world = ((Client2D) ServerManager.getClient()).getWorld();

		Debugger.start("sky");
		if (sky != null) {
			sky.render(g, container, x, y, 0, world, world.getLightColor());
		}
		Debugger.stop("sky");

		g.translate(container.getWidth() / 2, container.getHeight() / 2);

		g.scale(base_scale, base_scale);

		g.scale(zoom, zoom);

		if (shake > 0) {
			g.translate((float) (FastMath.random() * shake * 5), (float) (FastMath.random() * shake * 5));
		}
		float sx = (float) (x * RPGConfig.getTileSize());
		float sy = (float) (y * RPGConfig.getTileSize());

		long dist_x = (long) (container.getWidth() / base_scale / zoom / RPGConfig.getTileSize() / 2) + 2;
		long dist_y = (long) (container.getHeight() / base_scale / zoom / RPGConfig.getTileSize() / 2) + 7;

		Debugger.start("entity-compute");
		List<Entity> entities1 = ((Client2D) ServerManager.getClient()).getWorld().getEntities();
		List<Entity> entities = new ArrayList<Entity>();

		Rectangle screen_bounds = new Rectangle((float) (x - dist_x), (float) (y - dist_y), (float) (dist_x * 2),
				(float) (dist_y * 2));

		synchronized (entities1) {
			for (Entity e : entities1) {
				if (screen_bounds.contains((float) e.getX(), (float) e.getY())) {
					entities.add(e);
				}
			}
		}
		Debugger.stop("entity-compute");

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

		Debugger.start("world");
		long mix = (long) (x - dist_x);
		long max = (long) (x + dist_x);
		long miy = (long) (y - dist_y);
		long may = (long) (y + dist_y);
		long miz = world.getMinZ();
		long maz = world.getMaxZ();

		float wind = ((Client2D) ServerManager.getClient()).getWind();

		Image current = null;

		boolean hitbox = RPGConfig.isHitbox();
		for (long z = maz; z >= FastMath.min(-1, miz); z--) {
			for (long y = miy; y <= may; y++) {
				for (long x = mix; x <= max; x++) {
					Tile t = world.getTile(x, y, z);
					String state = world.getTileState(x, y, z);
					expandTexture(t.getTexture(), textures, x, y, z, world, t, state);

					for (TileTexture tex : textures) {
						if (tex.isCustom()) {
							Debugger.start("custom-tile");
							if (current != null)
								current.endUse();

							tex.render(g, x, y, z, world, state, t, x * RPGConfig.getTileSize() + tex.getX() - sx,
									y * RPGConfig.getTileSize() + tex.getY() - sy, wind);

							if (current != null)
								current.startUse();
							Debugger.stop("custom-tile");
						} else {
							Image img = TextureMap.getTexture(tex.getTexture(x, y, z, world, state, t));

							if (img != null) {
								if (TextureMap.getSheet(img) != current) {
									if (current != null)
										current.endUse();
									current = TextureMap.getSheet(img);
									current.startUse();
								}
								img.drawEmbedded(x * RPGConfig.getTileSize() + tex.getX() - sx,
										y * RPGConfig.getTileSize() + tex.getY() - sy, img.getWidth(), img.getHeight());
							}
						}
					}

					textures.clear();

					if (hitbox && z == -1 && t.isSolid(state)) {
						Debugger.start("hitbox");
						boolean collision = false;
						for (Entity e : entities) {
							if (((x - e.getX()) * (x - e.getX()) + (y - e.getY()) * (y - e.getY())) <= (4 * 4)) {
								if(e.isSolid()) {
									collision = true;
									break;
								}
								
							}
						}
						if (collision) {
							if (current != null)
								current.endUse();

							g.setColor(Color.orange);
							g.draw(t.getHitBox()
									.transform(Transform.createScaleTransform(RPGConfig.getTileSize(),
											RPGConfig.getTileSize()))
									.transform(Transform.createTranslateTransform(x * RPGConfig.getTileSize() - sx,
											y * RPGConfig.getTileSize() - sy)));

							if (current != null)
								current.startUse();
						}
						Debugger.stop("hitbox");
					}

					if (z == -1) {
						Debugger.start("entity");
						for (Entity e : entities) {
							if (!e.isFlying()) {
								if (FastMath.round(e.getX() + 0.5f) == x && FastMath.floor(e.getY() - 0.25) == y) {
									entityTextures.clear();

									expandTexture(e.getTexture(), entityTextures, e.getX(), e.getY(), world, wind, e);

									for (EntityTexture tex : entityTextures) {
										if (tex.isCustom()) {
											if (current != null)
												current.endUse();

											tex.render(g, x, y, -1, world, e, sx, sy, wind);

											if (current != null)
												current.startUse();
										} else {
											Image img = TextureMap
													.getTexture(tex.getTexture(e.getX(), e.getY(), -1, world, e, wind));

											if (img != null) {
												if (TextureMap.getSheet(img) != current) {
													if (current != null)
														current.endUse();
													current = TextureMap.getSheet(img);
													current.startUse();
												}
												img.drawEmbedded(
														(float) e.getX() * RPGConfig.getTileSize() + tex.getX() - sx,
														(float) e.getY() * RPGConfig.getTileSize() + tex.getY() - sy,
														img.getWidth(), img.getHeight());
											}
										}
									}

									if (hitbox) {
										if (current != null)
											current.endUse();

										g.setColor(Color.red);
										g.draw(e.getHitBox()
												.transform(Transform.createScaleTransform(RPGConfig.getTileSize(),
														RPGConfig.getTileSize()))
												.transform(Transform.createTranslateTransform(-sx, -sy)));

										if (current != null)
											current.startUse();
									}
								}
							}
						}
						Debugger.stop("entity");
					}
				}
			}
		}

		Debugger.start("entity");
		for (Entity e : entities) {
			if (e.isFlying()) {
				entityTextures.clear();

				expandTexture(e.getTexture(), entityTextures, e.getX(), e.getY(), world, wind, e);

				for (EntityTexture tex : entityTextures) {
					if (tex.isCustom()) {
						if (current != null)
							current.endUse();

						tex.render(g, x, y, -3, world, e, sx, sy, wind);

						if (current != null)
							current.startUse();
					} else {
						Image img = TextureMap.getTexture(tex.getTexture(e.getX(), e.getY(), -3, world, e, wind));

						if (img != null) {
							if (TextureMap.getSheet(img) != current) {
								if (current != null)
									current.endUse();
								current = TextureMap.getSheet(img);
								current.startUse();
							}
							img.drawEmbedded((float) e.getX() * RPGConfig.getTileSize() + tex.getX() - sx,
									(float) e.getY() * RPGConfig.getTileSize() + tex.getY() - sy, img.getWidth(),
									img.getHeight());
						}
					}
				}

				if (RPGConfig.isHitbox()) {
					if (current != null)
						current.endUse();

					g.setColor(Color.red);
					g.draw(e.getHitBox()
							.transform(Transform.createScaleTransform(RPGConfig.getTileSize(), RPGConfig.getTileSize()))
							.transform(Transform.createTranslateTransform(-sx, -sy)));

					if (current != null)
						current.startUse();
				}
			}
		}
		Debugger.stop("entity");

		Debugger.stop("world");

		if (RPGConfig.isParticles()) {
			Debugger.start("particles");
			Debugger.start("particle-light");
			for (Particle particle : particles_light) {
				if (particle.isCustom()) {
					if (current != null)
						current.endUse();
					particle.render(g, particle.getX() * RPGConfig.getTileSize() - sx,
							particle.getY() * RPGConfig.getTileSize() - sy);
					if (current != null)
						current.startUse();
				} else {
					Image img = TextureMap.getTexture(particle.getTexture());

					if (img != null) {
						if (TextureMap.getSheet(img) != current) {
							if (current != null)
								current.endUse();
							current = TextureMap.getSheet(img);
							current.startUse();
						}
						new Color(1, 1, 1, particle.getAlpha()).bind();
						img.drawEmbedded(particle.getX() * RPGConfig.getTileSize() - sx,
								particle.getY() * RPGConfig.getTileSize() - sy, img.getWidth(), img.getHeight());
					}
				}
			}

			Debugger.stop("particle-light");
			Debugger.stop("particles");
		}

		if (current != null)
			current.endUse();
		current = null;

		g.flush();

		g.resetTransform();

		if (RPGConfig.isLighting()) {
			Debugger.start("lighting");

			if (lights.size() == 0) {
				g.setColor(world.getLightColor());
				g.setDrawMode(Graphics.MODE_COLOR_MULTIPLY);
				g.fillRect(0, 0, container.getWidth(), container.getHeight());
				g.setDrawMode(Graphics.MODE_NORMAL);
			} else {
				Graphics sg = g;

				if (lightBuffer == null) {
					lightBuffer = new Image(container.getWidth(), container.getHeight());
				} else if (container.getWidth() != lightBuffer.getWidth()
						|| container.getHeight() != lightBuffer.getHeight()) {
					lightBuffer.destroy();
					lightBuffer = new Image(container.getWidth(), container.getHeight());
				}

				g = lightBuffer.getGraphics();

				g.clear();

				g.setDrawMode(Graphics.MODE_NORMAL);

				Color wl = world.getLightColor();

				float red = wl.r;
				float green = wl.g;
				float blue = wl.b;

				float lum = (red + green + blue) / 3;

				float rscale = FastMath.max(lum * 10, 1);
				float gscale = FastMath.max(lum * 10, 1);
				float bscale = FastMath.max(lum * 10, 1);

				g.setColor(wl);
				g.fillRect(0, 0, container.getWidth(), container.getHeight());

				g.translate(container.getWidth() / 2, container.getHeight() / 2);

				g.scale(base_scale, base_scale);
				g.pushTransform();

				g.scale(zoom, zoom);
				if (shake > 0) {
					g.translate((float) (FastMath.random() * shake * 5), (float) (FastMath.random() * shake * 5));
				}

				g.setDrawMode(Graphics.MODE_SCREEN);

				for (LightSource l : lights) {
					Image img = TextureMap.getTexture("light").getScaledCopy(l.getBrightness() / 2);

					img.setImageColor(l.getR() / rscale, l.getG() / gscale, l.getB() / bscale);

					g.drawImage(img,
							(float) l.getLX() * RPGConfig.getTileSize() - 256 * l.getBrightness() / 2
									- sx * RPGConfig.getTileSize(),
							(float) l.getLY() * RPGConfig.getTileSize() - 256 * l.getBrightness() / 2
									- sy * RPGConfig.getTileSize());
				}

				g.flush();

				sg.resetTransform();

				sg.setDrawMode(Graphics.MODE_COLOR_MULTIPLY);
				sg.drawImage(lightBuffer, 0, 0);
				sg.setDrawMode(Graphics.MODE_NORMAL);

				g = sg;
			}

			g.flush();

			Debugger.stop("lighting");
		}

		g.resetTransform();

		if (RPGConfig.isParticles()) {
			Debugger.start("particles");
			Debugger.start("particles-nolight");

			g.translate(container.getWidth() / 2, container.getHeight() / 2);

			g.scale(base_scale, base_scale);

			g.scale(zoom, zoom);

			if (shake > 0) {
				g.translate((float) (FastMath.random() * shake * 5), (float) (FastMath.random() * shake * 5));
			}

			for (Particle particle : particles_nolight) {
				if (particle.isCustom()) {
					if (current != null)
						current.endUse();
					particle.render(g, particle.getX() * RPGConfig.getTileSize() - sx,
							particle.getY() * RPGConfig.getTileSize() - sy);
					if (current != null)
						current.startUse();
				} else {
					Image img = TextureMap.getTexture(particle.getTexture());

					if (img != null) {
						if (TextureMap.getSheet(img) != current) {
							if (current != null)
								current.endUse();
							current = TextureMap.getSheet(img);
							current.startUse();
						}
						new Color(1, 1, 1, particle.getAlpha()).bind();
						img.drawEmbedded(particle.getX() * RPGConfig.getTileSize() - sx,
								particle.getY() * RPGConfig.getTileSize() - sy, img.getWidth(), img.getHeight());
					}
				}
			}

			if (current != null)
				current.endUse();

			g.resetTransform();

			Debugger.stop("particles-nolight");
			Debugger.stop("particles");
		}
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
	 * {@inheritDoc}
	 */
	@Override
	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
		Debugger.initRender();

		this.last_delta = delta;

		float delf = delta / 1000f;

		Input in = container.getInput();

		Debugger.start("input");
		
		double walk_x = 0;
		double walk_y = 0;
		
		Debugger.start("keyboard");
		if (Keyboard.isKeyDown(RPGConfig.getKeyInput().getKeyCodeForAction(InputUtils.WALK_NORTH))) {
			walk_y += -1;
		}
		if (Keyboard.isKeyDown(RPGConfig.getKeyInput().getKeyCodeForAction(InputUtils.WALK_SOUTH))) {
			walk_y += 1;
		}

		if (Keyboard.isKeyDown(RPGConfig.getKeyInput().getKeyCodeForAction(InputUtils.WALK_EAST))) {
			walk_x += 1;
		}
		if (Keyboard.isKeyDown(RPGConfig.getKeyInput().getKeyCodeForAction(InputUtils.WALK_WEST))) {
			walk_x -= 1;
		}
		Debugger.stop("keyboard");

		((Client2D) ServerManager.getClient()).walkX(walk_x, delf);
		((Client2D) ServerManager.getClient()).walkY(walk_y, delf);

		Debugger.start("controller");
//		if (in.getControllerCount() > 0) {
//			if(RPGConfig.getControllerInput().isLeftHanded()) {
//				ServerManager.getClient().walkY(in.getAxisValue(0, 2));
//				ServerManager.getClient().walkX(in.getAxisValue(0, 3));
//			} else {
//				ServerManager.getClient().walkY(in.getAxisValue(0, 0));
//				ServerManager.getClient().walkX(in.getAxisValue(0, 1));
//			}
//		}
		((Client2D) ServerManager.getClient()).setSprint(InputUtils.isActionPressed(in, InputUtils.SPRINT));
		Debugger.stop("controller");

		x = ((Client2D) ServerManager.getClient()).getPlayerX();
		y = ((Client2D) ServerManager.getClient()).getPlayerY();
		
		if (InputUtils.isActionPressed(in, InputUtils.EXIT)) {
			exit();
		}
		
		Debugger.stop("input");
		
		Debugger.start("world");
		((Client2D) ServerManager.getClient()).getWorld().doUpdateClient();
		Debugger.stop("world");

		if (shake > 0) {
			shake -= delf;
		}

		Debugger.start("gui");
		for (GUIItem gui : guis) {
			gui.update(container, game, delta);
		}

		for (UpdateHook hook : hooks) {
			hook.update(container, game, delta);
		}
		Debugger.stop("gui");

		Debugger.start("audio");
		AmbientMusic music = ServerManager.getClient().getMusic();
		AudioManager.setMusic(music);

		gui_cooldown -= delf;
		if (InputUtils.isActionPressed(in, InputUtils.GUI_TOGGLE) && gui_cooldown <= 0) {
			gui = !gui;
			gui_cooldown = 0.25f;
		}

		AudioManager.setPlayerPos((float) x, 0, (float) y);
		AudioManager.setPlayerVelocity((float) (x - px) / delf, 0, (float) (y - py) / delf);
		Debugger.stop("audio");

		Debugger.start("particles");
		px = x;
		py = y;

		float wind = ((Client2D) ServerManager.getClient()).getWind();
		World world = ((Client2D) ServerManager.getClient()).getWorld();
		for (int i = 0; i < particles.size(); i++) {
			particles.get(i).doBehaviour(world, wind, particles, delta / 1000f);
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

		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			post_enable = false;
		} else {
			post_enable = true;
		}
	}

	/**
	 * A method called upon game exit.
	 */
	public void exit() {
		AudioManager.dispose();
		System.exit(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * Adds a GUI to the state.
	 * 
	 * @param e A GUI element.
	 */
	public void addGUI(GUIItem e) {
		guis.add(e);
	}

	/**
	 * Removes a GUI from the state.
	 * 
	 * @param o A GUI element.
	 */
	public void removeGUI(GUIItem o) {
		guis.remove(o);
	}

	/**
	 * Adds a GUI element at a specific point.
	 * 
	 * @param index   The index of the element.
	 * @param element A GUI element.
	 */
	public void addGUI(int index, GUIItem element) {
		guis.add(index, element);
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
	 * @return A {@code PostEffect} object.
	 */
	public PostEffect getPost() {
		return post;
	}

	/**
	 * Sets the current shader effect.
	 * 
	 * @param post A {@code PostEffect} object.
	 */
	public void setPost(PostEffect post) {
		if (post == null) {
			post = NullPostProcessEffect.INSTANCE;
		}
		this.post = post;
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
	public void scale(float base) {
		base_scale = base;
	}
}