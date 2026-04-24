package Model;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TripGenerator {
	public static void main(String[] args) {
		String instancePath = "test/MCTOPMTWP-1-pr02-out.txt";

        // Si un argument est fourni, on le prend
        if (args.length > 0) {
            instancePath = args[0];
        }

		Path resolvedInstancePath = resolveExistingPath(instancePath);
		if (resolvedInstancePath == null) {
			System.out.println("Could not find file. " + instancePath + " (chemin introuvable depuis le dossier courant: " +
				Paths.get("").toAbsolutePath() + ")");
			System.exit(1);
		}

		System.out.println("Instance utilisee: " + resolvedInstancePath.toAbsolutePath());

        ProblemInput problemInput = null;
		try {
			problemInput = ProblemInput.getProblemInputFromFile(resolvedInstancePath.toString());
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

		String pythonExecutable = "py -3.11";
		PythonMLRunner pythonMLRunner = new PythonMLRunner();

		if (args.length > 1) {
			String secondArg = args[1];
			if (secondArg.endsWith(".py")) {
				pythonExecutable = args.length > 2 ? args[2] : pythonExecutable;
				pythonMLRunner.runAndPrint(secondArg, pythonExecutable);
			} else {
				pythonExecutable = secondArg;
				runDefaultPythonScripts(pythonMLRunner, pythonExecutable);
			}
		} else {
			runDefaultPythonScripts(pythonMLRunner, pythonExecutable);
		}
	}

	private static void runDefaultPythonScripts(PythonMLRunner pythonMLRunner, String pythonExecutable) {
		Path scoreScript = resolveExistingPath("python/score.py");
		Path villeScript = resolveExistingPath("python/ville.py");

		if (scoreScript != null) {
			pythonMLRunner.runAndPrint(scoreScript.toString(), pythonExecutable);
		} else {
			System.out.println("[Python] Script introuvable: python/score.py");
		}

		if (villeScript != null) {
			pythonMLRunner.runAndPrint(villeScript.toString(), pythonExecutable);
		} else {
			System.out.println("[Python] Script introuvable: python/ville.py");
		}
	}

	private static Path resolveExistingPath(String rawPath) {
		Path given = Paths.get(rawPath);
		if (given.isAbsolute() && Files.exists(given)) {
			return given;
		}

		Path[] prefixes = new Path[] {
			Paths.get(""),
			Paths.get("."),
			Paths.get(".."),
			Paths.get("..", ".."),
			Paths.get("TOP"),
			Paths.get("..", "TOP"),
			Paths.get("..", "..", "TOP")
		};

		for (Path prefix : prefixes) {
			Path candidate = prefix.resolve(rawPath).normalize();
			if (Files.exists(candidate)) {
				return candidate;
			}
		}

		return null;
	}
}
