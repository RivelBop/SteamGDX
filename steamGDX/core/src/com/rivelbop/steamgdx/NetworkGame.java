package com.rivelbop.steamgdx;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.Screen;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamNetworking.P2PSend;
import com.rivelbop.steam.PacketData;
import com.rivelbop.steam.Steam;

public class NetworkGame implements Screen {

	public SteamGDX steamGDX;
	public HashMap<SteamID, PlayerData> players;
	
	private final float SPEED = 50f;
	private float updateTimer;

	public NetworkGame(SteamGDX game, HashMap<SteamID, PlayerData> players) {
		steamGDX = game;
		this.players = players;
	}

	@Override
	public void show() {
		
	}

	@Override
	public void render(float delta) {
		updateTimer += Gdx.graphics.getDeltaTime();
		
		if (Gdx.input.isKeyPressed(Keys.W))
			players.get(Steam.User.getID()).sprite.translateY(SPEED * delta);

		if (Gdx.input.isKeyPressed(Keys.A))
			players.get(Steam.User.getID()).sprite.translateX(-SPEED * delta);

		if (Gdx.input.isKeyPressed(Keys.S))
			players.get(Steam.User.getID()).sprite.translateY(-SPEED * delta);
		
		if (Gdx.input.isKeyPressed(Keys.D))
			players.get(Steam.User.getID()).sprite.translateX(SPEED * delta);
		
		if(updateTimer > 0.10f) {
			for(SteamID p : players.keySet())
				Steam.Network.sendPacket(p, "x" + players.get(Steam.User.getID()).sprite.getX() +
						"y" + players.get(Steam.User.getID()).sprite.getY(), P2PSend.Reliable,
						Steam.Channel.MESSAGE);
			updateTimer = 0f;
		}
		
		if (Gdx.input.isKeyJustPressed(Input.Keys.F))
			Steam.User.startVoice();
		
		for(SteamID p : players.keySet())
			Steam.User.sendVoiceToUser(p, P2PSend.Reliable);
		
		if (!Gdx.input.isKeyPressed(Input.Keys.F))
			Steam.User.stopVoice();
		
		PacketData packet = Steam.Network.receivePacket(Steam.Channel.MESSAGE);
		if (packet != null) {
			String message = packet.message;
			int msgY = message.indexOf("y");
			
			players.get(packet.user).sprite.setX(Float.parseFloat(message.substring(message.indexOf("x") + 1, msgY)));
			players.get(packet.user).sprite.setY(Float.parseFloat(message.substring(msgY + 1)));
		}
		
		packet = Steam.Network.receivePacket(Steam.Channel.VOICE);
		if (packet != null) {
			byte[] bytes = new byte[Steam.VOICEBUFFERSIZE];
			packet.buffer.get(bytes);
			ByteArrayInputStream oInstream = new ByteArrayInputStream(bytes);
			try {
				AudioInputStream oAIS = AudioSystem.getAudioInputStream(oInstream);

				try {
					Clip clip = AudioSystem.getClip();
					clip.open(oAIS);
					clip.start();
				} catch (LineUnavailableException e) {
					e.printStackTrace();
				}
			} catch (UnsupportedAudioFileException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		steamGDX.batch.begin();
		for (PlayerData player : players.values())
			player.sprite.draw(steamGDX.batch);
		steamGDX.batch.end();
	}

	@Override
	public void resize(int width, int height) {
		steamGDX.viewport.update(width, height);
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {

	}

	@Override
	public void dispose() {

	}

}