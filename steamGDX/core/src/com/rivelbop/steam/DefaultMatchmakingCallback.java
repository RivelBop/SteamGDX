package com.rivelbop.steam;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamMatchmaking.ChatEntryType;
import com.codedisaster.steamworks.SteamMatchmaking.ChatMemberStateChange;
import com.codedisaster.steamworks.SteamMatchmaking.ChatRoomEnterResponse;
import com.codedisaster.steamworks.SteamMatchmakingCallback;
import com.codedisaster.steamworks.SteamResult;

public class DefaultMatchmakingCallback implements SteamMatchmakingCallback{
	
	public int lobbyCount, joinAttempt;
	public SteamID lobbyID;
	public ArrayList<LobbyMessage> messages;
	
	// ADDED JOIN SEND MESSAGE
	
	public DefaultMatchmakingCallback() {
		messages = new ArrayList<LobbyMessage>();
	}
	
	@Override
	public void onFavoritesListChanged(int ip, int queryPort, int connPort, int appID, int flags, boolean add,
			int accountID) {
		
	}

	@Override
	public void onLobbyInvite(SteamID steamIDUser, SteamID steamIDLobby, long gameID) {
		
	}

	@Override
	public void onLobbyEnter(SteamID steamIDLobby, int chatPermissions, boolean blocked,
			ChatRoomEnterResponse response) {
		lobbyID = steamIDLobby;
		Steam.sendLobbyMessage(Steam.getUsername(Steam.getUserID()) + " has joined the lobby!");
		System.out.println("Lobby has been entered!");
	}

	@Override
	public void onLobbyDataUpdate(SteamID steamIDLobby, SteamID steamIDMember, boolean success) {
	
	}

	@Override
	public void onLobbyChatUpdate(SteamID steamIDLobby, SteamID steamIDUserChanged, SteamID steamIDMakingChange,
			ChatMemberStateChange stateChange) {
		
	}

	@Override
	public void onLobbyChatMessage(SteamID steamIDLobby, SteamID steamIDUser, ChatEntryType entryType, int chatID) {
		messages.add(new LobbyMessage());
		LobbyMessage message = messages.get(messages.size() - 1);
		
		ByteBuffer buffer = ByteBuffer.allocateDirect(Steam.BUFFERSIZE);
		int size = 0;
		
		try {
			size = Steam.getMatchmaking().getLobbyChatEntry(steamIDLobby, chatID, message.getChatEntry(), buffer);
		} catch (SteamException e) {
			e.printStackTrace();
		} 
		
		message.setMessage(buffer, size);
		System.out.println(message.getUserMessage());
	}

	@Override
	public void onLobbyGameCreated(SteamID steamIDLobby, SteamID steamIDGameServer, int ip, short port) {
		
	}

	@Override
	public void onLobbyMatchList(int lobbiesMatching) {
		lobbyCount = lobbiesMatching;
		if(joinAttempt != 0) {
			SteamID lobby = Steam.searchForLobby(joinAttempt, lobbyCount);
			if(lobby != null) Steam.joinLobby(lobby);
			joinAttempt = 0;
		}
	}

	@Override
	public void onLobbyKicked(SteamID steamIDLobby, SteamID steamIDAdmin, boolean kickedDueToDisconnect) {
		
	}

	@Override
	public void onLobbyCreated(SteamResult result, SteamID steamIDLobby) {
		lobbyID = steamIDLobby;
		Steam.sendLobbyMessage(Steam.getUsername(Steam.getUserID()) + " has joined the lobby!");
		System.out.println("Lobby Created with ID: " + steamIDLobby.getAccountID());
	}
	
	@Override
	public void onFavoritesListAccountsUpdated(SteamResult result) {
		
	}
	
}