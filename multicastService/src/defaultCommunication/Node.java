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
}