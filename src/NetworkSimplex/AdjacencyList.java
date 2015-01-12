package NetworkSimplex;

import java.util.Iterator;
import java.util.LinkedList;

public class AdjacencyList implements Iterable<Arc2> {

	private LinkedList<Arc2>[] adjList;
	
	
	public AdjacencyList(int numberOfNodes) {
		this.adjList = new LinkedList[numberOfNodes];
		for(int i =0; i<numberOfNodes; i++){
			this.adjList[i] = new LinkedList<Arc2>();
		}
	}
	
	public void addEdge(int startNodeIndex, Arc2 edge){
		this.adjList[startNodeIndex].add(edge);
	}
	
	public boolean removeEdge(int startNodeIndex, int endNodeIndex){
		Iterator<Arc2> iterator = this.adjList[startNodeIndex].iterator();
		while(iterator.hasNext()){
			if(iterator.next().getEndNodeIndex() == endNodeIndex){
				iterator.remove();
				return true;
			}
		}
		return false;
	}
	
	public Iterator<Arc2> iterator() {
		AdjListIterator iterator= new AdjListIterator();
		iterator.setData(this.adjList);
		return iterator;
	}
	
	public String toString() {
		StringBuffer string = new StringBuffer("AdjacencyList:\nNumber of Nodes: ");
		string.append(adjList.length);
		string.append("\nlist: ");
		for (int i=0; i<adjList.length; i++){
			string.append("Node " + i);
			string.append(adjList[i].toString());
		}
		return string.toString();
	}
	
}

