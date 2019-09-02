package rpgonline.net;

import java.util.stream.Stream;

import org.newdawn.slick.util.Log;

import rpgonline.GameExceptionHandler;
import rpgonline.debug.DebugFrame;

/**
 * An interface for systems based on a constant tick rate.
 * @author Tomas
 *
 */
public interface TickBased {
	/**
	 * Start the system.
	 */
	public default void start() {
		new Thread(getMainThreadName()) {
			public void run() {
				Thread.currentThread().setUncaughtExceptionHandler(new GameExceptionHandler());

				long last_update = System.nanoTime();

				if (ServerManager.getServer() == TickBased.this) {
					ServerManager.server_max_time = (long) (1000000000 / getTickSpeed());
				}
				if (ServerManager.getClient() == TickBased.this) {
					ServerManager.client_max_time = (long) (1000000000 / getTickSpeed());
				}
				
				try {
					init();
				} catch (Exception e) {
					Log.error("Could not start " + getType() + ".", e);

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						Log.error(e);
					}

					Log.info("Retrying " + getType() + " start.");
					try {
						init();
					} catch (Exception e2) {
						Log.error(getTypeTitle() + " start failed again. Aborting.", e2);
						return;
					}
				}
				try {
					while (true) {
						double delta = (System.nanoTime() - last_update) / 1000000000.0;
						if(delta > 0.5) {
							delta = 0.5;
						}
						last_update = System.nanoTime();
						update(delta);
						if (System.nanoTime() - last_update > (1000000000 / getTickSpeed())) {
							if (System.nanoTime() - last_update - 1000000000 / getTickSpeed() > 32000) {
								Log.warn(getType() + " is running "
										+ (System.nanoTime() - last_update - 1000000000 / getTickSpeed()) / 1000000
										+ " millis behind.");
							}
						}
						if (ServerManager.getServer() == TickBased.this) {
							ServerManager.server_time = System.nanoTime() - last_update;
						}
						if (ServerManager.getClient() == TickBased.this) {
							ServerManager.client_time = System.nanoTime() - last_update;
						}
						while (System.nanoTime() - last_update < (1000000000 / getTickSpeed())) {
							Thread.yield();
						}
					}
				} catch (Exception e) {
					Log.error("Error in " + getType() + " thread.", e);

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						Log.error(e);
					}

					Log.info("Re-entering " + getType() + " loop.");
					try {
						while (true) {
							double delta = (System.nanoTime() - last_update) / 1000000000.0;
							last_update = System.nanoTime();
							update(delta);
							if (System.nanoTime() - last_update > (1000000000 / getTickSpeed())) {
								if (System.nanoTime() - last_update - 1000000000 / getTickSpeed() > 32000) {
									Log.warn(getType() + " is running "
											+ (System.nanoTime() - last_update - 1000000000 / getTickSpeed()) / 1000000
											+ " millis behind.");
								}
							}
							while (System.nanoTime() - last_update < (1000000000 / getTickSpeed())) {
								Thread.yield();
							}
							if (ServerManager.getServer() == TickBased.this) {
								ServerManager.server_time = System.nanoTime() - last_update;
							}
							if (ServerManager.getClient() == TickBased.this) {
								ServerManager.client_time = System.nanoTime() - last_update;
							}
						}
					} catch (Exception e2) {
						Log.error(
								getTypeTitle() + " loop failed again. Assuming the error will repeat indefinetly. Stopping " + getType() + ".",
								e2);

					}
				}
			}
		}.start();
	}
	/**
	 * Gets the type of the system.
	 * @return {@code server} if this is a server, {@code client} if this is a client and a lowercase classname if this is anything else.
	 */
	default String getType() {
		if (this instanceof Server) {
			return "server";
		}
		if (this instanceof Client) {
			return "client";
		}
		return getClass().getSimpleName().toLowerCase();
	}
	/**
	 * Gets the type of the system in title case.
	 * @return {@code Server} if this is a server, {@code Client} if this is a client and a title case classname if this is anything else.
	 */
	default String getTypeTitle() {
		return toTitleCase(getType());
	}
	/**
	 * Converts a string to title case
	 * @param word The string to convert.
	 * @return A title case string.
	 */
	static String toTitleCase(String word) {
	    return Stream.of(word.split(" "))
	            .map(w -> w.toUpperCase().charAt(0)+ w.toLowerCase().substring(1))
	            .reduce((s, s2) -> s + " " + s2).orElse("");
	}
	/**
	 * Initialises the system.
	 */
	public void init();
	/**
	 * Called per tick for an update.
	 * @param delta The time since the last tick in seconds.
	 */
	public void update(double delta);
	/**
	 * Gets the speed of the system in ticks per second.
	 * @return A double value.
	 */
	public double getTickSpeed();
	/**
	 * Gets the name to be used for the main thread of the system.
	 * @return A string value.
	 */
	public String getMainThreadName();
	/**
	 * Stops the system.
	 */
	public void stop();
	/**
	 * Determines if initialisation has been completed.
	 * @return {@code true} if {@code init()} has finished executing, {@code false} otherwise.
	 * 
	 * @see #init()
	 */
	public boolean isInit();
	/**
	 * Gets the most recent debug frame for the system.
	 * @return A complete debug frame.
	 */
	public DebugFrame getDebugFrame();
}
