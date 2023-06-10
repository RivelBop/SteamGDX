package com.rivelbop.steam;

import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamNetworking.P2PSessionError;
import com.codedisaster.steamworks.SteamNetworkingCallback;

public class DefaultNetworkingCallback implements SteamNetworkingCallback{

	@Override
	public void onP2PSessionConnectFail(SteamID steamIDRemote, P2PSessionError sessionError) {
		
	}

	@Override
	public void onP2PSessionRequest(SteamID steamIDRemote) {
		
	}

}