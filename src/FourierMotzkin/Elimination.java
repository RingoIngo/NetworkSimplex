package FourierMotzkin;

import java.util.Iterator;
import java.util.LinkedList;

public class Elimination {

	private LinkedList<Integer> P;

	private LinkedList<Integer> N;

	private LinkedList<Integer> Z;

	// the matrix with all coefficients of the conditions and the vector b in
	// the last column.
	private double[][] conditions;

	int[] eliminationVariables;

	// maybe not necessary
	private int numberOfConditions;

	// maybe not necessary
	boolean oneVariableWillBeLeft;

	/**
	 * This contructor needed informations the Reader read in out of the
	 * inputfile. This is the coefficientsmatrix with vector b in it and the
	 * variables we have to eliminate
	 */
	public Elimination(double[][] conditions, int[] eliminationVariables) {
		this.conditions = conditions;
		this.eliminationVariables = eliminationVariables;
	}

	/**
	 * getter method for conditions
	 * 
	 * @return matrixA
	 */
	public double[][] getConditions() {
		return this.conditions;
	}

	/**
	 * getter method for elimination variables
	 */
	public int[] getEliminationVariables() {
		return this.eliminationVariables;
	}

	/**
	 * 
	 * @return
	 */
	public int getNumberOfConditions() {
		return this.getConditions().length;
	}

