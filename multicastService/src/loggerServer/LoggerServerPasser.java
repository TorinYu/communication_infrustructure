package loggerServer;
/*
 * Author: Tao yu, Minglei Chen 
 * 
 * LoggerServerPasser, a subclass of messagepasser
 * with sorting and print message
 */
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Collections;

import clockPackage.LogicalTimeStamp;
import clockPackage.TimeStampedMessage;
import clockPackage.VectorTimeStamp;
import clockService.ClockType;
import defaultCommunication.Message;
import defaultCommunication.MessagePasser;


public class LoggerServerPasser extends MessagePasser{

	public LoggerServerPasser(String configuration_filename, String local_name) {
		super(configuration_filename, local_name);
		//messagePasser = this;
		// TODO Auto-generated constructor stub
		messagePasser = this;
	}
	
	// Only use incomming buffer, no need to use delay buffer
	public void receiveMsg(Message message) {
		
		synchronized (receiveLock) {
			incomingBuffer.add(message);
		}
	}
	
	public void print(ArrayList<Message> timestampMessagesList, ClockType clockType) {
		int n = timestampMessagesList.size();
		if (clockType == ClockType.LOGICAL) {
			if (n == 0)
				System.out.println("There is no event in logger");
			else { 
				for (Message message : timestampMessagesList) 
					System.out.println(((TimeStampedMessage)message).toString());
			}
		} else {
			if (n == 0)
				System.out.println("There is no event in logger");
			else if (n == 1)
				System.out.println((timestampMessagesList.get(0)).toString());
			else { 
				TimeStampedMessage temp = (TimeStampedMessage)timestampMessagesList.get(0);
				System.out.println(temp.toString());
				for (int i = 1; i < n; i++) {
					TimeStampedMessage message = (TimeStampedMessage)timestampMessagesList.get(i);
					long[] vector1 = ((VectorTimeStamp)temp.getTimeStamp()).getTimeStamps();
					long[] vector2 = ((VectorTimeStamp)message.getTimeStamp()).getTimeStamps();
					int flag = 0;
					for (int j = 0; j < vector1.length; j++) {
						if (vector2[j] < vector1[j]) {
							flag = 1;
							break;
						}
					}
					if (flag == 0)
						System.out.println("----->  " + message.toString());
					else 
						System.out.println("<---->  " + message.toString());
					temp = (TimeStampedMessage)timestampMessagesList.get(i);
				}
			}
	    }
	}
	
	public ArrayList<Message> sort(ClockType clockType) {
		//Combine messages from incomingBuffer and incomingDelayBuffer
		ArrayList<Message> timestampMessages = new ArrayList<Message>();
		Iterator<Message> iterator = incomingBuffer.iterator();
		while(iterator.hasNext()) {
			timestampMessages.add(iterator.next());
		}
		//Need to decide whether it is Vector of Logic  
		// When it uses Vector clock
		if (clockType == ClockType.VECTOR) {
			Collections.sort(timestampMessages, new Comparator<Message>() {

				@Override
				public int compare(Message o1, Message o2) {
					// TODO Auto-generated method stub
					TimeStampedMessage newO1 = (TimeStampedMessage)o1;
					TimeStampedMessage newO2 = (TimeStampedMessage)o2;
					long[] vector1 = ((VectorTimeStamp)newO1.getTimeStamp()).getTimeStamps();
					long[] vector2 = ((VectorTimeStamp)newO2.getTimeStamp()).getTimeStamps();
				    for (int i = 0; i < vector1.length; i++) {
				    	if (vector1[i] < vector2[i])
				    		return -1;
				    	if (vector1[i] > vector2[i])
				    		return 1;
				    }
					return 0;
				}
			});
			return timestampMessages;
	    } else {                        //When it uses Logic clock
			Collections.sort(timestampMessages, new Comparator<Message>() {

				@Override
				public int compare(Message o1, Message o2) {
					// TODO Auto-generated method stub
					TimeStampedMessage newO1 = (TimeStampedMessage)o1;
					TimeStampedMessage newO2 = (TimeStampedMessage)o2;
					long v1 = ((LogicalTimeStamp)newO1.getTimeStamp()).getValue();
					long v2 = ((LogicalTimeStamp)newO2.getTimeStamp()).getValue();
					if (v1 < v2) {
						return -1;
					} else if (v1 > v2){
						return 1;
					} else {
						return 0;
					}
				}
			});
	    	return timestampMessages;
		}	
	}
	
	
}
