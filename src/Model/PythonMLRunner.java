package Model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PythonMLRunner {

    private final PythonExecutor pythonExecutor = new PythonExecutor();

    public void runAndPrint(String pythonScriptPath, String pythonExecutable) {
        PythonExecutor.ExecutionResult result = pythonExecutor.executeScript(pythonExecutable, pythonScriptPath);
        printResult("SCRIPT PYTHON", result);
    }

    public void runDefaultScripts(String pythonExecutable) {
        runScriptIfExists("SCORE", Paths.get("python", "score.py"), pythonExecutable);
        runScriptIfExists("VILLE", Paths.get("python", "ville.py"), pythonExecutable);
    }

    private void runScriptIfExists(String title, Path scriptPath, String pythonExecutable) {
        if (!Files.exists(scriptPath)) {
            System.out.println("[Python] Script introuvable: " + scriptPath);
            return;
        }

        PythonExecutor.ExecutionResult result = pythonExecutor.executeScript(
            pythonExecutable,
            scriptPath.toString(),
            scriptPath.toAbsolutePath().getParent()
        );
        printResult(title, result);
    }

    private void printResult(String title, PythonExecutor.ExecutionResult result) {
        String border = "+" + "-".repeat(72) + "+";
        System.out.println(border);
        System.out.println("| PYTHON " + title + " | script=" + result.getScriptPath());
        System.out.println("| exit=" + result.getExitCode() + " | duree=" + result.getDurationMillis() + " ms | statut=" + (result.isSuccess() ? "OK" : "ERREUR"));
        System.out.println(border);

        String output = result.getOutput();
        if (output == null || output.isBlank()) {
            System.out.println("(Aucune sortie)");
        } else {
            String[] lines = output.split("\\R");
            for (String line : lines) {
                System.out.println("  " + line);
            }
        }
        System.out.println();
    }
}
