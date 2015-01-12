package NetworkSimplex;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class AdjListIterator implements Iterator<Arc2> {
	
	private  LinkedList<Arc2>[] adjListArray;
	private int nodeIndex;
	private Iterator<Arc2> iterator;

	public void setData(LinkedList<Arc2>[] adjListArray){
		this.adjListArray = adjListArray;
		nodeIndex = 0;
		iterator = this.adjListArray[this.nodeIndex].iterator();
	}
	
	@Override
	public boolean hasNext() {
		if(nodeIndex >= adjListArray.length)
			return false;
		while(nodeIndex<adjListArray.length){
			if(iterator.hasNext())
				return true;
			else if(nodeIndex == adjListArray.length-1)
				return false;
			else 
				iterator = adjListArray[++nodeIndex].iterator();
			
		}
		return false;
	}

	@Override
	public Arc2 next() throws NoSuchElementException {
		if (!hasNext()) {
		      throw new NoSuchElementException("No more elements");
		    }
		Arc2 arc = iterator.next();
		//
		if(!iterator.hasNext()){
			if(++this.nodeIndex < adjListArray.length)
				this.iterator = this.adjListArray[this.nodeIndex].iterator();
		}
		return arc;
	}

}
