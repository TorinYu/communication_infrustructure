package clockPackage;
/*
 * Author: Tao yu, Minglei Chen 
 * 
 * a subclass of message with timestamp
 */
import defaultCommunication.Message;


public class TimeStampedMessage extends Message{
    /**
	 * 
	 */
	private static final long serialVersionUID = -6728667465467965665L;
	private TimeStamp timeStamp = null;
	
    public TimeStampedMessage(Message message) {
    	super(message);
    }
    
    public TimeStampedMessage(String dest, String kind, Object data) {
    	super(dest, kind, data);
    }
	
    public void setTimeStamp(TimeStamp timeStamp) {
    	this.timeStamp = timeStamp;
    }
    
    public TimeStamp getTimeStamp() {
    	return timeStamp;
    }

	@Override
	public String toString() {
		return super.toString() + timeStamp.toString();
	}
}
