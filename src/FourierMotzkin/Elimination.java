package FourierMotzkin;

import java.util.Iterator;
import java.util.LinkedList;

public class Elimination {

	// contains all lines with positive coefficients of the current elimination
	// variable
	private LinkedList<Integer> P;

	// contains all lines with negative coefficients of the current elimination
	// variable
	private LinkedList<Integer> N;

	// contains all lines with 0 coefficients of the current elimination
	// variable
	private LinkedList<Integer> Z;

	// the matrix with all coefficients of the conditions and the vector b in
	// the last column.
	private double[][] conditions;

	// the elimination variables in an array
	int[] eliminationVariables;

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
	 * assign the lines to P, N, Z depending on elIndex
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
			for (int j = 0; j < this.conditions[0].length; j++) {

				if (n != 0)
					this.conditions[i][j] = this.conditions[i][j] / n;

			}
		}
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
	 * Method to clear the conditions. We delete all redundant lines and equal
	 * lines
	 */
	public void clearEqualOrRedundant() {

		int lineSize = this.conditions[0].length;
		int numberOfLines = this.conditions.length;

		double[][] solution = new double[numberOfLines][lineSize];

		// we count the conditions to create later a matrix with the necessary
		// dimensions
		int numberOfConditions = 0;

		// a boolean to note if we have to put in the condition in the solution
		// or not (then we swapped it already)
		boolean putIn = false;

		// put the first condition in the solution
		solution[0] = this.conditions[0];
		numberOfConditions++;

		// put all the other lines in matrix
		for (int k = 1; k < numberOfLines; k++) { // take one line in
													// conditions

			putIn = true;

			// compare to all lines in solution
			for (int j = 0; j < numberOfConditions; j++) {

				// test if the coefficients of the variables are equal
				if (testVariablesAreEqual(solution[j], this.conditions[k])) {
					putIn = false; // we cant put the line in the solution
									// (just if the b is less or equal)

					// redundant test:
					// if the b of the new line is less or equal than the
					// line in solution put the condition line instead of
					// the matrix line
					if (solution[j][lineSize - 1] >= this.conditions[k][lineSize - 1]) {
						solution[j][lineSize - 1] = this.conditions[k][lineSize - 1];
						break;
					}
				}
			}

			// if we didnt swap the line already we can put it in a new
			// line
			if (putIn) {

				// the line is not equal to another one in solution
				// and not redundant
				// put it in solution
				solution[numberOfConditions] = this.conditions[k];

				// it is a new condition
				numberOfConditions++;
			}
		}

		// now we create a copy of the matrix up to numberOfConditions. So we
		// cut off the 0-lines in solution
		double[][] solutionFinal = new double[numberOfConditions][lineSize];

		// copy all conditions
		for (int i = 0; i < numberOfConditions; i++) {
			solutionFinal[i] = solution[i];
		}

		this.conditions = solutionFinal;
	}

	/**
	 * This method scales the matrix and puts all lines of Z and all combination
	 * of N and P in the new matrix
	 * 
	 * @param elVar
	 *            the index we have to eliminate
	 * @return
	 */
	public void eliminate(boolean oneVariableWillBeLeft, int elIndex) {

		// scale the conditions with respect to the elimination index and clear
		this.scale(elIndex);
		this.clearEqualOrRedundant();

		assign(elIndex); // fill N,P,Z

		System.out.println("\n These are the sets of the conditions: \n ");
		System.out.println("Z has size " + Z.size() + " and contains: "
				+ this.toStringList(Z));
		System.out.println("N has size " + N.size() + " and contains: "
				+ this.toStringList(N));
		System.out.println("P has size " + P.size() + " and contains: "
				+ this.toStringList(P) + "\n");

		if (N.isEmpty() || P.isEmpty()) {
			System.out
					.println("\n >>>> NO ELIMINATION POSSIBLE WITH THESE CONDITIONS \n");
			return;
		}

		int lineSize = this.conditions[0].length; // number of
													// variables in the
													// problem

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

		// take a clear to the matrix. if there is one variable left and we do
		// the last elimination, we execute the special elimination
		if (oneVariableWillBeLeft) {
			this.specialClearEqualOrRedundant();
		} else {
			this.clearEqualOrRedundant();
		}

		System.out.println("\n >>>> ELIMINATION COMPLETE \n ");

		System.out.println("\n After the " + (i + 1) + ". elimination we have "
				+ this.conditions.length + " conditions \n");
	}

	/**
	 * This method is for the case we eliminate the last variable and there will
	 * be one variable left. In that case we scale the last 'uneliminated'
	 * variable to 1 or -1 an clear one again. So we get a minimal number of
	 * final conditions.
	 */
	public void specialClearEqualOrRedundant() {

		int lastVariableIndex = -1;

		// find the last variable != 0
		for (int i = 0; i < this.conditions[0].length; i++) {
			if (this.conditions[0][i] != 0) {
				lastVariableIndex = i;
				break;
			}
		}

		// scale the conditions and clear once again
		this.scale(lastVariableIndex);
		this.clearEqualOrRedundant();

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
		string.append("\n");
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
