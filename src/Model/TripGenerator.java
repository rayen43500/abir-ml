package Model;

import java.io.FileNotFoundException;

public class TripGenerator {
	public static void main(String[] args) {
	//	String instancePath = args[0];

	//	ProblemInput problemInput = null;
		  // Valeur par défaut
        String instancePath = "test/MCTOPMTWP-1-pr02-out.txt"  ;

        // Si un argument est fourni, on le prend
        if (args.length > 0) {
            instancePath = args[0];
        }

        ProblemInput problemInput = null;
		try {
			problemInput = ProblemInput.getProblemInputFromFile(instancePath);
			System.out.println(" file. " + problemInput.getBudgetLimit());
		} catch (FileNotFoundException ex) {
			System.out.println("Could not find file. " + ex.getMessage());
			System.exit(1);
		}
	
		/*  IteratedLocalSearch ILSAlgorithm = new IteratedLocalSearch();
		ILSAlgorithm.solve(problemInput);
		Solution bestSolution = ILSAlgorithm.getBestSolution(); */
		
		
		
		/*  VariableNeighborhoodSearch  VNSAlgorithm = new VariableNeighborhoodSearch ();
		VNSAlgorithm.solve(problemInput);
		Solution bestSolution = VNSAlgorithm.getBestSolution();   */  
		
		 VN  VNSAlgorithm = new VN ();
		VNSAlgorithm.solve(problemInput);
		Solution bestSolution = VNSAlgorithm.getBestSolution();   
		
		if(bestSolution.isValid()) {
			System.out.println("BEST SOLUTION: ");
			System.out.println(bestSolution);
		}

		String pythonExecutable = "python";
		PythonMLRunner pythonMLRunner = new PythonMLRunner();

		if (args.length > 1) {
			String secondArg = args[1];
			if (secondArg.endsWith(".py")) {
				pythonExecutable = args.length > 2 ? args[2] : pythonExecutable;
				pythonMLRunner.runAndPrint(secondArg, pythonExecutable);
			} else {
				pythonExecutable = secondArg;
				pythonMLRunner.runDefaultScripts(pythonExecutable);
			}
		} else {
			pythonMLRunner.runDefaultScripts(pythonExecutable);
		}
	}
}
