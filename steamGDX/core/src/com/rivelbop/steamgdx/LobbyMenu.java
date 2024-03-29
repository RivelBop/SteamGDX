package com.rivelbop.steamgdx;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamMatchmaking.LobbyType;
import com.rivelbop.steam.LobbyMessage;
import com.rivelbop.steam.Steam;

public class LobbyMenu implements Screen {

	public SteamGDX steamGDX;

	public Stage stage;
	public Skin skin;
	public Label createLobbyLabel, joinLobbyLabel, serverID, messageLabel, messages, readyLabel;
	public TextButton createLobbyButton, joinLobbyButton, messageButton, readyButton;
	public TextField joinLobbyID, messageField;

	public HashMap<SteamID, Boolean> playersReady;

	public final int START_COUNT = 2;
	public int messageCount;
	public float yOffset = 100;
	public boolean isReady;

	public LobbyMenu(SteamGDX game) {
		steamGDX = game;
	}

	@Override
	public void show() {
		skin = new Skin(Gdx.files.internal("default/skin/uiskin.json"));
		stage = new Stage(steamGDX.viewport);
		playersReady = new HashMap<>();
		
		createLobbyLabel = new Label("Host Lobby:", skin);
		centerUI(createLobbyLabel, 100+yOffset);
		stage.addActor(createLobbyLabel);

		createLobbyButton = new TextButton("Host", skin);
		createLobbyButton.setSize(80, 25);
		centerUI(createLobbyButton, 75+yOffset);
		createLobbyButton.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (event.isHandled()) {
					Steam.Lobby.create(LobbyType.Public, 4);
					return true;
				}
				return false;
			}
		});
		stage.addActor(createLobbyButton);

		joinLobbyLabel = new Label("Join Lobby: ", skin);
		centerUI(joinLobbyLabel, 25+yOffset);
		stage.addActor(joinLobbyLabel);

		joinLobbyID = new TextField("", skin);
		centerUI(joinLobbyID, -10+yOffset);
		stage.addActor(joinLobbyID);

		joinLobbyButton = new TextButton("Join", skin);
		joinLobbyButton.setSize(80, 25);
		centerUI(joinLobbyButton, -40+yOffset);
		joinLobbyButton.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (event.isHandled()) {
					try {
						Steam.Lobby.join(Integer.valueOf(joinLobbyID.getText()));
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
				return false;
			}
		});
		stage.addActor(joinLobbyButton);

		serverID = new Label("", skin);
		stage.addActor(serverID);

		// Message UI
		messageLabel = new Label("Message:", skin);
		centerUI(messageLabel, -75+yOffset);
		messageLabel.setVisible(false);
		stage.addActor(messageLabel);

		messageField = new TextField("", skin);
		centerUI(messageField, -110+yOffset);
		messageField.setVisible(false);
		stage.addActor(messageField);

		messageButton = new TextButton("Send", skin);
		messageButton.setSize(80, 25);
		centerUI(messageButton, -140+yOffset);
		messageButton.setVisible(false);
		messageButton.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (event.isHandled()) {
					Steam.Lobby.sendMessage(messageField.getText());
				}
				return false;
			}
		});
		stage.addActor(messageButton);

		messages = new Label("", skin);
		messages.setY(steamGDX.viewport.getScreenHeight() / 2 - 275 + yOffset);
		messages.setVisible(false);
		stage.addActor(messages);

		readyLabel = new Label("Ready Up:", skin);
		centerUI(readyLabel, -175+yOffset);
		readyLabel.setVisible(false);
		stage.addActor(readyLabel);

		readyButton = new TextButton("Ready", skin);
		centerUI(readyButton, -210+yOffset);
		readyButton.setVisible(false);
		readyButton.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (event.isHandled() && !isReady && readyButton.isVisible()) {
					Steam.Lobby.sendMessage(Steam.Friends.getUsername(Steam.User.getID()) + " is ready to play!");
					readyButton.setText("Not Ready");
					isReady = true;
				} else if (event.isHandled() && isReady && readyButton.isVisible()) {
					Steam.Lobby.sendMessage(Steam.Friends.getUsername(Steam.User.getID()) + " is not ready to play!");
					readyButton.setText("Ready");
					isReady = false;
				}
				return true;
			}
		});
		stage.addActor(readyButton);

		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void render(float delta) {
		steamGDX.camera.update();
		stage.act(Gdx.graphics.getDeltaTime());

		if (Steam.Lobby.inLobby()) {
			serverID.setText("Lobby ID: " + Steam.Lobby.getID().getAccountID() + "\nCurrent Player Count: "
					+ Steam.Lobby.count());
			serverID.setPosition(steamGDX.viewport.getScreenWidth() / 2 - serverID.getText().length * 4.5f,
					steamGDX.viewport.getScreenHeight() / 2 - 200);
			if (!messages.isVisible()) {
				messageLabel.setVisible(true);
				messageField.setVisible(true);
				messageButton.setVisible(true);
				messages.setVisible(true);
			}
			if (!readyLabel.isVisible() && Steam.Lobby.count() > START_COUNT - 1) {
				readyLabel.setVisible(true);
				readyButton.setVisible(true);
			} else if (Steam.Lobby.count() <= START_COUNT - 1) {
				readyLabel.setVisible(false);
				readyButton.setVisible(false);
			}
			if (messageCount < Steam.Lobby.getMessages().size()) {
				LobbyMessage lobbyMessage = Steam.Lobby.getMessages().get(messageCount);
				String message = lobbyMessage.getMessage();

				if (message.contains("joined") || message.contains("is not ready")) {
					playersReady.put(lobbyMessage.getChatEntry().getSteamIDUser(), false);
					messages.setText(messages.getText() + "\n" + message);
				} else if (message.contains("is ready")) {
					playersReady.put(lobbyMessage.getChatEntry().getSteamIDUser(), true);
					messages.setText(messages.getText() + "\n" + message);
				} else {
					messages.setText(messages.getText() + "\n" + lobbyMessage.getUserMessage());
				}
				messageCount++;
			}
		} else {
			messageLabel.setVisible(false);
			messageField.setVisible(false);
			messageButton.setVisible(false);
			messages.setVisible(false);
			readyLabel.setVisible(false);
			readyButton.setVisible(false);
		}

		if (playersReady.size() >= START_COUNT) {
			int readyLoop = 1;
			for (SteamID player : playersReady.keySet()) {
				if (!playersReady.get(player))
					break;
				if (readyLoop == playersReady.size()) {
					HashMap<SteamID, PlayerData> players = new HashMap<>();
					for (SteamID p : Steam.Lobby.players())
						players.put(p, new PlayerData());
					Steam.Lobby.leave();
					steamGDX.setScreen(new NetworkGame(steamGDX, players));
					break;
				}
				readyLoop++;
			}
		}

		stage.draw();
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
		dispose();
	}

	@Override
	public void dispose() {
		stage.clear();
		stage.dispose();
		skin.dispose();
	}
	
	private void centerUI(Actor actor, float yOffset) {
		actor.setPosition(steamGDX.viewport.getScreenWidth() / 2 - actor.getWidth() / 2,
				steamGDX.viewport.getScreenHeight() / 2 + actor.getHeight() / 2 + yOffset);
	}
	
}