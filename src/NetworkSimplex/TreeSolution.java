package NetworkSimplex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

public class TreeSolution {

	/**
	 * predecessorArray, depthArray and thread represents the tree
	 */

	// gives the predecessor of each node or -1 for the artificial node
	private int[] predecessorArray;

	// gives the depth in the treesolution of each node
	private int[] depthArray;

	// corresponds to the preorder array from the tutorial
	private int[] thread;

	// the partition where flow equals lower cap
	public AdjacencyList L;

	// /the partition where flow equals upper cap
	public AdjacencyList U;

	// the tree T which contains all nodes of the network
	private AdjacencyList Tree;

	// the costs(potentials) in the nodes
	// in the script referred to as y
	private double[] fairPrices;

	//
	private EnteringArcFinderCandidatesPivotRule enteringArcFinder;

	// the flow we can change in the current iteration (depending on the
	// orientation)
	private double epsilon;

	// just to debug
	// public int numberOfIterations = 0;
	// public boolean UWasNotEmptyBefore = false;
	// public boolean backwardEdge = false;

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
		// U partition is empty at the beginning
		this.U = new AdjacencyList(numberOfNodes + 1);
		// int kIndex = numberOfNodes +1; //index of the artificial node
		this.Tree = new AdjacencyList(numberOfNodes + 1);

		// the artificial node is always at index 0
		int kIndex = 0;

		// the definition of M in the script so that the artificial arcs won't
		// be chosen
		double costArtificialArc = 1 + 0.5 * numberOfNodes * maxCost;

		this.predecessorArray = new int[numberOfNodes + 1];
		predecessorArray[kIndex] = -1; // pred(k) is always -1

		this.depthArray = new int[numberOfNodes + 1];
		depthArray[kIndex] = 0; // k is the root of the tree

		this.thread = new int[numberOfNodes + 1];
		thread[kIndex] = 1; // initial thread(k)

		this.fairPrices = new double[numberOfNodes + 1];
		fairPrices[kIndex] = 0; // this is the one that choose arbitrarily (n
		// variables, n-1 equations)

		int startNodeIndex;
		int endNodeIndex;
		double flow; // flow is abs(nettodemand)
		for (int i = 1; i < nodes.length; i++) { // start at one because at
													// index
			// there is no node to keep
			// it easy i.e. nodeIndex =
			// arrayIndex
			flow = 0;
			Node node = nodes[i]; // could also be null

			// define fairprices of all nodes and the orientation of the
			// corresponding artifical arc
			if (node == null || node.getNettodemand() <= 0) { // for nonpositive
																// nettodemands
																// we insert an
																// arc (i,k)
				startNodeIndex = i;
				endNodeIndex = kIndex;
				fairPrices[i] = -costArtificialArc; // initial fair prices with
													// M
				if (node != null)
					flow = Math.abs(node.getNettodemand()); // flow =
															// nettodemand of
															// the node
			} else { // for positive nettodemands we insert an arc (k,i)
				startNodeIndex = kIndex;
				endNodeIndex = i;
				fairPrices[i] = costArtificialArc; // initial fair prices with
													// -M
				flow = Math.abs(node.getNettodemand()); // flow = nettodemand of
														// the node
			}
			// add artificial arcs
			Arc arc = new Arc(startNodeIndex, endNodeIndex, 0,
					Double.POSITIVE_INFINITY, costArtificialArc, flow);
			this.Tree.addEdge(arc); // add arc with upper cap = infinity and
									// lower cap = 0

			this.predecessorArray[i] = kIndex; // set pred(i) = k for all nodes
			this.depthArray[i] = 1; // all nodes != k have depth = 1
			/**
			 * initialization of thread
			 */
			if (i < numberOfNodes)
				this.thread[i] = i + 1; // nodes are traversed in index order
			else
				this.thread[i] = 0; // successor of the last node is the root
		}

