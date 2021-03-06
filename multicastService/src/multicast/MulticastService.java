package multicast;

import defaultCommunication.*;
import clockPackage.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import clockPackage.VectorTimeStamp;
import defaultCommunication.Message;

public class MulticastService{
	
	//HashMap<String, ArrayList<String>> groupList;
	HashMap<String, VectorTimeStamp> groupTimeStamp;
	ArrayList<Message> receivedMessages;
	ArrayList<Message> holdbackQueue;
	
	public MulticastService() {
		//super(configuration_filename, local_name);
		groupTimeStamp = new HashMap<String, VectorTimeStamp>();
		receivedMessages = new ArrayList<Message>();
		holdbackQueue = new ArrayList<Message>();
        //int groupNum = getGroups().size();
        Set<String> nodeSet = MessagePasser.getInstance().groups.keySet();
        for (String a : nodeSet) {
        	groupTimeStamp.put(a, new VectorTimeStamp(MessagePasser.getInstance().getNodes().size(),MessagePasser.getInstance().getLocal_node().getNode_index()));
        }     
	}
	
	public void bMulticast(String groupName, Message message) {
		ArrayList<String> sendArrayList = MessagePasser.getInstance().groups.get(groupName);
		VectorTimeStamp currentTimeStamp = groupTimeStamp.get(groupName);
		synchronized (currentTimeStamp) {
			//TODO
			currentTimeStamp.increaseValue();
		}
		for (String a : sendArrayList) {
			Message newMessage = new TimeStampedMessage(message);
			message.setMulticast(true);
			((TimeStampedMessage)newMessage).setTimeStamp(currentTimeStamp);
			newMessage.set_dest(a);
			newMessage.setGroupName(groupName);
			newMessage.setMulticast(true);
			MessagePasser.getInstance().send(newMessage);
		}
	}
	
	public void bDeliver(String groupName,Message message) {
		
		if (receivedMessages.contains(message)) {
			return;
		} else {
			receivedMessages.add(message);
			holdbackQueue.add(message);
			if (message.get_source() != MessagePasser.getInstance().getLocal_node().getNode_name()) {
				bMulticast(groupName, message);
			}
		}
		rDeliver(groupName, holdbackQueue);
	}
	
	//TODO syncronized 
	public void rDeliver(String groupName, ArrayList<Message> holdbackQueue) {	
		int size = holdbackQueue.size();
		int sendFlag = 1;
		while (sendFlag > 0) {
			sendFlag = 0;
			for (int i = 0; i < size; i++) {
				VectorTimeStamp currentTimeStamp = groupTimeStamp.get(groupName);
				long[] groupTime = currentTimeStamp.getTimeStamps();
				Message message = holdbackQueue.get(i);
				long[] messageTime = ((VectorTimeStamp)((TimeStampedMessage)message).getTimeStamp()).getTimeStamps();
				int length = groupTime.length;
				String src = message.get_source();
				int index = MessagePasser.getInstance().getNodes().get(src).getNode_index();
				int flag = 0;
				if (messageTime[index] - groupTime[index] == 1) {
					for (int j = 0; j < length; j++) {
						if (j == index) {
							continue;
						} else {
							if (messageTime[i] < groupTime[i]) {
								flag = 1;
								break;
							}
						}
					}
					if (flag == 0) {
						synchronized (currentTimeStamp) {
							currentTimeStamp.increaseValue();
							sendFlag++;
						}
						MessagePasser.getInstance().getIncomingBuffer().add(message);
				        holdbackQueue.remove(i);
					} 

				}

			}
		}

	}
		
}

	
