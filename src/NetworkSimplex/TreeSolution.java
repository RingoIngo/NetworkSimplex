package NetworkSimplex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

public class TreeSolution {

	private int[] predecessorArray;

	private int[] depthArray;

	private int[] thread; // corresponds to the preorder array from the tutorial

	// the partition where flow equals lower cap
	public AdjacencyList L; // not sure yet which arc type we will use

	// /the partition where flow equals upper cap
	public AdjacencyList U;

	// will prob get deleted soon and replaced by some array that store flow,cap
	// and so on;
	private AdjacencyList Tree;

	// private ArrayList<Double> fairPrices = new ArrayList<Double>(); //the
	// costs in the nodes
	// in the script referred to as y
	private double[] fairPrices;

	private EnteringArcFinderCandidatesPivotRule enteringArcFinder;
	
	// eine krücke
	private double epsilon;

	//just to debug
	public int numberOfIterations=0;
	public boolean UWasNotEmptyBefore = false;
	public boolean backwardEdge = false;
	

	/**
	 * this constructor uses the information that the Reader class connects from
	 * the inputfile and constructs the inital tree solution therefore an
	 * artificial node and artificial arcs are constructed
	 * 
	 * @param L
	 * @param nodes
	 * @param numberOfNodes
	 * @param maxCost
	 *            that is needed to calculate the costs of the artificial arcs
	 */
	public TreeSolution(AdjacencyList L, Node[] nodes, int numberOfNodes,
			double maxCost) {
		this.L = L;
		this.U = new AdjacencyList(numberOfNodes + 1); // U partition is empty
		// at the beginning
		// int kIndex = numberOfNodes +1; //index of the artificial node
		this.Tree = new AdjacencyList(numberOfNodes + 1);
		int kIndex = 0;
		double costArtificialArc = 1 + 0.5 * numberOfNodes * maxCost; // ->skript

		this.predecessorArray = new int[numberOfNodes + 1];
		predecessorArray[kIndex] = -1;

		this.depthArray = new int[numberOfNodes + 1];
		depthArray[kIndex] = 0;

		this.thread = new int[numberOfNodes + 1];
		thread[kIndex] = 1; // richtig initialisiert?

		this.fairPrices = new double[numberOfNodes + 1];
		fairPrices[kIndex] = 0; // this is the one that choose arbritrariliy (n
		// variables, n-1 equations)

		int startNodeIndex;
		int endNodeIndex;
		double flow; // flow is abs(nettodemand)
		for (int i = 1; i < nodes.length; i++) { // start at one bcz at index 
			// there is no node to keep
			// it easy i.e. nodeIndex =
			// arrayIndex
			flow = 0;
			Node node = nodes[i]; // could also be null
			//			if (node == null || node.getNettodemand() > 0) {
			//				startNodeIndex = kIndex;
			//				endNodeIndex = i;
			//				fairPrices[i] = costArtificialArc; // initial fair prices
			//				if (node != null)
			//					flow = Math.abs(node.getNettodemand());
			//			} else {
			//				startNodeIndex = i;
			//				endNodeIndex = kIndex;
			//				fairPrices[i] = -costArtificialArc; // initial fair prices
			//				flow = Math.abs(node.getNettodemand());
			//			}
			if (node == null || node.getNettodemand() <= 0) {
				startNodeIndex = i;
				endNodeIndex = kIndex;
				fairPrices[i] = -costArtificialArc; // initial fair prices
				if (node != null)
					flow = Math.abs(node.getNettodemand());
			} else {
				startNodeIndex = kIndex;
				endNodeIndex = i;
				fairPrices[i] = costArtificialArc; // initial fair prices
				flow = Math.abs(node.getNettodemand());
			}
			Arc arc = new Arc(startNodeIndex, endNodeIndex, 0,
					Double.POSITIVE_INFINITY, costArtificialArc, flow); // add
			// artificial
			// arcs
			this.Tree.addEdge(arc);

			this.predecessorArray[i] = kIndex;
			this.depthArray[i] = 1;
			/**
			 * initialization of thread
			 */
			if (i < numberOfNodes)
				this.thread[i] = i + 1; // nodes are traversed in index order
			else
				this.thread[i] = 0; // successor of the last node is the root
		}
		// System.out.print(this.graphvizStringTree());
		// System.out.print(this.graphvizStringTLU());
		this.enteringArcFinder = new EnteringArcFinderCandidatesPivotRule(40,20);
	}

