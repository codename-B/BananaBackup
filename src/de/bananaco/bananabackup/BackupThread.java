package de.bananaco.bananabackup;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.bukkit.World;
import org.bukkit.World.Environment;

/**
 * The custom thread to make this run beautifully
 * @author codename_B;
 */
public class BackupThread extends Thread {
	/**
	 * The world to backup
	 */
	private final World world;
	/**
	 * The file to backup to
	 */
	private final File file;
	/**
	 * The ZipOutputStream object
	 */
	private ZipOutputStream os;
	/**
	 * Instances the class, creates a file for the zip, and instances the ZipOutputStream.
	 * @param world
	 * @throws Exception
	 */
	public BackupThread(World world) throws Exception {
		this.world = world;
		this.file = new File("backups/worlds/" + world.getName() + "/"
				+ BananaBackup.format() + ".zip");
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		this.os = new ZipOutputStream(new DataOutputStream(
				new FileOutputStream(file)));
	}
	/**
	 * Lists all the .mcregion file for said world
	 * @return List<File> .mcregion
	 */
	public List<File> getRegions() {
		List<File> regions = new ArrayList<File>();
		File worldFile = new File(world.getName()+"/"
				+ (world.getEnvironment() == Environment.NETHER ? "DIM-1/region/"
						: "region/"));
		File[] subfiles = worldFile.listFiles();
		if(subfiles != null)
		for (File file : subfiles)
			regions.add(file);
		return regions;
	}
	/**
	 * Just your average runnable run();
	 */
	public void run() {
		List<File> files = getRegions();
		for (int i = 0; i < files.size(); i++) {
			File input = files.get(i);
			try {
				write(input);
				if ((i % 5) == 1)
					System.out.println("[BananaBackup] " + world.getName() + " " + ((i * 100) / files.size()) + "% backed up.");
				if(BananaBackup.intervalBetween>0)
					sleep(BananaBackup.intervalBetween);
			}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		try {
			// Closes the ZipOutputStream, finalising the backup
			os.close();
			System.out.println("[BananaBackup] " + world.getName()
					+ " 100% backed up.");
		} catch (IOException e) {
			// Let people know if something went wrong
			System.err.println("[BananaBackup] Error finalising backup for "
					+ world.getName());
		}
		interrupt();
	}
	/**
	 * A nice neat method for writing the .mcregion to our zip
	 * @param input
	 * @throws Exception
	 */
	public void write(File input) throws Exception {
		// The name of the .mcregion file
		String name = input.getName();
		// Creates an entry in the zip for this name
		ZipEntry e = new ZipEntry(name);
		os.putNextEntry(e);
		// Reads from the original file, per byte
		BufferedInputStream is = new BufferedInputStream(new DataInputStream(
				new FileInputStream(input)));
		int isb = 0;
		while ((isb = is.read()) >= 0)
			// Writes to our zip, per byte
			os.write(isb);
		// Closes the inputstream
		is.close();
		// Closes the zipentry
		os.closeEntry();
	}

}
