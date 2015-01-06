package NetworkSimplex;

import java.io.IOException;

public class Algorithm {
	
	
	public static void main(String[] args) {
		try {
			Reader reader = new Reader(args[0]);
			TreeSolution tree = reader.getTreeSolution();
			
			//while(tree.updateTreeSolution());
			
			//maybe write the solution now to a file or so
			
		} catch (IOException e) {
			System.out.println("file not found");
			e.printStackTrace();
		}
		
	}

}
