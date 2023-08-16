package com.rivelbop.steam;

import java.nio.ByteBuffer;

import com.codedisaster.steamworks.SteamID;

public class PacketData {

	public SteamID user;
	public String message;
	public ByteBuffer buffer;

	public PacketData(SteamID user, String message) {
		this.user = user;
		this.message = message;
	}

	public PacketData(SteamID user, ByteBuffer buffer) {
		this.user = user;
		this.buffer = buffer;
	}

}