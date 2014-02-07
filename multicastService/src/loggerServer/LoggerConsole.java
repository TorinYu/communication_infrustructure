package loggerServer;


/*
 * Author: Tao yu, Minglei Chen 
 * 
 * Logger console running on logger
 */
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Set;

import defaultCommunication.Message;

public class LoggerConsole {
	
	public static String usage = "Usage: \n    Print the log information: print \n";
	
	public static void main(String[] args) throws Exception {
		
		String configFile = "";
		String localName = "";
		Scanner scanner = new Scanner(System.in);

		// Get configuration file name and local name from user input
		while(configFile.length() == 0) {
			System.out.print("Please enter configuration file name: ");
			System.out.flush();
			configFile = scanner.nextLine().trim();
		}
		while(localName.length() == 0) {
			System.out.print("Please enter local name: ");
			localName = scanner.nextLine().trim();
		}
		
		LoggerServerPasser loggerServerPasser = new LoggerServerPasser(configFile, localName);	
		while(true) {
			System.out.print(usage);
			System.out.flush();
			String command = scanner.nextLine().trim();
			if(command.equalsIgnoreCase("print")) {
				ArrayList<Message> sortedEvents = loggerServerPasser.sort(loggerServerPasser.getClockType());
				System.out.println(sortedEvents.size());
				loggerServerPasser.print(sortedEvents, loggerServerPasser.getClockType());
			}
			else if (command.equalsIgnoreCase("exit")){
				Hashtable<String, Socket> socketMap = loggerServerPasser.getSocketsMap();
				Set<String> socketNameSet = socketMap.keySet();
				for (String a : socketNameSet) {
					Socket socket = socketMap.get(a);
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				System.exit(0);
			}
			else  {
				System.out.println(usage);
			}
		}
	}
}
