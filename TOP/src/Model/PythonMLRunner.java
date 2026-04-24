package Model;

public class PythonMLRunner {

    public void runAndPrint(String pythonScriptPath, String pythonExecutable) {
        PythonExecutor pythonExecutor = new PythonExecutor();
        PythonExecutor.ExecutionResult result = pythonExecutor.executeScript(pythonExecutable, pythonScriptPath);

        System.out.println("Python exit code: " + result.getExitCode());
        System.out.println("Python output:");
        System.out.println(result.getOutput());
    }
}
