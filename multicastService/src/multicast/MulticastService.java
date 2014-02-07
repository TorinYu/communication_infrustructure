package multicast;

import defaultCommunication.*;
import clockPackage.*;

import java.util.ArrayList;
import java.util.HashMap;

import clockPackage.VectorTimeStamp;
import defaultCommunication.Message;

public class MulticastService {
	
	HashMap<String, ArrayList<String>> groupList;
	HashMap<String, VectorTimeStamp> groupTimeStamp;
	ArrayList<Message> receivedMessages;
	
	public MulticastService() {
		HashMap<String, ArrayList<String>> groupList = new HashMap<String, ArrayList<String>>();
		HashMap<String, VectorTimeStamp> groupTimeStamp = new HashMap<String, VectorTimeStamp>();
		ArrayList<Message> receivedMessages = new ArrayList<Message>();

	}
	
	public void bMulticast(String groupName, Message message, MessagePasser mp) {
		ArrayList<String> sendArrayList = groupList.get(groupName);
		for (String a : sendArrayList) {
			Message newMessage = new Message(message);
			newMessage.set_dest(a);
			mp.send(newMessage);
		}
	}
	
	public void bDeliver(String groupName,Message message, MessagePasser mp) {
		if (receivedMessages.contains(message)) {
			
		} else {
			receivedMessages.add(message);
			ArrayList<String> sendArrayList = groupList.get(groupName);
			for (String a : sendArrayList) {
				if (a.equals(mp.getLocal_node())) {
					continue;
				} else {
					bMulticast(groupName, message, mp);
				}
			}
		}
	}
	
	public void rDeliver(String groupName, ArrayList<Message> receivedMessages, MessagePasser mp) {
		long[] groupTime = groupTimeStamp.get(groupName).getTimeStamps();
		Message message = receivedMessages.get(0);
		long[] messageTime = ((VectorTimeStamp)((TimeStampedMessage)message).getTimeStamp()).getTimeStamps();
		int length = groupTime.length;
		String src = message.get_source();
		int index = 0;
		int flag = 0;
		for (int i = 0; i < length; i++) {
			if (i != index) {
				if (groupTime[i] < messageTime[i]) {
					flag = 1;
					break;
				} 
			} else {
				if (groupTime[i] + 1 != messageTime[i] ) {
					flag = 1;
					break;
				} 
			}
		}
		if (flag == 0) {
			mp.getIncomingBuffer().add(message);
			receivedMessages.remove(0);
		} else {
			// TODO
			rDeliver(groupName, receivedMessages, mp);
		}
	}
		
}
