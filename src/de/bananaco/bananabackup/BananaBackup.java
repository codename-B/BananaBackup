package de.bananaco.bananabackup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

/**
 * The main JavaPlugin class for this simple plugin
 * @author codename_B
 */
public class BananaBackup extends JavaPlugin {
	/**
	 * The interval for this in hours - supports decimal points
	 */
	double interval;
	/**
	 * Should we backup all worlds?
	 */
	boolean allWorlds;
	/**
	 * Should we broadcast a message to the players in a world when it's being backed up?
	 * Static simply to avoid passing a reference to the original class when we don't need to.
	 */
	boolean broadcast = true;
	/**
	 * A list of worlds to backup if allWorlds == false
	 */
	List<String> backupWorlds;
	/**
	 * The number of ticks per hour, this will never change
	 */
	final double tph = 72000;

	/**
	 * Just your average onDisable();
	 */
	public void onDisable() {
		// Cancel our scheduled task
		getServer().getScheduler().cancelTasks(this);
		// Print disabled message
		System.out.println("[BananaBackup] Disabled.");
	}

	/**
	 * Nothing special here except a scheduler task.
	 */
	public void onEnable() {
		// Setup the configuration
		loadConfiguration();
		// Calculate the interval in ticks
		double ticks = tph * interval;
		// Schedule our task
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, doChecks(),
				(long) ticks, (long) ticks);
		// Print enabled message
		System.out.println("[BananaBackup] Enabled. Backup interval "
				+ interval + " hours.");
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			if(!sender.isOp()) {
				sender.sendMessage("Nope.");
				return true;
			}
		}
		getServer().getScheduler().scheduleAsyncDelayedTask(this, doChecks(), 10);
		sender.sendMessage("Backup initiated.");
		return true;
	}
	
	/**
	 * Loads our configuration
	 */
	public void loadConfiguration() {
		// The default config.yml
		Configuration c = getConfiguration();
		
		interval = c.getDouble("backup-interval-hours", 12.0);
		allWorlds = c.getBoolean("backup-all-worlds", true);
		broadcast = c.getBoolean("broadcast-message", true);
		backupWorlds = c
				.getStringList("backup-worlds", new ArrayList<String>());
		// This is just to make sure there is something in the config as an example
		if (backupWorlds.size() == 0)
			backupWorlds.add(getServer().getWorlds().get(0).getName());
		// Make sure our values are set
		c.setProperty("backup-worlds", backupWorlds);
		c.setProperty("backup-interval-hours", interval);
		c.setProperty("backup-all-worlds", allWorlds);
		c.setProperty("broadcast-message", broadcast);
		// Save to write the changes to disk
		c.save();
	}
	/**
	 * A simple method to make getting our runnable neater in onEnable();
	 * @return the runnable
	 */
	public Runnable doChecks() {
		return new Runnable() {
			public void run() {
				BackupThread bt;
				// Should we let people know it's starting?
				if(broadcast)
					getServer().broadcastMessage(ChatColor.BLUE
								+ "[BananaBackup] Backup starting. Expect a little lag.");
				// Loop through the worlds
				for (World world : getServer().getWorlds())
					try {
						// Save before the backup
						world.save();
						world.setAutoSave(false);
						// Then backup
						bt = backupWorld(world);
						if(bt != null) {
						bt.start();
						while(bt.isAlive()) {
							
						}
						world.setAutoSave(true);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				// Should we let people know it's done?
				if(broadcast)
				getServer().broadcastMessage(ChatColor.BLUE
						+ "[BananaBackup] Backup complete.");
			}
		};
	}
	/**
	 * Initiates the backup thread, each backup runs in its own thread
	 * @param world
	 * @throws Exception
	 */
	public BackupThread backupWorld(World world) throws Exception {
		// If allWorlds == true
		if (allWorlds)
			return new BackupThread(world);
		// Or if the world is in the backup list
		else if (!allWorlds && backupWorlds.contains(world.getName()))
			return new BackupThread(world);
		// Otherwise print an error message
		else
			System.out.println("[BananaBackup] Skipping backup for "
					+ world.getName());
		return null;
	}
	/**
	 * Used to format the date for the filename
	 * @return String date
	 */
	public static String format() {
		SimpleDateFormat formatter;
		Date date = new Date();
		formatter = new SimpleDateFormat("HH.mm@dd.MM.yyyy");
		return formatter.format(date);
	}
}
