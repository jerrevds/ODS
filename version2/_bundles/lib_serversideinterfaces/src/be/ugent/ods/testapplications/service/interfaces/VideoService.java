package be.ugent.ods.testapplications.service.interfaces;

import java.util.ArrayList;

import udprsa.annotation.UDP;

public interface VideoService{
	
	@UDP
	public void doSomething(int width, int height, byte[] data, int frame);
	
	public ArrayList<ArrayList<byte[]>> getResult();
	
}

