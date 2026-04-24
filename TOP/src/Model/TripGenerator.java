package Model;

import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
public class TripGenerator {
	public static void main(String[] args) {
		
		 
		        try {
		            // Commande à exécuter (comme dans le terminal)
		            // python chemin/vers/votre/script.py argument1 argument2
		            ProcessBuilder pb = new ProcessBuilder(
		                "python", 
		                "C:/Users/HP_2026/Desktop/version/untitled10.py",
		                "argument_optionnel"
		            );
		            
		            // Exécuter la commande
		            Process process = pb.start();
		            
		            // Lire la sortie (ce que Python a affiché avec print)
		            BufferedReader reader = new BufferedReader(
		            		
		            		  new InputStreamReader(process.getInputStream(), "UTF-8")  // ← Ajoutez ", UTF-8"
		            		
		            		  // new InputStreamReader(process.getInputStream())
		            );
		            
		            String ligne;
		            StringBuilder resultat = new StringBuilder();
		            while ((ligne = reader.readLine()) != null) {
		                resultat.append(ligne);
		            }
		            
		            // Attendre que Python finisse
		            int codeSortie = process.waitFor();
		            
		            if (codeSortie == 0) {
		                // Succès - afficher le résultat
		                System.out.println("Résultat du modèle : " + resultat.toString());
		                // Vous pouvez parser le JSON ici
		            } else {
		                // Erreur
		                System.err.println("Erreur dans Python");
		            }
		            
		        } catch (Exception e) {
		        	System.err.println("Erreur  :" + e.getMessage()   );
		            e.printStackTrace();
		        }
		 
	 
		
	//	String instancePath = args[0];

	//	ProblemInput problemInput = null;
		  // Valeur par défaut
        String instancePath = "test/MCTOPMTWP-2-pr02-out.txt"  ;

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
	
		  IteratedLocalSearch ILSAlgorithm = new IteratedLocalSearch();
		ILSAlgorithm.solve(problemInput);
		Solution bestSolution = ILSAlgorithm.getBestSolution(); 
		
		
		
		/* VariableNeighborhoodSearch  VNSAlgorithm = new VariableNeighborhoodSearch ();
		VNSAlgorithm.solve(problemInput);
		Solution bestSolution = VNSAlgorithm.getBestSolution();   */  
		
		/* VN  VNSAlgorithm = new VN ();
		VNSAlgorithm.solve(problemInput);
		Solution bestSolution = VNSAlgorithm.getBestSolution();  */ 
		
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
