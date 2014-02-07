/*
 * Author: Tao yu, Minglei Chen 
 * 
 * a subclass of timestamp with vector time
 */
package clockPackage;

import java.util.Arrays;

public class VectorTimeStamp extends TimeStamp{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2036431233165786629L;
	private long[] timeStamps = null;
	private int index = 0;
	
	public VectorTimeStamp(int size, int index) {
		this.index = index;
		timeStamps = new long[size];
		timeStamps[index] = 1;
	}
	
	public void updateValue(TimeStamp timeStamp) {
		VectorTimeStamp vectorTimeStamp = (VectorTimeStamp)timeStamp;
		long[] stamps = vectorTimeStamp.getValue();
		for (int i = 0; i < stamps.length; i++) {
			timeStamps[i] = Math.max(timeStamps[i], stamps[i]);
		}
	}
	
	public void increaseValue() {
		timeStamps[index]++;
	}
	
	public long[] getValue() {
		return timeStamps;
	}
	
	public TimeStamp getOneCopy() {
		VectorTimeStamp timeStamp = new VectorTimeStamp(timeStamps.length, index);
		timeStamp.updateValue(this);
		return timeStamp;
	}

	public long[] getTimeStamps() {
		return timeStamps;
	}

	public void setTimeStamps(long[] timeStamps) {
		this.timeStamps = timeStamps;
	}

	@Override
	public String toString() {
		return "VectorTimeStamp [timeStamps=" + Arrays.toString(timeStamps)
				 + "]";
	}
}
