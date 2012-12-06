package be.ugent.ods.testapplications.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import be.ugent.ods.testapplications.service.interfaces.EchoService;

public class EchoServiceImpl implements EchoService{

	public String echoString(String data) {
		System.out.println("Printing: "+data);
		return "I've printed the data: \"" + data + "\"";
	}
	
	
	//
	// --- TOOLS
	//
	private void script(String name){
		script(name, new String[0]);
	}
	
	private void script(String name, String... args) {
		String[] commandline = new String[args.length+2];
		commandline[0] = "/bin/bash";
		commandline[1] = name;
		for(int i=0;i<args.length;i++) {
			commandline[i+2] = args[i];
		}
		
		ProcessBuilder builder = new ProcessBuilder(commandline);
		builder.directory(new File("/home/jeroen/Desktop/sharedfolder/ODS/"));
		try {
			Process p = builder.start();
			try {
				p.waitFor();
			} catch (InterruptedException e) {}
			
		} catch (IOException e) {
			System.err.println("Could not run commandline");
		}
	}
	
	private void error(Exception e) {
		script("./ServerAddLog.sh", "ERROR: "+e.getClass().getName()+" => "+e.getMessage());
		e.printStackTrace();
	}
	
	
	private String currentMeasureFullName = null;
	private String currentMeasureCommandInfo = null;
	
	private boolean running = false;
	
	public boolean autoMeasureHasNext(){
		int index = 0;
		
		try{
			File file = new File("/home/jeroen/Desktop/sharedfolder/ODS/ServerTestIndex.txt");
			if(!file.exists()) {
				file.createNewFile();
				BufferedWriter w = new BufferedWriter(new FileWriter(file));
				try{
					w.write("-1");
				}finally {
					w.close();
				}
			}
			
			BufferedReader r = new BufferedReader(new FileReader(file));
			try{
				index = Integer.parseInt(r.readLine());
			}finally {
				r.close();
			}
			
			index++;
		
			BufferedWriter w = new BufferedWriter(new FileWriter(file));
			try{
				w.write(""+index);
			}finally {
				w.close();
			}
			
		}catch(Exception e) {
			error(e);
			return false;
		}
		
		try{
			File file = new File("/home/jeroen/Desktop/sharedfolder/ODS/Tests/"+index+".txt");
			
			if(file.exists()) {
				
				String commandline = "";
				
				BufferedReader r = new BufferedReader(new FileReader(file));
				try{
					commandline = r.readLine();
				}finally {
					r.close();
				}
				
				currentMeasureCommandInfo = commandline;
				
				String [] commandsplit = commandline.split(" ");
				currentMeasureFullName = commandsplit[3];
				
				return true;
			}else{
				return false;
			}
			
			
		}catch (Exception e) {
			error(e);
			return false;
		}
	}
	
	public void autoMeasureStartMeasure(){
		script("./ServerAddLog.sh", "STARTING MEASURE: "+currentMeasureFullName);
		script("./ServerStartMeasure.sh", currentMeasureCommandInfo.split(" "));
		running = true;
	}
	
	public String autoMeasureGetCurrentSettings(){
		return currentMeasureCommandInfo;
	}
	
	public void autoMeasureStopMeasure(){
		script("./ServerStopMeasure.sh");
		script("./ServerAddLog.sh", "MEASURE FINISHED: "+currentMeasureFullName);
		running = false;
	}
	
	public void autoMeasureCrashedMeasure(String error){
		script("./ServerStopMeasure.sh");
		script("./ServerAddLog.sh", "[ERROR] MEASURE CRASHED: "+currentMeasureFullName+" [Message: "+error+"]");
		running = false;
	}
	
	private void autoMeasureCrashedHardMeasure(){
		script("./ServerStopMeasure.sh");
		script("./ServerAddLog.sh", "[ERROR] MEASURE NOT FINISHED: "+currentMeasureFullName);
		running = false;
	}
	
	public void autoMeasureDumpData(String prefix, String suffix, String data) {
		File f = new File("ClientDump/"+prefix+currentMeasureFullName+suffix);
		
		try {
			PrintWriter w = new PrintWriter(f);
			
			w.write(data);
			
			w.close();
		} catch (Exception e) {
			error(e);
		}
		
	}

}
