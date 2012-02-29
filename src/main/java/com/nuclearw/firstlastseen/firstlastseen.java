package com.nuclearw.firstlastseen;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.Locale;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class firstlastseen extends JavaPlugin {
	
	static String mainDirectory = "plugins" + File.separator + "FirstLastSeen";
	static String dataDirectory = "plugins" + File.separator + "FirstLastSeen" + File.separator + "data";
	static File config = new File(mainDirectory + File.separator + "config");
	static File language = new File(mainDirectory + File.separator + "lang");
	static File version = new File(mainDirectory + File.separator + "VERSION");
	static Properties prop = new Properties();
	
	public String norecordfor;
	public String norecord;
	public String firstseen;
	public String lastseen;
	public String firstseennotify;
	
	public Locale locale = Locale.getDefault();
	public DateFormat dateformat = DateFormat.getDateInstance();
	
	private final firstlastseenPlayerListener playerListener = new firstlastseenPlayerListener(this);
	
	Logger log = Logger.getLogger("Minecraft");
	
	public void onEnable() {
		
		//Make the file
		new File(mainDirectory).mkdir();
		new File(dataDirectory).mkdir();
		if(!config.exists()) {
			try {
				config.createNewFile();
				FileOutputStream out = new FileOutputStream(config);
				prop.put("ImportTowny", "false");
				prop.store(out, "Enable ImportTowny to attempt import from Towny on next run, it will be disabled again afterwards automatically.");
				out.flush();
				out.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		if(!language.exists()) {
			Properties langpropout = new Properties();
			try {
				language.createNewFile();
				FileOutputStream out = new FileOutputStream(language);
				langpropout.put("norecordfor", "&7No record for &a<player> &7found.");
				langpropout.put("norecord", "&cNo record.");
				langpropout.put("firstseen", "&7First seen on &a<date>");
				langpropout.put("lastseen", "&7Last seen on &a<date>");
				langpropout.put("firstseennotify", "&eWelcome &a<player> &eto the server!");
				langpropout.store(out, "Loaclization.");
				out.flush();
				out.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		FileInputStream in;
		try {
			in = new FileInputStream(config);
			prop.load(in);
			in.close();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		FileInputStream langin;
		Properties langpropin = new Properties();
		try {
			langin = new FileInputStream(language);
			langpropin.load(langin);
			langin.close();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		//Attempt to load from towny
		if (prop.getProperty("ImportTowny").equalsIgnoreCase("true")) {
			
			log.info("Attempting to import data from Towny");
			
			File towny = new File("plugins" + File.separator + "Towny" + File.separator + "data" + File.separator + "residents");
			if(towny.exists()) {
				String[] residents = towny.list();
				for (int i = 0; i < residents.length; i++) {
					
					File residentin = new File("plugins" + File.separator + "Towny" + File.separator + "data" + File.separator + "residents" + File.separator + residents[i]);
					
					if(!residentin.isFile()) {
						log.info(residentin.toString() + " not a file, skipping.");
						continue;
					}

					String residentname = residents[i].substring(0, Integer.parseInt(Long.toString(residents[i].length()-4))).toLowerCase();
					
					FileInputStream townydatain;
					
					Properties townypropin = new Properties();
					
					try {
						townydatain = new FileInputStream(residentin);
						townypropin.load(townydatain);
						townydatain.close();
					} catch (FileNotFoundException ex) {
						ex.printStackTrace();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
					
					if(!townypropin.containsKey("lastOnline") || !townypropin.containsKey("registered")) {
						log.info(residentname + " not valid, skipping.");
						continue;
					}
					
					File residentout = new File(dataDirectory + File.separator + residentname);
					
					if(!residentout.exists()) {
						try {
							residentout.createNewFile();
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
						
					Properties newpropout = new Properties();
					
					try {							
						FileOutputStream townydataout = new FileOutputStream(residentout);
						newpropout.put("FirstSeen", townypropin.get("registered"));
						newpropout.put("LastSeen", townypropin.get("lastOnline"));
						newpropout.store(townydataout, "");
						townydataout.flush();
						townydataout.close();
						
						log.info(residentname + " imported.");
						
					} catch (IOException ex) {
						ex.printStackTrace();
					}
					
				}
				
			} else {
				log.info("Towny data not found, aborting.");
			}
			
			try {
				FileOutputStream out = new FileOutputStream(config);
				prop.put("ImportTowny", "false");
				prop.store(out, "Enable ImportTowny to attempt import from Towny on next run, it will be disabled again afterwards automatically.");
				out.flush();
				out.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
		}
		
		/*
		 * Versions prior to 0.4 did not have a version file, thus any install lacking it is either new, or an update.
		 * 
		 * If it is new, and Towny was just imported, this will cause some lag, but not that big of a deal as it will
		 * only run once.  Otherwise, no data exists, and it will not check anything and exit quickly.
		 */
		if(!version.exists()) {
			try {
				version.createNewFile();
				BufferedWriter vout = new BufferedWriter(new FileWriter(version));
				vout.write(this.getDescription().getVersion());
				vout.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (SecurityException ex) {
				ex.printStackTrace();
			}
			
			String[] dataEntries = new File(dataDirectory).list();
			
			for (int i = 0; i < dataEntries.length; i++) {
				if(containsUpperCase(dataEntries[i])) {
					File invalid = new File(dataDirectory + File.separator + dataEntries[i]);
					if(!invalid.renameTo(new File(dataDirectory + File.separator + dataEntries[i].toLowerCase()))) {
						log.info("Could not rename " + dataEntries[i] + " to " + dataEntries[i].toLowerCase());
					}
				}
			}
			
		} else {
			byte[] buffer = new byte[(int) version.length()];
			BufferedInputStream f = null;
			try {
				f = new BufferedInputStream(new FileInputStream(version));
				f.read(buffer);
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				if (f != null) try { f.close(); } catch (IOException ignored) { }
			}
			
			String vnum = new String(buffer);
			//In the future past versions can be checked for and dealt with here.
			if(vnum.equals("0.4")) updateFrom(1);
			if(vnum.equals("0.5")) updateFrom(2);
			if(vnum.equals("0.6")) updateFrom(-1);
			if(vnum.equals("0.7")) updateFrom(3);
		}
		
		//Sanity time on language
		if(!langpropin.containsKey("norecordfor") || !langpropin.containsKey("norecord") || !langpropin.containsKey("firstseen") || !langpropin.containsKey("lastseen") || !langpropin.containsKey("firstseennotify")) {
			log.info("FirstLastSeen lang file not complete!  Reverting file to english.");
			Properties langpropout = new Properties();
			try {
				language.createNewFile();
				FileOutputStream out = new FileOutputStream(language);
				langpropout.put("norecordfor", "No record for <player> found.");
				langpropout.put("norecord", "No record.");
				langpropout.put("firstseen", "First seen on <date>");
				langpropout.put("lastseen", "Last seen on <date>");
				langpropout.put("firstseennotify", "Welcome <player> to the server!");
				langpropout.store(out, "Loaclization.");
				out.flush();
				out.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		this.norecordfor = this.colorizeText(langpropin.getProperty("norecordfor"));
		this.norecord = this.colorizeText(langpropin.getProperty("norecord"));
		this.firstseen = this.colorizeText(langpropin.getProperty("firstseen"));
		this.lastseen = this.colorizeText(langpropin.getProperty("lastseen"));
		this.firstseennotify = this.colorizeText(langpropin.getProperty("firstseennotify"));
		
		PluginManager pluginManager = getServer().getPluginManager();
		
		pluginManager.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Monitor, this);
		pluginManager.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Monitor, this);
		pluginManager.registerEvent(Event.Type.PLAYER_KICK, playerListener, Event.Priority.Monitor, this);
		
		log.info("[FirstLastSeen] version "+this.getDescription().getVersion()+" loaded.");
		
		//Now we see if anyone is online who we need to add for the very first time.
		long curTime = System.currentTimeMillis();
		for(Player p : this.getServer().getOnlinePlayers()) {
			if(isFirstJoin(p)) {
				putFirstSeen(p, curTime);
			}
		}
	}
	
	public void updateFrom(int age) {
		//Just change version file
		if(age == -1) {
			updateVersion();
			return;
		}
		//Version 0.4 and later stuff
		if(age <= 1) {
			log.info("Version 0.4 of FirstLastSeen contained a bug affecting only Unix based servers, there may have been some data duplication.");
		}
		//Version 0.5 and later stuff
		if(age <= 2) {
			String[] updateStr = new String[5];
			
			prop.clear();
			FileInputStream updatein;
			try {
				updatein = new FileInputStream(language);
				prop.load(updatein);
				updatein.close();
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
			updateStr[0] = prop.getProperty("norecordfor");
			updateStr[1] = prop.getProperty("norecord");
			updateStr[2] = prop.getProperty("firstseen");
			updateStr[3] = prop.getProperty("lastseen");
			updateStr[4] = "Welcome <player> to the server!";
			
			prop.clear();
			try {
				language.createNewFile();
				FileOutputStream out = new FileOutputStream(language);
				prop.put("norecordfor", updateStr[0]);
				prop.put("norecord", updateStr[1]);
				prop.put("firstseen", updateStr[2]);
				prop.put("lastseen", updateStr[3]);
				prop.put("firstseennotify", updateStr[4]);
				prop.store(out, "Loaclization.");
				out.flush();
				out.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			log.info("Version 0.6 of FirstLastSeen adds a new line in the language file for when a player first joins the server!");
			log.info("Simply set it to an empty string to disable this feature.");
		}
		if(age <= 3) {
			log.info("FirstLastSeen can now support color codes in the lang file!");
		}
		updateVersion();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if(cmd.getName().equalsIgnoreCase("firstseen")) {
			if(args.length != 1) return false;
			try {
				if(getServer().getPlayer(args[0]).isOnline()) {
					args[0] = getServer().getPlayer(args[0]).getName();
				}
			} catch (NullPointerException ex) {
				//Nope
			}
			args[0] = args[0].toLowerCase();
			
			if(isPlayer(sender)) {
				if(!hasPermission(((Player) sender), "firstlastseen.firstseen")) return true;
			}
			
			File targetfile = new File(dataDirectory + File.separator + args[0]);
			if(!targetfile.exists()) {
				//sender.sendMessage("No record for " + args[0] + " found.");
				sender.sendMessage(norecordfor.replaceFirst("<player>", args[0]));
				return true;
			}
			sender.sendMessage(args[0]);
			//sender.sendMessage("First seen on: " + getFirstSeen(args[0]));
			sender.sendMessage(firstseen.replaceFirst("<date>", getFirstSeen(args[0])));
			
			return true;
		}
		if(cmd.getName().equalsIgnoreCase("lastseen")) {
			if(args.length != 1) return false;
			try {
				if(getServer().getPlayer(args[0]).isOnline()) {
					args[0] = getServer().getPlayer(args[0]).getName();
				}
			} catch (NullPointerException ex) {
				//Nope
			}
			args[0] = args[0].toLowerCase();
			
			if(isPlayer(sender)) {
				if(!hasPermission(((Player) sender), "firstlastseen.lastseen")) return true;
			}
			
			File targetfile = new File(dataDirectory + File.separator + args[0]);
			if(!targetfile.exists()) {
				//sender.sendMessage("No record for " + args[0] + " found.");
				sender.sendMessage(norecordfor.replaceFirst("<player>", args[0]));
				return true;
			}
			sender.sendMessage(args[0]);
			//sender.sendMessage("Last seen on: " + getLastSeen(args[0]));
			sender.sendMessage(lastseen.replaceFirst("<date>", getLastSeen(args[0])));
			return true;
		}
		if(cmd.getName().equalsIgnoreCase("seen")) {
			if(args.length != 1) return false;
			try {
				if(getServer().getPlayer(args[0]).isOnline()) {
					args[0] = getServer().getPlayer(args[0]).getName();
				}
			} catch (NullPointerException ex) {
				//Nope
			}
			args[0] = args[0].toLowerCase();
			
			if(isPlayer(sender)) {
				if(!hasPermission(((Player) sender), "firstlastseen.seen")) return true;
			}
			
			File targetfile = new File(dataDirectory + File.separator + args[0]);
			if(!targetfile.exists()) {
				//sender.sendMessage("No record for " + args[0] + " found.");
				sender.sendMessage(norecordfor.replaceFirst("<player>", args[0]));
				return true;
			}
			sender.sendMessage(args[0]);
			//sender.sendMessage("First seen on: " + getFirstSeen(args[0]));
			//sender.sendMessage("Last seen on: " + getLastSeen(args[0]));
			sender.sendMessage(firstseen.replaceFirst("<date>", getFirstSeen(args[0])));
			sender.sendMessage(lastseen.replaceFirst("<date>", getLastSeen(args[0])));
			return true;
		}
		return false;
	}
	
	public void updateVersion() {
		try {
			version.createNewFile();
			BufferedWriter vout = new BufferedWriter(new FileWriter(version));
			vout.write(this.getDescription().getVersion());
			vout.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (SecurityException ex) {
			ex.printStackTrace();
		}
	}
	
	public void onDisable() {
		log.info("[FirstLastSeen] version "+this.getDescription().getVersion()+" unloaded.");
	}
	
	public boolean isFirstJoin(Player player) {
		if(new File(dataDirectory + File.separator + player.getName().toLowerCase()).exists()) return false;
		return true;
	}
	
	public void putFirstSeen(Player player, long time) {
		storeTime(player, time, "FirstSeen");
	}
	
	public void putLastSeen(Player player, long time) {
		storeTime(player, time, "LastSeen");
		
	}
	
	public void storeTime(Player player, long time, String property) {
		File savefile = new File(dataDirectory + File.separator +  player.getName().toLowerCase());
		
		if(!savefile.exists()) {
			try {
				savefile.createNewFile();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		FileInputStream timedatain;
		Properties timepropin = new Properties();
		
		try {
			timedatain = new FileInputStream(savefile);
			timepropin.load(timedatain);
			timedatain.close();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		Properties timepropout = new Properties();
		
		try {
			FileOutputStream outtime = new FileOutputStream(savefile);
			if(timepropin.containsKey("FirstSeen")) timepropout.put("FirstSeen", timepropin.get("FirstSeen"));
			if(timepropin.containsKey("LastSeen")) timepropout.put("LastSeen", timepropin.get("LastSeen"));
			timepropout.put(property, Long.toString(time));
			timepropout.store(outtime, "");
			outtime.flush();
			outtime.close();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public String getFirstSeen(String name) {
		FileInputStream getseenin;
		File getseen = new File(dataDirectory + File.separator + name);
		Properties getseenprop = new Properties();
		try {
			getseenin = new FileInputStream(getseen);
			getseenprop.load(getseenin);
			getseenin.close();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		if(!getseenprop.containsKey("FirstSeen")) return norecord;
		Date date = new Date(Long.parseLong(getseenprop.getProperty("FirstSeen")));
		return DateFormat.getDateTimeInstance().format(date);
	}
	
	public String getLastSeen(String name) {
		FileInputStream getseenin;
		File getseen = new File(dataDirectory + File.separator + name);
		Properties getseenprop = new Properties();
		try {
			getseenin = new FileInputStream(getseen);
			getseenprop.load(getseenin);
			getseenin.close();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		if(!getseenprop.containsKey("LastSeen")) return norecord;
		Date date = new Date(Long.parseLong(getseenprop.getProperty("LastSeen")));
		return DateFormat.getDateTimeInstance().format(date);
	}
	
	public static long getFirstSeenLong(String name) {
		name = name.toLowerCase();
		FileInputStream getseenin;
		File getseen = new File(dataDirectory + File.separator + name);
		Properties getseenprop = new Properties();
		try {
			getseenin = new FileInputStream(getseen);
			getseenprop.load(getseenin);
			getseenin.close();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			return -1;
		} catch (IOException ex) {
			ex.printStackTrace();
			return -1;
		}
		if(!getseenprop.containsKey("FirstSeen")) return -1;
		return Long.parseLong(getseenprop.getProperty("FirstSeen"));
	}

	public static long getLastSeenLong(String name) {
		FileInputStream getseenin;
		File getseen = new File(dataDirectory + File.separator + name);
		Properties getseenprop = new Properties();
		try {
			getseenin = new FileInputStream(getseen);
			getseenprop.load(getseenin);
			getseenin.close();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			return -1;
		} catch (IOException ex) {
			ex.printStackTrace();
			return -1;
		}
		if(!getseenprop.containsKey("LastSeen")) return -1;
		return Long.parseLong(getseenprop.getProperty("LastSeen"));
	}
	
	public boolean isPlayer(CommandSender sender) {
        return sender != null && sender instanceof Player;
    }
	
	private boolean containsUpperCase(String string) {
		for (char c : string.toCharArray()) {
		    if (Character.isUpperCase(c)) {
		        return true;
		    }
		}
		return false;
	}
	
	public String colorizeText(String string) {
    	string = string.replaceAll("&0", ChatColor.BLACK+"");
		string = string.replaceAll("&1", ChatColor.DARK_BLUE+"");
		string = string.replaceAll("&2", ChatColor.DARK_GREEN+"");
		string = string.replaceAll("&3", ChatColor.DARK_AQUA+"");
		string = string.replaceAll("&4", ChatColor.DARK_RED+"");
		string = string.replaceAll("&5", ChatColor.DARK_PURPLE+"");
		string = string.replaceAll("&6", ChatColor.GOLD+"");
		string = string.replaceAll("&7", ChatColor.GRAY+"");
		string = string.replaceAll("&8", ChatColor.DARK_GRAY+"");
		string = string.replaceAll("&9", ChatColor.BLUE+"");
		string = string.replaceAll("&a", ChatColor.GREEN+"");
		string = string.replaceAll("&b", ChatColor.AQUA+"");
		string = string.replaceAll("&c", ChatColor.RED+"");
		string = string.replaceAll("&d", ChatColor.LIGHT_PURPLE+"");
		string = string.replaceAll("&e", ChatColor.YELLOW+"");
		string = string.replaceAll("&f", ChatColor.WHITE+"");
		return string;
    }
}
