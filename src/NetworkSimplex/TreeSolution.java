package NetworkSimplex;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;

public class TreeSolution {

	private int[] predecessorArray;

	private int[] depthArray;

	private int[] thread; //corresponds to the preorder array from the tutorial

//	private Arc[] L; //the partition where flow equals lower cap
	private ArrayList<Arc2>  L2; //not sure yet which arc type we will use 

//	private Arc[] U; //the partition where flow equals upper cap
	private ArrayList<Arc2>  U2;

//	private Arc[] Tree; //the arcs in the tree
	private ArrayList<Arc2> Tree2 = new ArrayList<Arc2>();

	//	private ArrayList<Double> fairPrices = new ArrayList<Double>(); //the costs in the nodes
	// in the script referred to as y
	private double[] fairPrices;



//	//the onstructor will only be used once in the Reader class to construct an intial feasable tree solution
//	public TreeSolution(Arc[] L, Node[] VPos, Node[] VNeg[]) {
//		this.L = L;
//		for(Node node : VPos){
//
//		}
//	}

	/**
	 * this constructor uses the information that the Reader class connects from the inputfile
	 * and constructs the inital tree solution
	 * therefore an artificial node and artificial arcs are constructed
	 * @param L2
	 * @param nodes
	 * @param numberOfNodes
	 * @param maxCost	that is needed to calculate the costs of the artificial arcs
	 */
	public TreeSolution(ArrayList<Arc2>  L2, Node[] nodes, int numberOfNodes, double maxCost) {
		this.L2 = L2;
		this.U2 = new ArrayList<Arc2>(); //U partition is empty at the beginning
		//		int kIndex = numberOfNodes +1;	//index of the artificial node
		int kIndex = 0;
		double costArtificialArc = 1 + 0.5 * numberOfNodes* maxCost;	//->skript

		this.predecessorArray = new int[numberOfNodes+1];                                                                                                                                                                         
		predecessorArray[kIndex] = -1;

		this.depthArray = new int[numberOfNodes+1];
		depthArray[kIndex]= 0;

		this.thread = new int[numberOfNodes +1];
		thread[kIndex] = 1; //richtig initialisiert?

		this.fairPrices = new double[numberOfNodes+1];
		fairPrices[kIndex] = 0; //this is the one that choose arbritrariliy (n variables, n-1 equations)

		int startNodeIndex;
		int endNodeIndex;
		double flow; //flow is abs(nettodemand)
		for(int i = 1; i<nodes.length; i++){ //start at one bcz at index 0 there is no node to keep it easy i.e. nodeIndex = arrayIndex
			flow = 0;
			Node node = nodes[i];	//could also be null
			if(node == null || node.getNettodemand() >= 0) {
				startNodeIndex = kIndex;
				endNodeIndex = i;
				fairPrices[i] = costArtificialArc;	// initial fair prices 
				if(node !=null) flow = Math.abs(node.getNettodemand());
			}
			else {
				startNodeIndex = i;
				endNodeIndex = kIndex;
				fairPrices[i] = -costArtificialArc; // initial fair prices
				flow = Math.abs(node.getNettodemand());
			}
			//flow has still to be added
			Arc2 arc = new Arc2(startNodeIndex, endNodeIndex, 0, Double.POSITIVE_INFINITY, costArtificialArc, flow);	//add artificial arcs
			Tree2.add(arc);
			this.predecessorArray[i] = kIndex;
			this.depthArray[i] = 1;
			/**
			 * initialization of thread
			 */
			if(i<numberOfNodes)
				this.thread[i] = i+1;	//nodes are traversed in index order
			else 
				this.thread[i] = 0;	//successor of the last node is the root
		}
	}

