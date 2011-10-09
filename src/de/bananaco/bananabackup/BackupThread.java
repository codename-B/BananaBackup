package de.bananaco.bananabackup;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The custom thread to make this run beautifully
 * @author codename_B;
 */
public class BackupThread extends Thread {
	/**
	 * The world to backup
	 */
	private final File world;
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
	 * @param file
	 * @throws Exception
	 */
	public BackupThread(File world) throws Exception {
		System.out.println("Backing up "+world.getCanonicalPath());
		this.world = world;
		this.file = new File(BananaBackup.backupFile + world.getName() + "/"
				+ BananaBackup.format() + ".zip");
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		this.os = new ZipOutputStream(new DataOutputStream(
				new FileOutputStream(file)));
	}

	/**
	 * Just your average runnable run();
	 */
	public void run() {
		File file = new File(world.getName());
		File[] subfiles = file.listFiles();
		loop(subfiles);
		try {
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		interrupt();
	}
	
	public void loop(File[] subfiles) {
		for(File file : subfiles) {
			if(file.isDirectory() || file.getName().endsWith("/") || file.getName().endsWith("\\")) {
				loop(file.listFiles());
			}
			else try {
				write(file);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	/**
	 * A nice neat method for writing the .mcregion to our zip
	 * @param input
	 * @throws Exception
	 */
	public void write(File input) throws Exception {
		// The name of the .mcregion file
		String name = input.getPath();
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
