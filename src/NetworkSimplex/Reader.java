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
		
		//Create object of FileReader
		FileReader inputFile = new FileReader(filename);

		System.out.println("invoke buffered reader");
		BufferedReader reader = new BufferedReader(inputFile);

		String line;
		System.out.println("start loop to read in sinlge lines");

		ArrayList<Node> VPos = new ArrayList<Node>();
		ArrayList<Node> VNeg = new ArrayList<Node>();
		ArrayList<Arc> L = new ArrayList<Arc>();
		ArrayList<Arc2> L2 = new ArrayList<Arc2>();
//		LinkedList<Arc2> L2 = new LinkedList<Arc2>();
		int numberOfNodes= -1;
		int numberOfArcs;
		double maxCost = 0; // need that for the costs of the artificial arcs

		while((line = reader.readLine()) != null) {
			System.out.println(line);
			String[] arr = line.split(" ");
			if(line.charAt(0) == 'p') {	
				numberOfNodes = Integer.parseInt(arr[2]);
				numberOfArcs = Integer.parseInt(arr[3]);
				//init node array
				nodes = new Node[numberOfNodes+1];
			}

			else if (line.charAt(0) == 'n') { //node description
				int nodeIndex = Integer.parseInt(arr[1]);
				double demand = Double.parseDouble(arr[2]);
				Node node = new Node(nodeIndex, demand);
				node.setNettodemand(demand);	//the actual nettodemand is updated whenever a new arc is read in
				nodes[nodeIndex] = node;
				//				if(demand < 0) VPos.add(node);		//this is wrong, need nettodemand for this
				//				else VNeg.add(node);
			}

			else if(line.charAt(0) == 'a'){	//arc description
				double[] arrDouble = new double[3];
				for(int i = 0; i<3 ; i++) arrDouble[i] = Double.parseDouble(arr[i+3]);

				//check if upper capacity is <0 --> Infinity
				double upperCapacity = arrDouble[1] < 0? Double.POSITIVE_INFINITY : arrDouble[1];

				maxCost = Double.max(maxCost, arrDouble[2]);
				//set flow to lower capacity
				int startNodeIndex = Integer.parseInt(arr[1]);
				int endNodeIndex = Integer.parseInt(arr[2]);
				Arc2 arc = new Arc2(startNodeIndex,endNodeIndex, arrDouble[0] ,upperCapacity, arrDouble[2], arrDouble[0]);
				//Arc arc = new Arc(Integer.parseInt(arr[1]),Integer.parseInt(arr[2]),arrDouble[0] ,arrDouble[1], arrDouble[2], arrDouble[0]);

							
				//add arc to L partition
				L2.add(arc); //and this seems to be also quite time consuming
				
				
				if(arc.getLowerLimit() > 0){		//this code snippet takes A LOT OF TIME!!!
					//update nettodemands of nodes
					if(nodes[startNodeIndex] == null){
						Node node = new Node(startNodeIndex, 0);
						nodes[startNodeIndex] = node;
					}
					Node node = nodes[startNodeIndex];
					double nettoDemand = node.getNettodemand();
					node.setNettodemand(nettoDemand + arc.getLowerLimit());


					if(nodes[endNodeIndex] == null){
						node = new Node(endNodeIndex, 0);
						nodes[endNodeIndex] = node;
					}
					node = nodes[endNodeIndex];
					nettoDemand = node.getNettodemand();
					node.setNettodemand(nettoDemand - arc.getLowerLimit());
				}

			}
		}

		//		System.out.println(VPos);
		//		System.out.println(VNeg);
//		System.out.println(L2);
		for(int i =0; i< nodes.length; i++)
			System.out.println(nodes[i]);
		reader.close();
		long endTime = System.currentTimeMillis();
		System.out.println("execution with LInkedList took " + (endTime - startTime) + " milliseconds");
		
		if(numberOfNodes<0)
			System.out.println("Fehler beim einlesen!");
		
		TreeSolution treeSolution = new TreeSolution(L2, nodes, numberOfNodes,maxCost);
		System.out.println(treeSolution.toString());
	}

	public TreeSolution getTreeSolution() {
		return this.tree;
	}
}
