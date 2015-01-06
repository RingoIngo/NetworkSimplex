package NetworkSimplex;

public class Arc {
	
	private Node startNode;
	private Node endNode;
	private double cost;
	private double lowerLimit;
	private double upperLimit;
	private double reducedCosts;
	private double flow;
	
	
	public Arc(Node startNode, Node endNode, double cost, double lowerLimit,
			double upperLimit, double flow) {
		super();
		this.startNode = startNode;
		this.endNode = endNode;
		this.cost = cost;
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
		this.flow =flow;
	}


	public double getFlow() {
		return flow;
	}


	public void setFlow(double flow) {
		this.flow = flow;
	}


	public Node getStartNode() {
		return startNode;
	}


	public void setStartNode(Node startNode) {
		this.startNode = startNode;
	}


	public Node getEndNode() {
		return endNode;
	}


	public void setEndNode(Node endNode) {
		this.endNode = endNode;
	}


	public double getCost() {
		return cost;
	}


	public void setCost(double cost) {
		this.cost = cost;
	}


	public double getLowerLimit() {
		return lowerLimit;
	}


	public void setLowerLimit(double lowerLimit) {
		this.lowerLimit = lowerLimit;
	}


	public double getUpperLimit() {
		return upperLimit;
	}


	public void setUpperLimit(double upperLimit) {
		this.upperLimit = upperLimit;
	}


	public double getReducedCosts() {
		return reducedCosts;
	}


	public void setReducedCosts(double reducedCosts) {
		this.reducedCosts = reducedCosts;
	}
	
	

}
