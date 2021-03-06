package defaultCommunication;
/*
 * Author: Tao yu, Minglei Chen 
 * 
 * Node data struction
 */
public class Node {
    protected String node_name;
    protected String node_ip;
    protected int node_port;
    protected int node_index;
    
    public Node(String name, String ip, int port, int index) {
        this.node_name = name;
        this.node_ip = ip;
        this.node_port = port;
        this.node_index = index;
    }

	public int getNode_index() {
		return node_index;
	}

	public void setNode_index(int node_index) {
		this.node_index = node_index;
	}

	public String getNode_name() {
		return node_name;
	}

	public void setNode_name(String node_name) {
		this.node_name = node_name;
	}  
} 