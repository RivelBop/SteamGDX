package com.rivelbop.steam;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import com.badlogic.gdx.Gdx;
import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamApps;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamFriends.OverlayDialog;
import com.codedisaster.steamworks.SteamFriendsCallback;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamMatchmaking;
import com.codedisaster.steamworks.SteamMatchmaking.LobbyType;
import com.codedisaster.steamworks.SteamMatchmakingCallback;
import com.codedisaster.steamworks.SteamNetworking;
import com.codedisaster.steamworks.SteamNetworking.P2PSend;
import com.codedisaster.steamworks.SteamNetworkingCallback;

public class Steam {
	
	// java -jar test.jar    FOR CONSOLE USE WITH JAR
	private static int appID;
	private static float updateTimer;
	
	private static SteamApps apps;
	
	private static SteamFriends friends;
	public static SteamFriendsCallback friendsCallback;
	
	private static SteamMatchmaking matchmaking;
	public static SteamMatchmakingCallback matchmakingCallback;
	private static boolean inLobby, isHost;
	
	private static SteamNetworking networking;
	public static SteamNetworkingCallback networkingCallback;
	
	// Callback enums used to check if the callbacks are default
	private static enum Callback{
		FRIENDS,
		MATCHMAKING,
		NETWORKING;
	}
	
	// Initialize the SteamAPI
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
			}
		} catch (SteamException e) {
			e.printStackTrace();
		}
		
		updateTimer = 0f;
		
		// Initialize the Steam objects
		apps = new SteamApps();
		
		if(friendsCallback == null) friendsCallback = new DefaultFriendsCallback();
		friends = new SteamFriends(friendsCallback);
		friends.activateGameOverlay(OverlayDialog.Friends);
		
		if(matchmakingCallback == null) matchmakingCallback = new DefaultMatchmakingCallback();
		matchmaking = new SteamMatchmaking(matchmakingCallback);
		
		if(networkingCallback == null) networkingCallback = new DefaultNetworkingCallback();
		networking = new SteamNetworking(networkingCallback);
	}
	// Check if Steam is running
	public static boolean update(float updateRate) {
		if(updateTimer < updateRate && isRunning()) {
			updateTimer += Gdx.graphics.getDeltaTime();
			return true;
		}
		if (isRunning()) {
			SteamAPI.runCallbacks();
			updateTimer = 0f;
			return true;
		}
		return false;
	}
	
	// Create a lobby with the publicity and player count
	public static void createLobby(LobbyType type, int players) {
		if(inLobby()) leaveLobby();
		matchmaking.createLobby(type, players);
		inLobby = true;
		isHost = true;
	}
	
	// Join a lobby with the given ID
	public static void joinLobby(SteamID id) {
		if(inLobby()) leaveLobby();
		matchmaking.joinLobby(id);
		inLobby = true;
		isHost = false;
	}
	
	// Returns the SteamID of the lobby with the provided steamID
	public static void joinLobby(int lobbyID) {
		if(checkIfDefault(Callback.MATCHMAKING)) ((DefaultMatchmakingCallback)matchmakingCallback).joinAttempt = lobbyID;
		requestLobbyList();
	}
	
	// Leave the lobby
	public static void leaveLobby() {
		if(inLobby()) {
		 matchmaking.leaveLobby(getLobbyID());
			inLobby = false;
			if(checkIfDefault(Callback.MATCHMAKING)) ((DefaultMatchmakingCallback)matchmakingCallback).lobbyID = null;
		}
	}
	
	// Search for a lobby with the provided id and amount of lobbies
	public static SteamID searchForLobby(int lobbyID, int lobbyCount) {
		for(int i = 0; i < lobbyCount; i++) {
			SteamID lobby = matchmaking.getLobbyByIndex(i);
			if(lobby.getAccountID() == lobbyID) return lobby;
		}
		return null;
	}
	
	// Check if it is default
	public static void sendLobbyMessage(String message) {
		if(inLobby()) matchmaking.sendLobbyChatMsg(getLobbyID(), message);
	}
	
	// Connect the lobby to the SteamNetworking object
	public static void sendPacket(String message) {
		if(inLobby())
			try {
				networking.sendP2PPacket(getLobbyID(), null, P2PSend.Reliable, 0);
			} catch (SteamException e) {
				e.printStackTrace();
			}
	}
	
	// Retrieve the provided AppID
	public static int getAppID() {
		return appID;
	}
	
	// Check to see if the SteamAPI is running
	public static boolean isRunning() {
		return SteamAPI.isSteamRunning();
	}
	
	// Retrieve the SteamApps object
	public static SteamApps getApps() {
		return apps;
	}
	
	// Retrieve the SteamFriends object
	public static SteamFriends getFriends() {
		return friends;
	}
	
	// Retrieve the user name of the provided SteamID
	public static String getUsername(SteamID id) {
		return friends.getFriendPersonaName(id);
	}
	
	// Retrieve the SteamMatchmaking object
	public static SteamMatchmaking getMatchmaking() {
		return matchmaking;
	}
	
	// Return the lobbyID stored in the DefaultMatchmakingCallback
	public static SteamID getLobbyID() {
		if(checkIfDefault(Callback.MATCHMAKING)) return ((DefaultMatchmakingCallback)matchmakingCallback).lobbyID;
		return null;
	}
	
	// Check to see if the player is in a lobby
	public static boolean inLobby() {
		return (inLobby && getLobbyID() != null) || (inLobby && !checkIfDefault(Callback.MATCHMAKING));
	}
	
	// Return the messages sent in the lobby
	public static ArrayList<LobbyMessage> getLobbyMessages(){
		if(checkIfDefault(Callback.MATCHMAKING)) return ((DefaultMatchmakingCallback)matchmakingCallback).messages;
		return null;
	}
	
	// Requests the lobby list
	public static void requestLobbyList() {
		matchmaking.requestLobbyList();
	}
	
	// Returns the number of players in the lobby
	public static int lobbyPlayerCount() {
		if(inLobby()) return matchmaking.getNumLobbyMembers(getLobbyID());
		return -1;
	}
	
	// Retrieve the SteamNetworking object
	public static SteamNetworking getNetworking() {
		return networking;
	}
	
	// Check to see if the provided Callback enum correlates to a default callback object
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
	
	// Shutdown the SteamAPI
	public static void dispose() {
		leaveLobby();
		
		apps.dispose();
		friends.dispose();
		matchmaking.dispose();
		networking.dispose();
		
		SteamAPI.shutdown();
	}
	
}