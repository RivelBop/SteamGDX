package com.rivelbop.steamgdx;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.rivelbop.steam.Steam;

public class LobbyMenu implements Screen{
	
	public SteamGDX steamGDX;
	public BitmapFont font;
	
	public LobbyMenu(SteamGDX game) {
		steamGDX = game;
	}
	
	@Override
	public void show() {
		font = new BitmapFont();
	}

	@Override
	public void render(float delta) {
		steamGDX.camera.update();
		steamGDX.batch.begin();
		if(Steam.matchmakingCallback.lobbyID != null) font.draw(steamGDX.batch, Integer.toString(Steam.matchmakingCallback.lobbyID.getAccountID()), 50, 50);
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