	/**
	 * 
	 * 
	 */
	public boolean iterate() {
		++numberOfIterations;
		// EnteringArcFinderCandidatesPivotRule finderPivotRule = new
		// EnteringArcFinderCandidatesPivotRule();
		// Arc2 enteringArc = finderPivotRule.getEnteringArc();

		// dont init each time
		EnteringArcFinderFirstRule finderFirstRule = new EnteringArcFinderFirstRule();
//		//EnteringArcObject enteringArcObject = finderFirstRule.getEnteringArcObject();	//this method is slower with standard1
		EnteringArcObject enteringArcObject = finderFirstRule.getMaxEnteringArcObject();
//		EnteringArcObject enteringArcObject = enteringArcFinder.getEnteringArc();
		if(enteringArcObject == null) return false; //no more entering arcs can be found 
		Arc enteringArc = enteringArcObject.getEnteringArc();
		System.out.println("Arc (found by first rule class: )");
		System.out.println(enteringArc);

		LinkedList<FlowFinderObject> pathUV = findPathBetweenUV(enteringArc);
		controlCircle(pathUV);
		System.out.println("\nEpsilon:");
		System.out.println(epsilon);
		System.out.println("changeFlowFindLeavingArc:");
		Arc leavingArc = changeFlowFindLeaving(pathUV, epsilon);
		System.out.println("updateLTU");
		updateLTU(leavingArc, enteringArc);
		//		System.out.println("updateFairPrices");
		//		updateFairPrices(leavingArc, enteringArc);
		System.out.println("updateThreadPredDepthFairPrices");
		updateThreadPredDepthFairPrices(enteringArc, leavingArc);

		System.out.println("\nleavingarc:");
		System.out.println(leavingArc);
		System.out.println(this.toString());
		assertReducedCostZeroInTree();
		assertEachNodeInThreadOnlyVisitedOnce();
		assertDepthOfSuccesorGreater();
		assertPred();
		return true;
	}
	/**
	 * checks if the cycle is actually a cycle and if the orientations
	 * if the arcs in it are correct
	 * @param pathUV
	 */
	private void controlCircle(LinkedList<FlowFinderObject> pathUV){
		Iterator<FlowFinderObject> iterator2 = pathUV.iterator();
		FlowFinderObject object1 = iterator2.next();
		int join, scheitel;
		if(object1.forwardEdge) join = object1.leavingArc.getStartNodeIndex();
		else join = object1.leavingArc.getEndNodeIndex();
		scheitel = join;
		Iterator<FlowFinderObject> iterator = pathUV.iterator();
		FlowFinderObject object = null;
		while(iterator.hasNext()){
			object= iterator.next();
			if(join == object.leavingArc.getStartNodeIndex()){
				assert object.forwardEdge;
				join = object.leavingArc.getEndNodeIndex();
			}
			else if(join == object.leavingArc.getEndNodeIndex()){
				join = object.leavingArc.getStartNodeIndex();
				assert !object.forwardEdge;
			}
			else assert false : "no circle"+ pathUV+ "\n"+join;
		}
		assert scheitel==join: "no circle"+pathUV+ "\n"+join;
	}
	private void assertPred(){
		System.out.println("control Pred");
		for(int i =0;i<this.predecessorArray.length;i++){
			int p=i;
			while(p!=0){
				p=this.predecessorArray[p];
			}
		}
	}
	/**
	 * tests if the solution still contains artificial arcs
	 * @return
	 */
	public boolean solutionFeasable(){
		Iterator<Arc> iterator = this.Tree.iterator();
		Arc arc;
		while(iterator.hasNext()){
			arc = iterator.next();
			if(arc.getStartNodeIndex()==0||arc.getEndNodeIndex()==0)
				if(arc.getFlow()!=0){
					System.out.println("artificial arc with flow:");
					System.out.println(arc);
					return false;
				}
		}
		iterator = this.U.iterator();
		while(iterator.hasNext()){
			arc = iterator.next();
			if(arc.getStartNodeIndex()==0||arc.getEndNodeIndex()==0){
				System.out.println("artificial arc with flow:");
				System.out.println(arc);
				return false;
			}
		}
		return true;
	}

	/**
	 * tests if the statement d(p(i)) +1 == d(i) holds for all nodes
	 */
	private void assertDepthOfSuccesorGreater(){
		for(int i=1; i<this.predecessorArray.length; i++){
			assert this.depthArray[this.predecessorArray[i]] +1 == this.depthArray[i] : "sth wrong with depthArray";
		}

	}
	/**
	 * asserts that each node is only visited once when performing 
	 * a depth first search with thread array
	 */
	private void assertEachNodeInThreadOnlyVisitedOnce(){
		//array is initialized with false
		boolean[] visited = new boolean[this.thread.length];
		for(int i=0; i<thread.length; i++){
			assert visited[this.thread[i]] == false:"sth wrong with thread";
			visited[this.thread[i]]= true;	
		}
	}
	/**
	 * tests if the reduced costs of all arcs in T are zero
	 */
	private void assertReducedCostZeroInTree(){
		Iterator<Arc> iterator = this.Tree.iterator();
		Arc arc;
		int startnode, endnode;
		while(iterator.hasNext()){
			arc = iterator.next();
			startnode = arc.getStartNodeIndex();
			endnode = arc.getEndNodeIndex();
			arc.setReducedCosts(arc.getCost() + fairPrices[startnode]
					- fairPrices[endnode]);
			assert arc.getReducedCosts() == 0 : "sth wrong with fair prices";
		}
	}

	/**
	 * adds the entering arc to T and removes it from L/U
	 * and deletes the leaving arc from T and adds it to L/U
	 * @param leavingArc
	 * @param enteringArc
	 */
	private void updateLTU(Arc leavingArc, Arc enteringArc) {
		Tree.addEdge(enteringArc);
		Tree.removeEdge(leavingArc);
		if (enteringArc.getReducedCosts() < 0)
			L.removeEdge(enteringArc);
		else
			U.removeEdge(enteringArc);
		assert leavingArc.getFlow() == leavingArc.getUpperLimit()||leavingArc.getFlow() == leavingArc.getLowerLimit() :"leavingArc did not reach upper or lower cap!";
		if (leavingArc.getFlow() == leavingArc.getUpperLimit()){
			U.addEdge(leavingArc);
			UWasNotEmptyBefore = true;
		}
		else {
			L.addEdge(leavingArc);
			assert leavingArc.getFlow() == leavingArc.getLowerLimit();
		}
	}


