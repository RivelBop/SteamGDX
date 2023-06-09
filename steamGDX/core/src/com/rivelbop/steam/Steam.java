package com.rivelbop.steam;

import com.badlogic.gdx.Gdx;
import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamFriends.OverlayDialog;
import com.codedisaster.steamworks.SteamMatchmaking;
import com.codedisaster.steamworks.SteamMatchmaking.LobbyType;
import com.codedisaster.steamworks.SteamNetworking;

public class Steam {
	
	public static int appID;
	private static boolean isInit;
	
	public static SteamFriends friends;
	public static FriendsCallback friendsCallback;
	
	public static SteamMatchmaking matchmaking;
	public static MatchmakingCallback matchmakingCallback;
	
	public static SteamNetworking networking;
	public static NetworkingCallback networkingCallback;
	
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
		friendsCallback = new FriendsCallback();
		friends = new SteamFriends(friendsCallback);
		friends.activateGameOverlay(OverlayDialog.Friends);
		
		matchmakingCallback = new MatchmakingCallback();
		matchmaking = new SteamMatchmaking(matchmakingCallback);
		matchmaking.createLobby(LobbyType.FriendsOnly, 4);
		
		networkingCallback = new NetworkingCallback();
		networking = new SteamNetworking(networkingCallback);
	}
	
	// Check if Steam is running
	public static boolean update() {
		if (isInit && SteamAPI.isSteamRunning()) {
			SteamAPI.runCallbacks();
			return true;
		}
		return false;
	}
	
}