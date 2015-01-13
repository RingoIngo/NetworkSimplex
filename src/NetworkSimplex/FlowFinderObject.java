package NetworkSimplex;

public class FlowFinderObject {

	public Arc2 leavingArc;
	public boolean forwardEdge;
	public double epsilon;
	
	
	public FlowFinderObject(Arc2 leavingArc, boolean forwardEdge, double epsilon) {
		this.leavingArc = leavingArc;
		this.forwardEdge = forwardEdge;
		this.epsilon = epsilon;
	}


	@Override
	public String toString() {
		return "FlowFinderObject [leavingArc=" + leavingArc + ", forwardEdge="
				+ forwardEdge + ", epsilon=" + epsilon + "]";
	}
	
	
}