	/**
	 * a method to update the thread-array
	 * 
	 * @param enteringArc
	 *            The entering arc in the current iteration
	 * @param leavingArc
	 *            The leaving arc in the current iteration
	 */

	private void updateThreadPredDepthFairPrices(Arc enteringArc, Arc leavingArc) {
		if(enteringArc == leavingArc) return;
		int node, e1, e2, f1, f2, a, b, i, j, k, r;
		int sign;
		double ce = enteringArc.getReducedCosts();

		// f has the two endpoints f1 and f2 with f2 is in S and f1 is not in S
		// (that would be the case when d(f2) > d(f1) )
		if (depthArray[leavingArc.getEndNodeIndex()] > depthArray[leavingArc
		                                                          .getStartNodeIndex()]) {
			f1 = leavingArc.getStartNodeIndex();
			f2 = leavingArc.getEndNodeIndex();
		} else {
			f1 = leavingArc.getEndNodeIndex();
			f2 = leavingArc.getStartNodeIndex();
		}

		// e has the two endpoints e1 and e2 with e2 is in S and e1 is not in S
		node = enteringArc.getStartNodeIndex();
		// check if the startnode is in S
		while ((node != f2) && (node != 0)) { // if node is in S then there is
			// a path from node to f2 on the
			// way from e to the root
			node = this.predecessorArray[node];
		}
		if (node == f2) {
			e2 = enteringArc.getStartNodeIndex();
			e1 = enteringArc.getEndNodeIndex();
			sign = -1;

		} else {
			e1 = enteringArc.getStartNodeIndex();
			e2 = enteringArc.getEndNodeIndex();
			sign = 1;
		}

		// 1. initialize
		a = f1;
		while (this.thread[a] != f2) {
			a = this.thread[a];
		}
		b = this.thread[e1];
		i = e2;

		//calculate c1 for depth update (c1 is the constant used for S1)
		int c = depthArray[e1] - depthArray[e2] +1;

		//		this.depthArray[i] = this.depthArray[i] + c;	//update depthArray in i ( = v1 = e2)
		this.fairPrices[i] = fairPrices[i] + sign * ce;
		// 2. finding the last node k in S_1 and initialize the value of r
		k = i;
		while (this.depthArray[this.thread[k]] > this.depthArray[i]) {
			k = this.thread[k];
			this.depthArray[k] = this.depthArray[k] + c;	//update depthArray in S1 except for v1
			this.fairPrices[k] = fairPrices[k] + sign * ce;
		}
		r = this.thread[k];
		//update AFTER while!!
		this.depthArray[i] = this.depthArray[i] + c;	//update depthArray in i ( = v1 = e2)

		int pred = e1;
		// 3. if we are at the end of S* (i.e. being at the last element
		// of the thread-Array within the subtree with root f2 -> i == f2 ),
		// remove S and insert S*
		while (i != f2) {
			// 4. climb up one step the pivot stem and update thread[k]
			j = i;
			i = this.predecessorArray[i];
			this.predecessorArray[j] = pred;
			pred = j;
			//			this.predecessorArray[i] = j; // update (swap) the predecessors
			this.thread[k] = i;

			//update c (the constant used to update depthArray)
			c = c +2;
			this.depthArray[i] = this.depthArray[i] + c;	//update depthArray in i
			this.fairPrices[i] = fairPrices[i] + sign * ce;
			// 5. find the last node k in the left part of S_t
			k = i;
			while (this.thread[k] != j) {
				k = this.thread[k];
				this.depthArray[k] = this.depthArray[k] +c;	//update depthArray in the left part of S_t
				this.fairPrices[k] = fairPrices[k] + sign * ce;
			}

			// 6. if the right part of S_t is not empty we update thread(k) and
			// search the last node k in S_t
			// At the end we update r.
			if (this.depthArray[r] +c > this.depthArray[i]) {	//we add the constant added to depthArray[i] also to depthArray[r]
				this.thread[k] = r;								//so that the inequation still gives us the right result
				while (this.depthArray[this.thread[k]] + c > this.depthArray[i]) {		//same here
					k = this.thread[k];		
					this.depthArray[k] = this.depthArray[k] +c;	//update depthArray in the right part of S_t
					this.fairPrices[k] = fairPrices[k] + sign * ce;
				}
				//i put this inside the if statement...?
				r = this.thread[k];
			}
		}
		this.predecessorArray[i]= pred;
		// execution of 3.
		this.thread[e1] = e2;
		this.predecessorArray[e2] = e1; // update pred(e2)
		if (e1 != a) {
			this.thread[k] = b;
			//that has to be in the if statement!
			this.thread[a] = r;
		} else {
			this.thread[k] = r;
		}

	}

