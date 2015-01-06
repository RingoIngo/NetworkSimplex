package NetworkSimplex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Reader {
	
	private TreeSolution tree;
	
	public Reader(String filename) throws IOException {
		
		System.out.println("invoke input stream");
		InputStream stream = this.getClass().getResource(filename).openStream();
		
		System.out.println("invoke buffered reader");
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		
		String line;
		System.out.println("start loop to read in sinlge lines");
		while((line = reader.readLine()) != null) {
			
		}
	}
	
	public TreeSolution getTreeSolution() {
		return this.tree;
	}
}
