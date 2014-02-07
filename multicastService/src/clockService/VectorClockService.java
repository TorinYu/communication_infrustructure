/*
 * Author: Tao yu, Minglei Chen 
 * 
 * a subclass of clock service using vector time stamp
 */
package clockService;

import clockPackage.VectorTimeStamp;


public class VectorClockService extends ClockService{
	
	public VectorClockService(int size, int index) {
		currentTime = new VectorTimeStamp(size, index);
	}
	
	
}
