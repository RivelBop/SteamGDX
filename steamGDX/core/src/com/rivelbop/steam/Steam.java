package com.rivelbop.steam;

import java.nio.Buffer;
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
import com.codedisaster.steamworks.SteamUser;
import com.codedisaster.steamworks.SteamUserCallback;

public class Steam {
	
	// java -jar test.jar    FOR CONSOLE USE WITH JAR
	public static final int BUFFERSIZE = 4096;
	private static int appID;
	private static float updateTimer;
	
	private static SteamApps apps;
	
	private static SteamUser user;
	public static SteamUserCallback userCallback;
	
	private static SteamFriends friends;
	public static SteamFriendsCallback friendsCallback;
	
	private static SteamMatchmaking matchmaking;
	public static SteamMatchmakingCallback matchmakingCallback;
	private static boolean inLobby, isHost;
	
	private static SteamNetworking networking;
	public static SteamNetworkingCallback networkingCallback;
	
	// Callback enums used to check if the callbacks are default
	private static enum Callback{
		USER,
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
		
		if(userCallback == null) userCallback = new DefaultUserCallback();
		user = new SteamUser(userCallback);
		
		if(friendsCallback == null) friendsCallback = new DefaultFriendsCallback();
		friends = new SteamFriends(friendsCallback);
		friends.activateGameOverlay(OverlayDialog.Friends);
		
		if(matchmakingCallback == null) matchmakingCallback = new DefaultMatchmakingCallback();
		matchmaking = new SteamMatchmaking(matchmakingCallback);
		
		if(networkingCallback == null) networkingCallback = new DefaultNetworkingCallback();
		networking = new SteamNetworking(networkingCallback);
		networking.allowP2PPacketRelay(true);
	}
	// Check if Steam is running
	public static boolean update(float updateRate) {
		if(updateTimer < updateRate && isRunning()) {
			updateTimer += Gdx.graphics.getDeltaTime();
			receivePacket();
			return true;
		}
		if (isRunning()) {
			SteamAPI.runCallbacks();
			receivePacket();
			updateTimer = 0f;
			return true;
		}
		return false;
	}
	
	// Create a lobby with the publicity and player count
	public static void createLobby(LobbyType type, int players) {
		leaveLobby();
		matchmaking.createLobby(type, players);
		inLobby = true;
		isHost = true;
	}
	
	// Join a lobby with the given ID
	public static void joinLobby(SteamID id) {
		leaveLobby();
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
	
	// Convert String to Directly Allocated ByteBuffer
	public static ByteBuffer stringToBuffer(String string) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFERSIZE);
		
		((Buffer)buffer).clear();
		
		byte[] bytes = string.getBytes();
		buffer.put(bytes);
		
		((Buffer)buffer).flip();
		
		return buffer;
	}
	
	// Send a packet to a lobby using the SteamNetworking object
	public static void sendLobbyPacket(String message) {
		if(inLobby()) {
			try {
				for(int i = 0; i < lobbyPlayerCount(); i++) {
					SteamID player = matchmaking.getLobbyMemberByIndex(getLobbyID(), i);
					if(player.getAccountID() != getUserID().getAccountID()) {
						networking.sendP2PPacket(player, stringToBuffer(message), P2PSend.Reliable, 0);
					}
				}
				System.out.println("Packet sent with message: |" + message + "| to lobby: " + getLobbyID().getAccountID());
			} catch (SteamException e) {
				e.printStackTrace();
			}
		}
	}
	
	// Receive a packet from a lobby using the SteamNetworking object
	public static void receivePacket() {
		int[] packetSize = new int[1];
		
		if(inLobby() && networking.isP2PPacketAvailable(0, packetSize)) {
			// Entry data objects
			SteamID player = new SteamID();
			ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFERSIZE);
			
			if(packetSize[0] > buffer.capacity()) {
				System.err.println("Incoming packet larger than read buffer can handle");
			}
			
			((Buffer)buffer).clear();
			
			int packetReadSize = -1;
			try {
				packetReadSize = networking.readP2PPacket(player, buffer, 0);
			} catch (SteamException e) {
				e.printStackTrace();
			}
			
			if (packetReadSize == 0) {
				System.err.println("Packet expected " + packetSize[0] + " bytes, but got none");
			} else if (packetReadSize < packetSize[0]) {
				System.err.println("Packet expected " + packetSize[0] + " bytes, but only got " + packetReadSize);
			}
			
			buffer.limit(packetReadSize);
			
			if(packetReadSize > 0) {
				int bytesReceived = buffer.limit();
				System.out.println("Packet recieved by: " + getUsername(player) + ", " + bytesReceived + " bytes");
	
				byte[] bytes = new byte[bytesReceived];
				buffer.get(bytes);
				
				String message = new String(bytes);
				System.out.println("Packet recieved: \"" + message + "\"");
			}
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
	
	// Retrieve the SteamUser object
	public static SteamUser getUser() {
		return user;
	}
	
	// Retrieve the current users SteamID
	public static SteamID getUserID() {
		return user.getSteamID();
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
		return (checkIfDefault(Callback.MATCHMAKING)) ? ((DefaultMatchmakingCallback)matchmakingCallback).lobbyID : null;
	}
	
	// Check to see if the player is in a lobby
	public static boolean inLobby() {
		return (inLobby && getLobbyID() != null) || (inLobby && !checkIfDefault(Callback.MATCHMAKING));
	}
	
	// Return the messages sent in the lobby
	public static ArrayList<LobbyMessage> getLobbyMessages(){
		return (checkIfDefault(Callback.MATCHMAKING)) ? ((DefaultMatchmakingCallback)matchmakingCallback).messages : null;
	}
	
	// Requests the lobby list
	public static void requestLobbyList() {
		matchmaking.requestLobbyList();
	}
	
	// Returns the number of players in the lobby
	public static int lobbyPlayerCount() {
		return (inLobby()) ? matchmaking.getNumLobbyMembers(getLobbyID()) : -1;
	}
	
	// Retrieve a list of all players connected to the lobby
	public static ArrayList<SteamID> playersInLobby(){
		if(inLobby()) {
			ArrayList<SteamID> players = new ArrayList<SteamID>();
			for(int i = 0; i < lobbyPlayerCount(); i++) {
				players.add(matchmaking.getLobbyMemberByIndex(getLobbyID(), i));
			}
			return players;
		}
		return null;
	}
	
	// Retrieve the SteamNetworking object
	public static SteamNetworking getNetworking() {
		return networking;
	}
	
	// Check to see if the provided Callback enum correlates to a default callback object
	private static boolean checkIfDefault(Callback callback) {
		switch(callback) {
			case USER:
				return userCallback instanceof DefaultUserCallback;
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
		user.dispose();
		friends.dispose();
		matchmaking.dispose();
		networking.dispose();
		
		SteamAPI.shutdown();
	}
	
}