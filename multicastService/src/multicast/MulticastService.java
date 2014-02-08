package multicast;

import defaultCommunication.*;
import clockPackage.*;

import java.util.ArrayList;
import java.util.HashMap;

import clockPackage.VectorTimeStamp;
import defaultCommunication.Message;

public class MulticastService extends MessagePasser{
	
	HashMap<String, ArrayList<String>> groupList;
	VectorTimeStamp groupTimeStamp;
	ArrayList<Message> receivedMessages;
	
	public MulticastService(String configuration_filename, String local_name) {
		super(configuration_filename, local_name);

		groupList = new HashMap<String, ArrayList<String>>();
		groupTimeStamp = new VectorTimeStamp(getNodes().size(),getLocal_node().getNode_index());
		receivedMessages = new ArrayList<Message>();

	}
	
	public void rMulticast(String groupName, Message message) {
		bMulticast(groupName, message);
		bDeliver(groupName, message); 
	}
	
	public void bMulticast(String groupName, Message message) {
		ArrayList<String> sendArrayList = groupList.get(groupName);
		synchronized (groupTimeStamp) {
			groupTimeStamp.increaseValue();
		}
		for (String a : sendArrayList) {
			Message newMessage = new Message(message);
			message.setMulticast(true);
			((TimeStampedMessage)newMessage).setTimeStamp(groupTimeStamp);
			newMessage.set_dest(a);
			send(newMessage);
		}
	}
	
	public void bDeliver(String groupName,Message message) {
		
		if (receivedMessages.contains(message)) {
			return;
		} else {
			receivedMessages.add(message);
			ArrayList<String> sendArrayList = groupList.get(groupName);
			for (String a : sendArrayList) {
				if (a.equals(getLocal_node())) {
					continue;
				} else {
					bMulticast(groupName, message);
				}
			}
		}
	}
	
	public void rDeliver(String groupName, ArrayList<Message> receivedMessages) {
		long[] groupTime = groupTimeStamp.getTimeStamps();
		Message message = receivedMessages.get(0);
		long[] messageTime = ((VectorTimeStamp)((TimeStampedMessage)message).getTimeStamp()).getTimeStamps();
		int length = groupTime.length;
		String src = message.get_source();
		int index = getNodes().get(src).getNode_index();
		int flag = 0;

		if (messageTime[index] - groupTime[index] == 1) {
			for (int i = 0; i < length; i++) {
				if (i == index) {
					continue;
				} else {
					if (messageTime[i] < groupTime[i]) {
						flag = 1;
						break;
					}
				}
			}
			if (flag == 0) {
				synchronized (groupTimeStamp) {
					groupTimeStamp.increaseValue();
				}
		        getIncomingBuffer().add(message);
		        receivedMessages.remove(0);
			} else {
				Message tempMessage = receivedMessages.remove(0);
				receivedMessages.add(tempMessage);
				rDeliver(groupName, receivedMessages);
			}
		} else {
			Message tempMessage = receivedMessages.remove(0);
			receivedMessages.add(tempMessage);
			rDeliver(groupName, receivedMessages);
		}	
	}
		
}