	/**
	 * 
	 * 
	 */
	public boolean iterate(){
		EnteringArcFinderCandidatesPivotRule finderPivotRule = new EnteringArcFinderCandidatesPivotRule();
		Arc2 enteringArc = finderPivotRule.getEnteringArc();
		System.out.println("Arc (found by pivot rule class): ");
		System.out.println(enteringArc);
		
		EnteringArcFinderFirstRule finderFirstRule = new EnteringArcFinderFirstRule();
		Arc2 enteringArc2 = finderFirstRule.getEnteringArc();
		System.out.println("Arc (found by first rule class: )");
		System.out.println(enteringArc2);

		ArrayList<Integer> pathUV = findPathBetweenUV(enteringArc.getStartNodeIndex(), enteringArc.getEndNodeIndex());
		return false;
	}



	/**
	 * executes one iteration of the algorithm
	 * @return true if the tree solution is not yet optimal, false else
	 * this is just so for the moment, i think we also have to capture cases like unbounded problems and problems without solution
	 */
	public boolean updateTreeSolution(){
		//...

		//this.updateFairPrices();

		this.updateCReduced();
		//..
		return true;
	}

	/**
	 * 
	 * @param leavingArc
	 * @param orientation true if the arc is directed toward the root, false if it is directed away from the root
	 */
	private void updateFairPrices(Arc2 leavingArc, Arc2 enteringArc, boolean orientation){
		double sign = orientation? -1 : 1; 
		double ce = enteringArc.getReducedCosts();
		int f1,f2;
		if(depthArray[leavingArc.getStartNodeIndex()] < depthArray[leavingArc.getEndNodeIndex()]) {
			f1 = leavingArc.getStartNodeIndex();
			f2 = leavingArc.getEndNodeIndex();
		}
		else {
			f1 = leavingArc.getEndNodeIndex();
			f2 = leavingArc.getStartNodeIndex();
		}
		assert depthArray[f2] == depthArray[f1] +1 : "initializing of f1 and f2 in updateFairPrices is wrong";
		//change the above!!!
		int k = f2;
		while(depthArray[k] > depthArray[f2]) {
			fairPrices[k] = fairPrices[k] + sign * ce;
			k = thread[k];
		}
	}

	private void updateCReduced(){

	}

	/**
	 * this method finds a path between the nodes with indexU and indexV
	 * it is used to find the cycle in the tree after adding the entering arc
	 * @param indexU
	 * @param indexV
	 * @return a list that contains the path
	 */
	private ArrayList<Integer> findPathBetweenUV(int indexU, int indexV){

		//maybe use another datastructure here, like a stack or so
		ArrayList<Integer> pathU = new ArrayList<Integer>();
		//		ArrayList<Integer> pathV = new ArrayList<Integer>();
		Stack<Integer> pathV = new Stack<Integer>();

		//initialize so that u is the index with the greater depth
		int u,v;
		if(depthArray[indexU] >= depthArray[indexV]) {
			u = indexU; v = indexV;
		}
		else {
			u = indexV ; v= indexU;
		}

		//climb up the longer path until level of v is reached
		while(depthArray[u] > depthArray[v]){
			pathU.add(u);
			u = predecessorArray[u];
		}

		//climb up on both paths until join is reached
		while(u != v) {
			pathU.add(u);
			pathV.add(v);
			u = predecessorArray[u];
			v = predecessorArray[v];
		}
		//		pathV.pop(); //remove last element v = u
		pathU.add(u);
		pathU.addAll(pathV);
		System.out.println("path between u and v: ");
		System.out.println(pathU);
		return pathU;

	}

	/**
	 * this is an inner class, as such it has access to all class variables and methods of the 
	 * outer class )even if they are private)
	 * it encapsulates the entering arc finding process
	 * Usage: create and EnteringArcFinder class instance and use the getEnteringArc method
	 * there are 2 constructors so far, one without arguments that uses a very simple pivoting rule 
	 * and one with more arguments that uses a more advanced one.
	 * the second one prob doesnt work so far
	 * @author IG
	 *
	 */
	private class EnteringArcFinderCandidatesPivotRule {
		//list of candidates, in order  to not search for new arcs in each iteration
		private LinkedList<Arc2> candidates = new LinkedList<Arc2>();
		//number of arcs that will be put in the list when it is refreshed
		private int filledListSize;
		//numbe of arcs we will choose after the rule of the best merit from the list before it is refreshed
		private int iterations;
		//true if there are not enough arcs left to fill the list with the requested number of arcs
		private boolean noMorecandidates;

