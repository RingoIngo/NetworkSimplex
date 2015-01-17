package NetworkSimplex;

import java.io.IOException;

public class Algorithm {
	
	
	public static void main(String[] args) {
		try {
			Reader reader = new Reader(args[0]);
			TreeSolution tree = reader.getTreeSolution();
			int numberOfITeration=0;
			while(tree.iterate()){
				++numberOfITeration;
				System.out.println("\n\n\n\n\n iteration:");
				System.out.println(numberOfITeration);
			};
			System.out.println("\nthe costs of this soultion are:");
			System.out.println("the way the costs are calculated might be wrong, so dont really rely on that. method was written late at night:)");
			System.out.println(tree.getCosts());
			System.out.println(tree);
			//maybe write the solution now to a file or so
			
//			System.out.println("\033[0;0mhallo\033[0;1m");
			
		} catch (IOException e) {
			System.out.println("file not found");
			e.printStackTrace();
		}
		
	}

}
