package com.rivelbop.steam;

import java.nio.ByteBuffer;

import com.codedisaster.steamworks.SteamMatchmaking.ChatEntry;

// Used to store data of a message sent within a lobby
public class LobbyMessage {
	
	// Private to insure the user cannot alter the data
	private ChatEntry entry;
	private ByteBuffer buffer;
	private int size;
	
	// Create an empty ChatEntry and allocate memory (4096) for the ByteBuffer
	public LobbyMessage() {
		entry = new ChatEntry();
		buffer = ByteBuffer.allocateDirect(4096);
		size = 0;
	}
	
	// Returns the ChatEntry object stored within the lobby message
	public ChatEntry getChatEntry() {
		return entry;
	}
	
	// Returns the ByteBuffer/Byte data stored within the lobby message
	public ByteBuffer getBuffer() {
		return buffer;
	}
	
	// Returns the size of the data received
	public int getSize() {
		return size;
	}
	
	// Sets the size of the data received
	public void setSize(int providedSize) {
		size = providedSize;
	}
	
	// Returns the String conversion of the lobby message
	public String toString() {
		if(size != 0) {
			byte[] bytes = new byte[size];
			buffer.get(bytes);
			return new String(bytes);
		}
		return null;
	}
	
}