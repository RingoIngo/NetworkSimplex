package NetworkSimplex;

public class Arc2 {
	
	private int startNodeIndex;
	private int endNodeIndex;
	private double cost;
	private double lowerLimit;
	private double upperLimit;
	private double reducedCosts;
	private double flow;
	
	
	public Arc2(int startNodeIndex, int endNodeIndex, double lowerLimit,
			double upperLimit, double cost, double flow) {
		super();
		this.startNodeIndex = startNodeIndex;
		this.endNodeIndex = endNodeIndex;
		this.cost = cost;
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
		this.flow =flow;
		this.reducedCosts = -1;
	}


	public double getFlow() {
		return flow;
	}


	public void setFlow(double flow) {
		this.flow = flow;
	}


	public int getStartNodeIndex() {
		return startNodeIndex;
	}


	public void setStartNodeIndex(int startNodeIndex) {
		this.startNodeIndex = startNodeIndex;
	}


	public int getEndNodeIndex() {
		return endNodeIndex;
	}


	public void setEndNodeIndex(int endNodeIndex) {
		this.endNodeIndex = endNodeIndex;
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
	
	public String toString(){
		StringBuffer string  = new StringBuffer("startNodeIndex: ");
		string.append(startNodeIndex);
		string.append(" endNodeIndex: ");
		string.append(endNodeIndex);
		string.append(" cost: ");
		string.append(cost);
		string.append(" lowerLimit: ");
		string.append(lowerLimit);
		string.append(" upperLimit: ");
		string.append(upperLimit);
		string.append(" reducedCosts: ");
		string.append(reducedCosts);
		string.append(" flow: ");
		string.append(flow);
		
		return string.toString();
	}

}