	/**
	 * this method finds a path between the nodes with indexU and indexV it is
	 * used to find the cycle in the tree after adding the entering arc
	 * 
	 * @param indexU
	 * @param indexV
	 * @return a list that contains the path
	 */
	private LinkedList<FlowFinderObject> findPathBetweenUV(Arc enteringArc) {
		int indexU = enteringArc.getStartNodeIndex();
		int indexV = enteringArc.getEndNodeIndex();

		LinkedList<FlowFinderObject> arcPathU = new LinkedList<FlowFinderObject>();

		// maybe use another datastructure here, like a stack or so
		ArrayList<Integer> pathU = new ArrayList<Integer>();
		// ArrayList<Integer> pathV = new ArrayList<Integer>();
		Stack<Integer> pathV = new Stack<Integer>();
		// the flow change epsilon
		//		double epsilon = Double.POSITIVE_INFINITY;

		// initialize so that u is the index with the greater depth
		int u, v;
		if (depthArray[indexU] >= depthArray[indexV]) {
			u = indexU;
			v = indexV;
		} else {
			u = indexV;
			v = indexU;
		}

		FlowFinderObject flowFinder;
		boolean forwardBefore = enteringArc.getReducedCosts() < 0 ? true
				: false;
		boolean uWasStart = indexU == u;

		boolean addUFirst;
		if (forwardBefore) {
			if (uWasStart)
				addUFirst = true;
			else
				addUFirst = false;
		} else {
			if (uWasStart)
				addUFirst = false;
			else
				addUFirst = true;
		}

		// climb up the longer path until level of v is reached
		double enteringEpsilon;
		if (forwardBefore)
			enteringEpsilon = enteringArc.getUpperLimit()
			- enteringArc.getFlow();
		else
			enteringEpsilon = enteringArc.getFlow()
			- enteringArc.getLowerLimit();
		FlowFinderObject enteringFlowFinderObject = new FlowFinderObject(
				enteringArc, forwardBefore, enteringEpsilon);
		arcPathU.add(enteringFlowFinderObject);
		double epsilon = enteringEpsilon;
		while (depthArray[u] > depthArray[v]) {
			pathU.add(u);
			flowFinder = getPossibleFlowChange(u, predecessorArray[u],
					uWasStart, forwardBefore);
			epsilon = Math.min(epsilon, flowFinder.epsilon);
			//hmmmmmm
			uWasStart = flowFinder.leavingArc.getStartNodeIndex() == predecessorArray[u] ? true
					: false;
			forwardBefore = flowFinder.forwardEdge;
			u = predecessorArray[u];
			if (addUFirst)
				arcPathU.addFirst(flowFinder);
			else
				arcPathU.add(flowFinder);
		}

		boolean forwardBeforeV = enteringArc.getReducedCosts() < 0 ? true
				: false;
		boolean vWasStart = indexU == v;
		// climb up on both paths until join is reached
		while (u != v) {

			pathU.add(u);
			flowFinder = getPossibleFlowChange(u, predecessorArray[u],
					uWasStart, forwardBefore);
			epsilon = Math.min(epsilon, flowFinder.epsilon);
			uWasStart = flowFinder.leavingArc.getStartNodeIndex() == predecessorArray[u] ? true
					: false;
			forwardBefore = flowFinder.forwardEdge;
			if (addUFirst)
				arcPathU.addFirst(flowFinder);
			else
				arcPathU.add(flowFinder);
			u = predecessorArray[u];

			pathV.add(v);
			flowFinder = getPossibleFlowChange(v, predecessorArray[v],
					vWasStart, forwardBeforeV);
			epsilon = Math.min(epsilon, flowFinder.epsilon);
			vWasStart = flowFinder.leavingArc.getStartNodeIndex() == predecessorArray[v] ? true
					: false;
			forwardBeforeV = flowFinder.forwardEdge;
			if (addUFirst)
				arcPathU.add(flowFinder);
			else
				arcPathU.addFirst(flowFinder);
			v = predecessorArray[v];
		}
		pathU.add(u);
		pathU.addAll(pathV);

		System.out.println("\n\n\nthe cycle starting at the scheitel and in orientation direction:");
		System.out.println(arcPathU);

		this.epsilon = epsilon;
		return arcPathU;

	}

	/**
	 * finds out the orientation of edge {u,Pu} in the cycle
	 * and how much the flow can be increased/decreased
	 * @param u
	 * @param Pu
	 * @param uWasStart
	 * @param forwardBefore
	 * @return
	 */
	private FlowFinderObject getPossibleFlowChange(int u, int Pu,
			boolean uWasStart, boolean forwardBefore) {
		Arc leavingArc = Tree.getEdgeInTree(u, Pu);
		boolean sameDirection;
		if (leavingArc.getStartNodeIndex() == u) {
			// <--u-->Pu
			if (uWasStart)
				sameDirection = false;
			// -->u-->Pu
			else
				sameDirection = true;
		}
		// u is end node
		else {
			// <--u<--Pu
			if (uWasStart)
				sameDirection = true;
			// -->u<--Pu
			else
				sameDirection = false;
		}
		// the edge examined before and this edge BOTH belong to either F or B
		// (forward edges or backwar edges)
		if (sameDirection) {
			// both edges belong to F
			if (forwardBefore)
				return new FlowFinderObject(leavingArc, true,
						leavingArc.getUpperLimit() - leavingArc.getFlow());
			// both edges belong to B
			else
				return new FlowFinderObject(leavingArc, false,
						leavingArc.getFlow() - leavingArc.getLowerLimit());
		}
		// edges belong to different partitions F or B
		else {
			// the edge examined before was a forward edge --> this one is a
			// backward edge
			if (forwardBefore)
				return new FlowFinderObject(leavingArc, false, leavingArc.getFlow()
						- leavingArc.getLowerLimit());
			// the other way round
			else
				return new FlowFinderObject(leavingArc, true,
						leavingArc.getUpperLimit() - leavingArc.getFlow());
		}
	}