		/**
		 * this constructor returns an instance of the e-Arc-Finder that uses the simplest
		 * pivoting rule i.e. return the first discovered arc with CReduced(Arc) < 0
		 */
		public EnteringArcFinderCandidatesPivotRule(){
			this.filledListSize = 1;
			this.iterations =1;
			this.candidates= findCandidatesForEnteringArc(true, this.filledListSize);
			this.noMorecandidates = this.candidates.size() < this.filledListSize ? true : false;

		}
		/**
		 * uses a more advanced pivoting rule, for more details see the grey book
		 * @param filledListSize the size of the retrieved list
		 * @param iterations number of arcs that will be chosen from the list
		 */
		public EnteringArcFinderCandidatesPivotRule(int filledListSize, int iterations) {
			this.filledListSize = filledListSize;
			this.iterations = iterations;
			this.candidates = findCandidatesForEnteringArc(true, this.filledListSize);	//when this class is instantiated it is the first run
			this.noMorecandidates = this.candidates.size() < this.filledListSize ? true : false; //if the returned list doesnt contain as many arcs
			//as we wanted that means that there are not enough candidates anymore to fullfil the request
		}

		//there will prob be problems when we run out of arcs
		/**
		 * 
		 * @return the entering arc
		 */
		public Arc2 getEnteringArc(){
			if(noMorecandidates && this.candidates.isEmpty()) return null;
			if(!noMorecandidates || this.candidates.size() <= this.filledListSize - this.iterations) {
				this.candidates = findCandidatesForEnteringArc(false, filledListSize);
				this.noMorecandidates = this.candidates.size() < this.filledListSize ? true : false; 
			}
			/**
			 * here we have to find the max value and at the same time aupdate the list
			 * bcz there might be candidates in the list that do not reduce the costs anymore
			 * due to updates in previous iterations
			 */
			return this.candidates.pop();	//this doesnt give back the ARc with the best merit yet, but will soon
			//therefore the datastructre LinkedList will prob be exchanged against a treeset

		}
	}
	/**
	 * inner class that implements the pivoting rule that return the first arc that could be used
	 * its not completely implemented yet.
	 * maybe this class will also provide methods to delete and add arcs to L and U in order to update them
	 * @author IG
	 *
	 */
	private class EnteringArcFinderFirstRule {
		
		private ListIterator<Arc2> LIterator;
		private ListIterator<Arc2> UIterator;
		
		public EnteringArcFinderFirstRule() {
			this.LIterator = L2.listIterator();
			this.UIterator = U2.listIterator();
		}
		
		private Arc2 getEnteringArc() {
			while(LIterator.hasNext()){
				if(LIterator.next().getReducedCosts() < 0)
					return LIterator.next();
			}
			while(UIterator.hasNext()){
				if(UIterator.next().getReducedCosts() > 0)
					return UIterator.next();
			}
			
			return null;
		}
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
				/**
				 * reduced costs are initialized here
				 */
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

	/**
	 * a small helper method for the string representation of the tree solution
	 * @param array
	 * @return
	 */
	private String intArrayToString(int[] array){
		StringBuffer string = new StringBuffer();
		for(int i = 0; i<array.length; i++) {
			string.append("\n");
			string.append(i);
			string.append(": ");
			string.append(array[i]);
		}
		return string.toString();
	}

	/**
	 * returns a string representation of the tree solution
	 */
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

	/**
	 * method to create a String for visualize the graph by highlighting T, L, U
	 * @return String for graphviz
	 */
	public String graphvizString(){
		return null;
	}

	/**
	 * method to create a String for visualize the entering and leaving arc
	 * @return String for graphviz
	 */
	public String graphvizString2(){
		return null;
	}

}
