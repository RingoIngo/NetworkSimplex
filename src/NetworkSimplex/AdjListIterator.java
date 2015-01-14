package NetworkSimplex;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class AdjListIterator implements Iterator<Arc> {
	
	private  LinkedList<Arc>[] adjListArray;
	private int nodeIndex;
	private Iterator<Arc> iterator;

	public void setData(LinkedList<Arc>[] adjListArray){
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
	public Arc next() throws NoSuchElementException {
		if (!hasNext()) {
		      throw new NoSuchElementException("No more elements");
		    }
		Arc arc = iterator.next();
		//
		if(!iterator.hasNext()){
			if(++this.nodeIndex < adjListArray.length)
				this.iterator = this.adjListArray[this.nodeIndex].iterator();
		}
		return arc;
	}

}