	/**
	 * this is an inner class, as such it has access to all class variables and
	 * methods of the outer class )even if they are private) it encapsulates the
	 * entering arc finding process Usage: create and EnteringArcFinder class
	 * instance and use the getEnteringArc method there are 2 constructors so
	 * far, one without arguments that uses a very simple pivoting rule and one
	 * with more arguments that uses a more advanced one. the second one prob
	 * doesnt work so far
	 * 
	 * @author IG
	 * 
	 */
	private class EnteringArcFinderCandidatesPivotRule {
		// list of candidates, in order to not search for new arcs in each
		// iteration
		private LinkedList<EnteringArcObject> candidates = new LinkedList<EnteringArcObject>();
		// number of arcs that will be put in the list when it is refreshed
		private int filledListSize;
		// numbe of arcs we will choose after the rule of the best merit from
		// the list before it is refreshed
		private int iterations;
		// true if there are not enough arcs left to fill the list with the
		// requested number of arcs
		private boolean phase1= true;

		private int startSearchNodeIndex=0;
		private boolean isL = true;

		/**
		 * this constructor returns an instance of the e-Arc-Finder that uses
		 * the simplest pivoting rule i.e. return the first discovered arc with
		 * CReduced(Arc) < 0
		 */
		public EnteringArcFinderCandidatesPivotRule() {
			this.filledListSize = 1;
			this.iterations = 1;
			//we start the search in node 0 in L
			this.candidates = findCandidatesForEnteringArc();
		}

		/**
		 * uses a more advanced pivoting rule, for more details see the grey
		 * book
		 * 
		 * @param filledListSize
		 *            the size of the retrieved list
		 * @param iterations
		 *            number of arcs that will be chosen from the list
		 */
		public EnteringArcFinderCandidatesPivotRule(int filledListSize,
				int iterations) {
			this.filledListSize = filledListSize;
			this.iterations = iterations;
			this.candidates = findCandidatesForEnteringArc(); 

		}

		// there will prob be problems when we run out of arcs
		/**
		 * 
		 * @return the entering arc
		 */
		public EnteringArcObject getEnteringArc() {
			if(!phase1) return new EnteringArcFinderFirstRule().getEnteringArcObject();
			Arc dummyArc = new Arc(0, 0, 0, 0, 0, 0); //a dummy
			dummyArc.setReducedCosts(0);
			EnteringArcObject candidate = new EnteringArcObject(dummyArc, false, false); //a dummy
			Iterator<EnteringArcObject> candIterator;
			EnteringArcObject arcObject;
			Arc enteringArc;
			int index = 0;	//counter
			int removeIndex = index;;
			for(int i=1; i<=2;i++){
				candIterator = this.candidates.iterator();
				while(candIterator.hasNext()){
					arcObject = candIterator.next();
					enteringArc = arcObject.getEnteringArc();
					++index;
					enteringArc.setReducedCosts(updateRedCostsOfOneArc(enteringArc));//update list entry before compare
					if(enteringArc.getReducedCosts()<0 && candidate.isL() || enteringArc.getReducedCosts() > 0 && candidate.isU()){
						if(Math.abs(enteringArc.getReducedCosts())>Math.abs(enteringArc.getReducedCosts())){
							candidate = arcObject;
							removeIndex = index;
						}
					}
				}
				//if arc is not dummy anymore
				if (candidate.getEnteringArc().getUpperLimit()!=0){
					this.candidates.remove(removeIndex);
					//if we need new arcs --> refresh
					if(this.candidates.size() < this.filledListSize - this.iterations){
						this.candidates = findCandidatesForEnteringArc();
					}
					return candidate;
				}
				else 
					this.candidates = findCandidatesForEnteringArc();
			}
			return new EnteringArcFinderFirstRule().getEnteringArcObject();
		}

		/**
		 * 
		 * @param filledListSize number of provided candidates
		 * @param startSearchNodeIndex
		 * @return
		 */
		LinkedList<EnteringArcObject> findCandidatesForEnteringArc() {
			if(startSearchNodeIndex>= predecessorArray.length) startSearchNodeIndex =0;
			LinkedList<EnteringArcObject> candidates = new LinkedList<EnteringArcObject>();
			//init where we want to start the search
			Iterator<Arc> iterator = isL? L.iterator(startSearchNodeIndex) : U.iterator(startSearchNodeIndex);
			Arc arc;
			//we do that three times
			//first from where we left, then the complete other list and then the first one again
			//bcz we might have skipped the beginning
			for(int i = 1; i<=3; i++){
				while(iterator.hasNext()){
					arc = iterator.next();
					//update reduced costs
					arc.setReducedCosts(updateRedCostsOfOneArc(arc));
					if((arc.getReducedCosts()<0 && isL) || (arc.getReducedCosts() > 0 && !isL))
						candidates.add(new EnteringArcObject(arc, isL, !isL));
					if(candidates.size() == this.filledListSize) {
						this.startSearchNodeIndex = arc.getStartNodeIndex()+1;
						return candidates;
					}
				}
				isL=!isL;
				//search in the other partition and start at node 0
				iterator = isL? L.iterator(0) : U.iterator(0);
			}
			//list is not full
			this.phase1 = false;
			return candidates;

		}
	}

