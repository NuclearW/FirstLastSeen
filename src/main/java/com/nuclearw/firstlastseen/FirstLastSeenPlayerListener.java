package com.nuclearw.firstlastseen;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.nuclearw.firstlastseen.FirstLastSeen;

public class FirstLastSeenPlayerListener implements Listener {
	public static FirstLastSeen plugin;

	public FirstLastSeenPlayerListener(FirstLastSeen instance) {
		plugin = instance;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		long time = System.currentTimeMillis();
		if(plugin.isFirstJoin(event.getPlayer())) {
			plugin.putFirstSeen(event.getPlayer(), time);
			plugin.log.info("First join for " + event.getPlayer().getName());
			if(plugin.firstseennotify != null) plugin.getServer().broadcastMessage(plugin.firstseennotify.replaceFirst("<player>", event.getPlayer().getDisplayName()));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		long time = System.currentTimeMillis();
		plugin.putLastSeen(event.getPlayer(), time);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerKick(PlayerKickEvent event) {
		long time = System.currentTimeMillis();
		plugin.putLastSeen(event.getPlayer(), time);
	}
}
