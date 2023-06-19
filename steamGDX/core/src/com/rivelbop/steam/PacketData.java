package com.rivelbop.steam;

import com.codedisaster.steamworks.SteamID;

public class PacketData {
	
	public SteamID user;
	public String packet;
	
	public PacketData(SteamID player, String message) {
		user = player;
		packet = message;
	}
	
}