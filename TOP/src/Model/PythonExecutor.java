package Model;



	import java.io.BufferedReader;
	import java.io.IOException;
	import java.io.InputStreamReader;

	public class PythonExecutor {

	    public static class ExecutionResult {
	        private final int exitCode;
	        private final String output;
	
	        public ExecutionResult(int exitCode, String output) {
	            this.exitCode = exitCode;
	            this.output = output;
	        }
	
	        public int getExitCode() {
	            return exitCode;
	        }
	
	        public String getOutput() {
	            return output;
	        }
	    }

	    public ExecutionResult executeScript(String pythonExecutable, String pythonScriptPath) {
	        try {
	            ProcessBuilder pb = new ProcessBuilder(pythonExecutable, pythonScriptPath);
	            pb.redirectErrorStream(true);
	
	            Process process = pb.start();
	            StringBuilder output = new StringBuilder();
	
	            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
	                String line;
	                while ((line = reader.readLine()) != null) {
	                    output.append(line).append(System.lineSeparator());
	                }
	            }
	
	            int exitCode = process.waitFor();
	            return new ExecutionResult(exitCode, output.toString());
	        } catch (IOException | InterruptedException e) {
	            if (e instanceof InterruptedException) {
	                Thread.currentThread().interrupt();
	            }
	            return new ExecutionResult(-1, "Erreur lors de l'execution Python: " + e.getMessage());
	        }
	    }
	
	}
