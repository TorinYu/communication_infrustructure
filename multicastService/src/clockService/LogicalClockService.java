/*
 * Author: Tao yu, Minglei Chen 
 * 
 * Logical clock service
 */
package clockService;

import clockPackage.LogicalTimeStamp;


public class LogicalClockService extends ClockService{
	public LogicalClockService() {
		currentTime = new LogicalTimeStamp();
	}
}
