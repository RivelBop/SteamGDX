package com.rivelbop.steam;

import com.badlogic.gdx.Gdx;
import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamFriends.OverlayDialog;
import com.codedisaster.steamworks.SteamFriendsCallback;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamMatchmaking;
import com.codedisaster.steamworks.SteamMatchmaking.LobbyType;
import com.codedisaster.steamworks.SteamMatchmakingCallback;
import com.codedisaster.steamworks.SteamNetworking;
import com.codedisaster.steamworks.SteamNetworkingCallback;

public class Steam {
	
	private static int appID;
	private static boolean isInit;
	
	private static SteamFriends friends;
	public static SteamFriendsCallback friendsCallback;
	
	private static SteamMatchmaking matchmaking;
	public static SteamMatchmakingCallback matchmakingCallback;
	private static boolean inLobby, isHost, isInviteInit;
	
	private static SteamNetworking networking;
	public static SteamNetworkingCallback networkingCallback;
	
	private static enum Callback{
		FRIENDS,
		MATCHMAKING,
		NETWORKING;
	}
	
	public static void init(int steamAppID) {
		// Attempt to initialize steam with the given appID
		try {
			SteamAPI.loadLibraries();
			
			appID = steamAppID;
			if(SteamAPI.restartAppIfNecessary(appID)){
				System.err.println("Steam App connection error!");
				Gdx.app.exit();
			}
			
			if (!SteamAPI.init()) {
				System.err.println("Steam API initialization error!");
			} else {
				System.out.println("Steam API has been successfully initialized!");
				isInit = true;
			}
		} catch (SteamException e) {
			e.printStackTrace();
		}
		
		// Initialize the Steam objects
		if(friendsCallback == null) friendsCallback = new DefaultFriendsCallback();
		friends = new SteamFriends(friendsCallback);
		friends.activateGameOverlay(OverlayDialog.Friends);
		
		if(matchmakingCallback == null) matchmakingCallback = new DefaultMatchmakingCallback();
		matchmaking = new SteamMatchmaking(matchmakingCallback);
		
		if(networkingCallback == null) networkingCallback = new DefaultNetworkingCallback();
		networking = new SteamNetworking(networkingCallback);
	}
	
	// Check if Steam is running
	public static boolean update() {
		if (isInit && SteamAPI.isSteamRunning()) {
			SteamAPI.runCallbacks();
			if(!isInviteInit && inLobby && matchmakingCallback instanceof DefaultMatchmakingCallback
					&& getLobbyID() != null) {
				friends.activateGameOverlayInviteDialog(getLobbyID());
				isInviteInit = true;
			}
			return true;
		}
		return false;
	}
	
	// Create a lobby with the publicity and player count
	// ADD ISHOST *
	public static void createLobby(LobbyType type, int players) {
		if(isInit) {
			matchmaking.createLobby(type, players);
			inLobby = true;
		}
	}
	
	// Join a lobby with the given ID
	public static void joinLobby(SteamID id) {
		if(isInit) {
			matchmaking.joinLobby(id);
			inLobby = true;
		}
	}
	
	public static int getAppID() {
		return appID;
	}
	
	public static SteamFriends getFriends() {
		return friends;
	}
	
	public static SteamMatchmaking getMatchmaking() {
		return matchmaking;
	}
	
	public static SteamID getLobbyID() {
		if(matchmakingCallback instanceof DefaultMatchmakingCallback) {
			return ((DefaultMatchmakingCallback)matchmakingCallback).lobbyID;
		}
		return null;
	}
	
	public static boolean inLobby() {
		return inLobby;
	}
	
	public static SteamNetworking getNetworking() {
		return networking;
	}
	
	// USE FOR CODE*
	private static boolean checkIfDefault(Callback callback) {
		switch(callback) {
			case FRIENDS:
				return friendsCallback instanceof DefaultFriendsCallback;
			case MATCHMAKING:
				return matchmakingCallback instanceof DefaultMatchmakingCallback;
			case NETWORKING:
				return networkingCallback instanceof DefaultNetworkingCallback;
			default:
				return false;
		}
	}
	
}