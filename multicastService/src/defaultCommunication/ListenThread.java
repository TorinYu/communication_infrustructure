package defaultCommunication;


/*
 * Author: Tao yu, Minglei Chen 
 * 
 * Listening thread for a normal node, try accept request
 */
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ListenThread implements Runnable{
	private ServerSocket serverSocket;
	private MessagePasser messagePasser = null;
	
	public ListenThread(int portNumber, MessagePasser messagePasser) {
		try {
			this.serverSocket = new ServerSocket(portNumber);
			this.messagePasser = messagePasser;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while (true) {
			Socket clientSocket = null;
			try {
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}
			/* Create new thread after accepting a request */
			new Thread(new ServerThread(clientSocket, messagePasser)).start();
		}
	}
}
