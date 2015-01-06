package NetworkSimplex;

public class TreeSolution {
	
	private int[] predecessorArray;
	
	private int[] depthArray;
	
	private int[] thread; //read this in some books, not sure yet how to use it
	
	private double[] y; //the node costs
	
	private double[] cReduced ;//the reduced costs for the arcs, calculated from y
	
	//... several other data fields maybe e.g. L, U,x
	
	
	//the onstructor will only be used once in the Reader class to construct an intial feasable tree solution
	public TreeSolution() {
		
	}

	public void setPredecessorArray(int[] predecessorArray) {
		this.predecessorArray = predecessorArray;
	}

	public void setDepthArray(int[] depthArray) {
		this.depthArray = depthArray;
	}

	public void setThread(int[] thread) {
		this.thread = thread;
	}
	
	/**
	 * executes one iteration of the algorithm
	 * @return true if the tree solution is not yet optimal, false else
	 * this is just so for the moment, i think we also have to capture cases like unbounded problems and problems without solution
	 */
	public boolean updateTreeSolution(){
		//...
		
		this.updateY();
		this.updateCReduced();
		//..
		return true;
	}
	
	
	private void updateY(){
		
	}
	
	private void updateCReduced(){
		
	}
	
	//...
	
}
