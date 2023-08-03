package com.rivelbop.steam;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamApps;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamFriendsCallback;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamMatchmaking;
import com.codedisaster.steamworks.SteamMatchmaking.LobbyType;
import com.codedisaster.steamworks.SteamMatchmakingCallback;
import com.codedisaster.steamworks.SteamNetworking;
import com.codedisaster.steamworks.SteamNetworking.P2PSend;
import com.codedisaster.steamworks.SteamNetworkingCallback;
import com.codedisaster.steamworks.SteamUser;
import com.codedisaster.steamworks.SteamUser.VoiceResult;
import com.codedisaster.steamworks.SteamUserCallback;

public class Steam {
	
	public static final int BUFFERSIZE = 4096, VOICEBUFFERSIZE = 1024;
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
	private static enum Callback {
		USER,
		FRIENDS,
		MATCHMAKING,
		NETWORKING;
	}
	
	// Predetermined channels to send data over
	public static class Channel {
		public static final int 
			MESSAGE = 0,
			VOICE = 1;
	}
	
	// Initialize the SteamAPI
	public static void init(int steamAppID) {
		// Attempt to initialize steam with the given appID
		try {
			SteamAPI.loadLibraries();
			if(SteamAPI.restartAppIfNecessary(steamAppID)){
				System.err.println("Steam App connection error!");
				Gdx.app.exit();
			}
			
			if(!SteamAPI.init()) System.err.println("Steam API initialization error!");
			else System.out.println("Steam API has been successfully initialized!");
			
		} catch (SteamException e) {
			e.printStackTrace();
		}
		
		// Initialize the Steam objects
		apps = new SteamApps();
		
		if(userCallback == null) userCallback = new DefaultUserCallback();
		user = new SteamUser(userCallback);
		
		if(friendsCallback == null) friendsCallback = new DefaultFriendsCallback();
		friends = new SteamFriends(friendsCallback);
		
		if(matchmakingCallback == null) matchmakingCallback = new DefaultMatchmakingCallback();
		matchmaking = new SteamMatchmaking(matchmakingCallback);
		
		if(networkingCallback == null) networkingCallback = new DefaultNetworkingCallback();
		networking = new SteamNetworking(networkingCallback);
		networking.allowP2PPacketRelay(true);
	}
	
	// Check if Steam is running
	public static boolean update(float updateRate) {
		if(isRunning()) {
			if(updateTimer < updateRate) {
				updateTimer += Gdx.graphics.getDeltaTime();
				return true;
			}
			SteamAPI.runCallbacks();
			updateTimer = 0f;
			return true;
		}
		
		return false;
	}
	
