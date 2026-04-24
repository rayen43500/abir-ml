package Model;
import java.util.*;

public class DynamicParameterTuning {
    private Map<String, ParameterConfig> parameters;
    private PerformanceTracker performanceTracker;
    private MLPredictor mlPredictor;

    public DynamicParameterTuning() {
        this.parameters = new HashMap<>();
        this.performanceTracker = new PerformanceTracker();
        initializeParameters();
        this.mlPredictor = new DummyMLPredictor(); // ✅ SANS ERREUR
    }

    private void initializeParameters() {
        parameters.put("TABU_TENURE", new ParameterConfig(5, 50, 10));
        parameters.put("K_MAX", new ParameterConfig(2, 8, 4));
        parameters.put("SHAKING_INTENSITY", new ParameterConfig(0.1, 0.8, 0.3));
    }

    public void adjustParameters(Solution currentSolution, int iteration) {
        if (currentSolution == null) {
            return;
        }
        
        try {
            // 1. Obtenir les métriques de performance
            PerformanceMetrics metrics = performanceTracker.getCurrentMetrics(currentSolution);
            
            // 2. Extraire les features de la solution
            Map<String, Double> solutionFeatures = SolutionFeatureExtractor.extractFeatures(currentSolution);
            
            // 3. Obtenir les suggestions du prédicteur ML
            Map<String, Double> suggestedParams = mlPredictor.suggestParameters(
                metrics, solutionFeatures, iteration
            );
            
            // 4. Appliquer les suggestions
            applySuggestedParameters(suggestedParams);
            
            System.out.printf("✅ Itération %d - Paramètres ajustés%n", iteration);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur ajustement paramètres: " + e.getMessage());
            applyDefaultParameters();
        }
    }

    private void applySuggestedParameters(Map<String, Double> suggestedParams) {
        for (Map.Entry<String, Double> entry : suggestedParams.entrySet()) {
            ParameterConfig config = parameters.get(entry.getKey());
            if (config != null) {
                double newValue = entry.getValue();
                config.setCurrentValue(newValue);
            }
        }
    }

    private void applyDefaultParameters() {
        parameters.get("TABU_TENURE").setCurrentValue(10);
        parameters.get("K_MAX").setCurrentValue(4);
        parameters.get("SHAKING_INTENSITY").setCurrentValue(0.3);
    }

    public double getParameterValue(String paramName) {
        ParameterConfig config = parameters.get(paramName);
        return config != null ? config.getCurrentValue() : 0.0;
    }
    
    public void recordIteration(double improvement, double diversity, double currentScore) {
        performanceTracker.recordIteration(improvement, diversity, currentScore);
    }
    
    public Map<String, Double> getCurrentParameters() {
        Map<String, Double> currentParams = new HashMap<>();
        for (Map.Entry<String, ParameterConfig> entry : parameters.entrySet()) {
            currentParams.put(entry.getKey(), entry.getValue().getCurrentValue());
        }
        return currentParams;
    }
    
    public void printCurrentParameters() {
        System.out.println("📊 Paramètres actuels:");
        for (Map.Entry<String, ParameterConfig> entry : parameters.entrySet()) {
            System.out.printf("   %s: %.1f (min=%.1f, max=%.1f)%n", 
                entry.getKey(), 
                entry.getValue().getCurrentValue(),
                entry.getValue().getMin(),
                entry.getValue().getMax());
        }
    }
}