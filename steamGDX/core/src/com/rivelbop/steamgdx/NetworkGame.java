package com.rivelbop.steamgdx;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamNetworking.P2PSend;
import com.rivelbop.steam.PacketData;
import com.rivelbop.steam.Steam;

public class NetworkGame implements Screen{
	
	public SteamGDX steamGDX;
	public HashMap<SteamID, PlayerData> players;
	public final float SPEED = 50f;
	public boolean movedX, movedY;
	
	public NetworkGame(SteamGDX game) {
		steamGDX = game;
	}
	
	@Override
	public void show() {
		players = new HashMap<SteamID, PlayerData>();
		for(SteamID player : Steam.Lobby.players()) {
			players.put(player, new PlayerData());
		}
	}

	@Override
	public void render(float delta) {
		movedX = false;
		movedY = false;
		
		if(Gdx.input.isKeyPressed(Keys.W)) {
			players.get(Steam.User.getID()).sprite.translateY(SPEED * delta);
			movedY = true;
		}
		if(Gdx.input.isKeyPressed(Keys.A)) {
			players.get(Steam.User.getID()).sprite.translateX(-SPEED * delta);
			movedX = true;
		}
		if(Gdx.input.isKeyPressed(Keys.S)) {
			players.get(Steam.User.getID()).sprite.translateY(-SPEED * delta);
			movedY = true;
		}
		if(Gdx.input.isKeyPressed(Keys.D)) {
			players.get(Steam.User.getID()).sprite.translateX(SPEED * delta);
			movedX = true;
		}
		
		if(movedX) Steam.Lobby.sendPacket("X: " + players.get(Steam.User.getID()).sprite.getX(), P2PSend.ReliableWithBuffering, Steam.Channel.MESSAGE);
		if(movedY) Steam.Lobby.sendPacket("Y: " + players.get(Steam.User.getID()).sprite.getY(), P2PSend.ReliableWithBuffering, Steam.Channel.MESSAGE);
		
		if(Gdx.input.isKeyJustPressed(Input.Keys.F)) Steam.User.startVoice();
		Steam.User.sendVoice(P2PSend.ReliableWithBuffering);
		if(!Gdx.input.isKeyPressed(Input.Keys.F)) Steam.User.stopVoice();
		
		PacketData packet = Steam.Network.receivePacket(Steam.Channel.MESSAGE);
		if(packet != null) {
			String message = packet.message;
			if(message.contains("X: ")) {
				players.get(packet.user).sprite.setX(Float.parseFloat(message.substring(3)));
			}else if(message.contains("Y: ")) {
				players.get(packet.user).sprite.setY(Float.parseFloat(message.substring(3)));
			}
		}
		
		packet = Steam.Network.receivePacket(Steam.Channel.VOICE);
		if(packet != null) {
			byte[] bytes = new byte[1024];
			packet.buffer.get(bytes);
			System.out.println(bytes);
			ByteArrayInputStream oInstream = new ByteArrayInputStream(bytes);
			try {
				AudioInputStream oAIS = AudioSystem.getAudioInputStream(oInstream);
				
				try {
					Clip clip = AudioSystem.getClip();
					clip.open(oAIS);
					clip.start();
				} catch (LineUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (UnsupportedAudioFileException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		steamGDX.batch.begin();
		for(PlayerData player : players.values()) player.sprite.draw(steamGDX.batch);
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