	// Convert String to Directly Allocated ByteBuffer
	public static ByteBuffer stringToBuffer(String string) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFERSIZE);
		
		byte[] bytes = string.getBytes();
		buffer.put(bytes);
		
		((Buffer)buffer).flip();
		
		return buffer;
	}
	
	// Check to see if the SteamAPI is running
	public static boolean isRunning() {
		return SteamAPI.isSteamRunning();
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
	
	public static class App {
		
		// Retrieve the provided AppID
		public static int getID() {
			return apps.getAppBuildId();
		}
		
		// Retrieve the SteamApps object
		public static SteamApps get() {
			return apps;
		}
		
	}
	
	public static class User {
		
		private static boolean voiceStarted;
		
		// Voice chat
		public static void startVoice() {
			if(!voiceStarted) {
				user.startVoiceRecording();
				voiceStarted = true;
				System.out.println("Voice Recording Has Started!");
			}
		}
		
		// Send a Voice Message to the specified SteamID
		public static void sendVoiceToUser(SteamID id, P2PSend packetType) {
			int[] voiceBytes = new int[1];
			VoiceResult result = user.getAvailableVoice(voiceBytes);
			
			if(result == VoiceResult.OK) {
				ByteBuffer buffer = ByteBuffer.allocateDirect(VOICEBUFFERSIZE);
				try {
					user.getVoice(buffer, voiceBytes);
					((Buffer)buffer).flip();
					Network.sendPacket(id, buffer, packetType, Channel.VOICE);
				} catch (SteamException e) {
					e.printStackTrace();
				}
			}
		}
		
		// Send a Voice Message to the Lobby the user is currently inside of
		public static void sendVoiceToLobby(P2PSend packetType) {
			int[] voiceBytes = new int[1];
			VoiceResult result = user.getAvailableVoice(voiceBytes);
			
			if(result == VoiceResult.OK) {
				ByteBuffer buffer = ByteBuffer.allocateDirect(VOICEBUFFERSIZE);
				try {
					user.getVoice(buffer, voiceBytes);
					((Buffer)buffer).flip();
					Lobby.sendPacket(buffer, packetType, Channel.VOICE);
				} catch (SteamException e) {
					e.printStackTrace();
				}
			}
		}
		
		public static ByteBuffer getVoice(ByteBuffer buffer) {
			int[] bytesWritten = new int[1];
			ByteBuffer audio = ByteBuffer.allocateDirect(VOICEBUFFERSIZE);
			
			try {
				user.decompressVoice(buffer, audio, bytesWritten, user.getVoiceOptimalSampleRate());
				return audio;
			} catch (SteamException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		public static void stopVoice() {
			if(voiceStarted) {
				user.stopVoiceRecording();
				voiceStarted = false;
				System.out.println("Voice Recording Has Stopped!");
			}
		}
		
		// Retrieve the current users SteamID
		public static SteamID getID() {
			return user.getSteamID();
		}
		
		// Retrieve the SteamUser object
		public static SteamUser get() {
			return user;
		}
		
	}
	
	public static class Friends {
		
		// Retrieve the user name of the provided SteamID
		public static String getUsername(SteamID id) {
			return friends.getFriendPersonaName(id);
		}
		
		// Retrieve the SteamFriends object
		public static SteamFriends get() {
			return friends;
		}
		
	}
	
	public static class Lobby {
		
		// Create a lobby with the publicity and player count
		public static void create(LobbyType type, int players) {
			leave();
			matchmaking.createLobby(type, players);
			inLobby = true;
			isHost = true;
		}
		
		// Join a lobby with the provided SteamID
		public static void join(SteamID id) {
			leave();
			matchmaking.joinLobby(id);
			inLobby = true;
			isHost = false;
		}
		
		// Join a lobby with the provided ID
		public static void join(int lobbyID) {
			if(checkIfDefault(Callback.MATCHMAKING))
				((DefaultMatchmakingCallback)matchmakingCallback).joinAttempt = lobbyID;
			
			requestList();
		}
		
		// Leave the lobby
		public static void leave() {
			if(inLobby()) {
				matchmaking.leaveLobby(getID());
				inLobby = false;
				
				if(checkIfDefault(Callback.MATCHMAKING))
					((DefaultMatchmakingCallback)matchmakingCallback).lobbyID = null;
			}
		}
		
		// Search for a lobby with the provided id and amount of lobbies
		public static SteamID search(int lobbyID, int lobbyCount) {
			for(int i = 0; i < lobbyCount; i++) {
				SteamID lobby = matchmaking.getLobbyByIndex(i);
				if(lobby.getAccountID() == lobbyID) return lobby;
			}
			return null;
		}
		
		// Sends a String message to the joined lobby
		public static void sendMessage(String message) {
			if(inLobby()) matchmaking.sendLobbyChatMsg(getID(), message);
		}
		
		// Send a string packet to a lobby using the SteamNetworking object
		public static void sendPacket(String message, P2PSend type, int channel) {
			if(inLobby()) {
				for(int i = 0; i < count(); i++)
					Network.sendPacket(matchmaking.getLobbyMemberByIndex(getID(), i), message, type, channel);
				
				//System.out.println("Packet sent with message: |" + message + "| to lobby: " + getID().getAccountID());
			}
		}
		
		// Send a buffer packet to a lobby using the SteamNetworking object
		public static void sendPacket(ByteBuffer message, P2PSend type, int channel) {
			if(inLobby()) {
				for(int i = 0; i < count(); i++)
					Network.sendPacket(matchmaking.getLobbyMemberByIndex(getID(), i), message, type, channel);
				
				//System.out.println("ByteBuffer sent to lobby: " + getID().getAccountID());
			}
		}
		
		// Return the lobbyID stored in the DefaultMatchmakingCallback
		public static SteamID getID() {
			return checkIfDefault(Callback.MATCHMAKING) ? ((DefaultMatchmakingCallback)matchmakingCallback).lobbyID : null;
		}
		
		// Check to see if the player is in a lobby
		public static boolean inLobby() {
			return (inLobby && getID() != null) || (inLobby && !checkIfDefault(Callback.MATCHMAKING));
		}
		
		// Return the messages sent in the lobby
		public static ArrayList<LobbyMessage> getMessages(){
			return checkIfDefault(Callback.MATCHMAKING) ? ((DefaultMatchmakingCallback)matchmakingCallback).messages : null;
		}
		
		// Requests the lobby list
		public static void requestList() {
			matchmaking.requestLobbyList();
		}
		
		// Returns the number of players in the lobby
		public static int count() {
			return inLobby() ? matchmaking.getNumLobbyMembers(getID()) : -1;
		}
		
		// Retrieve a list of all players connected to the lobby
		public static ArrayList<SteamID> players(){
			if(inLobby()) {
				ArrayList<SteamID> players = new ArrayList<SteamID>();
				for(int i = 0; i < count(); i++)
					players.add(matchmaking.getLobbyMemberByIndex(getID(), i));
				
				return players;
			}
			return null;
		}
		
		// Retrieve the SteamMatchmaking object
		public static SteamMatchmaking get() {
			return matchmaking;
		}
		
	}
	
	public static class Network {
		// Send a string packet to a user using the SteamNetworking object
		public static void sendPacket(SteamID id, String message, P2PSend type, int channel) {
			try {
				if(id != User.getID())
					networking.sendP2PPacket(id, stringToBuffer(message), type, channel);
				
				System.out.println("Packet sent with message: |" + message + "| to user: " + id.getAccountID());
			} catch (SteamException e) {
				e.printStackTrace();
			}
		}
		
		// Send a buffer packet to a user using the SteamNetworking object
		public static void sendPacket(SteamID id, ByteBuffer message, P2PSend type, int channel) {
			try {
				if(id != User.getID()) 
					networking.sendP2PPacket(id, message, type, channel);

				System.out.println("ByteBuffer sent to user: " + id.getAccountID());
			} catch (SteamException e) {
				e.printStackTrace();
			}
		}
		
		// Receive a packet using the SteamNetworking object
		public static PacketData receivePacket(int channel) {
			int[] packetSize = new int[1];
			
			if(networking.isP2PPacketAvailable(channel, packetSize)) {
				// Entry data objects
				SteamID player = new SteamID();
				ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFERSIZE);
				
				if(packetSize[0] > buffer.capacity())
					System.err.println("Incoming packet larger than read buffer can handle");
				
				int packetReadSize = -1;
				try {
					packetReadSize = networking.readP2PPacket(player, buffer, channel);
				} catch (SteamException e) {
					e.printStackTrace();
				}
				
				if(packetReadSize == 0)
					System.err.println("Packet expected " + packetSize[0] + " bytes, but got none");
				else if (packetReadSize < packetSize[0])
					System.err.println("Packet expected " + packetSize[0] + " bytes, but only got " + packetReadSize);
				
				((Buffer)buffer).limit(packetReadSize);
				
				if(packetReadSize > 0) {
					int bytesReceived = ((Buffer)buffer).limit();
					System.out.println("Packet recieved by: " + Friends.getUsername(player) + ", " + bytesReceived + " bytes");
					
					switch(channel) {
						case Channel.MESSAGE:
							byte[] bytes = new byte[bytesReceived];
							buffer.get(bytes);
							
							String message = new String(bytes);
							System.out.println("Packet recieved: \"" + message + "\"");
							
							return new PacketData(player, message);
						case Channel.VOICE:
							return new PacketData(player, buffer);
						default:
							System.err.println("This channel is unknown!");
							return null;
					}
				}
			}
			return null;
		}
		
		// Retrieve the SteamNetworking object
		public static SteamNetworking get() {
			return networking;
		}
		
	}
	
	// Shutdown the SteamAPI
	public static void dispose() {
		Lobby.leave();
		
		apps.dispose();
		user.dispose();
		friends.dispose();
		matchmaking.dispose();
		networking.dispose();
		
		SteamAPI.shutdown();
	}
	
}