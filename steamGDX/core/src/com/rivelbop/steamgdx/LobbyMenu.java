package com.rivelbop.steamgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.codedisaster.steamworks.SteamMatchmaking.LobbyType;
import com.rivelbop.steam.Steam;

public class LobbyMenu implements Screen{
	
	public SteamGDX steamGDX;
	
	public Stage stage;
	public Skin skin;
	public Label createLobbyLabel, joinLobbyLabel, serverID, messageLabel, messages;
	public TextButton createLobbyButton, joinLobbyButton, messageButton;
	public TextField joinLobbyID, messageField;
	
	public int messageCount;
	public float yOffset = 300;
	
	public LobbyMenu(SteamGDX game) {
		steamGDX = game;
	}
	
	@Override
	public void show() {
		skin = new Skin(Gdx.files.internal("default/skin/uiskin.json"));
        stage = new Stage(steamGDX.viewport);
        messageCount = 0;
		
		createLobbyLabel = new Label("Host Lobby:", skin);
		createLobbyLabel.setPosition(steamGDX.viewport.getScreenWidth() / 2 - createLobbyLabel.getWidth() / 2, steamGDX.viewport.getScreenHeight() / 2 + createLobbyLabel.getHeight() * 2 + yOffset);
        stage.addActor(createLobbyLabel);
		
        createLobbyButton = new TextButton("Host", skin);
        createLobbyButton.setSize(80, 25);
        createLobbyButton.setPosition(steamGDX.viewport.getScreenWidth() / 2 - createLobbyButton.getWidth() / 2, steamGDX.viewport.getScreenHeight() / 2 + createLobbyButton.getHeight() + yOffset);
        createLobbyButton.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if(event.isHandled()) Steam.createLobby(LobbyType.Public, 4);
                return false;
            }
        });
        stage.addActor(createLobbyButton);
        
        joinLobbyLabel = new Label("Join Lobby: ", skin);
        joinLobbyLabel.setPosition(steamGDX.viewport.getScreenWidth() / 2 - joinLobbyLabel.getWidth() / 2, steamGDX.viewport.getScreenHeight() / 2 - joinLobbyLabel.getHeight() * 2 + yOffset);
        stage.addActor(joinLobbyLabel);
        
        joinLobbyID = new TextField("", skin);
        joinLobbyID.setPosition(steamGDX.viewport.getScreenWidth() / 2 - joinLobbyID.getWidth() / 2, steamGDX.viewport.getScreenHeight() / 2 - joinLobbyID.getHeight() * 3 + yOffset);
        stage.addActor(joinLobbyID);
        
        joinLobbyButton = new TextButton("Join", skin);
        joinLobbyButton.setSize(80, 25);
        joinLobbyButton.setPosition(steamGDX.viewport.getScreenWidth() / 2 - joinLobbyButton.getWidth() / 2, steamGDX.viewport.getScreenHeight() / 2 - joinLobbyButton.getHeight() * 5 + yOffset);
        joinLobbyButton.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if(event.isHandled()) {
                	Steam.joinLobby(Integer.valueOf(joinLobbyID.getText()));
                }
                return false;
            }
        });
        stage.addActor(joinLobbyButton);
        
        serverID = new Label("", skin);
        stage.addActor(serverID);
        
        // Message UI
        messageLabel = new Label("Message:", skin);
        messageLabel.setPosition(steamGDX.viewport.getScreenWidth() / 2 - messageLabel.getWidth() / 2, steamGDX.viewport.getScreenHeight() / 2 - 200 + yOffset);
        messageLabel.setVisible(false);
        stage.addActor(messageLabel);
        
        messageField = new TextField("", skin);
        messageField.setPosition(steamGDX.viewport.getScreenWidth() / 2 - messageField.getWidth() / 2, steamGDX.viewport.getScreenHeight() / 2 - 235 + yOffset);
        messageField.setVisible(false);
        stage.addActor(messageField);
        
        messageButton = new TextButton("Send", skin);
        messageButton.setSize(80, 25);
        messageButton.setPosition(steamGDX.viewport.getScreenWidth() / 2 - messageButton.getWidth() / 2, steamGDX.viewport.getScreenHeight() / 2 - messageButton.getHeight() - 250 + yOffset);
        messageButton.setVisible(false);
        messageButton.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if(event.isHandled() && Steam.inLobby()) {
                	Steam.sendLobbyMessage(messageField.getText());
					Steam.sendLobbyPacket(messageField.getText());
                }
                return false;
            }
        });
        stage.addActor(messageButton);
        
        messages = new Label("", skin);
        messages.setY(steamGDX.viewport.getScreenHeight() / 2 - 275 + yOffset);
        messages.setVisible(false);
        stage.addActor(messages);
        
        Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void render(float delta) {
		steamGDX.camera.update();
		stage.act(Gdx.graphics.getDeltaTime());
		
		if(Steam.inLobby()) {
			serverID.setText("Lobby ID: " + Steam.getLobbyID().getAccountID() + "\nCurrent Player Count: " + Steam.lobbyPlayerCount());
			serverID.setPosition(steamGDX.viewport.getScreenWidth() / 2 - serverID.getText().length * 4.5f, steamGDX.viewport.getScreenHeight() / 2 - 200);
			if(!messages.isVisible()) {
				messageLabel.setVisible(true);
				messageField.setVisible(true);
				messageButton.setVisible(true);
				messages.setVisible(true);
			}
			if(messageCount < Steam.getLobbyMessages().size()) {
				messages.setText(messages.getText() + "\n" + Steam.getLobbyMessages().get(messageCount).getUserMessage());
				messageCount++;
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
		stage.dispose();
		skin.dispose();
	}
	
}