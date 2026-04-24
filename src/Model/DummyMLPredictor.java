package Model;
import java.util.*;

public class DummyMLPredictor implements MLPredictor {
    private Random random;
    
    public DummyMLPredictor() {
        this.random = new Random();
    }
    
    @Override
    public Map<String, Double> suggestParameters(PerformanceMetrics metrics, 
                                               Map<String, Double> solutionFeatures, 
                                               int iteration) {
        Map<String, Double> suggestions = new HashMap<>();
        
        // Logique basique sans dépendances complexes
        double tabuTenure = 10.0;
        double kMax = 4.0;
        double shakingIntensity = 0.3;
        
        // Ajustements simples
        if (metrics != null) {
            if (metrics.getStagnationCount() > 10) {
                tabuTenure += 5.0;
                shakingIntensity += 0.1;
            }
            if (metrics.getAvgImprovement() > 0.05) {
                tabuTenure -= 2.0;
            }
        }
        
        suggestions.put("TABU_TENURE", Math.max(5, Math.min(50, tabuTenure)));
        suggestions.put("K_MAX", Math.max(2, Math.min(8, kMax)));
        suggestions.put("SHAKING_INTENSITY", Math.max(0.1, Math.min(0.8, shakingIntensity)));
        
        return suggestions;
    }
    
    @Override
    public String predictBestOperator(Solution current, List<String> availableOperators) {
        if (availableOperators == null || availableOperators.isEmpty()) {
            return "2-opt";
        }
        
        // Retourne toujours le premier opérateur disponible
        return availableOperators.get(0);
    }
    
    @Override
    public double predictImprovementProbability(Solution solution, String operator) {
        return 0.5; // Probabilité fixe de 50%
    }
}