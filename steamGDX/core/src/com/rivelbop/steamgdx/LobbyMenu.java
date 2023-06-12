package com.rivelbop.steamgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.codedisaster.steamworks.SteamMatchmaking.LobbyType;
import com.rivelbop.steam.Steam;

public class LobbyMenu implements Screen{
	
	public SteamGDX steamGDX;
	public BitmapFont font;
	public float timer;
	
	public LobbyMenu(SteamGDX game) {
		steamGDX = game;
	}
	
	@Override
	public void show() {
		font = new BitmapFont();
		Steam.createLobby(LobbyType.Public, 4);
	}

	@Override
	public void render(float delta) {
		steamGDX.camera.update();
		
		timer += Gdx.graphics.getDeltaTime();
		
		if(timer >= 3) {
			timer = 0;
			Steam.sendMessageToLobby("This is a message!");
			System.out.println("Message sent!");
		}
		
		steamGDX.batch.begin();
		if(Steam.getLobbyID() != null) font.draw(steamGDX.batch, Integer.toString(Steam.getLobbyID().getAccountID()), 50, 50);
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