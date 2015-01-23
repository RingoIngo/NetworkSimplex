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
	private double[][] matrixA;

	/**
	 * This contructor needed informations the Reader read in out of the
	 * inputfile. This is the coefficientsmatrix with vector b in it and the
	 * variables we have to eliminate
	 */
	public Elimination(double[][] matrixA, int[] eliminationVariables) {
		this.eliminationVariables = eliminationVariables;
		this.matrixA = matrixA;
	}

	/**
	 * getter method for elimination variables
	 * @return eliminationVariables
	 */
	public int[] getEliminationVariables() {
		return this.eliminationVariables;
	}

	/**
	 * getter method for matrix A
	 * @return	matrixA
	 */
	public double[][] getMatrixA() {
		return this.matrixA;
	}

	/**
	 * assign the lines to P, N, Z depending on elVar
	 */
	public void assign(double[][] system, int elVar) {
		int i = 1;
		while (i < system.length) {
			if (system[i][elVar] < 0) {
				N.add(i);
			} else if (system[i][elVar] > 0) {
				P.add(i);
			} else {
				Z.add(i);
			}
			i++;
		}

	}

	/**
	 * Scale the lines of the matrix, so that the row with index elVar only contain 
	 * {-1,0,1}
	 */
	public double[][] scale(double[][] system, int elVar) {
		

		for (int i = 0; i < system.length; i++) {
			for (int j = 0; j < system[1].length; j++) {
				system[i][j] = system[i][j] / system[i][elVar];
			}
		}
		return system;
	}

	/**
	 * tests if a linevector is zero 
	 * @param dummy
	 * @return
	 */
	public boolean testZero (double[] dummy){
		boolean nonZero =false;
		int i=0;
		while (i<dummy.length){
			if (dummy[i]!=0) nonZero=true;
			i++;
		}
		
		return nonZero;
	}
	
	
	/**
	 * 
	 * @param system
	 *            Matrix der Ungleichungen
	 * @param elVar
	 *            zu eliminierende Variable
	 * @return
	 */

	public double[][] eliminate ( double[][] system, int elVar) {
		
		system=scale(system, elVar);    //scale matrix
		assign(system, elVar);			// fill N,P,Z
		
		//Create a new bigger matrix
		double [][] solution = new double [N.size()*P.size()+Z.size()][system[1].length];
		for (int j=0; j<system[0].length;j++) {
			system[0][j]=0;				// Fill first line with 0
		}
		
		// insert lines from P to the new matrix
		int i=0;
		while (i < P.size()) {
			for (int j=0; j<system.length ; j++) {
				solution[i][j]=system[i][j];
			}
			i++;
		
		 }
		
		// insert the combination of lines from N and P in the matrix 
		int l=0;
		while (l < N.size()) {
			double[] dummy = new double[system[1].length]; 
				for (int k=0;k<P.size();k++){
					for (int j=0; j<dummy.length;j++){
						dummy[j]=system[N.get(l)][j]+system[P.get(k)][j];
						
					}
					if (testZero(dummy)) { 		//test if all variables are zero
						solution[i]=dummy;		// if not insert the line in our matrix
						i++;
					}
				}
			l++;
					
		}
		
		return solution;
		
		
	}
}