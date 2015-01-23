package FourierMotzkin;

import java.util.LinkedList;


public class Elimination {
	
	private LinkedList<Integer> P;
	
	private LinkedList<Integer> N;
	
	private LinkedList<Integer> Z;
	
	/**
	 * Zuordnung der Gleichungen zu P, N, Z
	 */
	public void assign (double[][] system, int elVar) {
		int i=1;
		while (i < system.length) {
			if (system[i][elVar]<0){
				N.add(i);
			}
			else if (system[i][elVar]>0){
				P.add(i);
			}
			else {
				Z.add(i);
			}
		}
		
	}
	/** 
	 *  Skaliert die Zeilen der Matrix, sd in der Spalte der elVar nur -1,0,1 steht
	 */
	public double[][] scale (double[][] system, int elVar) {
		double [][] scaleSystem= new double [system.length][system[1].length];
		
		for (int i=0;i < system.length; i++) {
				for(int j=0; j< system[1].length; j++){
					scaleSystem[i][j] = system[i][j]/system[i][elVar];
				}
		}
		return scaleSystem;
	}
	
	
	
	/**
	 * 
	 * @param system Matrix der Ungleichungen
	 * @param elVar zu eliminierende Variable
	 * @return
	 */
	
	
	public void eliminate ( double[][] scaleSystem, int elVar) {
		double [][] solution = new double [][scaleSystem[1]]
	}
	
	
	
}