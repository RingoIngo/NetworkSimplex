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
	}
	
	/**
	 * 
	 * 
	 */
	public boolean iterate(){
		EnteringArcFinder finder = new EnteringArcFinder();
		Arc2 enteringArc = finder.getEnteringArc();
		System.out.println("Arc: ");
		System.out.println(enteringArc);
		return false;
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
	 * this is an inner class, as such it has acces to all class variables and methods of the 
	 * outer class )even if they are priate)
	 * it encapsulates the entering arc finding process
	 * Usage: create and EnteringArcFinder class instance and use the getEnteringArc method
	 * there are 2 constructors so far, one without arguments that uses a very simple pivoting rule 
	 * and one with more arguments that uses a more advanced one.
	 * the second one prob doesnt work so far
	 * @author IG
	 *
	 */
	private class EnteringArcFinder {
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
		public EnteringArcFinder(){
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
		public EnteringArcFinder(int filledListSize, int iterations) {
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
			return this.candidates.pop();	//this doesnt give back the ARc with the best merit yet, but will soon
			//therefore the datastructre LinkedList will prob be exchanged against a treeset
				
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
