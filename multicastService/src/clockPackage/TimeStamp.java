package clockPackage;

import java.io.Serializable;

public abstract class TimeStamp implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6758818201716291611L;

	public void updateValue(TimeStamp timeStamp) {
	}
	
	public void increaseValue() {
		
	}
	
	public TimeStamp getOneCopy() {
		return null;
	}
}