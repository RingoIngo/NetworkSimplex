package NetworkSimplex;

/**
 * this class is a wrapper class for the return information of the getEnteringArc
 * method of the EnteringArcFinder class
 * we dont only want to know the entering arc, but also the partition (L or U) it comes from
 * @author IG
 *
 */
public class EnteringArcObject {
	
	private Arc enteringArc;
	private boolean L;
	private boolean U;
	
	
	
	public EnteringArcObject(Arc enteringArc, boolean l, boolean u) {
		this.enteringArc = enteringArc;
		L = l;
		U = u;
	}
	
	public Arc getEnteringArc() {
		return enteringArc;
	}
	public void setEnteringArc(Arc enteringArc) {
		this.enteringArc = enteringArc;
	}
	public boolean isL() {
		return L;
	}
	public void setL(boolean l) {
		L = l;
	}
	public boolean isU() {
		return U;
	}
	public void setU(boolean u) {
		U = u;
	}

	@Override
	public String toString() {
		return "EnteringArcObject [enteringArc=" + enteringArc + ", L=" + L
				+ ", U=" + U + "]";
	}
	
	
	

}
