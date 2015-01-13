package NetworkSimplex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;

public class TreeSolution {

	private int[] predecessorArray;

	private int[] depthArray;

	private int[] thread; // corresponds to the preorder array from the tutorial

	// the partition where flow equals lower cap
	private AdjacencyList L2; // not sure yet which arc type we will use

	// /the partition where flow equals upper cap
	private AdjacencyList U2;

	// will prob get deleted soon and replaced by some array that store flow,cap
	// and so on;
	private AdjacencyList Tree2;

	// private ArrayList<Double> fairPrices = new ArrayList<Double>(); //the
	// costs in the nodes
	// in the script referred to as y
	private double[] fairPrices;

	// //the onstructor will only be used once in the Reader class to construct
	// an intial feasable tree solution
	// public TreeSolution(Arc[] L, Node[] VPos, Node[] VNeg[]) {
	// this.L = L;
	// for(Node node : VPos){
	//
	// }
	// }

	/**
	 * this constructor uses the information that the Reader class connects from
	 * the inputfile and constructs the inital tree solution therefore an
	 * artificial node and artificial arcs are constructed
	 * 
	 * @param L2
	 * @param nodes
	 * @param numberOfNodes
	 * @param maxCost
	 *            that is needed to calculate the costs of the artificial arcs
	 */
	public TreeSolution(AdjacencyList L2, Node[] nodes, int numberOfNodes,
			double maxCost) {
		this.L2 = L2;
		this.U2 = new AdjacencyList(numberOfNodes + 1); // U partition is empty
														// at the beginning
		// int kIndex = numberOfNodes +1; //index of the artificial node
		this.Tree2 = new AdjacencyList(numberOfNodes + 1);
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
		for (int i = 1; i < nodes.length; i++) { // start at one bcz at index 0
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
			Arc2 arc = new Arc2(startNodeIndex, endNodeIndex, 0,
					Double.POSITIVE_INFINITY, costArtificialArc, flow); // add
																		// artificial
																		// arcs
			this.Tree2.addEdge(startNodeIndex, arc);
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
		System.out.println("List L2:!!!");
		Iterator<Arc2> iterator = L2.iterator();
		while (iterator.hasNext()) {
			System.out.println(iterator.next());
		}
	}

	/**
	 * 
	 * 
	 */
	public boolean iterate() {
		// EnteringArcFinderCandidatesPivotRule finderPivotRule = new
		// EnteringArcFinderCandidatesPivotRule();
		// Arc2 enteringArc = finderPivotRule.getEnteringArc();
		// System.out.println("Arc (found by pivot rule class): ");
		// System.out.println(enteringArc);

		EnteringArcFinderFirstRule finderFirstRule = new EnteringArcFinderFirstRule();
		Arc2 enteringArc2 = finderFirstRule.getEnteringArc();
		System.out.println("Arc (found by first rule class: )");
		System.out.println(enteringArc2);

		ArrayList<Integer> pathUV = findPathBetweenUV(
				enteringArc2.getStartNodeIndex(),
				enteringArc2.getEndNodeIndex());
		return false;
	}

	/**
	 * executes one iteration of the algorithm
	 * 
	 * @return true if the tree solution is not yet optimal, false else this is
	 *         just so for the moment, i think we also have to capture cases
	 *         like unbounded problems and problems without solution
	 */
	public boolean updateTreeSolution() {
		// ...

		// this.updateFairPrices();

		this.updateCReduced();
		// ..
		return true;
	}

	/**
	 * 
	 * @param leavingArc
	 * @param orientation
	 *            true if the arc is directed toward the root, false if it is
	 *            directed away from the root
	 */
	private void updateFairPrices(Arc2 leavingArc, Arc2 enteringArc,
			boolean orientation) {
		double sign = orientation ? -1 : 1;
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
		while (depthArray[k] > depthArray[f2]) {
			fairPrices[k] = fairPrices[k] + sign * ce;
			k = thread[k];
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

	private void updateThread(Arc2 enteringArc, Arc2 leavingArc) {

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
		node=enteringArc.getStartNodeIndex();;	//check if the startnode is in S
		while((node != f2) && (node != -1)){	// if node is in S then there is a path from node to f2 on the way from e to the root
			node = this.predecessorArray[node];
		}
		if (node == f2){
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

		// 2. finding the last node k in S_1 and initialize the value of r
		k = i;
		while (this.depthArray[this.thread[k]] > this.depthArray[i]) {
			k = this.thread[k];
		}
		r = this.thread[k];

		// 3. if we are at the end of S* (thats means being at the last element
		// of the thread-Array within the subtree with root f2 -> i == f2 ),
		// remove S and insert S*
		while (i != f2) {

			// 4. climb up one step the pivot stem and update thread[k]
			j = i;
			i = this.predecessorArray[i];
			this.thread[k] = i;

			// 5. find the last node k in the left part of S_t
			k = i;
			while (this.thread[k] != j) {
				k = this.thread[k];
			}

			// 6. find the last node k in the right part of S_t (if it is not
			// empty)
			// and update thread[k]
			if (this.depthArray[r] > this.depthArray[i]) {
				this.thread[k] = r;
				while (this.depthArray[this.thread[k]] > this.depthArray[i]) {
					k = this.thread[k];
					r = this.thread[k];
				}
			}
		}

		// execution of 3.
		this.thread[a] = r;
		this.thread[e1] = e2;
		if (e1 != a) {
			this.thread[k] = b;
		} else {
			this.thread[k] = r;
		}

	}

	private void updateCReduced() {

	}

	/**
	 * this method finds a path between the nodes with indexU and indexV it is
	 * used to find the cycle in the tree after adding the entering arc
	 * 
	 * @param indexU
	 * @param indexV
	 * @return a list that contains the path
	 */
	private ArrayList<Integer> findPathBetweenUV(int indexU, int indexV) {

		// maybe use another datastructure here, like a stack or so
		ArrayList<Integer> pathU = new ArrayList<Integer>();
		// ArrayList<Integer> pathV = new ArrayList<Integer>();
		Stack<Integer> pathV = new Stack<Integer>();

		// initialize so that u is the index with the greater depth
		int u, v;
		if (depthArray[indexU] >= depthArray[indexV]) {
			u = indexU;
			v = indexV;
		} else {
			u = indexV;
			v = indexU;
		}

		// climb up the longer path until level of v is reached
		while (depthArray[u] > depthArray[v]) {
			pathU.add(u);
			u = predecessorArray[u];
		}

		// climb up on both paths until join is reached
		while (u != v) {
			pathU.add(u);
			pathV.add(v);
			u = predecessorArray[u];
			v = predecessorArray[v];
		}
		// pathV.pop(); //remove last element v = u
		pathU.add(u);
		pathU.addAll(pathV);
		System.out.println("path between u and v: ");
		System.out.println(pathU);
		return pathU;

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
		private LinkedList<Arc2> candidates = new LinkedList<Arc2>();
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
		public Arc2 getEnteringArc() {
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

		private Iterator<Arc2> LIterator;
		private Iterator<Arc2> UIterator;
		// just fo testing
		private Arc2 arc;

		public EnteringArcFinderFirstRule() {
			this.LIterator = L2.iterator();
			this.UIterator = U2.iterator();

			/**
			 * init reduced costs
			 */
			int startnode;
			int endnode;
			for (Arc2 arc : L2) {
				startnode = arc.getStartNodeIndex();
				endnode = arc.getEndNodeIndex();
				arc.setReducedCosts(arc.getCost() + fairPrices[startnode]
						- fairPrices[endnode]);
			}
			// assert U2 is empty
			System.out.println("L2 after init reduced costs");
			System.out.println(L2);

		}

		private Arc2 getEnteringArc() {
			// Arc2 arc;
			while (LIterator.hasNext()) {
				arc = LIterator.next();
				if (arc.getReducedCosts() < 0)
					return arc;
			}
			while (UIterator.hasNext()) {
				if (UIterator.next().getReducedCosts() > 0)
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
	LinkedList<Arc2> findCandidatesForEnteringArc(boolean firstRun, int r) { // r
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
		LinkedList<Arc2> candidates = new LinkedList<Arc2>();
		int i = 0;
		for (Arc2 arc : L2) {
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

	// /**
	// * method to create a String for visualize the Treesolution arc
	// description:
	// * [l / x / u] with l = lower limit, x = current flow, u = upper limit
	// *
	// * @return String for graphviz
	// */
	// public String graphvizStringTree() {
	// StringBuffer string = new StringBuffer(
	// "\n Treesolution String for GRAPHVIZ: \n	\n digraph Treesolution { \n	node [shape = circle]; ");
	// int lastIndexNodes = this.thread.length - 1;
	// int lastIndexArcs = Tree2.size();
	// Arc2 arc = new Arc2(0, 0, 0., 0., 0., 0.);
	// int startIndex = 0;
	// int endIndex = 0;
	//
	// // write all node (one node for each index...just in case that thread[]
	// // contains every node once)
	// for (int i = 1; i <= lastIndexNodes; i++) {
	// string.append(i);
	// string.append("; ");
	// }
	// string.append("\n");
	//
	// // write all arcs of the Tree
	// for (int i = lastIndexArcs - 1; i >= 0; i--) {
	// arc = Tree2.get(i);
	// startIndex = arc.getStartNodeIndex();
	// endIndex = arc.getEndNodeIndex();
	// string.append(startIndex);
	// string.append("->"); // write the arcs like " 1->2 "...it means that
	// // there is an arc from 1 to 2
	// string.append(endIndex);
	// string.append(" [ label = \"[");
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
	// string.append("} \n \n");
	//
	// return string.toString();
	// }
	//
	// /**
	// * method to create a String for visualize the data structure T,L,U
	// * The arcs of T will be black, the arcs of L will be yellow and the arcs
	// of U will be blue
	// * @return String for graphviz
	// */
	// public String graphvizStringTLU() {
	// StringBuffer string = new StringBuffer(
	// "\n T,L,U String for GRAPHVIZ: \n	\n digraph TLU { \n	node [shape = circle]; ");
	// int lastIndexNodes = this.thread.length - 1;
	// int lastIndexArcsT = Tree2.size();
	// int lastIndexArcsL = L2.size();
	// // int lastIndexArcsU = U2.size();
	// Arc2 arc = new Arc2(0, 0, 0., 0., 0., 0.);
	// int startIndex = 0;
	// int endIndex = 0;
	//
	// // write all node (one node for each index...just in case that thread[]
	// // contains every node once)
	// for (int i = 1; i <= lastIndexNodes; i++) {
	// string.append(i);
	// string.append("; ");
	// }
	// string.append("\n");
	//
	// // write all arcs of the Tree. They will be black.
	// for (int i = lastIndexArcsT - 1; i >= 0; i--) {
	// arc = Tree2.get(i);
	// startIndex = arc.getStartNodeIndex();
	// endIndex = arc.getEndNodeIndex();
	// string.append(startIndex);
	// string.append("->"); // write the arcs like " 1->2 "...it means that
	// // there is an arc from 1 to 2
	// string.append(endIndex);
	// string.append(" [ label = \"[");
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
	//
	// // write all arcs of L. They will be yellow
	// for (int i = lastIndexArcsL - 1; i >= 0; i--) {
	// arc = L2.get(i);
	// startIndex = arc.getStartNodeIndex();
	// endIndex = arc.getEndNodeIndex();
	// string.append(startIndex);
	// string.append("->"); // write the arcs like " 1->2 "...it means that
	// // there is an arc from 1 to 2
	// string.append(endIndex);
	// string.append(" [color=yellow, label = \"[");
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
	//
	// // write all arcs of U. They will be red
	// // for (int i = lastIndexArcsU - 1; i >= 0; i--) {
	// // arc = U2.get(i);
	// // startIndex = arc.getStartNodeIndex();
	// // endIndex = arc.getEndNodeIndex();
	// // string.append(startIndex);
	// // string.append("->"); // write the arcs like " 1->2 "...it means that
	// // // there is an arc from 1 to 2
	// // string.append(endIndex);
	// // string.append(" [color=blue, label = \"[");
	// // string.append(arc.getLowerLimit());
	// // string.append(" / ");
	// // string.append(arc.getFlow()); // write the current flow to the arc
	// // // like " 1->2 [ label = [l/x/u] ];
	// // string.append(" / ");
	// // string.append(arc.getUpperLimit());
	// // string.append(" / ");
	// // string.append(arc.getCost());
	// // string.append(" ]\" ]; \n");
	// // }
	// string.append("} \n \n");
	//
	// return string.toString();
	// }
	//
	// /**
	// * method to create a String for visualize the datastructure by
	// highlighting
	// * the entering arc. The entering arc will be green.
	// *
	// * @return String for graphviz
	// */
	// public String graphvizStringArcs(Arc2 enteringArc) {
	// StringBuffer string = new StringBuffer(
	// "\n Entering Arc String for GRAPHVIZ: \n	\n digraph enteringArc { \n	node [shape = circle]; ");
	// int lastIndexNodes = this.thread.length - 1;
	// int lastIndexArcsT = Tree2.size();
	// int lastIndexArcsL = L2.size();
	// // int lastIndexArcsU = U2.size();
	// Arc2 arc = new Arc2(0, 0, 0., 0., 0., 0.);
	// int startIndex = 0;
	// int endIndex = 0;
	//
	// // write all node (one node for each index...just in case that thread[]
	// // contains every node once)
	// for (int i = 1; i <= lastIndexNodes; i++) {
	// string.append(i);
	// string.append("; ");
	// }
	// string.append("\n");
	//
	// // write all arcs of the Tree. They will be black.
	// for (int i = lastIndexArcsT - 1; i >= 0; i--) {
	// arc = Tree2.get(i);
	// startIndex = arc.getStartNodeIndex();
	// endIndex = arc.getEndNodeIndex();
	// string.append(startIndex);
	// string.append("->"); // write the arcs like " 1->2 "...it means that
	// // there is an arc from 1 to 2
	// string.append(endIndex);
	// if (arc.equals(enteringArc)) {
	// string.append(" [color=green, label = \"[");
	// }else {
	// string.append(" [ label = \"[");
	// }
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
	//
	// // write all arcs of L. They will be yellow
	// for (int i = lastIndexArcsL - 1; i >= 0; i--) {
	// arc = L2.get(i);
	// startIndex = arc.getStartNodeIndex();
	// endIndex = arc.getEndNodeIndex();
	// string.append(startIndex);
	// string.append("->"); // write the arcs like " 1->2 "...it means that
	// // there is an arc from 1 to 2
	// string.append(endIndex);
	// if (arc.equals(enteringArc)) {
	// string.append(" [color=green, label = \"[");
	// }else {
	// string.append(" [color=yellow, label = \"["); //
	// }
	// string.append(arc.getLowerLimit());
	// string.append(" / ");
	// string.append(arc.getFlow()); // write the current flow to the arc
	// // like " 1->2 [ label = [l/x/u/c] ];
	// string.append(" / ");
	// string.append(arc.getUpperLimit());
	// string.append(" / ");
	// string.append(arc.getCost());
	// string.append(" ]\" ]; \n");
	// }
	//
	// // write all arcs of U. They will be red
	// // for (int i = lastIndexArcsU - 1; i >= 0; i--) {
	// // arc = U2.get(i);
	// // startIndex = arc.getStartNodeIndex();
	// // endIndex = arc.getEndNodeIndex();
	// // string.append(startIndex);
	// // string.append("->"); // write the arcs like " 1->2 "...it means that
	// // // there is an arc from 1 to 2
	// // string.append(endIndex);
	// // if (arc.equals(enteringArc)) {
	// // string.append(" [color=green, label = \"[");
	// // } else {
	// // string.append(" [color=blue, label = \"[");
	// // }
	// // string.append(arc.getLowerLimit());
	// // string.append(" / ");
	// // string.append(arc.getFlow());
	// // // like " 1->2 [ label = [l/x/u] ];
	// // string.append(" / ");
	// // string.append(arc.getUpperLimit());
	// // string.append(" / ");
	// // string.append(arc.getCost();
	// // string.append(" ]\" ]; \n");
	// // }
	// string.append("} \n \n");
	//
	// return string.toString();
	// }

}
