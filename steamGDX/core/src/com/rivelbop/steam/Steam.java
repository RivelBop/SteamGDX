package com.rivelbop.steam;

import com.badlogic.gdx.Gdx;
import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamException;

public class Steam {
	
	public static int appID;
	private static boolean isInit;
	
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
				isInit = true;
			}
		} catch (SteamException e) {
			e.printStackTrace();
		}
	}
	
	// Check if Steam is running
	public static boolean update() {
		if (isInit && SteamAPI.isSteamRunning()) {
			SteamAPI.runCallbacks();
			return true;
		}
		return false;
	}
	
}