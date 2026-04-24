package Model;



	import java.io.BufferedReader;
	import java.io.IOException;
	import java.io.InputStreamReader;
	import java.util.ArrayList;
	import java.util.List;
	import java.nio.charset.StandardCharsets;
	import java.nio.file.Path;

	public class PythonExecutor {

	    public static class ExecutionResult {
	        private final int exitCode;
	        private final String output;
	        private final String scriptPath;
	        private final long durationMillis;
	
	        public ExecutionResult(int exitCode, String output, String scriptPath, long durationMillis) {
	            this.exitCode = exitCode;
	            this.output = output;
	            this.scriptPath = scriptPath;
	            this.durationMillis = durationMillis;
	        }
	
	        public int getExitCode() {
	            return exitCode;
	        }
	
	        public String getOutput() {
	            return output;
	        }

	        public String getScriptPath() {
	            return scriptPath;
	        }

	        public long getDurationMillis() {
	            return durationMillis;
	        }

	        public boolean isSuccess() {
	            return exitCode == 0;
	        }
	    }

	    public ExecutionResult executeScript(String pythonExecutable, String pythonScriptPath) {
	        return executeScript(pythonExecutable, pythonScriptPath, null);
	    }

	    public ExecutionResult executeScript(String pythonExecutable, String pythonScriptPath, Path workingDirectory) {
	        long start = System.currentTimeMillis();
	        try {
	            ProcessBuilder pb = new ProcessBuilder(buildCommand(pythonExecutable, pythonScriptPath));
	            if (workingDirectory != null) {
	                pb.directory(workingDirectory.toFile());
	            }
	            pb.redirectErrorStream(true);
	
	            Process process = pb.start();
	            StringBuilder output = new StringBuilder();
	
	            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
	                String line;
	                while ((line = reader.readLine()) != null) {
	                    output.append(line).append(System.lineSeparator());
	                }
	            }
	
	            int exitCode = process.waitFor();
	            long duration = System.currentTimeMillis() - start;
	            return new ExecutionResult(exitCode, output.toString(), pythonScriptPath, duration);
	        } catch (IOException | InterruptedException e) {
	            if (e instanceof InterruptedException) {
	                Thread.currentThread().interrupt();
	            }
	            long duration = System.currentTimeMillis() - start;
	            return new ExecutionResult(-1, "Erreur lors de l'execution Python: " + e.getMessage(), pythonScriptPath, duration);
	        }
	    }

	    private List<String> buildCommand(String pythonExecutable, String pythonScriptPath) {
	        List<String> cmd = new ArrayList<>();
	        if (pythonExecutable != null) {
	            String trimmed = pythonExecutable.trim();
	            if (!trimmed.isEmpty()) {
	                String[] parts = trimmed.split("\\s+");
	                for (String part : parts) {
	                    if (!part.isEmpty()) {
	                        cmd.add(part);
	                    }
	                }
	            }
	        }

	        if (cmd.isEmpty()) {
	            cmd.add("python");
	        }

	        cmd.add(pythonScriptPath);
	        return cmd;
	}
	
	}
