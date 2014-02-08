package defaultCommunication;
/*
 * Author: Tao yu, Minglei Chen 
 * 
 * MessagePasser
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.yaml.snakeyaml.Yaml;

import sun.security.jca.GetInstance;
import clockPackage.TimeStampedMessage;
import clockService.ClockFactory;
import clockService.ClockService;
import clockService.ClockType;


public class MessagePasser {
    	
	private HashMap<String, Node> nodes = new HashMap<String, Node>();
    private Hashtable<String, Socket> socketsMap = new Hashtable<String, Socket>();
	
    private List<Rule> sendRules = new ArrayList<Rule>();
	private List<Rule> receiveRules = new ArrayList<Rule>();
	public HashMap<String, ArrayList<String>> groups = new HashMap<String, ArrayList<String>>();
	// Lock used by rules
    private byte[] ruleLock = new byte[0];
    // Lock for receive buffer
    protected byte[] receiveLock = new byte[0];
    // Lock for clock
    private byte[] clockLock = new byte[0];
	
	private Queue<Message> outgoingDelayBuffer = new LinkedList<Message>();
	protected BlockingQueue<Message> incomingBuffer = new LinkedBlockingQueue<Message>();
	protected BlockingQueue<Message> incomingDelayBuffer = new LinkedBlockingQueue<Message>(); 
	
	public static MessagePasser messagePasser = null;
	private Node local_node = null;
	private static int curMsgSeq = 1;
	private String configuration_filename;
	private long fileLastModifiedTime = 0;
	
	private ClockService clockService = null; 
	private ClockType clockType = null;
	private Node loggerNode = null;
	
	
 	public static MessagePasser createMessagePasser(String configuration_filename, String local_name) throws Exception {
		if (messagePasser == null) {
			messagePasser = new MessagePasser(configuration_filename, local_name);
		}
		else {
			throw new Exception("MessagePasser can only be created for once.");
		}
		return messagePasser;
	}
 	
 	public static MessagePasser getInstance() {
		return messagePasser;
	}
	
	public static MessagePasser getMessagePasser() {
		return messagePasser;
	}
	
	public MessagePasser(String configuration_filename, String local_name) {
		this.configuration_filename = configuration_filename;
		
		try {
			updateRules(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.local_node = nodes.get(local_name);
		
		if (local_node == null) {
			try {
				throw new Exception("localname invalid!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} 
		
		if (clockType != null) {
			ClockFactory clockFactory = new ClockFactory();
			clockService = clockFactory.createClockService(clockType, nodes.size(), local_node.node_index);
		}
		
		new Thread(new ListenThread(local_node.node_port, this)).start();
	}
	
	/* send a message called by application*/
	public void send(Message message) { 
		message.set_source(local_node.node_name);
		message.set_seqNum(curMsgSeq++);
		Action action = null;
		
		if (message.isMulticast() == false) {
			synchronized (clockLock) {
				if (message instanceof TimeStampedMessage && clockService != null ) {
					clockService.increaseTimeStamp();
					((TimeStampedMessage) message).setTimeStamp(clockService.copyOfTimeStamp());
				}
			}
		} 
		
		
		synchronized (ruleLock) {
			try {
				updateRules(false);
			} catch (IOException e) {
				e.printStackTrace();
			}
			action = compareMsgWithRules(message, sendRules);
		}

		switch (action) {
		case Delay:
			System.out.println("delay message");
			outgoingDelayBuffer.add(message);
		    break;
		case Duplicate:
			sendMsg(message);
			sendMsgsInDelayBuffer();
			
			Message message_dup = new Message(message);
			message_dup.set_duplicate(true);
		    sendMsg(message_dup);
			break;
		case None:
			sendMsg(message);
			sendMsgsInDelayBuffer();
			break;
		default:
			break;
		}
	}
	
	/* send the messages in delay buffer */
	private void sendMsgsInDelayBuffer() {
		Message message = null;
		while ((message = outgoingDelayBuffer.poll()) != null) {
			sendMsg(message);
		}
	}

	/* Send a message to destination */
	public void sendMsg(Message message) {
		Node destNode = nodes.get(message.get_dest());
		/*synchronized (clockLock) {
			if (message instanceof TimeStampedMessage && clockService != null ) {
				clockService.increaseTimeStamp();
				((TimeStampedMessage) message).setTimeStamp(clockService.copyOfTimeStamp());
			}
		}*/
		
		if (sendMsgToNode(destNode, message) == false) {
			if (sendMsgToNode(destNode, message) == false) {
				return;
			}
		}
		
		if (message.get_logger() == false) return;
		if (loggerNode == null) {
			System.out.println("System Error: Cannot find Logger Node.");
			return;
		}
		
		message.set_eventType(EventType.SEND);
		if (sendMsgToNode(loggerNode, message) == false) {;
			if (sendMsgToNode(loggerNode, message) == false) {
				return;
			}
		}
		
	}
	
	private boolean sendMsgToNode(Node destNode, Message message) {
		Socket socket = null;
		
		if (socketsMap.containsKey(destNode.node_name)) {
			socket = socketsMap.get(destNode.node_name);
		}
		else {
			socket = connectWithNode(destNode);
			if (socket == null) return true;
		}
		
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(message);
			oos.flush();
		} catch (SocketException e) {
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			socketsMap.remove(destNode.node_name);
			return false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	/* make a connection with destionation node */
	private Socket connectWithNode(Node destNode) {
		Socket socket = null;
		try {
			InetAddress ipAddress = InetAddress.getByName(destNode.node_ip);
			socket = new Socket(ipAddress, destNode.node_port);
		} catch (UnknownHostException e) {
			System.out.println("Connection Error: The host is unknow.");
			return socket;
		} catch (IOException e) {
			System.out.println("Connection Error: Connection refused.");
			return socket;
		}
		socketsMap.put(destNode.node_name, socket);
		return socket;
	}

	/* receive provided to application */
	public Message receive() {
		Message message = null;
		synchronized (receiveLock) {
			message = incomingBuffer.poll();
		}
		synchronized (clockLock) {
			if (message instanceof TimeStampedMessage && clockService != null) {
				clockService.increaseTimeStamp();
				TimeStampedMessage timeMessage = (TimeStampedMessage)message;
				clockService.updateCurrentTime(timeMessage.getTimeStamp());
				((TimeStampedMessage) message).setTimeStamp(clockService.copyOfTimeStamp());
			}
		}
		if (message != null && message.get_logger() != false) {
			if (loggerNode == null) {
				System.out.println("Cannot find Logger Node.");
			}
			else {
				message.set_eventType(EventType.RECEIVE);
				if (sendMsgToNode(loggerNode, message) == false) {
					sendMsgToNode(loggerNode, message);
				}
			}
		}
		
		return message;
	}
	
	/* receive a message and apply receive rules */
	public void receiveMsg(Message message) {
		Action action = null;
		synchronized (ruleLock) {
			try {
				updateRules(false);
			} catch (IOException e) {
				e.printStackTrace();
			}
			action = compareMsgWithRules(message, receiveRules);
		}
		
		synchronized (receiveLock) {
			switch (action) {
			case Delay:
				incomingDelayBuffer.add(message);
				break;
			case Duplicate:
				incomingBuffer.add(message);
				incomingDelayBuffer.drainTo(incomingBuffer);
				Message message_dup = new Message(message);
				incomingBuffer.add(message_dup);
				break;
			case None:
				incomingBuffer.add(message);
				incomingDelayBuffer.drainTo(incomingBuffer);
				break;
			default:
				break;
			}
		}
	}

	/* parse the configuration file */
	private void updateRules(boolean initial) throws IOException {
		File configFile = new File(configuration_filename);
		if (configFile.lastModified() == fileLastModifiedTime) 
			return;
		fileLastModifiedTime = configFile.lastModified();
		InputStream input = new FileInputStream(configFile);
		Yaml yaml = new Yaml();
		Map<String, Object> map = (Map<String, Object>)yaml.load(input);
		List configuration = (List) map.get("configuration");
		List rules1 = (List) map.get("sendRules");
		List rules2 = (List) map.get("receiveRules");
		String clock = (String) map.get("clockType");
		ArrayList<Map<String, Object>> groupsList = (ArrayList<Map<String, Object>>) map.get("Groups");
	
		input.close();
		
		if (initial) {
			nodes.clear();
			int index = 0;
			for (Object tmpO : configuration) {
				Map<String, Object> tmpMap = (Map<String, Object>)tmpO;
				Node tmpNode = new Node((String)tmpMap.get("name"), (String)tmpMap.get("ip"), (Integer)tmpMap.get("port"), index);
				nodes.put(tmpNode.node_name, tmpNode);
				if (tmpNode.node_name.equalsIgnoreCase("logger")) {
					loggerNode = tmpNode;
				}
				index++;
			}
			
			if (clock == null) {
				clockType = null;
			}
			else if (clock.equalsIgnoreCase("Logical")) {
				clockType = ClockType.LOGICAL;
			}
			else if (clock.equalsIgnoreCase("Vector")) {
				clockType = ClockType.VECTOR;
			}
		}
		
		sendRules.clear();
		for (Object tmpO : rules1) {
			Map<String, Object> tmpMap = (Map<String, Object>)tmpO;
			Rule rule = new Rule((String)tmpMap.get("action"), (String)tmpMap.get("src"), (String)tmpMap.get("dest"),
					(String)tmpMap.get("kind"), (Integer)tmpMap.get("seqNum"), (Boolean)tmpMap.get("duplicate"));
			sendRules.add(rule);
		}
		
		receiveRules.clear();
		for (Object tmpO : rules2) {
			Map<String, Object> tmpMap = (Map<String, Object>)tmpO;
			Rule rule = new Rule((String)tmpMap.get("action"), (String)tmpMap.get("src"), (String)tmpMap.get("dest"),
					(String)tmpMap.get("kind"), (Integer)tmpMap.get("seqNum"), (Boolean)tmpMap.get("duplicate"));
			receiveRules.add(rule);
		}
		
		groups.clear();
		for (Map<String, Object> yamlGroup : groupsList) {
    		try {
                String name = yamlGroup.get("Name").toString();
                ArrayList<String> members = (ArrayList<String>) yamlGroup.get("Members");
                groups.put(name, members);
            } catch (Exception e) {
                System.err.println("ERROR: configuration file error - " + yamlGroup);
                e.printStackTrace();
            }
    	} 
	}

	public Action compareMsgWithRules(Message msg, List<Rule> rules) {
		for (Rule rule : rules) {
			if (compareMsgWithRule(rule, msg)) {
				return rule.action;
			}
		}
		return Action.None;
	}  
	
	public boolean compareMsgWithRule(Rule rule, Message msg) {
		if (rule.src != null && !rule.src.equalsIgnoreCase(msg.get_source())) {
			return false;
		}
		
		if (rule.dest != null && !rule.dest.equalsIgnoreCase(msg.get_dest())) {
			return false;
		}
		
		if (rule.kind != null && !rule.kind.equalsIgnoreCase(msg.get_kind())) {
			return false;
		}
		
		if (rule.seqNum != -1 && rule.seqNum != msg.get_seqNum()) {
			return false;
		}
		
		if (rule.dupe != null && rule.dupe.booleanValue() != msg.get_duplicate().booleanValue()) {
			return false;
		}
		
		return true;
	}

	public HashSet<String> getNames() {
		HashSet<String> names = new HashSet<String>();
		for (String name : nodes.keySet()) {
			names.add(name);
		}
		return names;
	}
	
	public ClockType getClockType() {
		return clockType;
	}

	public Hashtable<String, Socket> getSocketsMap() {
		return socketsMap;
	}

	public void setSocketsMap(Hashtable<String, Socket> socketsMap) {
		this.socketsMap = socketsMap;
	}
	
	public Node getLocal_node() {
		return local_node;
	}

	public void setLocal_node(Node local_node) {
		this.local_node = local_node;
	}

	public BlockingQueue<Message> getIncomingBuffer() {
		return incomingBuffer;
	}

	public void setIncomingBuffer(BlockingQueue<Message> incomingBuffer) {
		this.incomingBuffer = incomingBuffer;
	}
	public HashMap<String, Node> getNodes() {
		return nodes;
	}

	public void setNodes(HashMap<String, Node> nodes) {
		this.nodes = nodes;
	}

	/* create an event other than message */
	public void createEvent() {
		synchronized (clockLock) {
			if (clockService != null ) {
				clockService.increaseTimeStamp();
				System.out.println(clockService.getTimeStamp().toString());
			}
		}
	}

	public HashMap<String, ArrayList<String>> getGroups() {
		return groups;
	}

	public void setGroups(HashMap<String, ArrayList<String>> groups) {
		this.groups = groups;
	}




}
