/*
 * Author: Tao yu, Minglei Chen 
 * 
 * a subclass of timestamp with logical time
 */
package clockPackage;

public class LogicalTimeStamp extends TimeStamp{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4042288134126758696L;
	private long value = 0;
	
	public LogicalTimeStamp() {
		super();
	}
	
	public void updateValue(TimeStamp timeStamp) {
		LogicalTimeStamp logicalTimeStamp = (LogicalTimeStamp)timeStamp;
		this.value = Math.max(logicalTimeStamp.getValue() + 1, value);
	}
	
	public long getValue() {
		return value;
	}
	
	public void increaseValue() {
		value++;
	}
	
	public void setValue(long value) {
		this.value = value;
	}
	
	public TimeStamp getOneCopy() {
		LogicalTimeStamp timeStamp = new LogicalTimeStamp();
		timeStamp.setValue(value);
		return timeStamp;
	}

	@Override
	public String toString() {
		return "LogicalTimeStamp [value=" + value + "]";
	}
}
