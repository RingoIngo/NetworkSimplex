package NetworkSimplex;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class AdjacencyList implements Iterable<Arc> {

	private LinkedList<Arc>[] adjList;
	
	
	public AdjacencyList(int numberOfNodes) {
		this.adjList = new LinkedList[numberOfNodes];
		for(int i =0; i<numberOfNodes; i++){
			this.adjList[i] = new LinkedList<Arc>();
		}
	}
	
	public void addEdge(Arc edge){
		int startNodeIndex = edge.getStartNodeIndex();
		this.adjList[startNodeIndex].add(edge);
	}
	
	public boolean removeEdge(int startNodeIndex, int endNodeIndex){
		Iterator<Arc> iterator = this.adjList[startNodeIndex].iterator();
		while(iterator.hasNext()){
			if(iterator.next().getEndNodeIndex() == endNodeIndex){
				iterator.remove();
				return true;
			}
		}
		return false;
	}
	
	public boolean removeEdge(Arc edge){
		return removeEdge(edge.getStartNodeIndex(),edge.getEndNodeIndex());
	}
	
	
	public Arc getEdge(int startNodeIndex, int endNodeIndex) {
		Iterator<Arc> iterator = this.adjList[startNodeIndex].iterator();
		Arc arc;
		while(iterator.hasNext()){
			arc = iterator.next();
			if(arc.getEndNodeIndex() == endNodeIndex){
				return arc;
			}
		}
		return null;
	}
	
	/**
	 * this method can only be used when the adjacency list is used for a TREE
	 * and not for a Network!!
	 * it looks for an arc with either startnode nodeIndex1 or startnode nodeIndex2
	 * this is only. this is only well defined in a tree, bcz there exists only one such arc
	 * in a network there might be more!
	 * @param nodeIndex1
	 * @param nodeIndex2
	 * @return
	 */
	public Arc getEdgeInTree(int nodeIndex1, int nodeIndex2){
			Arc arc = this.getEdge(nodeIndex1, nodeIndex2);
			if( arc!=null) return arc;
			else return this.getEdge(nodeIndex2, nodeIndex1);
	}
	
	public Iterator<Arc> iterator() {
		AdjListIterator iterator= new AdjListIterator();
		iterator.setData(this.adjList);
		return iterator;
	}
	
	/**
	 * an iterator starting at the specified node index
	 * @param nodeIndex
	 * @return
	 */
	public Iterator<Arc> iterator(int nodeIndex){
		AdjListIterator iterator = new AdjListIterator(nodeIndex, this.adjList);
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

