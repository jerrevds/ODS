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
private Handler memHandler;
private int maxMemory;
private int maxcpu;
private Context context;
private int memArb;

//todo placeholder for git
	

	public boolean startMeasuring(int iterations, Context context){	
		this.iterations = iterations;
		this.start = System.currentTimeMillis();
		try {
			appinfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			return false;
		}
		//only get data for current app, note that uid from app isn't perse unique but it will provide a minimum of filtering required
		this.dataR = TrafficStats.getUidRxBytes(appinfo.uid);
		this.packetsR = TrafficStats.getUidRxPackets(appinfo.uid);
		this.dataT = TrafficStats.getUidTxBytes(appinfo.uid);
		this.packetsT = TrafficStats.getUidTxPackets(appinfo.uid);
		this.context=context;
		started = true;
		memHandler = new Handler();
		maxMemory = 0;
		maxcpu = 0;
		ActivityManager am=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		android.os.Debug.MemoryInfo[] mem=am.getProcessMemoryInfo(new int[]{android.os.Process.myPid()});
		memArb = mem[0].getTotalPrivateDirty();
		memHandler.postDelayed(new memRunnable(), 100);
		return true;
	}
	
	public void stopMeasuring(){
		if(!started){
			throw new IllegalStateException("please fist start measuring");
		}
		long duration = System.currentTimeMillis()-start;
		this.dataR = TrafficStats.getUidRxBytes(appinfo.uid) - this.dataR;
		this.packetsR = TrafficStats.getUidRxPackets(appinfo.uid) - this.packetsR ;
		this.dataT = TrafficStats.getUidTxBytes(appinfo.uid) - this.dataT;
		this.packetsT = TrafficStats.getUidTxPackets(appinfo.uid) - this.packetsT;
		ActivityManager am=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		android.os.Debug.MemoryInfo[] mem=am.getProcessMemoryInfo(new int[]{android.os.Process.myPid()});
		memArb = mem[0].getTotalPrivateDirty() - memArb;
		started=false;
	}
	
	public class memRunnable implements Runnable {
		public void run() {
			//do measuring

			 BufferedReader in = null;

		        try {
					Process process = null;
					process = Runtime.getRuntime().exec("top -n 1 -d 1");

		            in = new BufferedReader(new InputStreamReader(process.getInputStream()));

		            String line ="";
		            String content = "";

		            while((line = in.readLine()) != null) {
		            	content += line + "\n";
		            }
		            //in here make a fix to get memory usage and cpu usage
		            System.out.println(content);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally {
					if(in != null) {
						try {
							in.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			if(started){
				memHandler.postDelayed(this, 100);
			}
		}
	}
}