		// just for the candidates pivot rule
		this.enteringArcFinder = new EnteringArcFinderCandidatesPivotRule(40,
				20);
	}

	/**
	 * performs an iteration if an entering arc exists i.e. finding an entering
	 * arc, removing the last blocking arc on the resulting circle, change the
	 * flow and update the treesolution and the corresponding arrays
	 * 
	 */
	public boolean iterate() {
		// ++numberOfIterations;

		// EnteringArcFinderCandidatesPivotRule finderPivotRule = new
		// EnteringArcFinderCandidatesPivotRule();
		// Arc2 enteringArc = finderPivotRule.getEnteringArc();

		// dont init each time
		EnteringArcFinderFirstRule finderFirstRule = new EnteringArcFinderFirstRule();
		// //EnteringArcObject enteringArcObject =
		// finderFirstRule.getEnteringArcObject(); //this method is slower with
		// standard1
		EnteringArcObject enteringArcObject = finderFirstRule
				.getMaxEnteringArcObject();
		// EnteringArcObject enteringArcObject =
		// enteringArcFinder.getEnteringArc();
		if (enteringArcObject == null)
			return false; // no more entering arcs can be found
		Arc enteringArc = enteringArcObject.getEnteringArc();
		System.out.println("Arc (found by first rule class: )");
		System.out.println(enteringArc);

		// finding the path between the nodes of the enterinc arc to identify
		// the 'scheitel', the whole circle and the epsilon
		LinkedList<FlowFinderObject> pathUV = findPathBetweenUV(enteringArc);
		// controlCircle(pathUV); // check if the circle is really a circle
		System.out.println("\nEpsilon:");
		System.out.println(epsilon);

		// change the flow on the circle and find meanwhile the leaving arc
		System.out.println("changeFlowFindLeavingArc:");
		Arc leavingArc = changeFlowFindLeaving(pathUV, epsilon);

		// update the treesolution (move entering arc out of L/U into T and the
		// leavig arc out of T into L/U)
		System.out.println("updateLTU");
		updateLTU(leavingArc, enteringArc);

		// update the four arrays thread, pred, depth and fairPrices
		System.out.println("updateThreadPredDepthFairPrices");
		updateThreadPredDepthFairPrices(enteringArc, leavingArc);

		System.out.println("\nleavingarc:");
		System.out.println(leavingArc);
		System.out.println(this.toString());
		// assertReducedCostZeroInTree();
		// assertEachNodeInThreadOnlyVisitedOnce();
		// assertDepthOfSuccesorGreater();
		// assertPred();
		return true; // return true if we found an entering arc
	}

	/**
	 * checks if the cycle is actually a cycle and if the orientations if the
	 * arcs in it are correct
	 * 
	 * @param pathUV
	 */
	private void controlCircle(LinkedList<FlowFinderObject> pathUV) {
		Iterator<FlowFinderObject> iterator2 = pathUV.iterator();
		FlowFinderObject object1 = iterator2.next();
		int join, scheitel;
		if (object1.forwardEdge)
			join = object1.leavingArc.getStartNodeIndex();
		else
			join = object1.leavingArc.getEndNodeIndex();
		scheitel = join;
		Iterator<FlowFinderObject> iterator = pathUV.iterator();
		FlowFinderObject object = null;
		while (iterator.hasNext()) {
			object = iterator.next();
			if (join == object.leavingArc.getStartNodeIndex()) {
				assert object.forwardEdge;
				join = object.leavingArc.getEndNodeIndex();
			} else if (join == object.leavingArc.getEndNodeIndex()) {
				join = object.leavingArc.getStartNodeIndex();
				assert !object.forwardEdge;
			} else
				assert false : "no circle" + pathUV + "\n" + join;
		}
		assert scheitel == join : "no circle" + pathUV + "\n" + join;
	}

	private void assertPred() {
		System.out.println("control Pred");
		for (int i = 0; i < this.predecessorArray.length; i++) {
			int p = i;
			while (p != 0) {
				p = this.predecessorArray[p];
			}
		}
	}

	/**
	 * tests if the solution still contains artificial arcs
	 * 
	 * @return
	 */
	public boolean solutionFeasable() {
		Iterator<Arc> iterator = this.Tree.iterator();
		Arc arc;
		while (iterator.hasNext()) {
			arc = iterator.next();
			// artificial arcs should not have flow > 0
			if (arc.getStartNodeIndex() == 0 || arc.getEndNodeIndex() == 0) // artificial
																			// arcs
																			// have
																			// k
																			// as
																			// one
																			// node
				if (arc.getFlow() != 0) {
					System.out.println("artificial arc with flow:");
					System.out.println(arc);
					return false;
				}
		}
		iterator = this.U.iterator();
		while (iterator.hasNext()) {
			arc = iterator.next();
			if (arc.getStartNodeIndex() == 0 || arc.getEndNodeIndex() == 0) {
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
	private void assertDepthOfSuccesorGreater() {
		for (int i = 1; i < this.predecessorArray.length; i++) {
			assert this.depthArray[this.predecessorArray[i]] + 1 == this.depthArray[i] : "sth wrong with depthArray";
		}

	}

	/**
	 * asserts that each node is only visited once when performing a depth first
	 * search with thread array
	 */
	private void assertEachNodeInThreadOnlyVisitedOnce() {
		// array is initialized with false
		boolean[] visited = new boolean[this.thread.length];
		for (int i = 0; i < thread.length; i++) {
			assert visited[this.thread[i]] == false : "sth wrong with thread";
			visited[this.thread[i]] = true;
		}
	}

	/**
	 * tests if the reduced costs of all arcs in T are zero
	 */
	private void assertReducedCostZeroInTree() {
		Iterator<Arc> iterator = this.Tree.iterator();
		Arc arc;
		int startnode, endnode;
		while (iterator.hasNext()) {
			arc = iterator.next();
			startnode = arc.getStartNodeIndex();
			endnode = arc.getEndNodeIndex();
			arc.setReducedCosts(arc.getCost() + fairPrices[startnode]
					- fairPrices[endnode]);
			assert arc.getReducedCosts() == 0 : "sth wrong with fair prices";
		}
	}

	/**
	 * adds the entering arc to T and removes it from L/U and deletes the
	 * leaving arc from T and adds it to L/U
	 * 
	 * @param leavingArc
	 * @param enteringArc
	 */
	private void updateLTU(Arc leavingArc, Arc enteringArc) {
		Tree.addEdge(enteringArc);
		Tree.removeEdge(leavingArc);
		if (enteringArc.getReducedCosts() < 0) // if reduced costs are < 0, the
												// entering arc is from L
			L.removeEdge(enteringArc);
		else
			// otherwise from U
			U.removeEdge(enteringArc);
		assert leavingArc.getFlow() == leavingArc.getUpperLimit()
				|| leavingArc.getFlow() == leavingArc.getLowerLimit() : "leavingArc did not reach upper or lower cap!";
		if (leavingArc.getFlow() == leavingArc.getUpperLimit()) { // put leaving
																	// arc to U
																	// if the
																	// flow
																	// reaches
																	// now the
																	// upper cap
			U.addEdge(leavingArc);
			// UWasNotEmptyBefore = true;
		} else {
			L.addEdge(leavingArc); // otherwise add it to L
			assert leavingArc.getFlow() == leavingArc.getLowerLimit();
		}
	}

	/**
	 * a method to update the thread-array, depth-array and predecessor-array
	 * 
	 * @param enteringArc
	 *            The entering arc in the current iteration
	 * @param leavingArc
	 *            The leaving arc in the current iteration
	 */

	private void updateThreadPredDepthFairPrices(Arc enteringArc, Arc leavingArc) {
		if (enteringArc == leavingArc) // if entering arc = leaving arc there is
										// nothing to update
			return;

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
		while ((node != f2) && (node != 0)) { // if node is in S there is
			// a path from node to f2 on the
			// way from e to the root
			node = this.predecessorArray[node]; // go 'up' to the root and look
												// if we reach f2 (remark that
												// f2 is in S)
		}
		if (node == f2) { // if startnode is in S
			e2 = enteringArc.getStartNodeIndex();
			e1 = enteringArc.getEndNodeIndex();
			sign = -1; // sign = -1 if the entering arc is directed towards the
						// root

		} else { // if endnode is in S
			e1 = enteringArc.getStartNodeIndex();
			e2 = enteringArc.getEndNodeIndex();
			sign = 1; // sign = 1 if the entering arc is directed away from the
						// root
		}

		// 1. Initialize
		a = f1;
		while (this.thread[a] != f2) { // is the 'thread-predecessor' of f2
			a = this.thread[a]; // the thread of a will be the last r
		}
		b = this.thread[e1]; // b will be the new thread-node of the last node
								// in S*
		i = e2; // we start at e2 and climb up the pivot stem ( = path from e2
				// to f2 )

		// calculate c1 for depth update (c1 is the constant used for S1)
		int c = depthArray[e1] - depthArray[e2] + 1;

		// update fairprice for i = e2
		this.fairPrices[i] = fairPrices[i] + sign * ce;

		// 2. finding the last node k in S_1 and initialize the value of r
		k = i; // we start at i = e2
		while (this.depthArray[this.thread[k]] > this.depthArray[i]) {
			k = this.thread[k]; // looking for the last node in S_1 by going
								// through all nodes in S_1
			this.depthArray[k] = this.depthArray[k] + c; // update depthArray in
															// S_1 except for v1
			this.fairPrices[k] = fairPrices[k] + sign * ce; // update
															// fairePrices in
															// every node in S_1
		}
		r = this.thread[k]; // at first r is the thread-node after the last node
							// in S_1

		this.depthArray[i] = this.depthArray[i] + c; // update depthArray in i (
														// = v1 = e2)

		int pred = e1; // initial pred-node

		// 3. if we are at the end of S* (i.e. being at the last element
		// of the thread-Array within the subtree with root f2 -> i == f2 ),
		// remove S and insert S*
		while (i != f2) {
			// 4. climb up one step the pivot stem up to f2 and update thread[k]
			// (and pred, depth, fairPrices)
			j = i; // j is to update the pred of i
			i = this.predecessorArray[i]; // go one step up on the pivot stem
			this.predecessorArray[j] = pred; // update the predecessors -> swap
			pred = j; // to swap the pred we have to keep the the j as the next
						// pred
			this.thread[k] = i; // i is thread-node of the last node in S_i-1

			// update c (the constant used to update depthArray)
			c = c + 2;
			this.depthArray[i] = this.depthArray[i] + c; // update depthArray in
															// i
			this.fairPrices[i] = fairPrices[i] + sign * ce;
			// 5. find the last node k in the left part of S_t (meanwhile we can
			// update fairprices an depth in the nodes)
			k = i;
			while (this.thread[k] != j) {
				k = this.thread[k];
				this.depthArray[k] = this.depthArray[k] + c; // update
																// depthArray in
																// the left part
																// of S_t
				this.fairPrices[k] = fairPrices[k] + sign * ce;
			}

			// 6. if the right part of S_t is not empty we update thread(k) and
			// search the last node k in S_t
			// At the end we update r.

			// we add the constant added to depthArray[i] also to depthArray[r]
			// so that the inequation still gives us the right result
			if (this.depthArray[r] + c > this.depthArray[i]) {

				this.thread[k] = r; // thread of the last node in the left part
									// is r (=the first node in the right part)

				while (this.depthArray[this.thread[k]] + c > this.depthArray[i]) { // same
																					// here
					k = this.thread[k]; // go through the right part and update
										// depth and fairprices

					// update depthArray in the right part of S_i
					this.depthArray[k] = this.depthArray[k] + c;
					// update fairprices in the right part of S_i
					this.fairPrices[k] = fairPrices[k] + sign * ce;
				}
				r = this.thread[k]; // r is the next first node in the next
									// notempty right part of an S_i
			}
		}
		this.predecessorArray[i] = pred; // update pred in i = f2

		// execution of 3. -> remove S and insert S*
		this.thread[e1] = e2; // connect e1 and e2 in thread-array
		this.predecessorArray[e2] = e1; // update pred(e2)
		if (e1 != a) {
			this.thread[k] = b; // b is the new thread-node of the last node k
								// in S*
			this.thread[a] = r; // set thread(a) because f2 is not more the
								// thread-node of a
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
		// double epsilon = Double.POSITIVE_INFINITY;

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

		System.out
				.println("\n\n\nthe cycle starting at the scheitel and in orientation direction:");
		System.out.println(arcPathU);

		this.epsilon = epsilon;
		return arcPathU;

	}

	/**
	 * finds out the orientation of edge {u,Pu} in the cycle and how much the
	 * flow can be increased/decreased
	 * 
	 * @param u
	 * @param Pu
	 * @param uWasStart
	 * @param forwardBefore
	 * @return
	 */
	private FlowFinderObject getPossibleFlowChange(int u, int Pu,
			boolean uWasStart, boolean forwardBefore) {
		Arc leavingArc = Tree.getEdgeInTree(u, Pu);
		boolean sameDirection; // says whether the arc between u and Pu and the
								// arc before are in the same direction
		if (leavingArc.getStartNodeIndex() == u) {
			// <--u-->Pu u is the startnode of the arc (u,Pu) and was the
			// startnode of the arc before
			if (uWasStart)
				sameDirection = false;
			// -->u-->Pu u is the startnode of the arc (u,Pu) and was not the
			// startnode of the arc before
			else
				sameDirection = true;
		}
		// u is end node
		else {
			// <--u<--Pu u is not the startnode of the arc (u,Pu) and was the
			// startnode of the arc before
			if (uWasStart)
				sameDirection = true;
			// -->u<--Pu u is not the startnode of the arc (u,Pu) and was not
			// the startnode of the arc before
			else
				sameDirection = false;
		}
		// the edge examined before and this edge BOTH belong to either F or B
		// (F = forward edges or B = backward edges)
		if (sameDirection) { // both edges has the same direction
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
				return new FlowFinderObject(leavingArc, false,
						leavingArc.getFlow() - leavingArc.getLowerLimit());
			// the other way round
			else
				return new FlowFinderObject(leavingArc, true,
						leavingArc.getUpperLimit() - leavingArc.getFlow());
		}
	}

	/**
	 * this is an inner class, as such it has access to all class variables and
	 * methods of the outer class (even if they are private) it encapsulates the
	 * entering arc finding process Usage: create and EnteringArcFinder class
	 * instance and use the getEnteringArc method there are 2 constructors so
	 * far, one without arguments that uses a very simple pivoting rule and one
	 * with more arguments that uses a more advanced one.
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
		private boolean phase1 = true;

		private int startSearchNodeIndex = 0;
		private boolean isL = true;

		/**
		 * this constructor returns an instance of the e-Arc-Finder that uses
		 * the simplest pivoting rule i.e. return the first discovered arc with
		 * CReduced(Arc) < 0
		 */
		public EnteringArcFinderCandidatesPivotRule() {
			this.filledListSize = 1;
			this.iterations = 1;
			// we start the search in node 0 in L
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

		/**
		 * 
		 * @return the entering arc
		 */
		public EnteringArcObject getEnteringArc() {
			if (!phase1)
				return new EnteringArcFinderFirstRule().getEnteringArcObject();
			Arc dummyArc = new Arc(0, 0, 0, 0, 0, 0); // a dummy
			dummyArc.setReducedCosts(0);
			EnteringArcObject candidate = new EnteringArcObject(dummyArc,
					false, false); // a dummy
			Iterator<EnteringArcObject> candIterator;
			EnteringArcObject arcObject;
			Arc enteringArc;
			int index = 0; // counter
			int removeIndex = index;
			;
			for (int i = 1; i <= 2; i++) {
				candIterator = this.candidates.iterator();
				while (candIterator.hasNext()) {
					arcObject = candIterator.next();
					enteringArc = arcObject.getEnteringArc();
					++index;
					enteringArc
							.setReducedCosts(updateRedCostsOfOneArc(enteringArc));// update
																					// list
																					// entry
																					// before
																					// compare
					if (enteringArc.getReducedCosts() < 0 && candidate.isL()
							|| enteringArc.getReducedCosts() > 0
							&& candidate.isU()) {
						if (Math.abs(enteringArc.getReducedCosts()) > Math
								.abs(enteringArc.getReducedCosts())) {
							candidate = arcObject;
							removeIndex = index;
						}
					}
				}
				// if arc is no dummy anymore
				if (candidate.getEnteringArc().getUpperLimit() != 0) {
					this.candidates.remove(removeIndex);
					// if we need new arcs --> refresh
					if (this.candidates.size() < this.filledListSize
							- this.iterations) {
						this.candidates = findCandidatesForEnteringArc();
					}
					return candidate;
				} else
					this.candidates = findCandidatesForEnteringArc();
			}
			return new EnteringArcFinderFirstRule().getEnteringArcObject();
		}

		/**
		 * 
		 * @param filledListSize
		 *            number of provided candidates
		 * @param startSearchNodeIndex
		 * @return
		 */
		LinkedList<EnteringArcObject> findCandidatesForEnteringArc() {
			if (startSearchNodeIndex >= predecessorArray.length)
				startSearchNodeIndex = 0;
			LinkedList<EnteringArcObject> candidates = new LinkedList<EnteringArcObject>();
			// init where we want to start the search
			Iterator<Arc> iterator = isL ? L.iterator(startSearchNodeIndex) : U
					.iterator(startSearchNodeIndex);
			Arc arc;
			// we do that three times
			// first from where we left, then the complete other list and then
			// the first one again
			// bcz we might have skipped the beginning
			for (int i = 1; i <= 3; i++) {
				while (iterator.hasNext()) {
					arc = iterator.next();
					// update reduced costs
					arc.setReducedCosts(updateRedCostsOfOneArc(arc));
					if ((arc.getReducedCosts() < 0 && isL)
							|| (arc.getReducedCosts() > 0 && !isL))
						candidates.add(new EnteringArcObject(arc, isL, !isL));
					if (candidates.size() == this.filledListSize) {
						this.startSearchNodeIndex = arc.getStartNodeIndex() + 1;
						return candidates;
					}
				}
				isL = !isL;
				// search in the other partition and start at node 0
				iterator = isL ? L.iterator(0) : U.iterator(0);
			}
			// list is not full
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

		// iterators to iterator over L and U to find the entering Arc
		private Iterator<Arc> LIterator;
		private Iterator<Arc> UIterator;

		private Arc arc;

		public EnteringArcFinderFirstRule() {
			this.LIterator = L.iterator();
			this.UIterator = U.iterator();
		}

		/**
		 * The used method to find an entering arc (Object). Potential entering
		 * arcs are those which are in L and have negative reduced costs and
		 * those which are in U and have positive reduced costs. We search for
		 * the potential entering arc with die maximal absolute reduced costs.
		 * We update the reduced costs on the fly.
		 * 
		 * @return
		 */
		private EnteringArcObject getMaxEnteringArcObject() {
			// create dummy arc with reduced costs zero
			Arc maxArc = new Arc(0, 0, 0, 0, 0, 0);

			// save always the maximal reduced costs (of the current potential
			// entering arc)
			// will always be nonpositive because we first iterate over L
			maxArc.setReducedCosts(0);
			boolean L = false; // at first the entering is not out of L and not
								// out of U
			boolean U = false;

			while (LIterator.hasNext()) { // at first iterate over L
				arc = LIterator.next();
				arc.setReducedCosts(updateRedCostsOfOneArc(arc)); // update
																	// reduced
																	// costs

				// potential entering arcs out of L have negative reduced costs
				// if the absolute reduced costs are bigger than the current
				// maximal reduced costs we update the entering arc
				if (arc.getReducedCosts() < maxArc.getReducedCosts()) {
					maxArc = arc; // update the new maximal reduced costs
					L = true; // entering is now out of L
					U = false;
				}
			}
			while (UIterator.hasNext()) { // then iterate over U
				arc = UIterator.next();
				arc.setReducedCosts(updateRedCostsOfOneArc(arc)); // update
																	// reduced
																	// costs

				// potential entering arcs out of U have positive reduced costs
				// if the absolute reduced costs are bigger than the current
				// maximal reduced costs we update the entering arc
				if (arc.getReducedCosts() > Math.abs(maxArc.getReducedCosts())) {
					maxArc = arc; // update the new maximal reduced costs
					L = false;
					U = true; // entering is now out of U
				}
			}

			// return the entering arc with the maximal absolute reduced costs
			// if the maximal reduced costs were updated once at least
			if (maxArc.getReducedCosts() != 0) {
				// assert !(L == false && U == false) :
				// "get max edge is wrong! in EnteringArcFinder";
				// assert maxArc.getReducedCosts()<0;//for standard only
				return new EnteringArcObject(maxArc, L, U);
			}
			// found no entering arc
			return null;
		}

		/**
		 * We search for the first potential entering arc in L and U. Potential
		 * entering arcs are those which are in L and have negative reduced
		 * costs and those which are in U and have positive reduced costs. We
		 * update the reduced costs on the fly.
		 * 
		 * @return
		 */
		private EnteringArcObject getEnteringArcObject() {
			while (LIterator.hasNext()) { // at first we iterate over L
				arc = LIterator.next();
				arc.setReducedCosts(updateRedCostsOfOneArc(arc)); // update the
																	// reduced
																	// costs
				if (arc.getReducedCosts() < 0) // if the reduced costs are
												// negative it is the entering
												// arc we will return
					return new EnteringArcObject(arc, true, false);
			}
			while (UIterator.hasNext()) { // now we iterate over U
				arc = UIterator.next();
				arc.setReducedCosts(updateRedCostsOfOneArc(arc)); // update the
																	// reduced
																	// costs
				if (arc.getReducedCosts() > 0) // if the reduced costs are
												// positive it is the entering
												// arc we will return
					return new EnteringArcObject(arc, false, true);
			}
			// found no entering arc
			return null;
		}
	}

	/**
	 * changes the flow on the cycle and finds a leaving arc in meantime
	 * FlowFinderObjects are used to save a arcs on the circle, the direction of
	 * it (depending on the leaving arc) and the epsilon we can achieve with
	 * this leaving arc
	 * 
	 * @param cycle
	 * @param epsilon
	 * @return
	 */
	private Arc changeFlowFindLeaving(LinkedList<FlowFinderObject> cycle,
			double epsilon) {
		Iterator<FlowFinderObject> iterator = cycle.iterator();
		FlowFinderObject flowFinder = null;
		FlowFinderObject leavingArcFlowFinder = null;
		Arc leavingArc = null;
		while (iterator.hasNext()) {
			flowFinder = iterator.next(); // gives us the next arc on the circle

			// determine the sign with the direction of the leaving arc
			double sign = 1;
			if (!flowFinder.forwardEdge)
				sign = -1;

			// change the flow on the arc (subtract if it is a backward arc and
			// add if it is a forward arc). Here we use the determined epsilon
			// not the epsilons in the FlowFinderObjects)
			flowFinder.leavingArc.setFlow(flowFinder.leavingArc.getFlow()
					+ sign * epsilon);

			// assert flowFinder.leavingArc.getFlow() <= flowFinder.leavingArc
			// .getUpperLimit() : "flow was increased above upper limit!";
			// assert flowFinder.leavingArc.getFlow() >= flowFinder.leavingArc
			// .getLowerLimit() : "flow was decreased under lower limit!";

			// if an circle-arcs flow reaches its upper or lower limit, it is
			// the new leaving arc (while there comes no one more)
			if (flowFinder.forwardEdge) {
				if (flowFinder.leavingArc.getFlow() == flowFinder.leavingArc
						.getUpperLimit()) { // flow reaches upper limit
					leavingArc = flowFinder.leavingArc; // current arc is the
														// new leaving arc
					leavingArcFlowFinder = flowFinder;
				}
			} else {
				if (flowFinder.leavingArc.getFlow() == flowFinder.leavingArc
						.getLowerLimit()) { // flow reaches lower limit
					leavingArc = flowFinder.leavingArc; // current arc is the
														// new leaving arc
					leavingArcFlowFinder = flowFinder;
				}
			}
		}
		// assert !leavingArcFlowFinder.forwardEdge :
		// "this edge should be in U "+ leavingArcFlowFinder;
		// assert leavingArcFlowFinder.leavingArc.getFlow() == 0;

		// the leaving arc of the iteration
		return leavingArc;
	}

	/**
	 * returns the updated reduced costs on a given arc by c*_ij = c_ij + y_i +
	 * y_j
	 * 
	 * @param arc
	 * @return
	 */
	public double updateRedCostsOfOneArc(Arc arc) {
		int startnode = arc.getStartNodeIndex();
		int endnode = arc.getEndNodeIndex();
		return arc.getCost() + fairPrices[startnode] - fairPrices[endnode];
	}

	/**
	 * calculates the costs of the current flow it is the sum of the costs *
	 * flow on every arc in T,L,U
	 * 
	 * @return
	 */
	public double getCosts() {
		double costs = 0;
		Iterator<Arc> iterator = L.iterator();
		Arc arc;
		while (iterator.hasNext()) {
			arc = iterator.next();
			costs = costs + arc.getFlow() * arc.getCost();
		}

		iterator = U.iterator();
		while (iterator.hasNext()) {
			arc = iterator.next();
			costs = costs + arc.getFlow() * arc.getCost();
		}

		iterator = Tree.iterator();
		while (iterator.hasNext()) {
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