	/**
	 * inner class that implements the pivoting rule that return the first arc
	 * that could be used its not completely implemented yet. maybe this class
	 * will also provide methods to delete and add arcs to L and U in order to
	 * update them
	 * 
	 * @author IG
	 * 
	 */
	private class EnteringArcFinderFirstRule {

		private Iterator<Arc> LIterator;
		private Iterator<Arc> UIterator;
		// just for testing

		private Arc arc;

		public EnteringArcFinderFirstRule() {
			this.LIterator = L.iterator();
			this.UIterator = U.iterator();

			// assert U is empty

		}

		private EnteringArcObject getMaxEnteringArcObject(){
			Arc maxArc = new Arc(0, 0, 0, 0, 0, 0);//create dummy arc with reduced costs zero
			maxArc.setReducedCosts(0);
			boolean L = false;
			boolean U = false;

			while (LIterator.hasNext()) {
				arc = LIterator.next();
				arc.setReducedCosts(updateRedCostsOfOneArc(arc));
				if (arc.getReducedCosts() < maxArc.getReducedCosts()){
					maxArc = arc;
					L = true;
					U = false;
				}
			}
			while (UIterator.hasNext()) {
				arc = UIterator.next();
				arc.setReducedCosts(updateRedCostsOfOneArc(arc));
				if (arc.getReducedCosts() > Math.abs(maxArc.getReducedCosts())){
					maxArc = arc;
					L = false;
					U = true;
				}
			}
			if(maxArc.getReducedCosts()!=0) {
				assert !(L==false &&U==false) : "get max edge is wrong! in EnteringArcFinder";
				//				assert maxArc.getReducedCosts()<0;//for standard only
				return new EnteringArcObject(maxArc, L, U);
			}
			return null;
		}

		private EnteringArcObject getEnteringArcObject() {
			while (LIterator.hasNext()) {
				arc = LIterator.next();
				arc.setReducedCosts(updateRedCostsOfOneArc(arc));
				if (arc.getReducedCosts() < 0)
					return new EnteringArcObject(arc, true, false);
			}
			while (UIterator.hasNext()) {
				arc = UIterator.next();
				arc.setReducedCosts(updateRedCostsOfOneArc(arc));
				if (arc.getReducedCosts() > 0)
					return new EnteringArcObject(arc, false, true);
			}

			return null;
		}
	}


	private Arc changeFlowFindLeaving(LinkedList<FlowFinderObject> cycle,
			double epsilon) {
		Iterator<FlowFinderObject> iterator = cycle.iterator();
		FlowFinderObject flowFinder=null;
		FlowFinderObject leavingArcFlowFinder = null;
		Arc leavingArc = null;
		while (iterator.hasNext()) {
			flowFinder = iterator.next();
			double sign = 1;
			if (!flowFinder.forwardEdge)
				sign = -1;
			flowFinder.leavingArc.setFlow(flowFinder.leavingArc.getFlow()
					+ sign * epsilon);
			assert flowFinder.leavingArc.getFlow()<=flowFinder.leavingArc.getUpperLimit() : "flow was increased above upper limit!";
			assert flowFinder.leavingArc.getFlow()>=flowFinder.leavingArc.getLowerLimit() : "flow was decreased under lower limit!";
			//			if (flowFinder.forwardEdge) {
			//				if (flowFinder.leavingArc.getFlow() == flowFinder.leavingArc
			//						.getUpperLimit())
			//					leavingArc = flowFinder.leavingArc;
			//			} else if (flowFinder.leavingArc.getFlow() == flowFinder.leavingArc
			//					.getLowerLimit())
			//				leavingArc = flowFinder.leavingArc;
			if (flowFinder.forwardEdge) {
				if (flowFinder.leavingArc.getFlow() == flowFinder.leavingArc.getUpperLimit()){
					leavingArc = flowFinder.leavingArc;
					leavingArcFlowFinder = flowFinder;
				}
			} else {
				if (flowFinder.leavingArc.getFlow() == flowFinder.leavingArc.getLowerLimit()){
					leavingArc = flowFinder.leavingArc;
					leavingArcFlowFinder = flowFinder;
				}
			}
		}
		//		assert !leavingArcFlowFinder.forwardEdge : "this edge should be in U "+ leavingArcFlowFinder;
		//		assert leavingArcFlowFinder.leavingArc.getFlow() == 0;
		return leavingArc;
	}

	/**
	 * 
	 * @param arc
	 * @return
	 */
	public double updateRedCostsOfOneArc(Arc arc){
		int startnode = arc.getStartNodeIndex();
		int endnode = arc.getEndNodeIndex();
		return arc.getCost() + fairPrices[startnode] - fairPrices[endnode];
	}

	/**
	 * calculates the costs of the current flow
	 * @return
	 */
	public double getCosts() {
		double costs = 0;
		Iterator<Arc> iterator = L.iterator();
		Arc arc;
		while(iterator.hasNext()){
			arc = iterator.next();
			costs = costs + arc.getFlow() * arc.getCost();
		}

		iterator = U.iterator();
		while(iterator.hasNext()){
			arc = iterator.next();
			costs = costs + arc.getFlow() * arc.getCost();
		}

		iterator = Tree.iterator();
		while(iterator.hasNext()){
			arc = iterator.next();
			costs = costs + arc.getFlow() * arc.getCost();
		}
		return costs;
	}

