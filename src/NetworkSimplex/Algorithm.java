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
//			System.out.println(tree.graphvizStringTree());
			while(tree.iterate()){
				++numberOfITeration;
//				System.out.println("GraphViz string");
//				System.out.println(tree.graphvizStringTree());
				System.out.println("\n\n\n\n\n iteration:");
				System.out.println(numberOfITeration);
			};
			long endTime = System.currentTimeMillis();
			System.out.println(tree);
			System.out.println("\nthe costs of this soultion are:");
			System.out.println(tree.getCosts());
//			assertOptimal(tree);
			System.out.println("That took " + (endTime - startTime) + " milliseconds");
			//maybe write the solution now to a file or so
//			System.out.println("solution is feasable:");
//			System.out.println(tree.solutionFeasable());
//			System.out.println("UWasNotEmptyBefore:");
//			System.out.println(tree.UWasNotEmptyBefore);

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

