package FourierMotzkin;

import java.util.LinkedList;

public class Elimination {

	private LinkedList<Integer> P;

	private LinkedList<Integer> N;

	private LinkedList<Integer> Z;

	// an array with all variables we have to eliminate
	private int[] eliminationVariables;

	// the matrix with all coefficients of the conditions and the vector b in
	// the last column.
	// to keep it easy, the first column and the first row is 0
	private double[][] conditions;

	private int numberOfConditionsNotZero;

	/**
	 * This contructor needed informations the Reader read in out of the
	 * inputfile. This is the coefficientsmatrix with vector b in it and the
	 * variables we have to eliminate
	 */
	public Elimination(double[][] conditions, int[] eliminationVariables) {
		this.eliminationVariables = eliminationVariables;
		this.conditions = conditions;
	}

	/**
	 * getter method for elimination variables
	 * 
	 * @return eliminationVariables
	 */
	public int[] getEliminationVariables() {
		return this.eliminationVariables;
	}

	/**
	 * getter method for matrix A
	 * 
	 * @return matrixA
	 */
	public double[][] getConditions() {
		return this.conditions;
	}

	/**
	 * 
	 * @return
	 */
	public int getNumberOfConditions() {
		return this.getConditions().length - 1;
	}

	/**
	 * assign the lines to P, N, Z depending on elVar
	 */
	public void assign(int elVar) {

		LinkedList<Integer> P = new LinkedList<Integer>();
		LinkedList<Integer> N = new LinkedList<Integer>();
		LinkedList<Integer> Z = new LinkedList<Integer>();

		for (int i = 1; i < this.conditions.length; i++) {
			if (this.conditions[i][elVar] < 0) {
				N.add(i);
			} else if (this.conditions[i][elVar] > 0) {
				P.add(i);
			} else {
				Z.add(i);
			}
		}

		this.P = P;
		this.N = N;
		this.Z = Z;

	}

	/**
	 * Scale the lines of the matrix, so that the row with index elVar only
	 * contain {-1,0,1}
	 */
	public void scale(int elVar) {

		for (int i = 1; i < conditions.length; i++) {

			double n = this.conditions[i][elVar];
			if (n < 0)
				n = -n;
			for (int j = 0; j < this.conditions[1].length; j++) {

				if (n != 0)
					this.conditions[i][j] = this.conditions[i][j] / n;

			}
		}
	}

	/**
	 * tests if a linevector is zero
	 * 
	 * @param dummy
	 * @return
	 */
	public boolean testIsNotZero(double[] dummy) {
		boolean nonZero = false;
		int i = 0;
		while (i < dummy.length - 1) {
			if (dummy[i] != 0)
				nonZero = true;
			i++;
		}

		return nonZero;
	}

	public boolean testEqualOrRedundant(double[][] solution, double[] dummy,
			int i) {
		boolean Equal = true;

		for (int j = 1; j < i; i++) {

			for (int k = 1; k < dummy.length - 1; k++) {
				if (dummy[k] != solution[j][k]) {
					Equal = false;
					break;
				}
			}
			if (Equal) {
				if (dummy[dummy.length - 1] >= solution[j][dummy.length - 1])
					return Equal;
			}
		}
		return false;
	}

	public double[][] testEqualOrRedundant2(double[][] solution,
			double[] dummy, int i) {
		boolean equal = true;

		for (int j = 1; j < i; j++) {

			for (int k = 1; k < dummy.length - 1; k++) {
				if (dummy[k] != solution[j][k]) {
					equal = false;
					break;
				}
			}
			if (equal) {
				if (dummy[dummy.length - 1] >= solution[j][dummy.length - 1]) {
					return solution; // no change necessary
				} else {
					solution[j] = dummy; // change line j with dummy
					return solution;
				}
			}

		}
		solution[i] = dummy;
		this.numberOfConditionsNotZero++;
		return solution;
	}

	/**
	 * checks, if the coefficients of the arrays are equal (not the b part)
	 * 
	 * @param array1
	 * @param array2
	 * @return
	 */
	public boolean testIsEqual(double[] array1, double[] array2) {

		for (int i = 0; i < array1.length; i++) {
			if (array1[i] != array2[i]) {
				return false;
			}
		}
		return true;
	}
	

	/**
	 * Method to clear the conditions. We delete all 0-lines, redundant lines
	 * and equal lines
	 */

	public void clearZeroEqualRedundant() {

		int columns = this.conditions[1].length;
		int rows = this.conditions.length;

		double[][] matrix = new double[rows][columns];

		int numberOfConditionsNotZero = 0;

		// put the first nonzero line in matrix
		for (int i = 1; i < rows; i++) {
			if (this.testIsNotZero(this.conditions[i])) {
				matrix[1] = this.conditions[i];
				numberOfConditionsNotZero++;
				break;
			}
		}

		// put all the other lines in matrix (when not zero, redundant or equal
		// to another one)
		for (int i = 2; i < rows; i++) { // take one line in conditions

			// test if zero
			if (testIsNotZero(this.conditions[i])) {

				for (int j = 1; j <= numberOfConditionsNotZero; j++) { // and
																		// compare
																		// to
																		// all
																		// lines
																		// in
																		// matrix

					// test if the coefficients are equal
					if (testIsEqual(matrix[j], this.conditions[i])) {

						// if the b of the new line is less or equal than the
						// line in the matrix put the condition line instead of
						// the matrix line
						if (matrix[j][columns - 1] >= this.conditions[i][columns - 1]) {
							matrix[j] = this.conditions[i];
							break;
						}
					}
				}
				
				// it is a new condition
				numberOfConditionsNotZero++;
				
				// put it in the matrix
				matrix[numberOfConditionsNotZero] = this.conditions[i];
			}
		}

	}