	/**
	 * a small helper method for the string representation of the tree solution
	 * 
	 * @param array
	 * @return
	 */
	private String intArrayToString(int[] array) {
		StringBuffer string = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			string.append("\n");
			string.append(i);
			string.append(": ");
			string.append(array[i]);
		}
		return string.toString();
	}

	//TODO : make one method generic
	private String doubleArrayToString(double[] array) {
		StringBuffer string = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
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

		string.append("\nfair prices Array: ");
		string.append(doubleArrayToString(fairPrices));

		string.append("\n\nL: ");
		string.append(L);

		string.append("\n\nU: ");
		string.append(U);

		string.append("\n\nTree: ");
		string.append(Tree);

		return string.toString();
	}

	/**
	 * method to create a String for visualize the Treesolution arc description:
	 * [l / x / u] with l = lower limit, x = current flow, u = upper limit
	 * 
	 * @return String for graphviz
	 */
	public String graphvizStringTree() {
		StringBuffer string = new StringBuffer(
				"\n Treesolution String for GRAPHVIZ: \n	\n digraph Treesolution { \n	node [shape = circle]; ");

		Iterator<Arc> iteratorT = Tree.iterator();

		int lastIndexNodes = this.thread.length - 1;
		// int lastIndexArcs = Tree.size();
		Arc arc = new Arc(0, 0, 0., 0., 0., 0.);
		int startIndex = 0;
		int endIndex = 0;

		// write all node (one node for each index)
		for (int i = 1; i <= lastIndexNodes; i++) {
			string.append(i);
			string.append("; ");
		}
		string.append("\n");

		// write all arcs of the Tree

		while (iteratorT.hasNext()) {
			// for (int i = lastIndexArcs - 1; i >= 0; i--)

			arc = iteratorT.next();
			startIndex = arc.getStartNodeIndex();
			endIndex = arc.getEndNodeIndex();
			string.append(startIndex);
			string.append("->"); // write the arcs like " 1->2 "...it means that
			// there is an arc from 1 to 2
			string.append(endIndex);
			string.append(" [ label = \"[");
			string.append(arc.getLowerLimit());
			string.append(" / ");
			string.append(arc.getFlow()); // write the current flow to the arc
			// like " 1->2 [ label = [l/x/u] ];
			string.append(" / ");
			string.append(arc.getUpperLimit());
			string.append(" / ");
			string.append(arc.getCost());
			string.append(" ]\" ]; \n");
		}
		string.append("} \n \n");

		return string.toString();
	}

	/**
	 * method to create a String for visualize the data structure T,L,U The arcs
	 * of T will be black, the arcs of L will be yellow and the arcs of U will
	 * be blue
	 * 
	 * @return String for graphviz
	 */
	public String graphvizStringTLU() {
		StringBuffer string = new StringBuffer(
				"\n T,L,U String for GRAPHVIZ: \n	\n digraph TLU { \n	node [shape = circle]; ");

		Iterator<Arc> iteratorT = Tree.iterator();
		Iterator<Arc> iteratorL = L.iterator();
		Iterator<Arc> iteratorU = U.iterator();

		int lastIndexNodes = this.thread.length - 1;
		// int lastIndexArcsT = Tree.size();
		// int lastIndexArcsL = L.size();
		// int lastIndexArcsU = U.size();
		Arc arc = new Arc(0, 0, 0., 0., 0., 0.);
		int startIndex = 0;
		int endIndex = 0;

		// write all node (one node for each index)
		for (int i = 1; i <= lastIndexNodes; i++) {
			string.append(i);
			string.append("; ");
		}
		string.append("\n");

		// write all arcs of the Tree. They will be black.
		while (iteratorT.hasNext()) {
			// for (int i = lastIndexArcsT - 1; i >= 0; i--) {

			arc = iteratorT.next();
			startIndex = arc.getStartNodeIndex();
			endIndex = arc.getEndNodeIndex();
			string.append(startIndex);
			string.append("->"); // write the arcs like " 1->2 "...it means that
			// there is an arc from 1 to 2
			string.append(endIndex);
			string.append(" [ label = \"[");
			string.append(arc.getLowerLimit());
			string.append(" / ");
			string.append(arc.getFlow()); // write the current flow to the arc
			// like " 1->2 [ label = [l/x/u] ];
			string.append(" / ");
			string.append(arc.getUpperLimit());
			string.append(" / ");
			string.append(arc.getCost());
			string.append(" ]\" ]; \n");
		}

		// write all arcs of L. They will be yellow
		while (iteratorL.hasNext()) {
			// for (int i = lastIndexArcsL - 1; i >= 0; i--) {

			arc = iteratorL.next();
			startIndex = arc.getStartNodeIndex();
			endIndex = arc.getEndNodeIndex();
			string.append(startIndex);
			string.append("->"); // write the arcs like " 1->2 "...it means that
			// there is an arc from 1 to 2
			string.append(endIndex);
			string.append(" [color=yellow, label = \"[");
			string.append(arc.getLowerLimit());
			string.append(" / ");
			string.append(arc.getFlow()); // write the current flow to the arc
			// like " 1->2 [ label = [l/x/u] ];
			string.append(" / ");
			string.append(arc.getUpperLimit());
			string.append(" / ");
			string.append(arc.getCost());
			string.append(" ]\" ]; \n");
		}

		// write all arcs of U. They will be red
		while (iteratorU.hasNext()) {
			// for (int i = lastIndexArcsU - 1; i >= 0; i--) {

			arc = iteratorU.next();
			startIndex = arc.getStartNodeIndex();
			endIndex = arc.getEndNodeIndex();
			string.append(startIndex);
			string.append("->"); // write the arcs like " 1->2 "...it means that
			// there is an arc from 1 to 2
			string.append(endIndex);
			string.append(" [color=blue, label = \"[");
			string.append(arc.getLowerLimit());
			string.append(" / ");
			string.append(arc.getFlow()); // write the current flow to the arc
			// like " 1->2 [ label = [l/x/u] ];
			string.append(" / ");
			string.append(arc.getUpperLimit());
			string.append(" / ");
			string.append(arc.getCost());
			string.append(" ]\" ]; \n");
		}
		string.append("} \n \n");

		return string.toString();
	}

	/**
	 * method to create a String for visualize the datastructure by highlighting
	 * the entering arc. The entering arc will be green.
	 * 
	 * @return String for graphviz
	 */
	public String graphvizStringArcs(Arc enteringArc) {
		StringBuffer string = new StringBuffer(
				"\n Entering Arc String for GRAPHVIZ: \n	\n digraph enteringArc { \n	node [shape = circle]; ");

		Iterator<Arc> iteratorT = Tree.iterator();
		Iterator<Arc> iteratorL = L.iterator();
		Iterator<Arc> iteratorU = U.iterator();

		int lastIndexNodes = this.thread.length - 1;
		// int lastIndexArcsT = Tree.size();
		// int lastIndexArcsL = L.size();
		// int lastIndexArcsU = U.size();
		Arc arc = new Arc(0, 0, 0., 0., 0., 0.);
		int startIndex = 0;
		int endIndex = 0;

		// write all node (one node for each index)
		for (int i = 1; i <= lastIndexNodes; i++) {
			string.append(i);
			string.append("; ");
		}
		string.append("\n");

		// write all arcs of the Tree. They will be black.
		while (iteratorT.hasNext()) {
			// for (int i = lastIndexArcsT - 1; i >= 0; i--) {

			arc = iteratorT.next();
			startIndex = arc.getStartNodeIndex();
			endIndex = arc.getEndNodeIndex();
			string.append(startIndex);
			string.append("->"); // write the arcs like " 1->2 "...it means that
			// there is an arc from 1 to 2
			string.append(endIndex);
			if (arc.equals(enteringArc)) {
				string.append(" [color=green, label = \"[");
			} else {
				string.append(" [ label = \"[");
			}
			string.append(arc.getLowerLimit());
			string.append(" / ");
			string.append(arc.getFlow()); // write the current flow to the arc
			// like " 1->2 [ label = [l/x/u] ];
			string.append(" / ");
			string.append(arc.getUpperLimit());
			string.append(" / ");
			string.append(arc.getCost());
			string.append(" ]\" ]; \n");
		}

		// write all arcs of L. They will be yellow
		while (iteratorL.hasNext()) {
			// for (int i = lastIndexArcsL - 1; i >= 0; i--) {

			arc = iteratorL.next();
			startIndex = arc.getStartNodeIndex();
			endIndex = arc.getEndNodeIndex();
			string.append(startIndex);
			string.append("->"); // write the arcs like " 1->2 "...it means that
			// there is an arc from 1 to 2
			string.append(endIndex);
			if (arc.equals(enteringArc)) {
				string.append(" [color=green, label = \"[");
			} else {
				string.append(" [color=yellow, label = \"["); //
			}
			string.append(arc.getLowerLimit());
			string.append(" / ");
			string.append(arc.getFlow()); // write the current flow to the arc
			// like " 1->2 [ label = [l/x/u/c] ];
			string.append(" / ");
			string.append(arc.getUpperLimit());
			string.append(" / ");
			string.append(arc.getCost());
			string.append(" ]\" ]; \n");
		}

		// write all arcs of U. They will be red
		while (iteratorU.hasNext()) {
			// for (int i = lastIndexArcsU - 1; i >= 0; i--) {

			arc = iteratorU.next();
			startIndex = arc.getStartNodeIndex();
			endIndex = arc.getEndNodeIndex();
			string.append(startIndex);
			string.append("->"); // write the arcs like " 1->2 "...it means that
			// there is an arc from 1 to 2
			string.append(endIndex);
			if (arc.equals(enteringArc)) {
				string.append(" [color=green, label = \"[");
			} else {
				string.append(" [color=blue, label = \"[");
			}
			string.append(arc.getLowerLimit());
			string.append(" / ");
			string.append(arc.getFlow());
			// like " 1->2 [ label = [l/x/u] ];
			string.append(" / ");
			string.append(arc.getUpperLimit());
			string.append(" / ");
			string.append(arc.getCost());
			string.append(" ]\" ]; \n");
		}
		string.append("} \n \n");

		return string.toString();
	}
}
