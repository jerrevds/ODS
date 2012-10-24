package com.jabistudio.androidjhlabs.felix;



import odscommon.service.GlowFilterService;

import android.util.Log;

import com.jabistudio.androidjhlabs.filter.GaussianFilter;
import com.jabistudio.androidjhlabs.filter.util.PixelUtils;

public class LocalGlowFilterServiceImpl  extends GaussianFilter
	 implements GlowFilterService{

private float amount = 0.5f;
	
	public LocalGlowFilterServiceImpl() {
		radius = 2;
	}
	
	/**
	 * Set the amount of glow.
	 * @param amount the amount
     * @min-value 0
     * @max-value 1
     * @see #getAmount
	 */
	public void setAmount( float amount ) {
		this.amount = amount;
	}
	
	/**
	 * Get the amount of glow.
	 * @return the amount
     * @see #setAmount
	 */
	public float getAmount() {
		return amount;
	}
	
    public int[] filter( int[] src ,int w, int h) {
    	Log.d("local","local filter");
        int width = w;
        int height = h;

        int[] inPixels = new int[width*height];
        int[] outPixels = new int[width*height];
        
        inPixels = src;

		if ( radius > 0 ) {
			convolveAndTranspose(kernel, inPixels, outPixels, width, height, alpha, alpha && premultiplyAlpha, false, CLAMP_EDGES);
			convolveAndTranspose(kernel, outPixels, inPixels, height, width, alpha, false, alpha && premultiplyAlpha, CLAMP_EDGES);
		}

		outPixels = src;

		float a = 4*amount;

		int index = 0;
		for ( int y = 0; y < height; y++ ) {
			for ( int x = 0; x < width; x++ ) {
				int rgb1 = outPixels[index];
				int r1 = (rgb1 >> 16) & 0xff;
				int g1 = (rgb1 >> 8) & 0xff;
				int b1 = rgb1 & 0xff;

				int rgb2 = inPixels[index];
				int r2 = (rgb2 >> 16) & 0xff;
				int g2 = (rgb2 >> 8) & 0xff;
				int b2 = rgb2 & 0xff;

				r1 = PixelUtils.clamp( (int)(r1 + a * r2) );
				g1 = PixelUtils.clamp( (int)(g1 + a * g2) );
				b1 = PixelUtils.clamp( (int)(b1 + a * b2) );

				inPixels[index] = (rgb1 & 0xff000000) | (r1 << 16) | (g1 << 8) | b1;
				index++;
			}
		}

        return inPixels;
    }

	public String toString() {
		return "Blur/Glow...";
	}

	@Override
	public void setRadius(int mRadiusValue) {
		super.setRadius(mRadiusValue);
		
	}
}
