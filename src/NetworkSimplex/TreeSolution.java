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
	private AdjacencyList L; // not sure yet which arc type we will use

	// /the partition where flow equals upper cap
	private AdjacencyList U;

	// will prob get deleted soon and replaced by some array that store flow,cap
	// and so on;
	private AdjacencyList Tree;

	// private ArrayList<Double> fairPrices = new ArrayList<Double>(); //the
	// costs in the nodes
	// in the script referred to as y
	private double[] fairPrices;

	// eine krücke
	private double epsilon;

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
			if (node == null || node.getNettodemand() >= 0) {
				startNodeIndex = kIndex;
				endNodeIndex = i;
				fairPrices[i] = costArtificialArc; // initial fair prices
				if (node != null)
					flow = Math.abs(node.getNettodemand());
			} else {
				startNodeIndex = i;
				endNodeIndex = kIndex;
				fairPrices[i] = -costArtificialArc; // initial fair prices
				flow = Math.abs(node.getNettodemand());
			}
			// flow has still to be added
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
	}

	/**
	 * 
	 * 
	 */
	public boolean iterate() {
		// EnteringArcFinderCandidatesPivotRule finderPivotRule = new
		// EnteringArcFinderCandidatesPivotRule();
		// Arc2 enteringArc = finderPivotRule.getEnteringArc();

		// dont init each time
		EnteringArcFinderFirstRule finderFirstRule = new EnteringArcFinderFirstRule();
//		EnteringArcObject enteringArcObject = finderFirstRule.getEnteringArcObject();
		EnteringArcObject enteringArcObject = finderFirstRule.getMaxEnteringArcObject();
		if(enteringArcObject == null) return false; //no more entering arcs can be found 
		Arc enteringArc = enteringArcObject.getEnteringArc();
		System.out.println("Arc (found by first rule class: )");
		System.out.println(enteringArc);

		LinkedList<FlowFinderObject> pathUV = findPathBetweenUV(enteringArc);
		System.out.println("\nEpsilon:");
		System.out.println(epsilon);
		Arc leavingArc = changeFlowFindLeaving(pathUV, epsilon);
		updateLTU(leavingArc, enteringArc);
		updateFairPrices(leavingArc, enteringArc);
		updateThreadPredDepth(enteringArc, leavingArc);

		System.out.println("\nleavingarc:");
		System.out.println(leavingArc);
		System.out.println(this.toString());
		return true;
	}

	private void updateLTU(Arc leavingArc, Arc enteringArc) {
		Tree.addEdge(enteringArc);
		Tree.removeEdge(leavingArc);
		if (enteringArc.getReducedCosts() < 0)
			L.removeEdge(enteringArc);
		else
			U.removeEdge(enteringArc);

		if (leavingArc.getFlow() == leavingArc.getUpperLimit())
			U.addEdge(leavingArc);
		else {
			L.addEdge(leavingArc);
			assert leavingArc.getFlow() == leavingArc.getLowerLimit();
		}
	}

	/**
	 * 
	 * @param leavingArc
	 * @param orientation
	 *            true if the arc is directed toward the root, false if it is
	 *            directed away from the root
	 */
	private void updateFairPrices(Arc leavingArc, Arc enteringArc) {
		if(leavingArc == enteringArc) return;
		double sign;
		// enteringArc from T1 to T2
		if (thread[enteringArc.getStartNodeIndex()] < thread[enteringArc
				.getEndNodeIndex()])
			sign = 1;
		else
			sign = -1;

		double ce = enteringArc.getReducedCosts();
		int f1, f2;
		if (depthArray[leavingArc.getStartNodeIndex()] < depthArray[leavingArc
				.getEndNodeIndex()]) {
			f1 = leavingArc.getStartNodeIndex();
			f2 = leavingArc.getEndNodeIndex();
		} else {
			f1 = leavingArc.getEndNodeIndex();
			f2 = leavingArc.getStartNodeIndex();
		}
		assert depthArray[f2] == depthArray[f1] + 1 : "initializing of f1 and f2 in updateFairPrices is wrong";
		// change the above!!!
		int k = f2;
		fairPrices[k] = fairPrices[k] + sign * ce;
		while (depthArray[thread[k]] > depthArray[f2]) {
			k = thread[k];
			fairPrices[k] = fairPrices[k] + sign * ce;
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

	private void updateThreadPredDepth(Arc enteringArc, Arc leavingArc) {
		if(enteringArc == leavingArc) return;
		int node, e1, e2, f1, f2, a, b, i, j, k, r;

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
		; // check if the startnode is in S
		while ((node != f2) && (node != 0)) { // if node is in S then there is
												// a path from node to f2 on the
												// way from e to the root
			node = this.predecessorArray[node];
		}
		if (node == f2) {
			e2 = enteringArc.getStartNodeIndex();
			e1 = enteringArc.getEndNodeIndex();
		} else {
			e1 = enteringArc.getStartNodeIndex();
			e2 = enteringArc.getEndNodeIndex();
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
		
		this.depthArray[i] = this.depthArray[i] + c;	//update depthArray in i ( = v1 = e2)
		// 2. finding the last node k in S_1 and initialize the value of r
		k = i;
		while (this.depthArray[this.thread[k]] > this.depthArray[i]) {
			k = this.thread[k];
			this.depthArray[k] = this.depthArray[k] + c;	//update depthArray in S1 except for v1
		}
		r = this.thread[k];

		// 3. if we are at the end of S* (i.e. being at the last element
		// of the thread-Array within the subtree with root f2 -> i == f2 ),
		// remove S and insert S*
		while (i != f2) {

			// 4. climb up one step the pivot stem and update thread[k]
			j = i;
			i = this.predecessorArray[i];
			this.predecessorArray[i] = j; // update (swap) the predecessors
			this.thread[k] = i;

			//update c (the constant used to update depthArray)
			c = c +2;
			this.depthArray[i] = this.depthArray[i] + c;	//update depthArray in i
			// 5. find the last node k in the left part of S_t
			k = i;
			while (this.thread[k] != j) {
				k = this.thread[k];
				this.depthArray[k] = this.depthArray[k] +c;	//update depthArray in the left part of S_t
			}

			// 6. if the right part of S_t is not empty we update thread(k) and
			// search the last node k in S_t
			// At the end we update r.
			if (this.depthArray[r] +c > this.depthArray[i]) {	//we add the constant added to depthArray[i] also to depthArray[r]
				this.thread[k] = r;								//so that the inequation still gives us the right result
				while (this.depthArray[this.thread[k]] + c > this.depthArray[i]) {		//same here
					k = this.thread[k];		
					this.depthArray[k] = this.depthArray[k] +c;	//update depthArray in the right part of S_t
				}
			}
			r = this.thread[k];
		}

		// execution of 3.
		this.thread[a] = r;
		this.thread[e1] = e2;
		this.predecessorArray[e2] = e1; // update pred(e2)
		if (e1 != a) {
			this.thread[k] = b;
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
		double epsilon = Double.POSITIVE_INFINITY;

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
		boolean uWasStart = indexU == u ? true : false;

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
		boolean vWasStart = indexU == v ? true : false;
		// climb up on both paths until join is reached
		while (u != v) {

			pathU.add(u);
			flowFinder = getPossibleFlowChange(u, predecessorArray[u],
					uWasStart, forwardBefore);
			epsilon = Math.min(epsilon, flowFinder.epsilon);
			uWasStart = flowFinder.leavingArc.getStartNodeIndex() == u ? true
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
			vWasStart = flowFinder.leavingArc.getStartNodeIndex() == u ? true
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
		else
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
		private LinkedList<Arc> candidates = new LinkedList<Arc>();
		// number of arcs that will be put in the list when it is refreshed
		private int filledListSize;
		// numbe of arcs we will choose after the rule of the best merit from
		// the list before it is refreshed
		private int iterations;
		// true if there are not enough arcs left to fill the list with the
		// requested number of arcs
		private boolean noMorecandidates;

		/**
		 * this constructor returns an instance of the e-Arc-Finder that uses
		 * the simplest pivoting rule i.e. return the first discovered arc with
		 * CReduced(Arc) < 0
		 */
		public EnteringArcFinderCandidatesPivotRule() {
			this.filledListSize = 1;
			this.iterations = 1;
			this.candidates = findCandidatesForEnteringArc(true,
					this.filledListSize);
			this.noMorecandidates = this.candidates.size() < this.filledListSize ? true
					: false;

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
			this.candidates = findCandidatesForEnteringArc(true,
					this.filledListSize); // when this class is instantiated it
											// is the first run
			this.noMorecandidates = this.candidates.size() < this.filledListSize ? true
					: false; // if the returned list doesnt contain as many arcs
			// as we wanted that means that there are not enough candidates
			// anymore to fullfil the request
		}

		// there will prob be problems when we run out of arcs
		/**
		 * 
		 * @return the entering arc
		 */
		public Arc getEnteringArc() {
			if (noMorecandidates && this.candidates.isEmpty())
				return null;
			if (!noMorecandidates
					|| this.candidates.size() <= this.filledListSize
							- this.iterations) {
				this.candidates = findCandidatesForEnteringArc(false,
						filledListSize);
				this.noMorecandidates = this.candidates.size() < this.filledListSize ? true
						: false;
			}
			/**
			 * here we have to find the max value and at the same time aupdate
			 * the list bcz there might be candidates in the list that do not
			 * reduce the costs anymore due to updates in previous iterations
			 */
			return this.candidates.pop(); // this doesnt give back the ARc with
											// the best merit yet, but will soon
			// therefore the datastructre LinkedList will prob be exchanged
			// against a treeset

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
			int startnode;
			int endnode;
			Arc maxArc = new Arc(0, 0, 0, 0, 0, 0);//create dummy arc with reduced costs zero
			maxArc.setReducedCosts(0);
			boolean L = false;
			boolean U = false;
			
			while (LIterator.hasNext()) {
				arc = LIterator.next();
				startnode = arc.getStartNodeIndex();
				endnode = arc.getEndNodeIndex();
				arc.setReducedCosts(arc.getCost() + fairPrices[startnode] - fairPrices[endnode]);
				if (arc.getReducedCosts() < maxArc.getReducedCosts()){
					maxArc = arc;
					L = true;
					U = false;
				}
			}
			//TODO: assert L is empty --> if maxArc.getReducedCosts == 0
			while (UIterator.hasNext()) {
				arc = UIterator.next();
				startnode = arc.getStartNodeIndex();
				endnode = arc.getEndNodeIndex();
				arc.setReducedCosts(arc.getCost() + fairPrices[startnode]- fairPrices[endnode]);
				if (arc.getReducedCosts() > Math.abs(maxArc.getReducedCosts())){
					maxArc = arc;
					L = false;
					U = true;
				}
			}
			if(maxArc.getReducedCosts()!=0) {
				assert !(L==false &&U==false) : "get max edge is wrong! in EnteringArcFinder";
				return new EnteringArcObject(maxArc, L, U);
			}
			return null;
		}
		
		private EnteringArcObject getEnteringArcObject() {
			int startnode;
			int endnode;
			while (LIterator.hasNext()) {
				arc = LIterator.next();
				startnode = arc.getStartNodeIndex();
				endnode = arc.getEndNodeIndex();
				arc.setReducedCosts(arc.getCost() + fairPrices[startnode] - fairPrices[endnode]);
				if (arc.getReducedCosts() < 0)
					return new EnteringArcObject(arc, true, false);
			}
			while (UIterator.hasNext()) {
				arc = UIterator.next();
				startnode = arc.getStartNodeIndex();
				endnode = arc.getEndNodeIndex();
				arc.setReducedCosts(arc.getCost() + fairPrices[startnode]- fairPrices[endnode]);
				if (arc.getReducedCosts() > 0)
					return new EnteringArcObject(arc, false, true);
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
	LinkedList<Arc> findCandidatesForEnteringArc(boolean firstRun, int r) { // r
																			// is
																			// the
																			// number
																			// of
																			// candidates
																			// that
																			// we
																			// ll
																			// provide
																			// in
																			// this
																			// call
		LinkedList<Arc> candidates = new LinkedList<Arc>();
		int i = 0;
		for (Arc arc : L) {
			int startnode;
			int endnode;
			if (firstRun) {
				/**
				 * reduced costs are initialized here
				 */
				startnode = arc.getStartNodeIndex();
				endnode = arc.getEndNodeIndex();
				arc.setReducedCosts(arc.getCost() + fairPrices[startnode]
						- fairPrices[endnode]);
			}
			if (arc.getReducedCosts() < 0) {
				candidates.add(arc);
				i++;
			}
			if (i == r)
				break;
		}
		return candidates;

	}

	private Arc changeFlowFindLeaving(LinkedList<FlowFinderObject> cycle,
			double epsilon) {
		Iterator<FlowFinderObject> iterator = cycle.iterator();
		FlowFinderObject flowFinder;
		Arc leavingArc = null;
		while (iterator.hasNext()) {
			flowFinder = iterator.next();
			double sign = 1;
			if (!flowFinder.forwardEdge)
				sign = -1;
			flowFinder.leavingArc.setFlow(flowFinder.leavingArc.getFlow()
					+ sign * epsilon);
			if (flowFinder.forwardEdge) {
				if (flowFinder.leavingArc.getFlow() == flowFinder.leavingArc
						.getUpperLimit())
					leavingArc = flowFinder.leavingArc;
			} else if (flowFinder.leavingArc.getFlow() == flowFinder.leavingArc
					.getLowerLimit())
				leavingArc = flowFinder.leavingArc;
		}
		return leavingArc;
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
		// Iterator<Arc> iteratorU = U.iterator();

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

		// // write all arcs of U. They will be red
		// while (iteratorU.hasNext()) {
		// // for (int i = lastIndexArcsU - 1; i >= 0; i--) {
		//
		// arc = iteratorU.next();
		// startIndex = arc.getStartNodeIndex();
		// endIndex = arc.getEndNodeIndex();
		// string.append(startIndex);
		// string.append("->"); // write the arcs like " 1->2 "...it means that
		// // there is an arc from 1 to 2
		// string.append(endIndex);
		// string.append(" [color=blue, label = \"[");
		// string.append(arc.getLowerLimit());
		// string.append(" / ");
		// string.append(arc.getFlow()); // write the current flow to the arc
		// // like " 1->2 [ label = [l/x/u] ];
		// string.append(" / ");
		// string.append(arc.getUpperLimit());
		// string.append(" / ");
		// string.append(arc.getCost());
		// string.append(" ]\" ]; \n");
		// }
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
		// Iterator<Arc> iteratorU = U.iterator();

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

		// // write all arcs of U. They will be red
		// while (iteratorU.hasNext()) {
		// // for (int i = lastIndexArcsU - 1; i >= 0; i--) {
		//
		// arc = iteratorU.next();
		// startIndex = arc.getStartNodeIndex();
		// endIndex = arc.getEndNodeIndex();
		// string.append(startIndex);
		// string.append("->"); // write the arcs like " 1->2 "...it means that
		// // there is an arc from 1 to 2
		// string.append(endIndex);
		// if (arc.equals(enteringArc)) {
		// string.append(" [color=green, label = \"[");
		// } else {
		// string.append(" [color=blue, label = \"[");
		// }
		// string.append(arc.getLowerLimit());
		// string.append(" / ");
		// string.append(arc.getFlow());
		// // like " 1->2 [ label = [l/x/u] ];
		// string.append(" / ");
		// string.append(arc.getUpperLimit());
		// string.append(" / ");
		// string.append(arc.getCost());
		// string.append(" ]\" ]; \n");
		// }
		string.append("} \n \n");

		return string.toString();
	}
}
