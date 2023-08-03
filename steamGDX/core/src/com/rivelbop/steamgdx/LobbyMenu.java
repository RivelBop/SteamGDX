package com.rivelbop.steamgdx;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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

public class LobbyMenu implements Screen{
	
	public SteamGDX steamGDX;
	
	public Stage stage;
	public Skin skin;
	public Label createLobbyLabel, joinLobbyLabel, serverID, messageLabel, messages, readyLabel;
	public TextButton createLobbyButton, joinLobbyButton, messageButton, readyButton;
	public TextField joinLobbyID, messageField;
	
	public HashMap<SteamID, Boolean> playersReady;
	
	public final int START_COUNT = 2;
	public int messageCount;
	public float yOffset = 300;
	public boolean isReady;
	
	public LobbyMenu(SteamGDX game) {
		steamGDX = game;
	}
	
	@Override
	public void show() {
		skin = new Skin(Gdx.files.internal("default/skin/uiskin.json"));
        stage = new Stage(steamGDX.viewport);
        playersReady = new HashMap<>();
        messageCount = 0;
        isReady = false;
		
		createLobbyLabel = new Label("Host Lobby:", skin);
		createLobbyLabel.setPosition(steamGDX.viewport.getScreenWidth() / 2 - createLobbyLabel.getWidth() / 2, steamGDX.viewport.getScreenHeight() / 2 + createLobbyLabel.getHeight() * 2 + yOffset);
        stage.addActor(createLobbyLabel);
		
        createLobbyButton = new TextButton("Host", skin);
        createLobbyButton.setSize(80, 25);
        createLobbyButton.setPosition(steamGDX.viewport.getScreenWidth() / 2 - createLobbyButton.getWidth() / 2, steamGDX.viewport.getScreenHeight() / 2 + createLobbyButton.getHeight() + yOffset);
        createLobbyButton.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if(event.isHandled()) Steam.Lobby.create(LobbyType.Public, 4);
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
                	try {
                		Steam.Lobby.join(Integer.valueOf(joinLobbyID.getText()));
                	} catch(NumberFormatException e) {
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
                if(event.isHandled()) {
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
        readyLabel.setPosition(steamGDX.viewport.getScreenWidth() / 2 - readyLabel.getWidth() / 2, steamGDX.viewport.getScreenHeight() / 2 - messageButton.getHeight() - 300 + yOffset);
        readyLabel.setVisible(false);
        stage.addActor(readyLabel);
        
        readyButton = new TextButton("Ready", skin);
        readyButton.setPosition(steamGDX.viewport.getScreenWidth() / 2 - readyButton.getWidth() / 2, steamGDX.viewport.getScreenHeight() / 2 - readyButton.getHeight() - 350 + yOffset);
        readyButton.setVisible(false);
        readyButton.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if(event.isHandled() && !isReady && readyButton.isVisible()) {
                	Steam.Lobby.sendMessage(Steam.Friends.getUsername(Steam.User.getID()) + " is ready to play!");
                	readyButton.setText("Not Ready");
                	isReady = true;
                }else if(event.isHandled() && isReady && readyButton.isVisible()) {
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
		
		if(Steam.Lobby.inLobby()) {
			serverID.setText("Lobby ID: " + Steam.Lobby.getID().getAccountID() + "\nCurrent Player Count: " + Steam.Lobby.count());
			serverID.setPosition(steamGDX.viewport.getScreenWidth() / 2 - serverID.getText().length * 4.5f, steamGDX.viewport.getScreenHeight() / 2 - 200);
			if(!messages.isVisible()) {
				messageLabel.setVisible(true);
				messageField.setVisible(true);
				messageButton.setVisible(true);
				messages.setVisible(true);
			}
			if(!readyLabel.isVisible() && Steam.Lobby.count() > START_COUNT - 1) {
				readyLabel.setVisible(true);
				readyButton.setVisible(true);
			}else if(Steam.Lobby.count() <= START_COUNT - 1){
				readyLabel.setVisible(false);
				readyButton.setVisible(false);
			}
			if(messageCount < Steam.Lobby.getMessages().size()) {
				LobbyMessage lobbyMessage = Steam.Lobby.getMessages().get(messageCount);
				String message = lobbyMessage.getMessage();
				
				if(message.contains("joined") || message.contains("is not ready")) {
					playersReady.put(lobbyMessage.getChatEntry().getSteamIDUser(), false);
					messages.setText(messages.getText() + "\n" + message);
				}else if(message.contains("is ready")) {
					playersReady.put(lobbyMessage.getChatEntry().getSteamIDUser(), true);
					messages.setText(messages.getText() + "\n" + message);
				}else {
					messages.setText(messages.getText() + "\n" + lobbyMessage.getUserMessage());
				}
				messageCount++;
			}
		}else {
			messageLabel.setVisible(false);
			messageField.setVisible(false);
			messageButton.setVisible(false);
			messages.setVisible(false);
			readyLabel.setVisible(false);
			readyButton.setVisible(false);
		}
		
		if(playersReady.size() >= START_COUNT) {
			int readyLoop = 1;
			for(SteamID player : playersReady.keySet()) {
				if(!playersReady.get(player)) break;
				if(readyLoop == playersReady.size()) {
					steamGDX.setScreen(new NetworkGame(steamGDX));
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
	
}