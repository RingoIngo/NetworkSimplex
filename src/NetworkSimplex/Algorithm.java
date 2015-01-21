package NetworkSimplex;

import java.io.IOException;
import java.util.Iterator;

public class Algorithm {


	public static void main(String[] args) {
		try {
			long startTime = System.currentTimeMillis();
			Reader reader = new Reader(args[0]);
			TreeSolution tree = reader.getTreeSolution();
			int numberOfITeration=0;
			System.out.println("\n\n\n\n\n iteration:");
			System.out.println(numberOfITeration);
			System.out.println(tree);
			while(tree.iterate()){	// iterate while there is a entering arc
				++numberOfITeration;	// count number of iterations
//				System.out.println("GraphViz string");
//				System.out.println(tree.graphvizStringTree()); // to print the Tree for graph viz
				System.out.println("\n\n\n\n\n iteration:");
				System.out.println(numberOfITeration);
			};
			long endTime = System.currentTimeMillis();
			System.out.println(tree);
			System.out.println("\nthe costs of this solution are:");
			System.out.println(tree.getCosts());
//			assertOptimal(tree);	// check if the tree solution is optimal
			System.out.println("That took " + (endTime - startTime) + " milliseconds");
			System.out.println("solution is feasable:");
			System.out.println(tree.solutionFeasable());	// checks if the tree solution is feasable (no flow on artificial arcs)
			System.out.println("UWasNotEmptyBefore:");
			System.out.println(tree.UWasNotEmptyBefore);	// gives us the information if U was empty anytime before

		} catch (IOException e) {
			System.out.println("file not found");
			e.printStackTrace();
		}

	}

	private static void assertOptimal(TreeSolution tree){
		Iterator<Arc> iterator = tree.L.iterator();
		while(iterator.hasNext()){
			assert iterator.next().getReducedCosts() >= 0;
		}
		iterator = tree.U.iterator();
		while(iterator.hasNext()){
			assert iterator.next().getReducedCosts() <= 0;
		}
	}

}

