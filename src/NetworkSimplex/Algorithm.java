package NetworkSimplex;

import java.io.IOException;

public class Algorithm {
	
	
	public static void main(String[] args) {
		try {
			long startTime = System.currentTimeMillis();
			Reader reader = new Reader(args[0]);
			TreeSolution tree = reader.getTreeSolution();
			int numberOfITeration=0;
			System.out.println("\n\n\n\n\n iteration:");
			System.out.println(numberOfITeration);
			while(tree.iterate()){
				++numberOfITeration;
//				System.out.println(tree.numberOfIterations);
//				if(numberOfITeration==8) {
					System.out.println("GraphViz string");
					System.out.println(tree.graphvizStringTree());
//				}
					System.out.println("\n\n\n\n\n iteration:");
					System.out.println(numberOfITeration);
			};
			long endTime = System.currentTimeMillis();
			System.out.println(tree);
			System.out.println("\nthe costs of this soultion are:");
			System.out.println("the way the costs are calculated might be wrong, so dont really rely on that. method was written late at night:)");
			System.out.println(tree.getCosts());
			System.out.println("That took " + (endTime - startTime) + " milliseconds");
			//maybe write the solution now to a file or so
			
//			System.out.println("\033[0;0mhallo\033[0;1m");
			
		} catch (IOException e) {
			System.out.println("file not found");
			e.printStackTrace();
		}
		
	}

}

