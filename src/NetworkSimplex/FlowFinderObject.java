package NetworkSimplex;

public class FlowFinderObject {

	public Arc leavingArc;
	public boolean forwardEdge;
	public double epsilon;
	
	
	public FlowFinderObject(Arc leavingArc, boolean forwardEdge, double epsilon) {
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
