package FourierMotzkin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Reader {

	private Elimination elimination;

	public Reader(String filename) throws IOException {

		// Create object of FileReader
		FileReader inputFile = new FileReader(filename);
		FileReader inputFile2 = new FileReader(filename);

		System.out.println("invoke buffered reader");
		BufferedReader reader = new BufferedReader(inputFile);

		// reader2 is to determine the number of elimination variables,
		// variables and conditions
		BufferedReader reader2 = new BufferedReader(inputFile2);

		String line;
		System.out.println("start loop to read in single lines");

		/**
		 * At first we determine how many variable we have to delete We found
		 * them in the first line.
		 */

		// read in the first line
		line = reader2.readLine();
		String[] arr = line.split(" ");

		// number of variables in the first line
		int numberOfEliminationVariables = arr.length;

		/**
		 * Then we determine the number of variables in the problem. We found
		 * them in the second line.
		 */

		// read in second line
		line = reader2.readLine();
		arr = line.split(" ");

		// number of variables in the second line including 1 place for b.
		int numberOfAllVariables = arr.length;

		// number of conditions is now 1
		int numberOfConditions = 1;

		/**
		 * Now we determine the number of conditions
		 */
		while ((line = reader2.readLine()) != null) {
			numberOfConditions++;
		}

		// create an array with variables to eliminate
		int[] eliminationVariables = new int[numberOfEliminationVariables];

		// create the matrix A with the coefficients. In the last column will be
		// the vector b
		double[][] matrixA = new double[numberOfConditions][numberOfAllVariables];

		/**
		 * Here comes the read in part.
		 * 
		 * At First we put all variables we have to eliminate in the array
		 */

		// read first line
		line = reader.readLine();
		arr = line.split(" ");

		// put all elimination variables in an array
		for (int i = 0; i < numberOfEliminationVariables; i++) {
			eliminationVariables[i] = Integer.parseInt(arr[i]);
		}

		/**
		 * Now put read every condition line and put all coefficients and b in a
		 * matrix To keep it easy, the first line and the first column will be
		 * 0.
		 */
		int j = 1; // the row index starts at 1
		while ((line = reader.readLine()) != null) {
			arr = line.split(" ");
			for (int i = 0; i < arr.length; i++) {
				matrixA[j][i + 1] = Double.parseDouble(arr[i]); // put coefficient
																// in the matrix
																// A
			}
			j++; // next row
		}
		reader.close();
		reader2.close();

		Elimination elimination = new Elimination(matrixA, eliminationVariables);
		this.elimination = elimination;

	}

	public Elimination getElimination() {
		return this.elimination;
	}

}