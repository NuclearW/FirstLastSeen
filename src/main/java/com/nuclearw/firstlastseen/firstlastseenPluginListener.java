package com.nuclearw.firstlastseen;

import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

public class firstlastseenPluginListener extends ServerListener {
	public static firstlastseen plugin;
	
	public firstlastseenPluginListener(firstlastseen instance) {
		plugin = instance;
	}
	
    public void onPluginEnable(PluginEnableEvent event) {
    	firstlastseenPermissionsHandler.onEnable(event.getPlugin());
    }
	
}
