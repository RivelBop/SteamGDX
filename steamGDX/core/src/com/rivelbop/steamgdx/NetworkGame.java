package com.rivelbop.steamgdx;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.codedisaster.steamworks.SteamID;
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
		for(SteamID player : Steam.playersInLobby()) {
			players.put(player, new PlayerData());
		}
	}

	@Override
	public void render(float delta) {
		movedX = false;
		movedY = false;
		
		if(Gdx.input.isKeyPressed(Keys.W)) {
			players.get(Steam.getUserID()).sprite.translateY(SPEED * delta);
			movedY = true;
		}
		if(Gdx.input.isKeyPressed(Keys.A)) {
			players.get(Steam.getUserID()).sprite.translateX(-SPEED * delta);
			movedX = true;
		}
		if(Gdx.input.isKeyPressed(Keys.S)) {
			players.get(Steam.getUserID()).sprite.translateY(-SPEED * delta);
			movedY = true;
		}
		if(Gdx.input.isKeyPressed(Keys.D)) {
			players.get(Steam.getUserID()).sprite.translateX(SPEED * delta);
			movedX = true;
		}
		
		if(movedX) Steam.sendLobbyPacket("X: " + players.get(Steam.getUserID()).sprite.getX());
		if(movedY) Steam.sendLobbyPacket("Y: " + players.get(Steam.getUserID()).sprite.getY());
		
		PacketData packet = Steam.receivePacket();
		if(packet != null) {
			String message = packet.packet;
			if(message.contains("X: ")) {
				players.get(packet.user).sprite.setX(Float.parseFloat(message.substring(3)));
			}else if(message.contains("Y: ")) {
				players.get(packet.user).sprite.setY(Float.parseFloat(message.substring(3)));
			}
		}
		
		steamGDX.batch.begin();
		for(PlayerData player : players.values()) {
			player.sprite.draw(steamGDX.batch);
		}
		steamGDX.batch.end();
	}

	@Override
	public void resize(int width, int height) {
		
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