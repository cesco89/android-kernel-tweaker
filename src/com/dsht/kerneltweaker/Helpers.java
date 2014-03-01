package com.dsht.kerneltweaker;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import com.dsht.kernetweaker.cmdprocessor.CMDProcessor;
import com.dsht.kernetweaker.cmdprocessor.CMDProcessor.CommandResult2;
import com.dsht.settings.SettingsFragment;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class Helpers {

	private static final String FREQ_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
	private static final String GOVERNOR_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
	static String[] error = {"error while reading frequencies. Please reload this page"};

	public static String[]  getFrequencies() {
		setPermissions(FREQ_FILE);
		File freqfile = new File(FREQ_FILE);
		FileInputStream fin1 = null;
		String s1 = null;
		try {
			fin1 = new FileInputStream(freqfile);
			byte fileContent[] = new byte[(int)freqfile.length()];
			fin1.read(fileContent);
			s1 = new String(fileContent);
		}
		catch (FileNotFoundException e1) {
			//System.out.println("File not found" + e1);
		}
		catch (IOException ioe1) {
			//System.out.println("Exception while reading file " + ioe1);
		}
		finally {
			try {
				if (fin1 != null) {
					fin1.close();
				}
			}
			catch (IOException ioe1) {
				//System.out.println("Error while closing stream: " + ioe1);
			}
		}
		if(s1 != null) {
			String[] frequencies = s1.trim().split(" ");
			return frequencies;
		} else {
			return error;
		}
	}

	public static void setPermissions(String file) {
		if(new File(file).exists()) {
			CommandCapture command = new CommandCapture(0, "chmod 655 "+file);
			try {
				RootTools.getShell(true).add(command);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RootDeniedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static String binExist(String b) {
		CommandResult2 cr = null;
		cr = new CMDProcessor().sh.runWaitFor("busybox which " + b);
		if (cr.success()) {
			return cr.stdout;
		} else {
			return "NOT_FOUND";
		}
	}

	public static boolean writeOneLine(String fname, String value) {
		if (!new File(fname).exists()) {
			return false;
		}
		try {
			FileWriter fw = new FileWriter(fname);
			try {
				fw.write(value);
			} finally {
				fw.close();
			}
		} catch (IOException e) {
			String Error = "Error writing to " + fname + ". Exception: ";
			Log.e("TAG", Error, e);
			return false;
		}
		return true;
	}

	public static void runRootCommand(String command) {
		CommandCapture comm = new CommandCapture(0, command);
		try {
			RootTools.getShell(true).add(comm);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RootDeniedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static int getNumOfCpus() {
		int numOfCpu = 1;
		setPermissions("/sys/devices/system/cpu/present");
		String numOfCpus = Helpers.readFileViaShell("/sys/devices/system/cpu/present", false);
		String[] cpuCount = numOfCpus.trim().split("-");
		Log.d("NUM", numOfCpus+ "----"+cpuCount.length);
		if (cpuCount.length > 1) {
			try {
				int cpuStart = Integer.parseInt(cpuCount[0]);
				int cpuEnd = Integer.parseInt(cpuCount[1]);

				numOfCpu = cpuEnd - cpuStart + 1;
				Log.d("NUM", numOfCpu+"");

				if (numOfCpu < 0) {
					numOfCpu = 1;
					Log.d("NUM", "ONE");
				}

			} catch (NumberFormatException ex) {
				numOfCpu = 1;
				Log.d("NUM", "ERROR");
			}
		}
		return numOfCpu;
	}

	public static boolean fileExists(String fname) {
		return new File(fname).exists();
	}

	public static String shExec(StringBuilder s, Context c, Boolean su) {
		get_assetsScript("run", c, s.toString(), "");
		if (isSystemApp(c)) {
			new CMDProcessor().sh.runWaitFor("busybox chmod 750 " + c.getFilesDir() + "/run");
		} else {
			new CMDProcessor().su.runWaitFor("busybox chmod 750 " + c.getFilesDir() + "/run");
		}
		CommandResult2 cr = null;
		if (su && !isSystemApp(c))
			cr = new CMDProcessor().su.runWaitFor(c.getFilesDir() + "/run");
		else
			cr = new CMDProcessor().sh.runWaitFor(c.getFilesDir() + "/run");
		if (cr.success()) {
			return cr.stdout;
		} else {
			Log.d("TAG", "execute: " + cr.stderr);
			return null;
		}
	}

	public static void get_assetsScript(String fn, Context c, String prefix, String postfix) {
		byte[] buffer;
		final AssetManager assetManager = c.getAssets();
		try {
			InputStream f = assetManager.open(fn);
			buffer = new byte[f.available()];
			f.read(buffer);
			f.close();
			final String s = new String(buffer);
			final StringBuilder sb = new StringBuilder(s);
			if (!postfix.equals("")) {
				sb.append("\n\n").append(postfix);
			}
			if (!prefix.equals("")) {
				sb.insert(0, prefix + "\n");
			}
			sb.insert(0, "#!" + Helpers.binExist("sh") + "\n\n");
			try {
				FileOutputStream fos;
				fos = c.openFileOutput(fn, Context.MODE_PRIVATE);
				fos.write(sb.toString().getBytes());
				fos.close();

			} catch (IOException e) {
				Log.d("TAG", "error write " + fn + " file");
				e.printStackTrace();
			}

		} catch (IOException e) {
			Log.d("TAG", "error read " + fn + " file");
			e.printStackTrace();
		}
	}

	public static void restartPC(final Activity activity) {
		if (activity == null)
			return;
		final int enter_anim = android.R.anim.fade_in;
		final int exit_anim = android.R.anim.fade_out;
		activity.overridePendingTransition(enter_anim, exit_anim);
		activity.finish();
		activity.overridePendingTransition(enter_anim, exit_anim);
		activity.startActivity(activity.getIntent());
	}


	public static String[]  getFrequenciesNames() {
		ArrayList<String> names = new ArrayList<String>();
		setPermissions(FREQ_FILE);
		File freqfile = new File(FREQ_FILE);
		FileInputStream fin1 = null;
		String s1 = null;
		try {
			fin1 = new FileInputStream(freqfile);
			byte fileContent[] = new byte[(int)freqfile.length()];
			fin1.read(fileContent);
			s1 = new String(fileContent);
		}
		catch (FileNotFoundException e1) {
			//System.out.println("File not found" + e1);
		}
		catch (IOException ioe1) {
			//System.out.println("Exception while reading file " + ioe1);
		}
		finally {
			try {
				if (fin1 != null) {
					fin1.close();
				}
			}
			catch (IOException ioe1) {
				//System.out.println("Error while closing stream: " + ioe1);
			}
		}
		if(s1 != null) {
			String[] frequencies = s1.trim().split(" ");
			for(String s : frequencies) {
				int conv = (Integer.parseInt(s) / 1000);
				names.add(conv + " Mhz");
			}
			String[] toMhz = new String[names.size()];
			toMhz = names.toArray(toMhz);
			return toMhz;
		}else {
			return error;
		}
	}


	public static String[]  getFreqToMhz(String file) {
		ArrayList<String> names = new ArrayList<String>();
		setPermissions(file);
		File freqfile = new File(file);
		FileInputStream fin1 = null;
		String s1 = null;
		try {
			fin1 = new FileInputStream(freqfile);
			byte fileContent[] = new byte[(int)freqfile.length()];
			fin1.read(fileContent);
			s1 = new String(fileContent);
		}
		catch (FileNotFoundException e1) {
			//System.out.println("File not found" + e1);
		}
		catch (IOException ioe1) {
			//System.out.println("Exception while reading file " + ioe1);
		}
		finally {
			try {
				if (fin1 != null) {
					fin1.close();
				}
			}
			catch (IOException ioe1) {
				//System.out.println("Error while closing stream: " + ioe1);
			}
		}
		if(s1 != null) {
			String[] frequencies = s1.trim().split(" ");
			for(String s : frequencies) {
				int conv = (Integer.parseInt(s) / 1000000);
				names.add(conv + " Mhz");
			}
			String[] toMhz = new String[names.size()];
			toMhz = names.toArray(toMhz);
			return toMhz;
		}else {
			return error;
		}
	}

	public static String[] getGovernors() {
		setPermissions(GOVERNOR_FILE);
		File govfile = new File(GOVERNOR_FILE);
		FileInputStream fin = null;
		String s = null;
		try {
			fin = new FileInputStream(govfile);
			byte fileContent[] = new byte[(int)govfile.length()];
			fin.read(fileContent);
			s = new String(fileContent);
		}
		catch (FileNotFoundException e) {
		}
		catch (IOException ioe) {
			//System.out.println("Exception while reading file " + ioe);
		}
		finally {
			try {
				fin.close();
			}
			catch (IOException ioe) {
				//System.out.println("Error while closing stream: " + ioe);
			}
		}
		if(s != null) {
			String[] governors = s.trim().split(" ");
			return governors;
		}else {
			return error;
		}
	}

	public static String[] getUvTableNames() {
		ArrayList<String> Tokens = new ArrayList<String>();

		try {
			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = null;
			File f = new File("/sys/devices/system/cpu/cpufreq/vdd_table/vdd_levels");
			if(f.exists()) {
				fstream = new FileInputStream("/sys/devices/system/cpu/cpufreq/vdd_table/vdd_levels");
			} 
			else {
				File ff = new File("/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table");
				if(ff.exists()) {
					fstream = new FileInputStream("/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table");
				}
			}
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				strLine = strLine.trim();

				if ((strLine.length()!=0)) {
					String[] names = strLine.replaceAll(":", "").split("\\s+");
					Tokens.add(names[0]);
				}


			}
			// Close the input stream
			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		String[] names = new String[Tokens.size()-1];
		names = Tokens.toArray(names);
		return names;
	}

	public static String[] getUvValues() {
		ArrayList<String> value = new ArrayList<String>();

		try {
			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = null;
			File f = new File("/sys/devices/system/cpu/cpufreq/vdd_table/vdd_levels");
			if(f.exists()) {
				fstream = new FileInputStream("/sys/devices/system/cpu/cpufreq/vdd_table/vdd_levels");
			} 
			else {
				File ff = new File("/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table");
				if(ff.exists()) {
					fstream = new FileInputStream("/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table");
				}
			}
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				strLine = strLine.trim();

				if ((strLine.length()!=0)) {
					String[] val = strLine.split("\\s+");
					value.add(val[1]);
				}


			}
			// Close the input stream
			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		String[] values = new String[value.size()-1];
		values = value.toArray(values);
		return values;
	}

	public static boolean UvTableExists(String file) {
		File f = new File(file);
		if(f.exists()) {
			return true;
		}
		return false;
	}

	public static String[] getAvailableSchedulers() {
		File iofile = new File("/sys/block/mmcblk0/queue/scheduler"); 
		String s ="";
		FileInputStream fin2 = null;
		try {
			fin2 = new FileInputStream(iofile);
			byte fileContent[] = new byte[(int)iofile.length()];
			fin2.read(fileContent);
			s = new String(fileContent).trim().split("\n")[0];
		}
		catch (FileNotFoundException e) {
			//System.out.println("File not found" + e);
		}
		catch (IOException ioe) {
			//System.out.println("Exception while reading file " + ioe);
		}
		finally {
			try {
				if (fin2 != null) {
					fin2.close();
				}
			}
			catch (IOException ioe) {
				//System.out.println("Error while closing stream: " + ioe);
			}
		} 
		String[] IOSchedulers = s.replace("[", "").replace("]", "").split(" ");
		return IOSchedulers;
	}

	public static String getCurrentScheduler() {
		File iofile = new File("/sys/block/mmcblk0/queue/scheduler"); 
		String s ="";
		FileInputStream fin2 = null;
		try {
			fin2 = new FileInputStream(iofile);
			byte fileContent[] = new byte[(int)iofile.length()];
			fin2.read(fileContent);
			s = new String(fileContent).trim().split("\n")[0];
		}
		catch (FileNotFoundException e) {
			//System.out.println("File not found" + e);
		}
		catch (IOException ioe) {
			//System.out.println("Exception while reading file " + ioe);
		}
		finally {
			try {
				if (fin2 != null) {
					fin2.close();
				}
			}
			catch (IOException ioe) {
				//System.out.println("Error while closing stream: " + ioe);
			}
		} 
		int bropen = s.indexOf("[");
		int brclose = s.lastIndexOf("]");
		return s.substring(bropen + 1, brclose);
	}

	public static String getCurrentGovernor() {
		setPermissions("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor");
		File govfile = new File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor");
		FileInputStream fin = null;
		String s = null;
		try {
			fin = new FileInputStream(govfile);
			byte fileContent[] = new byte[(int)govfile.length()];
			fin.read(fileContent);
			s = new String(fileContent);
		}
		catch (FileNotFoundException e) {
		}
		catch (IOException ioe) {
			//System.out.println("Exception while reading file " + ioe);
		}
		finally {
			try {
				fin.close();
			}
			catch (IOException ioe) {
				//System.out.println("Error while closing stream: " + ioe);
			}
		}

		return s.trim();
	}

	public static String getFileContent( File file) {
		setPermissions(file.getAbsolutePath());
		FileInputStream fin = null;
		//Log.d("FILE", file.getAbsolutePath());
		String s = null;
		try {
			fin = new FileInputStream(file);
			byte fileContent[] = new byte[(int)file.length()];
			fin.read(fileContent);
			s = new String(fileContent);
		}
		catch (FileNotFoundException e) {
		}
		catch (IOException ioe) {
			//System.out.println("Exception while reading file " + ioe);
		}
		finally {
			try {
				if(fin != null) {
					fin.close();
				}
			}
			catch (IOException ioe) {
				//System.out.println("Error while closing stream: " + ioe);
			}
		}
		if(s == null) {
			s=" ";
		} else {
			s = s.split("\n")[0];
		}
		return s;
	}

	public static void waitForMillis(final int millis, Context context) {
		Thread thread=  new Thread(){
			@Override
			public void run(){
				try {
					synchronized(this){
						wait(millis);
					}
				}
				catch(InterruptedException ex){                    
				}

				// TODO              
			}
		};

		thread.start();
	}


	public static String readOneLine(String fname) {
		BufferedReader br = null;
		String line = null;
		try {
			br = new BufferedReader(new FileReader(fname), 1024);
			line = br.readLine();
		} catch (FileNotFoundException ignored) {
			Log.d("TAG", "File was not found! trying via shell...");
			return readFileViaShell(fname, true).trim().split("\\W+")[0];
		} catch (IOException e) {
			Log.d("TAG", "IOException while reading system file", e);
			return readFileViaShell(fname, true).trim().split("\\W+")[0];
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException ignored) {
					// failed to close reader
				}
			}
		}
		return line.trim().split("\\W+")[0];
	}

	public static String readFileViaShell(String filePath, boolean useSu) {
		String command = new String("cat " + filePath);
		return useSu ? CMDProcessor.runSuCommand(command).getStdout()
				: CMDProcessor.runShellCommand(command).getStdout();
	}

	public static String readCommandStrdOut(String command, boolean useSu) {
		return useSu ? CMDProcessor.runSuCommand(command).getStdout()
				: CMDProcessor.runShellCommand(command).getStdout();
	}

	public static File[] listFilesViaShell(String dirPath, boolean useSu) {
		String command = new String("ls" + dirPath);
		String content = useSu ? CMDProcessor.runSuCommand(command).getStdout()
				: CMDProcessor.runShellCommand(command).getStdout();
		String[] folders = content.trim().split("\n");
		File[] list = new File[folders.length];
		for(int i = 0; i<folders.length; i++) {
			list[i] = new File(dirPath+"/"+folders[i]);
		}

		return list;
	}

	public static boolean getMount(String mount) {
		String[] mounts = getMounts("/system");
		if (mounts != null && mounts.length >= 3) {
			String device = mounts[0];
			String path = mounts[1];
			String point = mounts[2];
			String preferredMountCmd = new String("mount -o " + mount + ",remount -t " + point + ' ' + device + ' ' + path);
			if (CMDProcessor.runSuCommand(preferredMountCmd).success()) {
				return true;
			}
		}
		String fallbackMountCmd = new String("busybox mount -o remount," + mount + " /system");
		return CMDProcessor.runSuCommand(fallbackMountCmd).success();
	}

	public static String[] getMounts(CharSequence path) {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader("/proc/mounts"), 256);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				if (line.contains(path)) {
					return line.split(" ");
				}
			}
		} catch (FileNotFoundException ignored) {
			Log.d("TAG", "/proc/mounts does not exist");
		} catch (IOException ignored) {
			Log.d("TAG", "Error reading /proc/mounts");
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ignored) {
					// ignored
				}
			}
		}
		return null;
	}

	public static boolean isSystemApp(Context c) {
		boolean mIsSystemApp;
		return mIsSystemApp = c.getResources().getBoolean(R.bool.config_isSystemApp);
	}
	
	/*
     * Find value of build.prop item (/system can be ro or rw)
     *
     * @param prop /system/build.prop property name to find value of
     *
     * @returns String value of @param:prop
     */
    public static String findBuildPropValueOf(String prop) {
        String mBuildPath = "/system/build.prop";
        String DISABLE = "disable";
        String value = null;
        try {
            //create properties construct and load build.prop
            Properties mProps = new Properties();
            mProps.load(new FileInputStream(mBuildPath));
            //get the property
            value = mProps.getProperty(prop, DISABLE);
            Log.d("TAG", String.format("Helpers:findBuildPropValueOf found {%s} with the value (%s)", prop, value));
        } catch (IOException ioe) {
            Log.d("TAG", "failed to load input stream");
        } catch (NullPointerException npe) {
            //swallowed thrown by ill formatted requests
        }

        if (value != null) {
            return value;
        } else {
            return DISABLE;
        }
    }
    
    public static void debugger(Context mContext,String message ) {
    	SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    	String FILE_NAME = "KernelTweaker_log.txt";
    	if(mPrefs.getBoolean(SettingsFragment.KEY_DEBUG, false)){
    		String command = new String("echo \""+ message + "\" >> " + Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+FILE_NAME);
    		CMDProcessor.runShellCommand(command);
    	}
    }
    
    public static void checkApply(Context mContext, String name, String curValue, String filepath) {
    	String filevalue = readFileViaShell(filepath, true);
    	if(!filevalue.contains(curValue)) {
    		debugger(mContext, "\n-------------\n"+name + " " + "value: "+ curValue + " Not Applied --- File value is: "+ filevalue+"\n-------------\n");
    	}else {
    		debugger(mContext, "\n-------------\n"+name + " " + "value: "+ curValue + " Applied"+"\n-------------\n");
    	}
    }

}