	/**
	 * assign the lines to P, N, Z depending on elVar
	 */
	public void assign(int elIndex) {

		LinkedList<Integer> P = new LinkedList<Integer>();
		LinkedList<Integer> N = new LinkedList<Integer>();
		LinkedList<Integer> Z = new LinkedList<Integer>();

		for (int i = 0; i < this.conditions.length; i++) {
			if (this.conditions[i][elIndex] < 0) {
				N.add(i);
			} else if (this.conditions[i][elIndex] > 0) {
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
	public void scale(int elIndex) {

		for (int i = 0; i < conditions.length; i++) {

			double n = Math.abs(this.conditions[i][elIndex]); // because we dont
																// want change
																// the
																// inequality <=
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
		for (int i = 0; i < dummy.length - 1; i++) {
			if (dummy[i] != 0)
				return true;
		}
		return false;
	}

	public boolean testEqualOrRedundant(double[][] solution, double[] dummy,
			int i) {
		boolean equal = true;

		for (int j = 0; j < i; i++) {

			for (int k = 0; k < dummy.length - 1; k++) {
				if (dummy[k] != solution[j][k]) {
					equal = false;
					break;
				}
			}
			if (equal) {
				if (dummy[dummy.length - 1] >= solution[j][dummy.length - 1])
					return equal;
			}
		}
		return false;
	}

	public double[][] testEqualOrRedundant2(double[][] solution,
			double[] dummy, int i) {
		boolean equal = true;

		for (int j = 0; j < i; j++) {

			for (int k = 0; k < dummy.length - 1; k++) {
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
		this.numberOfConditions++;
		return solution;
	}

	/**
	 * checks, if the coefficients of the arrays (with the same size) are equal
	 * (not the b part)
	 * 
	 * @param array1
	 * @param array2
	 * @return
	 */
	public boolean testVariablesAreEqual(double[] array1, double[] array2) {

		for (int i = 0; i < array1.length - 1; i++) {
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

		int lineSize = this.conditions[1].length;
		int numberOfLines = this.conditions.length;

		double[][] solution = new double[numberOfLines][lineSize];

		int numberOfConditions = 0;
		boolean putIn = false;

		int n = 0; // n will give us the index of the line where the first line
					// was inserted

		// put the first nonzero line in solution
		// (go through the lines of 'conditions' and find the first nonzero
		// line)
		for (int i = 0; i < numberOfLines; i++) {
			if (this.testIsNotZero(this.conditions[i])) {
				solution[0] = this.conditions[i];
				numberOfConditions++;
				n = i;
				break;
			}
		}

		// put all the other lines in matrix
		for (int i = n + 1; i < numberOfLines; i++) { // take one line in
														// conditions
			putIn = true;
			// test if the line is zero (or even the coefficients of the
			// variables
			if (testIsNotZero(this.conditions[i])) {

				// compare to all lines in solution
				for (int j = 0; j < numberOfConditions; j++) {

					// test if the coefficients of the variables are equal
					if (testVariablesAreEqual(solution[j], this.conditions[i])) {
						putIn = false; // we cant put the line in the solution
										// (just if the b is less or equal)

						// redundant test:
						// if the b of the new line is less or equal than the
						// line in solution put the condition line instead of
						// the matrix line
						if (solution[j][lineSize - 1] >= this.conditions[i][lineSize - 1]) {
							solution[j][lineSize - 1] = this.conditions[i][lineSize - 1];
							break;
						}
					}
				}
				// if we didnt swap the line already we can put it in a new
				// line
				if (putIn) {
					
					// the line is not zero, equal to another one in solution
					// and not redundant
					// put it in solution
					solution[numberOfConditions] = this.conditions[i];

					// it is a new condition
					numberOfConditions++;
				}
			}
		}

		// now we create a copy of the matrix up to numberOfConditions. So we
		// cut off the 0-lines in solution
		double[][] solutionFinal = new double[numberOfConditions][lineSize];

		for (int i = 0; i < numberOfConditions; i++) {
			solutionFinal[i] = solution[i];
		}

		this.conditions = solutionFinal;
	}

	/**
	 * 
	 */
	public void lastClearZeroEqualRedundant() {

		int lastVariableIndex = -1;
		for (int i = 0; i < this.conditions[1].length; i++) {
			if (this.conditions[1][i] != 0) {
				lastVariableIndex = i;
				break;
			}
		}

		this.scale(lastVariableIndex);
		this.clearZeroEqualRedundant();

	}

	/**
	 * This method scales the matrix and puts all lines of Z and all combination
	 * of N and P in the new matrix
	 * 
	 * @param elVar
	 *            the index we have to eliminate
	 * @return
	 */
	public void eliminate(boolean lastElimination, int elIndex) {

		assign(elIndex); // fill N,P,Z
		System.out.println("Z: " + this.toStringList(Z));
		System.out.println("N: " + this.toStringList(N));
		System.out.println("P: " + this.toStringList(P));

		int lineSize = this.conditions[1].length; // number of
													// variables in the
													// problem

		this.scale(elIndex); // scale matrix

		// Create a new bigger matrix
		double[][] solution = new double[N.size() * P.size() + Z.size()][lineSize];

		// insert lines from Z to the new matrix
		int i = 0; // gives us the position for the next line
		for (int x = 0; x < Z.size(); x++) {
			solution[i] = this.conditions[Z.get(x)];
			i++;
		}

		// insert the combination of lines from N and P in the matrix
		for (int l = 0; l < N.size(); l++) {
			for (int k = 0; k < P.size(); k++) {
				for (int j = 0; j < lineSize; j++) {
					solution[i][j] = this.conditions[N.get(l)][j]
							+ this.conditions[P.get(k)][j];
				}
				i++;
			}
		}
		this.conditions = solution;

		System.out.println("\n BEFORE CLEAR: \n");
		System.out.println(this.toStringConditions());

		this.clearZeroEqualRedundant();

		System.out.println("\n AFTER CLEAR: \n");
		System.out.println(this.toStringConditions());

		if (lastElimination) {
			this.lastClearZeroEqualRedundant();

			System.out.println("\n AFTER LAST CLEAR: \n");
			System.out.println(this.toStringConditions());
		}

	}

	/**
	 * Method to print the coefficients matrix
	 * 
	 * @return String which represents the coefficients matrix
	 */
	public String toStringCoefficients() {

		StringBuffer string = new StringBuffer();

		for (int j = 0; j < conditions.length; j++) {
			for (int i = 0; i < conditions[j].length - 1; i++) {
				string.append(conditions[j][i] + " ");
			}
			string.append("\n \n");
		}

		return string.toString();
	}

	/**
	 * Method to print the conditions
	 * 
	 * @return String which contains all the conditions we can create out of
	 *         'conditions' line by line
	 */
	public String toStringConditions() {

		StringBuffer string = new StringBuffer();

		for (int j = 0; j < conditions.length; j++) {
			string.append("( " + (j + 1) + " )      " + conditions[j][0]
					+ "*x1");

			for (int i = 1; i < conditions[j].length; i++) {

				// the last entry in a row is the constant of b
				if (i == conditions[j].length - 1) {
					string.append("<= " + conditions[j][i]);
				} else { // other entries are coefficients
					string.append(" + " + conditions[j][i] + "*x" + (i + 1)
							+ " ");
				}
			}
			string.append("\n \n");
		}

		return string.toString();
	}

	public String toStringSolution(double[][] solution) {

		StringBuffer string = new StringBuffer();

		for (int j = 0; j < solution.length; j++) {
			string.append(solution[j][0] + "*x1");

			for (int i = 1; i < solution[j].length; i++) {

				// the last entry in a row is the constant of b
				if (i == solution[j].length - 1) {
					string.append("<= " + solution[j][i]);
				} else { // other entries are coefficients
					string.append(" + " + solution[j][i] + "*x" + (i + 1) + " ");
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
			string.append("X" + (eliminationVariables[i] + 1) + " ");
		}
		string.append("\n \n");
		return string.toString();
	}

	public String toStringList(LinkedList<Integer> list) {
		StringBuffer string = new StringBuffer("{ ");
		Iterator<Integer> iter = list.iterator();
		int k = 0;
		if (iter.hasNext()) {
			k = iter.next();
			string.append((k + 1) + " ");
		}

		while (iter.hasNext()) {
			k = iter.next();
			string.append(", " + (k + 1) + " ");
		}
		string.append("}");
		return string.toString();

	}

}
