package com.nuclearw.firstlastseen;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

import sl.nuclearw.firstlastseen.firstlastseen;

public class firstlastseenPlayerListener extends PlayerListener {
	public static firstlastseen plugin;
	
	public firstlastseenPlayerListener(firstlastseen instance) {
		plugin = instance;
	}
	
	public void onPlayerJoin(PlayerJoinEvent event) {
		long time = System.currentTimeMillis();
		if(plugin.isFirstJoin(event.getPlayer())) {
			plugin.putFirstSeen(event.getPlayer(), time);
			plugin.log.info("First join for " + event.getPlayer().getName());
			if(plugin.firstseennotify != null) plugin.getServer().broadcastMessage(plugin.firstseennotify.replaceFirst("<player>", event.getPlayer().getDisplayName()));
		}
	}
	
	public void onPlayerQuit(PlayerQuitEvent event) {
		long time = System.currentTimeMillis();
		plugin.putLastSeen(event.getPlayer(), time);
	}
	
	public void onPlayerKick(PlayerKickEvent event) {
		long time = System.currentTimeMillis();
		plugin.putLastSeen(event.getPlayer(), time);
	}
	
}
