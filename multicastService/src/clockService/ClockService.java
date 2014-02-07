/*
 * Author: Tao yu, Minglei Chen 
 * 
 * abstract class clockservice
 */
package clockService;

import clockPackage.TimeStamp;

public abstract class ClockService {
	protected TimeStamp currentTime;
	
	public TimeStamp getTimeStamp() {
		return currentTime;
	}
	
	public void updateCurrentTime(TimeStamp timeStamp) {
		currentTime.updateValue(timeStamp);
	}

	public void increaseTimeStamp() {
		currentTime.increaseValue();
	}

	public TimeStamp copyOfTimeStamp() {
		return currentTime.getOneCopy();
	}
}
