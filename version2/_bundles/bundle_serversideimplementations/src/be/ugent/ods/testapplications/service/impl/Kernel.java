package be.ugent.ods.testapplications.service.impl;

public class Kernel {
	private int mWidth;
	private int mHeight;
	private float[] mMatrix;
	
	public Kernel(int w, int h, float[] matrix){
		mWidth = w;
		mHeight = h;
		mMatrix = matrix;
	}
	
	public int getWidth(){
		return mWidth;		
	}
	public int getHeight(){
		return mHeight;		
	}
	public float[] getKernelData(float[] data){
		return mMatrix;
	}
}
