package com.rivelbop.steamgdx;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.rivelbop.steam.Steam;

public class SteamGDX extends Game {

	public OrthographicCamera camera;
	public StretchViewport viewport;
	public SpriteBatch batch;
	public static int screenHeight = 720, screenWidth = screenHeight * 16 / 9;

	@Override
	public void create() {
		Steam.init(480);

		camera = new OrthographicCamera();
		viewport = new StretchViewport(screenWidth, screenHeight, camera);
		camera.update();

		batch = new SpriteBatch();

		setScreen(new LobbyMenu(this));
	}

	@Override
	public void render() {
		Steam.update(1 / 15f);

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		super.render();
	}

	@Override
	public void dispose() {
		Steam.dispose();
		batch.dispose();
		getScreen().dispose();
	}

}