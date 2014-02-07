package defaultCommunication;

/*
 * Author: Tao yu, Minglei Chen 
 * 
 * Server thread for each connection
 */
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;


public class ServerThread implements Runnable{
	private Socket socket;
	//private MessagePasser messagePasser = MessagePasser.getMessagePasser(); 
	private MessagePasser messagePasser = null;
	
	public ServerThread(Socket socket, MessagePasser messagePasser) {
		this.socket = socket;
		this.messagePasser = messagePasser;
	}
	
	@Override
	public void run() {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(socket.getInputStream());
			Message message = null;

			while (true) {
				message = (Message)ois.readObject();
				//System.out.println("ServerThread: " + message.get_dest());
				//System.out.println("ServerThread: " + message.get_data());
				messagePasser.receiveMsg(message);
				ois = new ObjectInputStream(socket.getInputStream());
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (EOFException e) {
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
				ois.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
