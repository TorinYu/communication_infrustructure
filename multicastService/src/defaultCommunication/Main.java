package defaultCommunication;
/*
 * Name: Tao yu, Minglei Chen
 * 
 * Main function for application running on node
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

import clockPackage.TimeStampedMessage;


public class Main {
	public static String usage = "Usage: \n    Send a Message: send [-d destination] [-k kind] [-m data] [-l log]\n" +
			"    Receive a Message: receive\n" +
			"    Create an Event: event\n" +
			"    Info: help \n" +
			"    Exit: exit\n";
	public static MessagePasser messagePasser = null;
	
	public static void main(String[] args) {		
		
	    if (args.length < 2) {
			System.out.println("Parameters invalid.");
			return;
		}
	    
		BufferedReader br = null;
		HashSet<String> nodeNames = null;
		try {
			//messagePasser = MessagePasser.createMessagePasser(args[0], args[1]);
			messagePasser = MessagePasser.createMessagePasser("resource/Lab0.yaml", "alice");
			nodeNames = messagePasser.getNames();
			
			br = new BufferedReader (new InputStreamReader(System.in));
			System.out.println(usage);
			
			/* Continue reading user's command */
			while (true) {
				String cmd = br.readLine().trim().toLowerCase();
				String[] sArray = cmd.split(" ");
				if (sArray.length != 1 && sArray.length != 9) {
					System.out.println(usage);
				}
				else if (sArray.length == 1) {
					if (sArray[0].equals("receive")) {
						receive();
					}
					else if (sArray[0].equals("event")) {
						createEvent();
					}
					else if (sArray[0].equals("exit")) {
						System.exit(0);
					}
					else {
						System.out.println(usage);
					}
				}
				else {
					if (!sArray[0].equals("send") || !sArray[1].equals("-d") 
							|| !sArray[3].equals("-k") || !sArray[5].equals("-m") || !sArray[7].equals("-l")) {
						System.out.println(usage);
					}
					else if (!nodeNames.contains(sArray[2])) {
						System.out.println("Destination Name Invalid.");
					}
					else {
						Message message = new TimeStampedMessage(sArray[2], sArray[4], sArray[6]);
						if (sArray[8].equalsIgnoreCase("true"))
							message.set_logger(true);
						messagePasser.send(message);
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/* create an event */
	private static void createEvent() {
		messagePasser.createEvent();
	}
	
	/* receive message */
	private static void receive() {
		Message receiveMsg = messagePasser.receive();
		while (receiveMsg == null) {
			receiveMsg = messagePasser.receive();
		}
		
		if (receiveMsg instanceof TimeStampedMessage) {
			System.out.println("Message Received:" + receiveMsg.get_source() + "-" +  
					receiveMsg.get_seqNum() + "-" + (String)receiveMsg.get_kind() + "-" + (String)receiveMsg.get_data());
		}
		else 
			System.out.println("Message Received:" + receiveMsg.get_source() + "-" +  
				receiveMsg.get_seqNum() + "-" + (String)receiveMsg.get_kind() + "-" + (String)receiveMsg.get_data());
	} 
}
