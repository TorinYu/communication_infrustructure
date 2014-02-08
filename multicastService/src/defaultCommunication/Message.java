package defaultCommunication;
/*
 * Author: Tao yu, Minglei Chen 
 * 
 * Message data structure
 */

import java.io.Serializable;


public class Message implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2764019369486786867L;
	private String dest;
	private String kind;
	private Object data;
	
    private int seqNum;
    private String source;
    private Boolean duplicate = false;
    
    private Boolean logger = false;
    
    private EventType eventType = null; 
    private boolean multicast = false;
    private String groupName;  
    
    public Message(String dest, String kind, Object data) {
    	this.dest = dest;
    	this.kind = kind;
    	this.data = data;
    }
    
    public Message(Message message) {
    	this.dest = message.get_dest();
    	this.kind = message.get_kind();
    	this.data = message.get_data();
    	
    	this.seqNum = message.get_seqNum();
    	this.source = message.get_source();
    	this.duplicate = message.get_duplicate();
    	this.logger = message.get_logger();
    }
    
    public void set_dest(String dest) {
    	this.dest = dest;
    }
    
    public String get_dest() {
    	return dest;
    }
    
    public void set_kind(String kind) {
    	this.kind = kind;
    }
    
    public String get_kind() {
    	return kind;
    }
    
    public void set_data(String data) {
    	this.data = data;
    }
    
    public Object get_data() {
    	return data;
    }
    
    public void set_source(String source) {
    	this.source = source;
    }
    
    public String get_source() {
    	return source;
    }
    
    public void set_seqNum(int num) {
    	this.seqNum = num;
    }
    
    public int get_seqNum() {
    	return seqNum;
    }
    
    public void set_duplicate(Boolean dupe) {
    	this.duplicate = dupe;
    }
    
    public Boolean get_duplicate() {
    	return duplicate;
    }
    
    public void set_logger(Boolean logger) {
    	this.logger = logger;
    }
    
    public Boolean get_logger() {
    	return logger;
    }
    
    public void set_eventType(EventType eventType) {
    	this.eventType = eventType;
    }
    
    public EventType get_eventType() {
    	return eventType;
    }
    
    public boolean isMulticast() {
		return multicast;
	}

	public void setMulticast(boolean multicast) {
		this.multicast = multicast;
	}

	@Override
	public String toString() {
		return "Message [dest=" + dest + ", kind=" + kind + ", Group= "+ groupName + ", data=" + data
				+ ", seqNum=" + seqNum + ", source=" + source + ", duplicate="
				+ duplicate + ", eventType=" + eventType + ", multicast=" + multicast  
				+ "]";
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	
}