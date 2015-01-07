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
	private ArrayList<Arc2> Tree2 = new ArrayList<Arc2>();



	//the onstructor will only be used once in the Reader class to construct an intial feasable tree solution
	public TreeSolution(Arc[] L, Node[] VPos, Node[] VNeg[]) {
		this.L = L;
		for(Node node : VPos){

		}


	}

	public TreeSolution(ArrayList<Arc2>  L2, Node[] nodes, int numberOfNodes) {
		this.L2 = L2;
		int kIndex = numberOfNodes +1;	//index of the artificial node
		
		this.predecessorArray = new int[numberOfNodes+2];
		predecessorArray[numberOfNodes] = -1;
		
		this.depthArray = new int[numberOfNodes+2];
		depthArray[numberOfNodes +1]= 0;
		
		this.thread = new int[numberOfNodes +2];
		thread[numberOfNodes +1] = numberOfNodes; //richtig initialisiert?
		
		
		int startNodeIndex;
		int endNodeIndex;
		for(int i = 1; i<nodes.length; i++){ //start at one bcz at index 0 there is no node to keep it easy i.e. nodeIndex = arrayIndex
			Node node = nodes[i];	//could also be null
			if(node == null || node.getNettodemand() >= 0) {
				startNodeIndex = kIndex;
				endNodeIndex = i;
			}
			else {
				startNodeIndex = i;
				endNodeIndex = kIndex;
			}
			Arc2 arc = new Arc2(startNodeIndex, endNodeIndex, 0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0);	//add artificial arcs
			Tree2.add(arc);
			this.predecessorArray[i] = kIndex;
			this.depthArray[i] = 1;
			this.thread[i] = kIndex;
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

	private String intArrayToString(int[] array){
		StringBuffer string = new StringBuffer();
		for(int i = 0; i<array.length; i++) {
			string.append(" ");
			string.append(i);
			string.append(": ");
			string.append(array[i]);
		}
		return string.toString();
	}
	
	
	public String toString() {
		StringBuffer string = new StringBuffer("Predecessor Array: ");
		string.append(intArrayToString(predecessorArray));
		
		string.append("\ndepth Array: ");
		string.append(intArrayToString(depthArray));
		
		string.append("\nthread Array: ");
		string.append(intArrayToString(thread));
		
		
		string.append("\nL2: ");
		string.append(L2);
		
		string.append("\nTree2: ");
		string.append(Tree2);
		
		return string.toString();
	}

}
