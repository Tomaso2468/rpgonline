package rpgonline.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;

import org.newdawn.slick.util.Log;

import rpgonline.net.packet.KeyPacket;
import rpgonline.net.packet.NetPacket;

public class SocketServerConnection extends AESSecurityCap implements Connection {
	/**
	 * The list of packets to send.
	 */
	private List<NetPacket> toSend = Collections.synchronizedList(new ArrayList<NetPacket>());
	/**
	 * The list of received packets.
	 */
	private List<NetPacket> recieved = Collections.synchronizedList(new ArrayList<NetPacket>());
	/**
	 * Determines if the connection has been closed.
	 */
	private boolean stopped = false;
	/**
	 * The encryption cipher.
	 */
	private Cipher encryptCipher;
	/**
	 * The decryption cipher.
	 */
	private Cipher decryptCipher;
	/**
	 * Determines the mode to use.
	 */
	private int mode;

	/**
	 * Constructs a new SocketServerConnection.
	 * @param s The incoming socket.
	 */
	public SocketServerConnection(Socket s) {
		reload(s);
	}
	
	/**
	 * Reopens the connection on a new socket.
	 * @param s The new socket.
	 */
	public void reload(Socket s) {
		new Thread(toString()) {
			public void run() {
				try {
					mode = s.getInputStream().read();
				} catch (IOException e) {
					Log.error(e);
				}

				try {
					ObjectInputStream in = null;
					ObjectOutputStream out = null;

					if (mode == SocketClientConnection.MODE_COMPRESS) {
						in = new ObjectInputStream(new BufferedInputStream(new GZIPInputStream(s.getInputStream())));
						out = new ObjectOutputStream(
								new BufferedOutputStream(new GZIPOutputStream(s.getOutputStream())));
						Log.debug("Connected in compress mode");
					} else if (mode == SocketClientConnection.MODE_ENCRYPT_COMPRESS) {
						in = new ObjectInputStream(new BufferedInputStream(
								new CipherInputStream(new GZIPInputStream(s.getInputStream()), decryptCipher)));
						out = new ObjectOutputStream(new BufferedOutputStream(
								new CipherOutputStream(new GZIPOutputStream(s.getOutputStream()), encryptCipher)));
						Log.debug("Connected in encrypt/compress mode");
					}

					while (!stopped) {
						while (toSend.size() > 0) {
							try {
								out.writeObject(toSend.get(0));
								toSend.remove(0);
							} catch (IOException e) {
								Log.error("Error writing packet.", e);
							}
						}

						while (in.available() > 0) {
							try {
								Object o = in.readObject();

								NetPacket p = (NetPacket) o;

								recieved.add(p);
							} catch (ClassNotFoundException e) {
								Log.error("Error reading packet.", e);
							}
						}

						if (s.isClosed()) {
							stopped = true;
						}

						Thread.yield();
					}
					
					s.close();
				} catch (IOException e) {
					Log.error(e);
				}
			}
		}.start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		stopped = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void send(NetPacket p) {
		toSend.add(p);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAvaliable() {
		return recieved.size() > 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NetPacket getNext() {
		return recieved.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void encrypt() {
		Log.info("Attempting encryption.");
		
		Log.debug("Sending key");
		send(new KeyPacket(getPublickey()));

		Log.debug("Waiting for client key.");
		while (!isAvaliable()) {
			Thread.yield();
		}

		KeyPacket p = (KeyPacket) getNext();

		Log.debug("Encrypting");
		try {
			PublicKey k = KeyFactory.getInstance("AES").generatePublic(new X509EncodedKeySpec(p.key));
			setReceiverPublicKey(k);

			decryptCipher = Cipher.getInstance("AES");
			decryptCipher.init(Cipher.DECRYPT_MODE, generateKey());
			
			encryptCipher = Cipher.getInstance("AES");
			encryptCipher.init(Cipher.ENCRYPT_MODE, generateKey());
		} catch (InvalidKeySpecException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			Log.error("Error reading/generating encryption key.", e);
		}
	}

	/**
	 * Gets the mode of the connection.
	 * @return see SocketClientConnection
	 * 
	 * @see SocketClientConnection#MODE_COMPRESS
	 * @see SocketClientConnection#MODE_NORMAL
	 * @see SocketClientConnection#MODE_ENCRYPT
	 * @see SocketClientConnection#MODE_ENCRYPT_COMPRESS
	 */
	public int getMode() {
		return mode;
	}

}
