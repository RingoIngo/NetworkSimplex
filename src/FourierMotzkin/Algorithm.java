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

			// System.out
			// .println("\n This is the coefficient matrix row by row: \n");
			// System.out.println(elimination.toStringCoefficients());

			System.out.println("\n These are the conditions: \n");
			System.out.println(elimination.toStringConditions());

			int[] eliminationVariables = elimination.getEliminationVariables();
			int numberOfEliminationVariables = eliminationVariables.length;

			for (int i = 0; i < numberOfEliminationVariables; i++) {

				// System.out.println("\n Execute the eliminiation of X"
				// +(eliminationVariables[i]+1)+ "\n");

				if ((i + 1) == numberOfEliminationVariables) {
					elimination.eliminate(true, eliminationVariables[i]);
				} else {
					elimination.eliminate(false, eliminationVariables[i]);
				}

				// System.out.println("\n Here are the conditions after the elimination of X"
				// + (eliminationVariables[i]+1)+ ": \n");
				// System.out.println(elimination.toStringConditions());
				//
				// System.out.println("\n Now we have "+elimination.getNumberOfConditions()+" conditions \n ");
			}

			// System.out.println("\n Here are the final conditions: \n");
			// System.out.println(elimination.toStringConditions());

			long endTime = System.currentTimeMillis();

			System.out.println("\n \n The eliminations took "
					+ (endTime - startTime) + " ms");

		} catch (IOException e) {
			System.out.println("file not found");
			e.printStackTrace();
		}
	}

}