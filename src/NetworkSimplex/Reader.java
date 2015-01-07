package NetworkSimplex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Reader {
	
	private TreeSolution tree;
	Node[] nodes; 
	
	public Reader(String filename) throws IOException {
		

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
		int numberOfNodes;
		int numberOfArcs;

		
		while((line = reader.readLine()) != null) {
			System.out.println(line);
			String[] arr = line.split(" ");
			if(line.charAt(0) == 'c') {	//comment line
				//read in number of nodes and number of arcs
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
				
				//set flow to lower capacity
				int startNodeIndex = Integer.parseInt(arr[1]);
				int endNodeIndex = Integer.parseInt(arr[2]);
				Arc2 arc = new Arc2(startNodeIndex,endNodeIndex, arrDouble[0] ,upperCapacity, arrDouble[2], arrDouble[0]);
				//Arc arc = new Arc(Integer.parseInt(arr[1]),Integer.parseInt(arr[2]),arrDouble[0] ,arrDouble[1], arrDouble[2], arrDouble[0]);
				
				//update nettodemands of nodes
				//nodes[startNodeIndex]
				
				//add arc to L partition
				L2.add(arc);
					
			}
		}
		
		System.out.println(VPos);
		System.out.println(VNeg);
		System.out.println(L2);
		reader.close();
	}

	public TreeSolution getTreeSolution() {
		return this.tree;
	}
}
