package com.rivelbop.steamgdx;

import com.badlogic.gdx.Screen;

public class LobbyMenu implements Screen{
	
	public SteamGDX steamGDX;
	
	public LobbyMenu(SteamGDX game) {
		steamGDX = game;
	}
	
	@Override
	public void show() {
		
	}

	@Override
	public void render(float delta) {
		steamGDX.camera.update();
		
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