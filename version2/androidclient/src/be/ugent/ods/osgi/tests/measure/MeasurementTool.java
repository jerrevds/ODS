package be.ugent.ods.osgi.tests.measure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.TrafficStats;
import android.os.Handler;

public class MeasurementTool {
	private int iterations;
	private long start;
	private ApplicationInfo appinfo;
	private boolean started;
	private long dataR;
	private long dataT;
	private long packetsT;
	private long packetsR;
	private int maxcpu;
	private Context context;
	private int memArb;
	private int datapoints;
	private int cumCpu;
	private int cumMem;
	private int cumMemArb;
	private int maxMemArb;
	private Thread thread;

	// todo placeholder for git

	public boolean startMeasuring(int iterations, Context context) {
		this.iterations = iterations;
		this.start = System.currentTimeMillis();
		try {
			appinfo = context.getPackageManager().getApplicationInfo(
					context.getPackageName(), PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			return false;
		}
		// only get data for current app, note that uid from app isn't perse
		// unique but it will provide a minimum of filtering required
		this.dataR = TrafficStats.getUidRxBytes(appinfo.uid);
		this.packetsR = TrafficStats.getUidRxPackets(appinfo.uid);
		this.dataT = TrafficStats.getUidTxBytes(appinfo.uid);
		this.packetsT = TrafficStats.getUidTxPackets(appinfo.uid);
		this.context = context;
		started = true;
		thread = new Thread(new memRunnable());
		thread.start();
		maxMemArb = 0;
		maxcpu = 0;
		datapoints = 0;
		cumCpu = 0;
		cumMem = 0;
		cumMemArb = 0;
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		android.os.Debug.MemoryInfo[] mem = am
				.getProcessMemoryInfo(new int[] { android.os.Process.myPid() });
		memArb = mem[0].getTotalPrivateDirty();
		return true;
	}

	public void stopMeasuring() {
		if (!started) {
			throw new IllegalStateException("please fist start measuring");
		}
		long duration = System.currentTimeMillis() - start;
		this.dataR = TrafficStats.getUidRxBytes(appinfo.uid) - this.dataR;
		this.packetsR = TrafficStats.getUidRxPackets(appinfo.uid)
				- this.packetsR;
		this.dataT = TrafficStats.getUidTxBytes(appinfo.uid) - this.dataT;
		this.packetsT = TrafficStats.getUidTxPackets(appinfo.uid)
				- this.packetsT;
		if(datapoints==0){
			//avoid divide by zero in debug
			datapoints=1;
		}
		int avgCpu = cumCpu / datapoints;
		int avgMem = cumMem / datapoints;
		int avgMemArb = cumMemArb / datapoints;

		started = false;

	}

	public class memRunnable implements Runnable {
		public void run() {
			// do measuring
			while (started) {
				ActivityManager am = (ActivityManager) context
						.getSystemService(Context.ACTIVITY_SERVICE);
				android.os.Debug.MemoryInfo[] mem = am
						.getProcessMemoryInfo(new int[] { android.os.Process
								.myPid() });
				cumMemArb += mem[0].getTotalPrivateDirty() - memArb;
				if (mem[0].getTotalPrivateDirty() - memArb > maxMemArb) {
					maxMemArb = mem[0].getTotalPrivateDirty() - memArb;

				}
				BufferedReader in = null;

				try {
					Process process = null;
					process = Runtime.getRuntime().exec("top -n 1 -d 1");

					in = new BufferedReader(new InputStreamReader(
							process.getInputStream()));

					String line = "";


					while ((line = in.readLine()) != null && !line.contains("be.ugent.ods.osgi")) {
						
					}
					String[] data = line.split(" ");
					int cpu =Integer.parseInt(data[6].replace("%", ""));
					cumCpu+=cpu;
					if(maxcpu < cpu){
						maxcpu=cpu;
					}
					

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
