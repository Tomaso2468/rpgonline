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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.newdawn.slick.Color;
import org.newdawn.slick.util.Log;

import io.github.tomaso2468.abt.*;
import io.github.tomaso2468.rpgonline.world2d.chunk.CacheEntry;
import io.github.tomaso2468.rpgonline.world2d.chunk.Chunk;
import io.github.tomaso2468.rpgonline.world2d.chunk.ChunkWorld;
import io.github.tomaso2468.rpgonline.world2d.entity.Entity;
import io.github.tomaso2468.rpgonline.world2d.entity.EntityManager;

/**
 * A world format that stores data as ABT.
 * @author Tomaso2468
 */
public class ABTWorld extends ChunkWorld {
	/**
	 * The folder to store ABT files in.
	 */
	protected File folder;
	/**
	 * The format version number for this ABTWorld.
	 */
	protected int format;
	/**
	 * 
	 * @param folder The folder containing world data.
	 * @param registry The tile registry for tiles.
	 * @param em The entity manager to use.
	 * @param server Determines if the world is running on a server.
	 * @throws IOException If an error occurs reading world metadata.
	 */
	public ABTWorld(File folder, Map<String, Tile> registry, EntityManager em, boolean server) throws IOException {
		super(registry);
		this.folder = folder;
		folder.mkdirs();
		
		if (new File(folder, "map.abt").exists()) {
			TagDoc d = TagDoc.read(new BufferedInputStream(new FileInputStream(new File(folder, "map.abt"))), "map");
			
			format = ((TagInt) d.getTags().getTag("version")).getData();
			
			if (format == 0) {
				TagGroup lights = (TagGroup) d.getTags().getTag("lights");
				
				for(Tag t : lights.getTags()) {
					TagGroup g = (TagGroup) t;
					
					TagGroup c = (TagGroup) g.getTag("color");
					
					float lr = ((TagFloat) c.getTag("r")).getData();
					float lg = ((TagFloat) c.getTag("g")).getData();
					float lb = ((TagFloat) c.getTag("b")).getData();
					
					TagGroup pos = (TagGroup) g.getTag("pos");
					
					double x = ((TagDouble) pos.getTag("x")).getData();
					double y = ((TagDouble) pos.getTag("y")).getData();
					
					float b = ((TagFloat) c.getTag("brightness")).getData();
					
					Color color = new Color(lr, lg, lb);
					
					addLight(new LightSource(x, y, color, b, server));
				}
				
				TagGroup entities = (TagGroup) d.getTags().getTag("entities");
				
				for (Tag t : entities.getTags()) {
					TagGroup g = (TagGroup) t;
					
					getEntities().add(new Entity(em, g, server));
				}
			} else {
				throw new IOException("Unknown version file: " + format);
			}
		} else {
			TagGroup root = new TagGroup("root");
			
			root.add(new TagInt("version", 0));
			root.add(new TagGroup("lights"));
			root.add(new TagGroup("entities"));
			
			TagDoc doc = new TagDoc("map", root);
			
			new File(folder, "map.abt").createNewFile();
			
			doc.write(new BufferedOutputStream(new FileOutputStream(new File(folder, "map.abt"))));
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected synchronized Chunk getChunk(long x, long y, long z) {
		long cx = (int) Math.floor(x / (Chunk.SIZE * 1f));
		long cy = (int) Math.floor(y / (Chunk.SIZE * 1f));
		long cz = (int) Math.floor(z / (2 * 1f));

		if (last_chunk != null) {
			if (last_chunk.isAt(cx, cy, cz)) {
				return last_chunk;
			}
		}

		synchronized (cache) {
			for (CacheEntry e : cache) {
				Chunk chunk = e.getChunk();
				if (chunk.isAt(cx, cy, cz)) {
					e.setTime(System.currentTimeMillis());
					last_chunk = chunk;
					return chunk;
				}
			}
		}

		for (Chunk chunk : chunks) {
			if (chunk.isAt(cx, cy, cz)) {
				cache.add(new CacheEntry(chunk, System.currentTimeMillis()));
				last_chunk = chunk;
				return chunk;
			}
		}
		
		File f = new File(folder, "chunk_" + Long.toHexString(x) + "_" + Long.toHexString(y) + "_" + Long.toHexString(z) + ".abt");
		
		if (f.exists()) {
			Chunk c;
			try {
				c = loadChunk(registry, cx, cy, cz, f);
				
				chunks.add(c);
				cache.add(new CacheEntry(c, System.currentTimeMillis()));
				last_chunk = c;
				
				return c;
			} catch (IOException | NullPointerException e) {
				Log.error("Error reading chunk from file " + f.getName(), e);
			}
		}

		Chunk chunk = new Chunk(registry, cx, cy, cz);
		generateChunk(chunk);
		chunks.add(chunk);
		cache.add(new CacheEntry(chunk, System.currentTimeMillis()));
		last_chunk = chunk;

		return chunk;
	}
	
	/**
	 * Loads a chunk from a file.
	 * @param registry The tile registry to use.
	 * @param x The X position of the chunk.
	 * @param y The Y position of the chunk.
	 * @param z The Z position of the chunk.
	 * @param f The file to read from.
	 * @return A chunk object.
	 * @throws FileNotFoundException If the chunk file does not exist.
	 * @throws IOException If an error occurs reading chunk data.
	 */
	protected Chunk loadChunk(Map<String, Tile> registry, long x, long y, long z, File f) throws FileNotFoundException, IOException {
		TagDoc d = TagDoc.read(new GZIPInputStream(new BufferedInputStream(new FileInputStream(f))), "map_c");
		
		TagGroup tg = d.getTags();
		
		int version = ((TagInt) tg.getTag("version")).getData();
		
		if (version == 0) {
			if (x == ((TagLong) tg.getTag("x")).getData()
					&& y == ((TagLong) tg.getTag("y")).getData()
					&& z == ((TagLong) tg.getTag("z")).getData()) {
				Chunk c = new Chunk(registry, x, y, z);
				
				for (int cz = 0; cz < 1; cz++) {
					for (int cy = 0; cy < Chunk.SIZE; cy++) {
						for (int cx = 0; cx < Chunk.SIZE; cx++) {
							c.setTile(cx, cy, cz, registry.get(((TagString) tg.getTag("tile/" + cz + "/" + cy + "/" + cx)).getData()));
							c.setState(cx, cy, cz, ((TagString) tg.getTag("state/" + cz + "/" + cy + "/" + cx)).getData());
							c.setFlag(cx, cy, cz, ((TagBoolean) tg.getTag("flag/" + cz + "/" + cy + "/" + cx)).getData());
							c.setArea(cx, cy, cz, ((TagString) tg.getTag("area/" + cz + "/" + cy + "/" + cx)).getData());
							c.setBiome(cx, cy, cz, ((TagInt) tg.getTag("biome/" + cz + "/" + cy + "/" + cx)).getData());
						}
					}
				}
				
				return c;
			} else {
				throw new IOException("Chunk has inconsistant location data.");
			}
		} else {
			throw new IOException("Chunk file is from newer version.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void save() {
		TagGroup tg = new TagGroup("map");
		tg.add(new TagInt("version", 0));
		
		TagGroup lights = new TagGroup("lights");
		for(LightSource l : getLights()) {
			TagGroup g = new TagGroup(l.toString());
			
			TagGroup c = new TagGroup("color");
			c.add(new TagFloat("r", l.getR()));
			c.add(new TagFloat("g", l.getG()));
			c.add(new TagFloat("b", l.getB()));
			c.add(new TagFloat("brightness", l.getBrightness()));
			g.add(c);
			
			TagGroup pos = new TagGroup("pos");
			pos.add(new TagDouble("x", l.getLX()));
			pos.add(new TagDouble("y", l.getLY()));
			g.add(pos);
			
			lights.add(g);
		}
		
		tg.add(lights);
		
		TagGroup entities = new TagGroup("entities");
		for (Entity e : getEntities()) {
			entities.add(e.toABT(e.toString()));
		}
		
		TagDoc d = new TagDoc("map", tg);
		
		try {
			d.write(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(new File(folder, "map.abt")))));
		} catch (IOException e) {
			Log.error("Error writing map data.", e);
		}
		
		for (Chunk c : chunks) {
			File f = new File(folder, "chunk_" + Long.toHexString(c.getX()) + "_" + Long.toHexString(c.getY()) + "_" + Long.toHexString(c.getZ()) + ".abt");
			TagDoc doc = new TagDoc("map_c", c.save());
			
			try {
				doc.write(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(f))));
			} catch (IOException e) {
				Log.error("Error writing chunk " + c.getX() + " " + c.getY() + " " + c.getZ(), e);
			}
		}
	}
	
	/**
	 * Generates a new empty chunk using the defaultGround and defaultabove data.
	 * @param c The chunk to generate.
	 */
	public void generateChunk(Chunk c) {
		for (int x = 0; x < Chunk.SIZE; x++) {
			for (int y = 0; y < Chunk.SIZE; y++) {
				if (c.getZ() == 0) {
					c.setTile(x, y, 0, registry.get("defaultGround"));
				}
				if (c.getZ() == -1) {
					c.setTile(x, y, 0, registry.get("defaultAbove"));
				}
			}
		}
	}
}
