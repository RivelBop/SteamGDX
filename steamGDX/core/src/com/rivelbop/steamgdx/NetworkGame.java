package com.rivelbop.steamgdx;

import java.util.HashMap;

import com.badlogic.gdx.Screen;
import com.codedisaster.steamworks.SteamID;
import com.rivelbop.steam.Steam;

public class NetworkGame implements Screen{
	
	public SteamGDX steamGDX;
	public HashMap<SteamID, PlayerData> players;
	
	public NetworkGame(SteamGDX game) {
		steamGDX = game;
	}
	
	@Override
	public void show() {
		for(SteamID player : Steam.playersInLobby()) {
			if(player != null) players.put(player, new PlayerData());
		}
	}

	@Override
	public void render(float delta) {
		
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