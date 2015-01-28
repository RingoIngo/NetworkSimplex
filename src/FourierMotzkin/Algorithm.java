package FourierMotzkin;

import java.io.IOException;

import FourierMotzkin.Reader;

public class Algorithm {

	public static void main(String[] args) {
		try {

			long startTime = System.currentTimeMillis();
			Reader reader = new Reader(args[0]);
			Elimination elimination = reader.getElimination(); // get the
																// elimination
																// problem

			System.out.println("\n These are the elimination variables: \n");
			System.out.println(elimination.toStringEliminationVariables());

//			System.out.println("\n These are the conditions: \n");
//			System.out.println(elimination.toStringConditions());

			int[] eliminationVariables = elimination.getEliminationVariables();

			int numberOfEliminationVariables = eliminationVariables.length;
			int numberOfVariables = elimination.getConditions()[0].length-1;

			// eliminate all the given the variables
			for (int i = 0; i < numberOfEliminationVariables; i++) {

				System.out.println("\n >>>> START ELIMINATION OF X"
						+ (eliminationVariables[i] + 1) + "\n");

				// if there will be one variable left after the last elimination
				// we execute a special elimination
				if(((i+1) == numberOfEliminationVariables) && ((i+1) == numberOfVariables-1)){
					elimination.eliminate(true, eliminationVariables[i]);
				} else {
					elimination.eliminate(false, eliminationVariables[i]);
				}
				
//				System.out.println("\n These are the conditions after the elimination: \n");
//				System.out.println(elimination.toStringConditions());
				
			}

			// print final conditions
			System.out.println("\n Here are the final conditions: \n");
			System.out.println(elimination.toStringConditions());
			System.out.println("\n At the end we have "
					+ elimination.getConditions().length + " conditions \n");

			long endTime = System.currentTimeMillis();

			System.out.println("\n The eliminations took "
					+ (endTime - startTime) + " ms");

		} catch (IOException e) {
			System.out.println("file not found");
			e.printStackTrace();
		}
	}

}