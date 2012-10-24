package odscommon.service.interfaces;



public interface GlowFilterService {
	
	
	/**
	 * Set the amount of glow.
	 * @param amount the amount
     * @min-value 0
     * @max-value 1
     * @see #getAmount
	 */
	public void setAmount( float amount );
	
	/**
	 * Get the amount of glow.
	 * @return the amount
     * @see #setAmount
	 */
	public float getAmount();
	
    public int[] filter( int[] src ,int w, int h);

	public String toString();

	public void setRadius(int mRadiusValue);
}
