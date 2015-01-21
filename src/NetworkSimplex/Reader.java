package NetworkSimplex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class Reader {

	private TreeSolution tree;
	Node[] nodes;

	public Reader(String filename) throws IOException {

		long startTime = System.currentTimeMillis();

		// Create object of FileReader
		FileReader inputFile = new FileReader(filename);
		FileReader inputFile2 = new FileReader(filename);

		System.out.println("invoke buffered reader");
		BufferedReader reader = new BufferedReader(inputFile);
		BufferedReader reader2 = new BufferedReader(inputFile2);

		String line;
		System.out.println("start loop to read in single lines");

		ArrayList<Node> VPos = new ArrayList<Node>();
		ArrayList<Node> VNeg = new ArrayList<Node>();

		AdjacencyList ArcList = null;
		AdjacencyList L = null;
		
		int numberOfArtificialNodes = 0;	// to count the nodes we have to create
		int numberOfNodes = -1;
		int numberOfAllNodes = -1;	// number of nodes including the nodes we create additionally 
		int numberOfArcs;
		int nodeIndex = 0;
		int artificialNodeIndex;
		double maxCost = 0; // need that for the costs of the artificial arcs

		// at first we count the number of parallel arcs to know how many
		// artificial node we have to create.
		// Therefore we go through the whole file once and count the parallel arcs
		while ((line = reader2.readLine()) != null) {
			String[] arr = line.split(" ");
			if (line.charAt(0) == 'p') {		// Problem Line

				numberOfArcs = Integer.parseInt(arr[3]);
				
				ArcList = new AdjacencyList(numberOfArcs);	// to save all arcs we see in the file
				
			} else if (line.charAt(0) == 'a') { // arc description
				int startNodeIndex = Integer.parseInt(arr[1]);
				int endNodeIndex = Integer.parseInt(arr[2]);

				// if there is already an arc with that startnode and endnode
				if (ArcList.isInAdjList(startNodeIndex, endNodeIndex) != null) {
					numberOfArtificialNodes++;		// count parallel arcs
				}
				Arc arc = new Arc(startNodeIndex, endNodeIndex, 0, 0, 0, 0);
				ArcList.addEdge(arc);
			}
		}

		while ((line = reader.readLine()) != null) {
			System.out.println(line);
			String[] arr = line.split(" ");
			if (line.charAt(0) == 'p') {
				
				numberOfNodes = Integer.parseInt(arr[2]);
				numberOfAllNodes = numberOfNodes + numberOfArtificialNodes;
				
				// init node array, L and ArcList
				nodes = new Node[numberOfAllNodes + 1];
				L = new AdjacencyList(numberOfAllNodes + 1);
				
			} else if (line.charAt(0) == 'n') { // node description
				nodeIndex = Integer.parseInt(arr[1]);
				double demand = Double.parseDouble(arr[2]);
				Node node = new Node(nodeIndex, demand);
				node.setNettodemand(-demand); // the actual nettodemand is
												// updated whenever a new arc is
												// read in
				nodes[nodeIndex] = node;
				// if(demand < 0) VPos.add(node); //this is wrong, need
				// nettodemand for this
				// else VNeg.add(node);
			} else if (line.charAt(0) == 'a') { // arc description
				double[] arrDouble = new double[3];
				for (int i = 0; i < 3; i++)
					arrDouble[i] = Double.parseDouble(arr[i + 3]);

				// check if upper capacity is <0 --> Infinity
				double upperCapacity = arrDouble[1] < 0 ? Double.POSITIVE_INFINITY
						: arrDouble[1];

				maxCost = Double.max(maxCost, arrDouble[2]);
				// set flow to lower capacity
				int startNodeIndex = Integer.parseInt(arr[1]);
				int endNodeIndex = Integer.parseInt(arr[2]);

				// if there is already an arc with that startnode and endnode
				if (L.isInAdjList(startNodeIndex, endNodeIndex) != null) {
					Arc arc = L.isInAdjList(startNodeIndex, endNodeIndex);
					// create an artificial node in nodesArray and an additional
					// artificial arc
					numberOfNodes++; // gives us the next Index for the
										// artificial node
					artificialNodeIndex = numberOfNodes;
					Node node = new Node(artificialNodeIndex, 0); // artificial node
															// has demand 0
					node.setNettodemand(0);

					nodes[artificialNodeIndex] = node; // add to nodes array

					// create artificial arc with startnode = artificial node,
					// endnode = endNodeIndex; costs = 0
					// and the same lower and upper cap and the same flow
					Arc artificialArc = new Arc(artificialNodeIndex, endNodeIndex,
							arrDouble[0], upperCapacity, 0,
							arrDouble[0]);

					// add to L
					L.addEdge(artificialArc);
					
					// modify the new parallel arc: startnode = startNodeIndex,
					// endnode = artificial node; costs, flow, lower/upper cap of new parallel arc
					Arc parallelArc = new Arc(startNodeIndex, artificialNodeIndex,
							arrDouble[0], upperCapacity, arrDouble[2],
							arrDouble[0]);
					
					L.addEdge(parallelArc);
					

				} else {
					Arc arc = new Arc(startNodeIndex, endNodeIndex,
							arrDouble[0], upperCapacity, arrDouble[2],
							arrDouble[0]);
					// Arc arc = new
					// Arc(Integer.parseInt(arr[1]),Integer.parseInt(arr[2]),arrDouble[0]
					// ,arrDouble[1], arrDouble[2], arrDouble[0]);

					// add arc to L partition
					L.addEdge(arc);

					if (arc.getLowerLimit() > 0) {
						// update nettodemands of nodes
						if (nodes[startNodeIndex] == null) {
							Node node = new Node(startNodeIndex, 0); // in case
																		// that
																		// not
																		// all
																		// nodes
																		// are
																		// written
																		// in
																		// the
																		// file
							nodes[startNodeIndex] = node;
						}
						Node node = nodes[startNodeIndex];
						double nettoDemand = node.getNettodemand();
						node.setNettodemand(nettoDemand + arc.getLowerLimit());

						if (nodes[endNodeIndex] == null) {
							node = new Node(endNodeIndex, 0);
							nodes[endNodeIndex] = node;
						}
						node = nodes[endNodeIndex];
						nettoDemand = node.getNettodemand();
						node.setNettodemand(nettoDemand - arc.getLowerLimit());
					}
				}

			}
		}

		// System.out.println(VPos);
		// System.out.println(VNeg);
		// System.out.println(L);
		for (int i = 0; i < nodes.length; i++){
			System.out.println(nodes[i]);
		}
		reader.close();
		reader2.close();
		long endTime = System.currentTimeMillis();
		System.out.println("execution with LInkedList took "
				+ (endTime - startTime) + " milliseconds");

		if (numberOfNodes < 0)
			System.out.println("Fehler beim einlesen!");

		tree = new TreeSolution(L, nodes, numberOfNodes, maxCost);
	}

	public TreeSolution getTreeSolution() {
		return this.tree;
	}
}
