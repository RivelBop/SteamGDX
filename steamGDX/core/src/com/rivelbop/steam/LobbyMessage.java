package com.rivelbop.steam;

import java.nio.ByteBuffer;

import com.codedisaster.steamworks.SteamMatchmaking.ChatEntry;

// Used to store data of a message sent within a lobby
public class LobbyMessage {
	
	// Private to insure the user cannot alter the data
	private ChatEntry entry;
	private String message;
	
	// Create an empty ChatEntry and String
	public LobbyMessage() {
		entry = new ChatEntry();
		message = "";
	}
	
	// Returns the ChatEntry object stored within the lobby message
	public ChatEntry getChatEntry() {
		return entry;
	}
	
	// Returns the message
	public String getMessage() {
		return message;
	}
	
	// Returns the user name of the player along with the message
	public String getUserMessage() {
		return Steam.getUsername(entry.getSteamIDUser()) + ": " + message;
	}
	
	// Converts the provided ByteBuffer into a String
	public void setMessage(ByteBuffer buffer, int size) {
		byte[] bytes = new byte[size];
		buffer.get(bytes);
		message = new String(bytes);
	}
	
}