	/**
	 * 
	 * @param system
	 *            Matrix der Ungleichungen
	 * @param elVar
	 *            zu eliminierende Variable
	 * @return
	 */

	public void eliminate(int elVar) {

		assign(elVar); // fill N,P,Z
		System.out.println(Z.toString());
		System.out.println(N.toString());
		System.out.println(P.toString());

		int length = this.conditions[1].length; // number of
												// variables in the
												// problem
		this.numberOfConditionsNotZero = 0; // count the number of conditions
											// that are not zero

		this.scale(elVar); // scale matrix

		// Create a new bigger matrix
		double[][] solution = new double[N.size() * P.size() + Z.size() + 1][length];
		for (int j = 0; j < this.conditions[0].length; j++) {
			solution[0][j] = 0; // Fill first line with 0
		}

		// insert lines from Z to the new matrix

		int i = 1;
		for (int x = 1; x <= Z.size(); x++) {

			double[] dummy = new double[length];
			dummy = this.conditions[Z.get(x - 1)];

//			if (testIsNotZero(dummy)) {
//
//				solution = testEqualOrRedundant2(solution, dummy, i);
//				i++;
//
//			}
			solution[i] = dummy;
		}

		// insert the combination of lines from N and P in the matrix

		for (int l = 0; l < N.size(); l++) {

			for (int k = 0; k < P.size(); k++) {

				double[] dummy = new double[length];

				for (int j = 0; j < dummy.length; j++) {
					dummy[j] = this.conditions[N.get(l)][j]
							+ this.conditions[P.get(k)][j];

				}
//				if (testIsNotZero(dummy)) { // test if all variables are zero
//					// solution[i] = dummy; // if not insert the line in our
//					// matrix
//
//					solution = testEqualOrRedundant2(solution, dummy, i);
//					i++;
//
//					// if (!testIsNotZero(solution[i])) i++;
//
//				}
				solution[i] = dummy;
				i++;
			}
		}
		System.out.println("DAS IST SOLUTION");
		System.out.println(this.toStringSolution(solution));

//		// now create an array with the right number of conditions. so we wont
//		// have any 0-lines
//		this.conditions = new double[this.numberOfConditionsNotZero + 1][length];
//
//		// copy the non-0-lines from solution to conditions
//		for (int j = 1; j <= this.numberOfConditionsNotZero; j++) {
//			this.conditions[j] = solution[j];
//		}
		
		this.conditions = solution;
		this.clearZeroEqualRedundant();
	}

	/**
	 * Method to print the coefficients matrix
	 * 
	 * @return String which represents the coefficients matrix
	 */
	public String toStringCoefficients() {

		StringBuffer string = new StringBuffer();

		for (int j = 1; j < conditions.length; j++) {
			for (int i = 1; i < conditions[j].length - 1; i++) {
				string.append(conditions[j][i] + " ");
			}
			string.append("\n \n");
		}

		return string.toString();
	}

	/**
	 * Method to print the conditions
	 * 
	 * @return String which contains all the conditions we can create out of the
	 *         matrix line by line
	 */
	public String toStringConditions() {

		StringBuffer string = new StringBuffer();

		for (int j = 1; j < conditions.length; j++) {

			for (int i = 1; i < conditions[j].length; i++) {

				// the last entry in a row is the constant of b
				if (i == conditions[j].length - 1) {
					string.append("<= " + conditions[j][i]);
				} else { // other entries are coefficients
					string.append(conditions[j][i] + "*x" + i + " ");
				}
			}
			string.append("\n \n");
		}

		return string.toString();
	}

	public String toStringSolution(double[][] solution) {

		StringBuffer string = new StringBuffer();

		for (int j = 1; j < solution.length; j++) {

			for (int i = 1; i < solution[j].length; i++) {

				// the last entry in a row is the constant of b
				if (i == solution[j].length - 1) {
					string.append("<= " + solution[j][i]);
				} else { // other entries are coefficients
					string.append(solution[j][i] + "*x" + i + " ");
				}
			}
			string.append("\n \n");
		}

		return string.toString();
	}

	/**
	 * Method to print the elimination variables
	 * 
	 * @return String which contains all elimination variables
	 */
	public String toStringEliminationVariables() {

		StringBuffer string = new StringBuffer();

		for (int i = 0; i < eliminationVariables.length; i++) {
			string.append("X" + eliminationVariables[i] + " ");
		}
		string.append("\n \n");
		return string.toString();
	}

}
