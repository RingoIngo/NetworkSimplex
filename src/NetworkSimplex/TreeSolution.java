package NetworkSimplex;

import java.util.ArrayList;
import java.util.LinkedList;

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

	//	private ArrayList<Double> fairPrices = new ArrayList<Double>(); //the costs in the nodes
	private double[] fairPrices;



	//the onstructor will only be used once in the Reader class to construct an intial feasable tree solution
	public TreeSolution(Arc[] L, Node[] VPos, Node[] VNeg[]) {
		this.L = L;
		for(Node node : VPos){

		}


	}

	public TreeSolution(ArrayList<Arc2>  L2, Node[] nodes, int numberOfNodes, double maxCost) {
		this.L2 = L2;
		//		int kIndex = numberOfNodes +1;	//index of the artificial node
		int kIndex = 0;
		double costArtificialArc = 1 + 0.5 * numberOfNodes* maxCost;	//->skript

		this.predecessorArray = new int[numberOfNodes+1];
		predecessorArray[kIndex] = -1;

		this.depthArray = new int[numberOfNodes+1];
		depthArray[kIndex]= 0;

		this.thread = new int[numberOfNodes +1];
		thread[kIndex] = numberOfNodes; //richtig initialisiert?

		this.fairPrices = new double[numberOfNodes+1];
		fairPrices[kIndex] = 0; //this is the one that choose arbritrariliy (n variables, n-1 equations)

		int startNodeIndex;
		int endNodeIndex;
		for(int i = 1; i<nodes.length; i++){ //start at one bcz at index 0 there is no node to keep it easy i.e. nodeIndex = arrayIndex
			Node node = nodes[i];	//could also be null
			if(node == null || node.getNettodemand() >= 0) {
				startNodeIndex = kIndex;
				endNodeIndex = i;
				fairPrices[i] = costArtificialArc;
			}
			else {
				startNodeIndex = i;
				endNodeIndex = kIndex;
				fairPrices[i] = -costArtificialArc;
			}
			Arc2 arc = new Arc2(startNodeIndex, endNodeIndex, 0, Double.POSITIVE_INFINITY, costArtificialArc, 0);	//add artificial arcs
			Tree2.add(arc);
			this.predecessorArray[i] = kIndex;
			this.depthArray[i] = 1;
			this.thread[i] = kIndex;
		}
		LinkedList<Arc2> candidates = findCandidatesForEnteringArc(true, 10);
		System.out.println("candidates:");
		System.out.println(candidates);

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

	/**
	 * 
	 * @param firstRun
	 * @param r
	 * @return
	 */
	LinkedList<Arc2> findCandidatesForEnteringArc(boolean firstRun,int r) { //r is the number of candidates that we ll provide in this call
		LinkedList<Arc2> candidates = new LinkedList<Arc2>();
		int i =0;
		for(Arc2 arc : L2){
			int startnode;
			int endnode;
			if(firstRun) {
				startnode = arc.getStartNodeIndex();
				endnode = arc.getEndNodeIndex();
				arc.setReducedCosts(arc.getCost() + fairPrices[startnode] - fairPrices[endnode]);
			}
			if(arc.getReducedCosts()<0){
				candidates.add(arc);
				i++;
			}
			if(i==r) break;
		}
		return candidates;

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
