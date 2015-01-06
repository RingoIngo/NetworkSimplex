package NetworkSimplex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Reader {
	
	private TreeSolution tree;
	
	public Reader(String filename) throws IOException {
		

		//Create object of FileReader
        FileReader inputFile = new FileReader(filename);
		
		System.out.println("invoke buffered reader");
		BufferedReader reader = new BufferedReader(inputFile);
		
		String line;
		System.out.println("start loop to read in sinlge lines");
		while((line = reader.readLine()) != null) {
			System.out.println(line);
			
		}
		reader.close();
	}
	
	public TreeSolution getTreeSolution() {
		return this.tree;
	}
}
