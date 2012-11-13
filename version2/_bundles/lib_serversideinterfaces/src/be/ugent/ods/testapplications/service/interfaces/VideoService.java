package be.ugent.ods.testapplications.service.interfaces;

import java.util.ArrayList;

public interface VideoService{
	public void doSomething(int width, int height, byte[] data, int frame);
	
	public ArrayList<ArrayList<byte[]>> getResult();
	
}

