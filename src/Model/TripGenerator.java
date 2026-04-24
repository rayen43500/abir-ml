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

		String defaultPythonScriptPath = "";
		String pythonScriptPath = args.length > 1 ? args[1] : defaultPythonScriptPath;
		if (!pythonScriptPath.isBlank()) {
			String pythonExecutable = args.length > 2 ? args[2] : "python";
			PythonMLRunner pythonMLRunner = new PythonMLRunner();
			pythonMLRunner.runAndPrint(pythonScriptPath, pythonExecutable);
		}
	}
}
