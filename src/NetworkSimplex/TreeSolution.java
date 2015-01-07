package NetworkSimplex;

import java.util.ArrayList;

public class TreeSolution {

	private int[] predecessorArray;

	private int[] depthArray;

	private int[] thread; //corresponds to the preorder array from the tutorial

	private Arc[] L; //the partition where flow equals lower cap
	private ArrayList<Arc2>  L2; //not sure yet which arc type we will use 

	private Arc[] U; //the partition where flow equals upper cap
	private ArrayList<Arc2>  U2;
	
	private Arc[] Tree; //the arcs in the tree
	private ArrayList<Arc2> Tree2;



	//the onstructor will only be used once in the Reader class to construct an intial feasable tree solution
	public TreeSolution(Arc[] L, Node[] VPos, Node[] VNeg[]) {
		this.L = L;
		for(Node node : VPos){

		}


	}

	public TreeSolution(ArrayList<Arc2>  L2, Node[] VPos, Node[] VNeg, int numberOfNodes) {
		this.L2 = L2;
		int kIndex = numberOfNodes +1;	//index of the artificial node
		for(Node node : VPos){
			Arc2 arc = new Arc2(node.getIndex(), kIndex, 0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0);	//add artificial arcs (i,k)
			Tree2.add(arc);
			this.predecessorArray[node.getIndex()] = kIndex;
			this.depthArray[node.getIndex()] = 1;
			this.thread[node.getIndex()] = kIndex;
		};
		
		for(Node node : VNeg){
			Arc2 arc = new Arc2( kIndex,node.getIndex() , 0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0);	//add artificial arcs (k,i)
			Tree2.add(arc);
			this.predecessorArray[node.getIndex()] = kIndex;
			this.depthArray[node.getIndex()] = 1;
			this.thread[node.getIndex()] = kIndex;
		}

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
