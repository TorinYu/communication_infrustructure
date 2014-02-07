package defaultCommunication;
/*
 * Author: Tao yu, Minglei Chen 
 * 
 * Rule data structure
 */
public class Rule {
	protected Action action = null;
	protected String src = null;
	protected String dest = null;
	protected String kind = null;
	protected int seqNum = -1;
	protected Boolean dupe = null;
	
	public Rule(String action, String src, String dest, String kind, Integer seqNum, Boolean dupe) {
		if (action.equalsIgnoreCase("Drop")) {
			this.action = Action.Drop;
		}
		else if (action.equalsIgnoreCase("Delay")) {
			this.action = Action.Delay;
		}
		else if (action.equalsIgnoreCase("Duplicate")) {
			this.action = Action.Duplicate;
		}

	    this.src = src;
	    this.dest = dest;
	    this.kind = kind;
	    //this.dupe = dupe == null ? null : Boolean.parseBoolean(dupe);
	    this.dupe = dupe;
	    this.seqNum = seqNum == null ? -1 : seqNum;
	}
}
