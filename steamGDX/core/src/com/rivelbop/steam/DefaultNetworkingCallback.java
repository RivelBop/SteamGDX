package com.rivelbop.steam;

import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamNetworking.P2PSessionError;
import com.codedisaster.steamworks.SteamNetworkingCallback;

public class DefaultNetworkingCallback implements SteamNetworkingCallback{

	@Override
	public void onP2PSessionConnectFail(SteamID steamIDRemote, P2PSessionError sessionError) {
		System.err.println("P2P Session Connection failure with SteamID: " + steamIDRemote.getAccountID());
	}

	@Override
	public void onP2PSessionRequest(SteamID steamIDRemote) {
		Steam.getNetworking().acceptP2PSessionWithUser(steamIDRemote);
		System.out.println("P2P Session Request from SteamID: " + steamIDRemote.getAccountID() + " has been accepted!");
	}

}