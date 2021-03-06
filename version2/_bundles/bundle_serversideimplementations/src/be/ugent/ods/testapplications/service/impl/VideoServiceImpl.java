package be.ugent.ods.testapplications.service.impl;

import java.util.ArrayList;

import be.ugent.ods.testapplications.service.interfaces.VideoService;

public class VideoServiceImpl implements VideoService{

	private ArrayList<ArrayList<byte[]>> result= new ArrayList<ArrayList<byte[]>>();
	int algframe = -1;
	
	public void clear(){
		result = new ArrayList<ArrayList<byte[]>>();
		algframe = -1;
	}

	@Override
	public void doSomething(int width, int height, byte[] data, int frame) {
		//
		if(frame > algframe){
			result.add(new ArrayList<byte[]>()) ;
			algframe = frame;
		}
		result.get(frame).add(data);
		//doing dummy processing work
		int a, b=0, c=1;
		for(int i=0;i<1000;i++){
			a = b;
			b = c;
			c = a + b;
		}
	}

	@Override
	public ArrayList<ArrayList<byte[]>> getResult() {

		return result;
	}

}

