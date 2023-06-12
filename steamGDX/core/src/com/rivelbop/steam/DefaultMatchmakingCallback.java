package com.rivelbop.steam;

import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamMatchmaking.ChatEntryType;
import com.codedisaster.steamworks.SteamMatchmaking.ChatMemberStateChange;
import com.codedisaster.steamworks.SteamMatchmaking.ChatRoomEnterResponse;
import com.codedisaster.steamworks.SteamMatchmakingCallback;
import com.codedisaster.steamworks.SteamResult;

public class DefaultMatchmakingCallback implements SteamMatchmakingCallback{
	
	public SteamID lobbyID;
	
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
		
	}

	@Override
	public void onLobbyGameCreated(SteamID steamIDLobby, SteamID steamIDGameServer, int ip, short port) {
		
	}

	@Override
	public void onLobbyMatchList(int lobbiesMatching) {
		
	}

	@Override
	public void onLobbyKicked(SteamID steamIDLobby, SteamID steamIDAdmin, boolean kickedDueToDisconnect) {
		
	}

	@Override
	public void onLobbyCreated(SteamResult result, SteamID steamIDLobby) {
		lobbyID = steamIDLobby;
	}
	
	@Override
	public void onFavoritesListAccountsUpdated(SteamResult result) {
		
	}
	
}